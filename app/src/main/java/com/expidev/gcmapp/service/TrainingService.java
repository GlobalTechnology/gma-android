package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.TrainingDao;
import com.expidev.gcmapp.http.GmaApiClient;

import org.json.JSONArray;

import static com.expidev.gcmapp.service.Type.DOWNLOAD_TRAINING;
import static com.expidev.gcmapp.service.Type.TRAINING;
import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class TrainingService extends IntentService
{
    private final String TAG = getClass().getSimpleName();

    private static final String EXTRA_TYPE = TrainingService.class.getName() + ".EXTRA_TYPE";
    private static final String MINISTRY_ID = TrainingService.class.getName() + ".MINISTRY_ID";
    private static final String MINISTRY_MCC = TrainingService.class.getName() + ".MCC";

    private final String PREF_NAME = "gcm_prefs";

    @NonNull
    private GmaApiClient mApi;
    private LocalBroadcastManager broadcastManager;
    private SharedPreferences sharedPreferences;
    
    public TrainingService()
    {
        super("TrainingService");        
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mApi = GmaApiClient.getInstance(this);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        sharedPreferences = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        this.broadcastManager.sendBroadcast(startBroadcast());
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.i(TAG, "Handle Intent");
        broadcastManager.sendBroadcast(runningBroadcast());
        
        final Type type = (Type) intent.getSerializableExtra(EXTRA_TYPE);
        Log.i(TAG, "Type: " + type);
        try
        {
            switch (type)
            {
                case DOWNLOAD_TRAINING:
                    searchTraining(intent.getStringExtra(MINISTRY_ID), intent.getStringExtra(MINISTRY_MCC));
                    break;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static Intent baseIntent(final Context context, final Bundle extras)
    {
        final Intent intent = new Intent(context, TrainingService.class);
        if (extras != null)
        {
            intent.putExtras(extras);
        }
        return intent;
    }
    
    public static void downloadTraining(final Context context, String ministryId, String mcc)
    {
        final Bundle extras = new Bundle(3);
        extras.putSerializable(EXTRA_TYPE, DOWNLOAD_TRAINING);
        extras.putString(MINISTRY_ID, ministryId);
        extras.putString(MINISTRY_MCC, mcc);
        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }
    
    private void searchTraining(String ministryId, String mcc)
    {
        try
        {
            String sessionTicket = sharedPreferences.getString("session_ticket", null);

            JSONArray jsonArray = mApi.searchTraining(ministryId, mcc, sessionTicket);

            if (jsonArray != null)
            {
                Log.i(TAG, jsonArray.toString());
                
                TrainingDao trainingDao = TrainingDao.getInstance(this);
                trainingDao.saveTrainingFromAPI(jsonArray);
            }
            else
            {
                Log.d(TAG, "JSON Object is null");
            }
            
            broadcastManager.sendBroadcast(stopBroadcast(TRAINING));
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
