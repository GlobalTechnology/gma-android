package com.expidev.gcmapp.service;

import static com.expidev.gcmapp.Constants.EXTRA_MINISTRY_ID;
import static com.expidev.gcmapp.service.Type.RETRIEVE_ALL_MINISTRIES;
import static com.expidev.gcmapp.service.Type.RETRIEVE_ASSOCIATED_MINISTRIES;
import static com.expidev.gcmapp.service.Type.SAVE_ASSOCIATED_MINISTRIES;
import static com.expidev.gcmapp.service.Type.SYNC_ASSIGNMENTS;
import static com.expidev.gcmapp.service.Type.SYNC_CHURCHES;
import static com.expidev.gcmapp.service.Type.SYNC_DIRTY_CHURCHES;
import static com.expidev.gcmapp.utils.BroadcastUtils.allMinistriesReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.associatedMinistriesReceivedBroadcast;
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
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.AssignmentsJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.AbstractDao.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by William.Randall on 1/22/2015.
 */
public class MinistriesService extends ThreadedIntentService {
    private static final String TAG = MinistriesService.class.getSimpleName();

    private static final String PREFS_SYNC = "gma_sync";
    private static final String PREF_SYNC_TIME_ASSIGNMENTS = "last_synced.assignments";
    private static final String PREF_SYNC_TIME_MINISTRIES = "last_synced.ministries";

    private static final String EXTRA_SYNCTYPE = "type";
    private static final String EXTRA_FORCE = MinistriesService.class.getName() + ".EXTRA_FORCE";

    // various stale data durations
    private static final long HOUR_IN_MS = 60 * 60 * 1000;
    private static final long DAY_IN_MS = 24 * HOUR_IN_MS;
    private static final long STALE_DURATION_ASSIGNMENTS = DAY_IN_MS;
    private static final long STALE_DURATION_MINISTRIES = 7 * DAY_IN_MS;

    @NonNull
    private GmaApiClient mApi;
    @NonNull
    private MinistriesDao mDao;
    private LocalBroadcastManager broadcastManager;

    public MinistriesService()
    {
        super("MinistriesService", 5);
    }

    public static void syncAssignments(final Context context) {
        syncAssignments(context, false);
    }

    public static void syncAssignments(final Context context, final boolean force) {
        final Intent intent = new Intent(context, MinistriesService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNC_ASSIGNMENTS);
        intent.putExtra(EXTRA_FORCE, force);
        context.startService(intent);
    }

    public static void syncChurches(@NonNull final Context context, @NonNull final String ministryId) {
        final Intent intent = new Intent(context, MinistriesService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNC_CHURCHES);
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        context.startService(intent);
    }

    public static void syncDirtyChurches(@NonNull final Context context) {
        final Intent intent = new Intent(context, MinistriesService.class);
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
        mDao = MinistriesDao.getInstance(this);
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        final Type type = (Type) intent.getSerializableExtra(EXTRA_SYNCTYPE);

        try {
            switch (type) {
                case RETRIEVE_ASSOCIATED_MINISTRIES:
                    retrieveAssociatedMinistries();
                    break;
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
        final Intent intent = new Intent(context, MinistriesService.class);

        if(extras != null)
        {
            intent.putExtras(extras);
        }

        return intent;
    }

    /**
     * Retrieve ministries this user is associated with
     * from the local database
     */
    public static void retrieveMinistries(final Context context)
    {
        Bundle extras = new Bundle(1);
        extras.putSerializable(EXTRA_SYNCTYPE, RETRIEVE_ASSOCIATED_MINISTRIES);

        context.startService(baseIntent(context, extras));
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

    public static void saveAssociatedMinistriesFromServer(@NonNull final Context context,
                                                          @Nullable final JSONArray assignments) {
        Log.i(TAG, assignments != null ? assignments.toString() : "null");

        Bundle extras = new Bundle(1);
        extras.putSerializable(EXTRA_SYNCTYPE, SAVE_ASSOCIATED_MINISTRIES);

        if(assignments != null)
        {
            List<Assignment> assignmentList = AssignmentsJsonParser.parseAssignments(assignments);
            extras.putSerializable("assignments", (ArrayList<Assignment>) assignmentList);
        }

        context.startService(baseIntent(context, extras));
    }


    /////////////////////////////////////////////////////
    //           Actions                              //
    ////////////////////////////////////////////////////
    private void retrieveAssociatedMinistries()
    {
        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        List<AssociatedMinistry> associatedMinistries = ministriesDao.retrieveAssociatedMinistriesList();
        Log.i(TAG, "Retrieved associated ministries");

        broadcastManager.sendBroadcast(associatedMinistriesReceivedBroadcast((ArrayList<AssociatedMinistry>) associatedMinistries));
    }

    private void syncAssignments(final Intent intent) throws ApiException {
        final SharedPreferences prefs = this.getSharedPreferences(PREFS_SYNC, MODE_PRIVATE);
        final boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
        final boolean stale =
                System.currentTimeMillis() - prefs.getLong(PREF_SYNC_TIME_ASSIGNMENTS, 0) > STALE_DURATION_ASSIGNMENTS;

        if (force || stale) {
            // fetch raw data from API & parse it
            final JSONArray json = mApi.getAssignments(true);
            if (json != null) {
                this.updateAllAssignments(AssignmentsJsonParser.parseAssignments(json));
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
                        .get(Church.class, Contract.Church.SQL_WHERE_MINISTRY_ID, bindValues(ministryId))) {
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

        // process all churches that are dirty
        for (final Church church : dirty) {
            try {
                // generate dirty JSON
                final JSONObject json = church.dirtyToJson();

                // update the church
                final boolean success = mApi.updateChurch(church.getId(), json);

                // clear dirty attributes if update was successful
                if (success) {
                    church.setDirty(null);
                    mDao.update(church, new String[] {Contract.Church.COLUMN_DIRTY});
                }
            } catch (final JSONException ignored) {
                // this shouldn't happen when generating json
            }
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
                    broadcastManager.sendBroadcast(allMinistriesReceivedBroadcast((ArrayList<Ministry>) ministries));
                } finally {
                    tx.end();
                }
            }
        }
    }

    private void saveAssociatedMinistriesFromServer(Intent intent)
    {
        List<Assignment> assignments = (ArrayList<Assignment>) intent.getSerializableExtra("assignments");

        this.updateAllAssignments(assignments);
    }

    private void updateAllAssignments(@NonNull final List<Assignment> assignments) {
        // wrap entire update in a transaction
        final AbstractDao.Transaction tx = mDao.newTransaction();
        try {
            tx.begin();

            // load pre-existing Assignments
            final Map<String, Assignment> existing = new HashMap<>();
            for (final Assignment assignment : mDao.get(Assignment.class)) {
                existing.put(assignment.getId(), assignment);
            }

            // update assignments in local database
            for (final Assignment assignment : assignments) {
                // update all attached ministries
                mDao.insertOrUpdateAssociatedMinistry(assignment.getMinistry());

                // now update assignment
                mDao.updateOrInsert(assignment, new String[] {Contract.Assignment.COLUMN_ROLE,
                        Contract.Assignment.COLUMN_MINISTRY_ID, Contract.Assignment.COLUMN_LAST_SYNCED});

                // remove it from the list of existing assignments
                existing.remove(assignment.getId());
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
            broadcastManager.sendBroadcast(BroadcastUtils.updateAssignmentsBroadcast());
            broadcastManager.sendBroadcast(stopBroadcast(SAVE_ASSOCIATED_MINISTRIES));
        } catch (final SQLException e) {
            Log.d(TAG, "error updating assignments", e);
        } finally {
            tx.end();
        }
    }
}
