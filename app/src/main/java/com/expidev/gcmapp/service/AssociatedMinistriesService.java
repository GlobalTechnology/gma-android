package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.json.AssignmentsJsonParser;
import com.expidev.gcmapp.json.MinistryJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.JsonStringReader;

import org.apache.http.HttpStatus;
import org.json.JSONArray;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.expidev.gcmapp.service.Action.RETRIEVE_ALL_MINISTRIES;
import static com.expidev.gcmapp.service.Action.RETRIEVE_ASSOCIATED_MINISTRIES;
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
    public static void retrieveAllMinistries(final Context context, String apiUrl)
    {
        Bundle extras = new Bundle(2);
        extras.putSerializable("action", RETRIEVE_ALL_MINISTRIES);
        extras.putString("apiUrl", apiUrl);

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
        String apiUrl = intent.getStringExtra("apiUrl");
        Intent broadcastAllMinistriesRetrieved = new Intent(ACTION_RETRIEVE_ALL_MINISTRIES);
        String reason;

        try
        {
            URL url = new URL(apiUrl);

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(1000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            urlConnection.connect();
            int statusCode = urlConnection.getResponseCode();

            if (statusCode == HttpStatus.SC_OK)
            {
                InputStream inputStream = urlConnection.getInputStream();

                if (inputStream != null)
                {
                    String jsonAsString = JsonStringReader.readFully(inputStream, "UTF-8");
                    Log.i(TAG, jsonAsString);

                    // A successful response will return an array, an error response will return an object
                    if(jsonAsString.substring(0,1).equals("["))
                    {
                        JSONArray jsonArray = new JSONArray(jsonAsString);
                        List<Ministry> ministryTeamList = MinistryJsonParser.parseMinistriesJson(jsonArray);

                        broadcastAllMinistriesRetrieved.putExtra(
                            "ministryTeamList",
                            (ArrayList<Ministry>) ministryTeamList);
                    }
                    else
                    {
                        broadcastAllMinistriesRetrieved.putExtra(
                            "ministryTeamList",
                            dummyMinistryList());

//                        JSONObject jsonObject = new JSONObject(jsonAsString);
//                        reason = jsonObject.optString("reason");
//                        broadcastAllMinistriesRetrieved.putExtra("reason", reason);
                    }
                }
            }
            else
            {
                broadcastAllMinistriesRetrieved.putExtra(
                    "ministryTeamList",
                    dummyMinistryList());
//                reason = "Status Code: " + statusCode + " returned";
//                Log.e(TAG, reason);
//                broadcastAllMinistriesRetrieved.putExtra("reason", reason);
            }
        }
        catch(Exception e)
        {
            //Do stuff
//            reason = e.getMessage();
//            Log.e(TAG, "Problem occurred while retrieving ministries: " + reason);
//            broadcastAllMinistriesRetrieved.putExtra("reason", reason);
        }
        finally
        {
            broadcastManager.sendBroadcast(broadcastAllMinistriesRetrieved);
        }
    }

    private CharSequence[] listToCharSequenceArray(List<String> list)
    {
        return list.toArray(new CharSequence[list.size()]);
    }

    private ArrayList<Ministry> dummyMinistryList()
    {
        ArrayList<Ministry> dummyList = new ArrayList<Ministry>();

        Ministry dummy1 = new Ministry();
        dummy1.setMinistryId("37e3bb68-da0b-11e3-9786-12725f8f377c");
        dummy1.setName("Addis Ababa Campus Team (ETH)");

        dummyList.add(dummy1);

        return dummyList;
    }

    private void saveAssociatedMinistriesFromServer(Intent intent)
    {
        List<Assignment> assignments = (ArrayList<Assignment>) intent.getSerializableExtra("assignments");

        MinistriesDao ministriesDao = MinistriesDao.getInstance(this);
        ministriesDao.saveAssociatedMinistries(assignments);

        //TODO: May need to notify when running and when finished at some point
    }
}
