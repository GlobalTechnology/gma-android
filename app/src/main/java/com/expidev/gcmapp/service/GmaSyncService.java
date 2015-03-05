package com.expidev.gcmapp.service;

import static com.expidev.gcmapp.Constants.EXTRA_GUID;
import static com.expidev.gcmapp.Constants.EXTRA_MINISTRY_ID;
import static com.expidev.gcmapp.service.Type.RETRIEVE_ALL_MINISTRIES;
import static com.expidev.gcmapp.service.Type.SAVE_ASSOCIATED_MINISTRIES;
import static com.expidev.gcmapp.service.Type.SYNC_ASSIGNMENTS;
import static com.expidev.gcmapp.service.Type.SYNC_CHURCHES;
import static com.expidev.gcmapp.service.Type.SYNC_DIRTY_CHURCHES;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.GmaDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.BroadcastUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.AbstractDao.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GmaSyncService extends ThreadedIntentService {
    private static final String TAG = GmaSyncService.class.getSimpleName();

    private static final String PREFS_SYNC = "gma_sync";
    private static final String PREF_SYNC_TIME_ASSIGNMENTS = "last_synced.assignments";
    private static final String PREF_SYNC_TIME_MINISTRIES = "last_synced.ministries";

    private static final String EXTRA_SYNCTYPE = "type";
    private static final String EXTRA_FORCE = GmaSyncService.class.getName() + ".EXTRA_FORCE";
    private static final String EXTRA_ASSIGNMENTS = GmaSyncService.class.getName() + ".EXTRA_ASSIGNMENTS";

    // various stale data durations
    private static final long HOUR_IN_MS = 60 * 60 * 1000;
    private static final long DAY_IN_MS = 24 * HOUR_IN_MS;
    private static final long STALE_DURATION_ASSIGNMENTS = DAY_IN_MS;
    private static final long STALE_DURATION_MINISTRIES = 7 * DAY_IN_MS;

    @NonNull
    private GmaApiClient mApi;
    @NonNull
    private GmaDao mDao;
    private LocalBroadcastManager broadcastManager;

    public GmaSyncService() {
        super("GmaSyncService", 5);
    }

    public static void syncAssignments(@NonNull final Context context, @NonNull final String guid) {
        syncAssignments(context, guid, false);
    }

    public static void syncAssignments(@NonNull final Context context, @NonNull final String guid,
                                       final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNC_ASSIGNMENTS);
        intent.putExtra(EXTRA_GUID, guid);
        intent.putExtra(EXTRA_FORCE, force);
        context.startService(intent);
    }

    public static void syncChurches(@NonNull final Context context, @NonNull final String ministryId) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNC_CHURCHES);
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        context.startService(intent);
    }

    public static void syncDirtyChurches(@NonNull final Context context) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNC_DIRTY_CHURCHES);
        context.startService(intent);
    }

    /////////////////////////////////////////////////////
    //           Lifecycle Handlers                   //
    ////////////////////////////////////////////////////
    @Override
    public void onCreate()
    {
        super.onCreate();
        mApi = GmaApiClient.getInstance(this);
        mDao = GmaDao.getInstance(this);
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        final Type type = (Type) intent.getSerializableExtra(EXTRA_SYNCTYPE);

        try {
            switch (type) {
                case RETRIEVE_ALL_MINISTRIES:
                    syncAllMinistries(intent);
                    break;
                case SAVE_ASSOCIATED_MINISTRIES:
                    saveAssociatedMinistriesFromServer(intent);
                    break;
                case SYNC_ASSIGNMENTS:
                    syncAssignments(intent);
                    break;
                case SYNC_CHURCHES:
                    syncChurches(intent);
                    break;
                case SYNC_DIRTY_CHURCHES:
                    syncDirtyChurches();
                    break;
                default:
                    break;
            }
        } catch (final ApiException e) {
            // XXX: ignore for now, maybe eventually broadcast something on specific ApiExceptions
        }
    }


    /////////////////////////////////////////////////////
    //           Service API                          //
    ////////////////////////////////////////////////////
    private static Intent baseIntent(final Context context, Bundle extras)
    {
        final Intent intent = new Intent(context, GmaSyncService.class);

        if(extras != null)
        {
            intent.putExtras(extras);
        }

        return intent;
    }

    /**
     * Retrieve all ministries from the GCM API
     */
    public static void syncAllMinistries(final Context context) {
        syncAllMinistries(context, false);
    }

    public static void syncAllMinistries(final Context context, final boolean force) {
        Bundle extras = new Bundle(2);
        extras.putSerializable(EXTRA_SYNCTYPE, RETRIEVE_ALL_MINISTRIES);
        extras.putBoolean(EXTRA_FORCE, force);

        context.startService(baseIntent(context, extras));
    }

    public static void saveAssociatedMinistriesFromServer(@NonNull final Context context, @NonNull final String guid,
                                                          @Nullable final JSONArray assignments) {
        Log.i(TAG, assignments != null ? assignments.toString() : "null");

        Bundle extras = new Bundle(3);
        extras.putSerializable(EXTRA_SYNCTYPE, SAVE_ASSOCIATED_MINISTRIES);
        extras.putString(EXTRA_GUID, guid);
        extras.putString(EXTRA_ASSIGNMENTS, assignments != null ? assignments.toString() : null);

        context.startService(baseIntent(context, extras));
    }


    /////////////////////////////////////////////////////
    //           Actions                              //
    ////////////////////////////////////////////////////

    private void syncAssignments(final Intent intent) throws ApiException {
        final SharedPreferences prefs = this.getSharedPreferences(PREFS_SYNC, MODE_PRIVATE);
        final String guid = intent.getStringExtra(EXTRA_GUID);
        final boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
        final boolean stale =
                System.currentTimeMillis() - prefs.getLong(PREF_SYNC_TIME_ASSIGNMENTS, 0) > STALE_DURATION_ASSIGNMENTS;

        if (force || stale) {
            // fetch raw data from API & parse it
            final List<Assignment> assignments = mApi.getAssignments(true);
            if (assignments != null) {
                this.updateAllAssignments(guid, assignments);
            }
        }
    }

    private void syncChurches(final Intent intent) throws ApiException {
        final String ministryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
        if (ministryId == null) {
            return;
        }

        final List<Church> churches = mApi.getChurches(ministryId);

        // only update churches if we get data back
        if(churches != null) {
            final Transaction tx = mDao.newTransaction();
            try {
                tx.begin();

                // load current churches
                final LongSparseArray<Church> current = new LongSparseArray<>();
                for (final Church church : mDao
                        .get(Church.class, Contract.Church.SQL_WHERE_MINISTRY, bindValues(ministryId))) {
                    current.put(church.getId(), church);
                }

                // process all fetched churches
                long[] ids = new long[current.size() + churches.size()];
                int j = 0;
                for(final Church church : churches) {
                    final long id = church.getId();
                    final Church existing = current.get(id);

                    // persist church in database (if it doesn't exist or isn't dirty)
                    if (existing == null || !existing.isDirty()) {
                        church.setLastSynced(new Date());
                        mDao.updateOrInsert(church);

                        // mark this id as having been changed
                        ids[j++] = id;
                    }

                    // remove this church from the list of churches
                    current.remove(id);
                }

                // delete any remaining churches that weren't returned from the API
                for(int i = 0; i< current.size(); i++) {
                    final Church church = current.valueAt(i);
                    mDao.delete(church);

                    // mark these ids as being updated as well
                    ids[j++] = church.getId();
                }

                // mark transaction successful
                tx.setSuccessful();

                // send broadcasts that data has been updated
                broadcastManager.sendBroadcast(
                        BroadcastUtils.updateChurchesBroadcast(ministryId, Arrays.copyOf(ids, j)));
            } finally {
                tx.end();
            }
        }
    }

    private synchronized void syncDirtyChurches() throws ApiException {
        final List<Church> dirty = mDao.get(Church.class, Contract.Church.SQL_WHERE_DIRTY, null);

        // ministry_id => church_id
        final Multimap<String, Long> broadcasts = HashMultimap.create();

        // process all churches that are dirty
        for (final Church church : dirty) {
            try {
                // generate dirty JSON
                final JSONObject json = church.dirtyToJson();

                // update the church
                final boolean success = mApi.updateChurch(church.getId(), json);

                // was successful update?
                if (success) {
                    // clear dirty attributes
                    church.setDirty(null);
                    mDao.update(church, new String[] {Contract.Church.COLUMN_DIRTY});

                    // add church to list of broadcasts
                    broadcasts.put(church.getMinistryId(), church.getId());
                }
            } catch (final JSONException ignored) {
                // this shouldn't happen when generating json
            }
        }

        // send broadcasts for each ministryId with churches that were changed
        for (final String ministryId : broadcasts.keySet()) {
            broadcastManager.sendBroadcast(
                    BroadcastUtils.updateChurchesBroadcast(ministryId, Longs.toArray(broadcasts.get(ministryId))));
        }
    }

    private void syncAllMinistries(final Intent intent) throws ApiException {
        final SharedPreferences prefs = this.getSharedPreferences(PREFS_SYNC, MODE_PRIVATE);
        final boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
        final boolean stale =
                System.currentTimeMillis() - prefs.getLong(PREF_SYNC_TIME_MINISTRIES, 0) > STALE_DURATION_MINISTRIES;

        // only sync if being forced or the data is stale
        if (force || stale) {
            // refresh the list of ministries if the load is being forced
            final List<Ministry> ministries = mApi.getAllMinistries();

            // only update the saved ministries if we received any back
            if (ministries != null) {
                // save all ministries to the database
                final Transaction tx = mDao.newTransaction();
                try {
                    tx.begin();

                    // load current ministries
                    final Map<String, Ministry> current = new HashMap<>();
                    for (final Ministry ministry : mDao.get(Ministry.class)) {
                        current.put(ministry.getMinistryId(), ministry);
                    }

                    // update all the ministry names
                    for (final Ministry ministry : ministries) {
                        // this is only a very minimal update, so don't log last synced for new ministries
                        ministry.setLastSynced(0);
                        mDao.updateOrInsert(ministry, new String[] {Contract.Ministry.COLUMN_NAME});

                        // remove from the list of current ministries
                        current.remove(ministry.getMinistryId());
                    }

                    // remove any current ministries we didn't see, we can do this because we just retrieved a complete list
                    for (final Ministry ministry : current.values()) {
                        mDao.delete(ministry);
                    }

                    tx.setSuccessful();

                    // update the synced time
                    prefs.edit().putLong(PREF_SYNC_TIME_MINISTRIES, System.currentTimeMillis()).apply();

                    // send broadcasts that data has been updated in the database
                    broadcastManager.sendBroadcast(BroadcastUtils.updateMinistriesBroadcast());
                } finally {
                    tx.end();
                }
            }
        }
    }

    private void saveAssociatedMinistriesFromServer(Intent intent)
    {
        final String guid = intent.getStringExtra(EXTRA_GUID);
        final String raw = intent.getStringExtra(EXTRA_ASSIGNMENTS);
        if(raw != null) {
            try {
                final List<Assignment> assignments = Assignment.listFromJson(new JSONArray(raw));

                this.updateAllAssignments(guid, assignments);
            } catch (final JSONException ignored) {
            }
        }
    }

    private void updateAllAssignments(@NonNull final String guid, @NonNull final List<Assignment> assignments) {
        // wrap entire update in a transaction
        final AbstractDao.Transaction tx = mDao.newTransaction();
        try {
            tx.begin();

            // load pre-existing Assignments (ministry_id => assignment)
            final Map<String, Assignment> existing = new HashMap<>();
            for (final Assignment assignment : mDao
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

                // set the guid on this assignment
                assignment.setGuid(guid);

                // update the ministry
                final Ministry ministry = assignment.getMinistry();
                if (ministry != null) {
                    mDao.updateOrInsert(ministry, PROJECTION_MINISTRY);
                }

                // now update the actual assignment
                mDao.updateOrInsert(assignment, PROJECTION_ASSIGNMENT);

                // queue up sub assignments for processing
                toProcess.addAll(assignment.getSubAssignments());

                // remove it from the list of existing assignments
                existing.remove(assignment.getMinistryId());
            }

            // delete any remaining assignments, we don't have them anymore
            for (final Assignment assignment : existing.values()) {
                mDao.delete(assignment);
            }

            tx.setSuccessful();

            // update the sync time
            this.getSharedPreferences(PREFS_SYNC, MODE_PRIVATE).edit()
                    .putLong(PREF_SYNC_TIME_ASSIGNMENTS, System.currentTimeMillis()).apply();

            // send broadcasts for updated data
            broadcastManager.sendBroadcast(BroadcastUtils.updateAssignmentsBroadcast(guid));
            broadcastManager.sendBroadcast(stopBroadcast(SAVE_ASSOCIATED_MINISTRIES));
        } catch (final SQLException e) {
            Log.d(TAG, "error updating assignments", e);
        } finally {
            tx.end();
        }
    }
}
