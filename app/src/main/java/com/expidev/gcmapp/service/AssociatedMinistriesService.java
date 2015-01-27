package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.AssignmentsJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.expidev.gcmapp.service.Action.RETRIEVE_ALL_MINISTRIES;
import static com.expidev.gcmapp.service.Action.RETRIEVE_ASSOCIATED_MINISTRIES;
import static com.expidev.gcmapp.service.Action.SAVE_ASSIGNMENT;
import static com.expidev.gcmapp.service.Action.SAVE_ASSOCIATED_MINISTRIES;

/**
 * Created by William.Randall on 1/22/2015.
 */
public class AssociatedMinistriesService extends IntentService
{
    private static final String TAG = AssociatedMinistriesService.class.getSimpleName();

    private LocalBroadcastManager broadcastManager;

    public static final String ACTION_RETRIEVE_ASSOCIATED_MINISTRIES =
        AssociatedMinistriesService.class.getName() + ".ACTION_RETRIEVE_ASSOCIATED_MINISTRIES";
    public static final String ACTION_RETRIEVE_ALL_MINISTRIES =
        AssociatedMinistriesService.class.getName() + ".ACTION_RETRIEVE_ALL_MINISTRIES";

    public AssociatedMinistriesService()
    {
        super("AssociatedMinistriesService");
    }

    /////////////////////////////////////////////////////
    //           Lifecycle Handlers                   //
    ////////////////////////////////////////////////////
    @Override
    public void onCreate()
    {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        final Action action = (Action)intent.getSerializableExtra("action");

        switch(action)
        {
            case RETRIEVE_ASSOCIATED_MINISTRIES:
                retrieveMinistries();
                break;
            case RETRIEVE_ALL_MINISTRIES:
                retrieveAllMinistries(intent);
                break;
            case SAVE_ASSOCIATED_MINISTRIES:
                saveAssociatedMinistriesFromServer(intent);
                break;
            case SAVE_ASSIGNMENT:
                assignUserToMinistry(intent);
                break;
            default:
                break;
        }
    }


    /////////////////////////////////////////////////////
    //           Service API                          //
    ////////////////////////////////////////////////////
    private static Intent baseIntent(final Context context, Bundle extras)
    {
        final Intent intent = new Intent(context, AssociatedMinistriesService.class);

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
        extras.putSerializable("action", RETRIEVE_ASSOCIATED_MINISTRIES);

        context.startService(baseIntent(context, extras));
    }

    /**
     * Retrieve all ministries from the GCM API
     */
    public static void retrieveAllMinistries(final Context context, String sessionTicket)
    {
        Bundle extras = new Bundle(2);
        extras.putSerializable("action", RETRIEVE_ALL_MINISTRIES);
        extras.putString("sessionTicket", sessionTicket);

        context.startService(baseIntent(context, extras));
    }

    public static void saveAssociatedMinistriesFromServer(final Context context, JSONArray assignments)
    {
        Bundle extras = new Bundle(1);
        extras.putSerializable("action", SAVE_ASSOCIATED_MINISTRIES);

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
        extras.putSerializable("action", SAVE_ASSIGNMENT);
        extras.putSerializable("assignment", assignment);
        context.startService(baseIntent(context, extras));
    }


    /////////////////////////////////////////////////////
    //           Actions                              //
    ////////////////////////////////////////////////////
    private void retrieveMinistries()
    {
        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        List<String> associatedMinistries = ministriesDao.retrieveAssociatedMinistries();
        Log.i(TAG, "Retrieved associated ministries");

        Intent broadcastToSettingsActivity = new Intent(ACTION_RETRIEVE_ASSOCIATED_MINISTRIES);
        broadcastToSettingsActivity.putExtra("associatedMinistries", listToCharSequenceArray(associatedMinistries));

        broadcastManager.sendBroadcast(broadcastToSettingsActivity);
    }

    private void retrieveAllMinistries(final Intent intent)
    {
        Intent broadcastAllMinistriesRetrieved = new Intent(ACTION_RETRIEVE_ALL_MINISTRIES);

        GmaApiClient apiClient = new GmaApiClient(this);
        List<Ministry> ministryList = apiClient.getAllMinistries(intent.getStringExtra("sessionTicket"));

        broadcastAllMinistriesRetrieved.putExtra("ministryTeamList", (ArrayList<Ministry>) ministryList);
        broadcastManager.sendBroadcast(broadcastAllMinistriesRetrieved);
    }

    private CharSequence[] listToCharSequenceArray(List<String> list)
    {
        return list.toArray(new CharSequence[list.size()]);
    }

    private void saveAssociatedMinistriesFromServer(Intent intent)
    {
        List<Assignment> assignments = (ArrayList<Assignment>) intent.getSerializableExtra("assignments");

        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        ministriesDao.saveAssociatedMinistries(assignments);

        //TODO: May need to notify when running and when finished at some point
    }

    private void assignUserToMinistry(Intent intent)
    {
        List<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add((Assignment) intent.getSerializableExtra("assignment"));

        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        ministriesDao.saveAssociatedMinistries(assignments);
    }
}
