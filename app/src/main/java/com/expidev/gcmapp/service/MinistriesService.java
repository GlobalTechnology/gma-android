package com.expidev.gcmapp.service;

import static com.expidev.gcmapp.service.Type.RETRIEVE_ALL_MINISTRIES;
import static com.expidev.gcmapp.service.Type.RETRIEVE_ASSOCIATED_MINISTRIES;
import static com.expidev.gcmapp.service.Type.SAVE_ASSOCIATED_MINISTRIES;
import static com.expidev.gcmapp.utils.BroadcastUtils.allMinistriesReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.associatedMinistriesReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.AssignmentsJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.ccci.gto.android.common.db.AbstractDao.Transaction;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by William.Randall on 1/22/2015.
 */
public class MinistriesService extends ThreadedIntentService {
    private static final String TAG = MinistriesService.class.getSimpleName();

    private static final String PREFS_SYNC = "gma_sync";
    private static final String PREF_SYNC_TIME_MINISTRIES = "last_synced.ministries";

    public static final String EXTRA_FORCE = MinistriesService.class.getName() + ".EXTRA_FORCE";

    // various stale data durations
    private static final long HOUR_IN_MS = 60 * 60 * 1000;
    private static final long DAY_IN_MS = 24 * HOUR_IN_MS;
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
        final Type type = (Type)intent.getSerializableExtra("type");

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
        extras.putSerializable("type", RETRIEVE_ASSOCIATED_MINISTRIES);

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
        extras.putSerializable("type", RETRIEVE_ALL_MINISTRIES);
        extras.putBoolean(EXTRA_FORCE, force);

        context.startService(baseIntent(context, extras));
    }

    public static void saveAssociatedMinistriesFromServer(@NonNull final Context context,
                                                          @Nullable final JSONArray assignments) {
        Log.i(TAG, assignments != null ? assignments.toString() : "null");

        Bundle extras = new Bundle(1);
        extras.putSerializable("type", SAVE_ASSOCIATED_MINISTRIES);

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

    private void syncAllMinistries(final Intent intent) throws ApiException {
        final SharedPreferences prefs = this.getSharedPreferences(PREFS_SYNC, MODE_PRIVATE);
        final boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
        final boolean stale =
                System.currentTimeMillis() - prefs.getLong(PREF_SYNC_TIME_MINISTRIES, 0) > STALE_DURATION_MINISTRIES;

        // only sync if being forced or the data is stale
        if (force || stale) {
            // refresh the list of ministries if the load is being forced
            final List<Ministry> ministries = mApi.getAllMinistries(force);

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
                } finally {
                    tx.end();
                }

                // update the synced time
                prefs.edit().putLong(PREF_SYNC_TIME_MINISTRIES, System.currentTimeMillis()).apply();

                // send broadcasts that data has been updated in the database
                broadcastManager.sendBroadcast(BroadcastUtils.updateMinistriesBroadcast());
                broadcastManager.sendBroadcast(allMinistriesReceivedBroadcast((ArrayList<Ministry>) ministries));
            }
        }
    }

    private void saveAssociatedMinistriesFromServer(Intent intent)
    {
        List<Assignment> assignments = (ArrayList<Assignment>) intent.getSerializableExtra("assignments");

        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        ministriesDao.saveAssociatedMinistries(assignments);

        broadcastManager.sendBroadcast(stopBroadcast(SAVE_ASSOCIATED_MINISTRIES));
    }
}
