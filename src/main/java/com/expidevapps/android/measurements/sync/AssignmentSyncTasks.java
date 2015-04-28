package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class AssignmentSyncTasks {
    private static final Logger LOG = LoggerFactory.getLogger(AssignmentSyncTasks.class);

    static final String EXTRA_ASSIGNMENTS = AssignmentSyncTasks.class.getName() + ".EXTRA_ASSIGNMENTS";

    private static final String SYNC_TIME_ASSIGNMENTS = "last_synced.assignments";

    private static final long STALE_DURATION_ASSIGNMENTS = Constants.DAY_IN_MS;

    static void syncAssignments(@NonNull final Context context, @Nullable final Bundle args) throws ApiException {
        if (args != null) {
            final String guid = args.getString(EXTRA_GUID);
            final boolean force = args.getBoolean(Constants.EXTRA_FORCE, false);

            // only update if we have a valid guid
            if (guid != null) {
                final GmaApiClient api = GmaApiClient.getInstance(context, guid);
                final GmaDao dao = GmaDao.getInstance(context);

                // update if we are forcing an update or the assignments are stale
                if (force || System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_ASSIGNMENTS, guid) >
                        STALE_DURATION_ASSIGNMENTS) {
                    // fetch raw data from API & parse it
                    final List<Assignment> assignments = api.getAssignments();
                    if (assignments != null) {
                        AssignmentSyncTasks.updateAllAssignments(context, guid, assignments);
                    }
                }
            }
        }
    }

    static void saveAssignments(@NonNull final Context context, @Nullable final Bundle args) {
        if (args != null) {
            final String guid = args.getString(EXTRA_GUID);
            final String raw = args.getString(EXTRA_ASSIGNMENTS);
            if (guid != null && raw != null) {
                try {
                    updateAllAssignments(context, guid, Assignment.listFromJson(new JSONArray(raw), guid));
                } catch (final JSONException ignored) {
                }
            }
        }
    }

    private static void updateAllAssignments(@NonNull final Context context, @NonNull final String guid,
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
                    Contract.Assignment.COLUMN_LAST_SYNCED};
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
        } catch (final SQLException e) {
            LOG.debug("error updating assignments", e);
        } finally {
            tx.end();
        }
    }
}
