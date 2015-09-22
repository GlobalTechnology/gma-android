package com.expidevapps.android.measurements.sync;

import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.content.SyncResult;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class AssignmentSyncTasks extends BaseSyncTasks {
    private static final Logger LOG = LoggerFactory.getLogger(AssignmentSyncTasks.class);

    static final String EXTRA_ASSIGNMENTS = AssignmentSyncTasks.class.getName() + ".EXTRA_ASSIGNMENTS";
    static final String EXTRA_PERSON_ID = AssignmentSyncTasks.class.getName() + ".EXTRA_PERSON_ID";

    private static final String SYNC_TIME_ASSIGNMENTS = "last_synced.assignments";

    private static final long STALE_DURATION_ASSIGNMENTS = DAY_IN_MS;

    static boolean syncAssignments(@NonNull final Context context, @NonNull final String guid,
                                   @NonNull final Bundle args) throws ApiException {
        // short-circuit if we aren't forcing a sync and the data isn't stale
        final boolean force = isForced(args);
        final GmaDao dao = GmaDao.getInstance(context);
        if (!force && System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_ASSIGNMENTS, guid) <
                STALE_DURATION_ASSIGNMENTS) {
            return true;
        }

        // fetch raw data from API & process it
        final GmaApiClient api = GmaApiClient.getInstance(context, guid);
        final List<Assignment> assignments = api.getAssignments();
        return assignments != null && AssignmentSyncTasks.updateAllAssignments(context, guid, assignments);
    }

    static void saveAssignments(@NonNull final Context context, @NonNull final String guid,
                                @NonNull final Bundle args, @NonNull final SyncResult result) {
        final String raw = args.getString(EXTRA_ASSIGNMENTS);
        final String personId = args.getString(EXTRA_PERSON_ID);
        if (raw != null) {
            try {
                updateAllAssignments(context, guid, Assignment.listFromJson(new JSONArray(raw), guid, personId));
            } catch (final JSONException e) {
                result.stats.numParseExceptions++;
            }
        }
    }

    private static boolean updateAllAssignments(@NonNull final Context context, @NonNull final String guid,
                                                @NonNull final List<Assignment> assignments) {
        // wrap entire update in a transaction
        final GmaDao dao = GmaDao.getInstance(context);
        final Transaction tx = dao.newTransaction();
        try {
            tx.beginTransactionNonExclusive();

            // load pre-existing Assignments (ministry_id => assignment)
            final Map<String, Assignment> existing = new HashMap<>();
            for (final Assignment assignment : dao
                    .get(Assignment.class, Contract.Assignment.SQL_WHERE_GUID, bindValues(guid))) {
                existing.put(assignment.getMinistryId(), assignment);
            }

            // column projections for updates
            final String[] PROJECTION_ASSIGNMENT = {Contract.Assignment.COLUMN_ROLE, Contract.Assignment.COLUMN_ID,
                    Contract.Assignment.COLUMN_PERSON_ID, Contract.Assignment.COLUMN_LAST_SYNCED};
            final String[] PROJECTION_MINISTRY =
                    {Contract.Ministry.COLUMN_NAME, Contract.Ministry.COLUMN_MIN_CODE, Contract.Ministry.COLUMN_MCCS,
                            Contract.Ministry.COLUMN_LATITUDE, Contract.Ministry.COLUMN_LONGITUDE,
                            Contract.Ministry.COLUMN_LOCATION_ZOOM, Contract.Ministry.COLUMN_PARENT_MINISTRY_ID,
                            Contract.Ministry.COLUMN_LAST_SYNCED};

            // update assignments in local database
            final LinkedList<Assignment> toProcess = new LinkedList<>(assignments);
            while (toProcess.size() > 0) {
                final Assignment assignment = toProcess.pop();

                // update the ministry
                final Ministry ministry = assignment.getMinistry();
                if (ministry != null) {
                    dao.updateOrInsert(ministry, PROJECTION_MINISTRY);

                    // update lmi visibility
                    final Collection<String> show = ministry.getLmiShow();
                    final Collection<String> hide = ministry.getLmiHide();
                    if (show != null && hide != null) {
                        dao.setMeasurementVisibility(ministry.getMinistryId(), show, hide);
                    }
                }

                // now update the actual assignment
                dao.updateOrInsert(assignment, PROJECTION_ASSIGNMENT);

                // queue up sub assignments for processing
                toProcess.addAll(assignment.getSubAssignments());

                // remove it from the list of existing assignments
                existing.remove(assignment.getMinistryId());
            }

            // delete any remaining assignments, we don't have them anymore
            for (final Assignment assignment : existing.values()) {
                dao.delete(assignment);
            }

            // update the sync time
            dao.updateLastSyncTime(SYNC_TIME_ASSIGNMENTS, guid);

            tx.setSuccessful();

            // send broadcasts for updated data
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            broadcastManager.sendBroadcast(BroadcastUtils.updateAssignmentsBroadcast(guid));
            if (assignments.isEmpty()) {
                broadcastManager.sendBroadcast(BroadcastUtils.noAssignmentsBroadcast(guid));
            }

            return true;
        } catch (final SQLException e) {
            LOG.debug("error updating assignments", e);
            return false;
        } finally {
            tx.end();
        }
    }
}
