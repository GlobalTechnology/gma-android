package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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
import org.ccci.gto.android.common.db.AbstractDao.Transaction;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.expidev.gcmapp.service.Type.LOAD_ALL_MINISTRIES;
import static com.expidev.gcmapp.service.Type.RETRIEVE_ALL_MINISTRIES;
import static com.expidev.gcmapp.service.Type.RETRIEVE_ASSOCIATED_MINISTRIES;
import static com.expidev.gcmapp.service.Type.SAVE_ASSIGNMENT;
import static com.expidev.gcmapp.service.Type.SAVE_ASSOCIATED_MINISTRIES;
import static com.expidev.gcmapp.utils.BroadcastUtils.allMinistriesLoadedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.allMinistriesReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.associatedMinistriesReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;

/**
 * Created by William.Randall on 1/22/2015.
 */
public class MinistriesService extends IntentService
{
    private static final String TAG = MinistriesService.class.getSimpleName();

    @NonNull
    private GmaApiClient mApi;
    @NonNull
    private MinistriesDao mDao;
    private LocalBroadcastManager broadcastManager;

    public MinistriesService()
    {
        super("MinistriesService");
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
                case SAVE_ASSIGNMENT:
                    assignUserToMinistry(intent);
                    break;
                case LOAD_ALL_MINISTRIES:
                    loadAllMinistriesFromLocalStorage();
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
        Bundle extras = new Bundle(1);
        extras.putSerializable("type", RETRIEVE_ALL_MINISTRIES);

        context.startService(baseIntent(context, extras));
    }

    public static void loadAllMinistriesFromLocalStorage(final Context context)
    {
        Bundle extras = new Bundle(1);
        extras.putSerializable("type", LOAD_ALL_MINISTRIES);

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

    public static void assignUserToMinistry(final Context context, Assignment assignment)
    {
        Bundle extras = new Bundle(2);
        extras.putSerializable("type", SAVE_ASSIGNMENT);
        extras.putSerializable("assignment", assignment);
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
        //TODO: use SharedPreferences to track all ministries sync time to reduce server load
        final List<Ministry> ministries = mApi.getAllMinistries();

        if(ministries != null) {
            // save all ministries to the database
            final Transaction tx = mDao.newTransaction();
            try {
                tx.begin();

                // update all the ministry names
                for(final Ministry ministry : ministries) {
                    // this is only a very minimal update, so don't log last synced for new ministries
                    ministry.setLastSynced(0);
                    mDao.updateOrInsert(ministry, new String[] {Contract.Ministry.COLUMN_NAME});
                }

                tx.setSuccessful();
            } finally {
                tx.end();
            }

            // send broadcasts that data has been updated in the database
            broadcastManager.sendBroadcast(BroadcastUtils.updateMinistriesBroadcast());
            broadcastManager.sendBroadcast(allMinistriesReceivedBroadcast((ArrayList<Ministry>) ministries));
        }
    }

    private void loadAllMinistriesFromLocalStorage()
    {
        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        List<Ministry> allMinistries = ministriesDao.get(Ministry.class);

        broadcastManager.sendBroadcast(allMinistriesLoadedBroadcast((ArrayList<Ministry>) allMinistries));
    }

    private void saveAssociatedMinistriesFromServer(Intent intent)
    {
        List<Assignment> assignments = (ArrayList<Assignment>) intent.getSerializableExtra("assignments");

        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        ministriesDao.saveAssociatedMinistries(assignments);

        broadcastManager.sendBroadcast(stopBroadcast(SAVE_ASSOCIATED_MINISTRIES));
    }

    private void assignUserToMinistry(Intent intent)
    {
        List<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add((Assignment) intent.getSerializableExtra("assignment"));

        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        ministriesDao.saveAssociatedMinistries(assignments);

        broadcastManager.sendBroadcast(stopBroadcast(SAVE_ASSIGNMENT));
    }
}
