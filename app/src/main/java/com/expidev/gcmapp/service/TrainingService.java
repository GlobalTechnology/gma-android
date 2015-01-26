package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.TrainingDao;
import com.expidev.gcmapp.http.GmaApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.trainingReceivedBroadcast;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class TrainingService extends IntentService
{
    private final String TAG = getClass().getSimpleName();

    private static final String EXTRA_TYPE = TrainingService.class.getName() + ".EXTRA_TYPE";
    private static final String MINISTRY_ID = TrainingService.class.getName() + ".MINISTRY_ID";
    
    private static final int DOWNLOAD_TRAINING = 0;
    private static final int UPLOAD_TRAINING = 1;

    private final String PREF_NAME = "gcm_prefs";
    
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
        broadcastManager = LocalBroadcastManager.getInstance(this);

        sharedPreferences = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        this.broadcastManager.sendBroadcast(startBroadcast());
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        broadcastManager.sendBroadcast(runningBroadcast());
        
        final int type = intent.getIntExtra(EXTRA_TYPE, -1);
        Log.i(TAG, "Type: " + type);
        try
        {
            switch (type)
            {
                case DOWNLOAD_TRAINING:
                    searchTraining(intent.getStringExtra(MINISTRY_ID));
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
        final Intent intent = new Intent(context, AuthService.class);
        if (extras != null)
        {
            intent.putExtras(extras);
        }
        return intent;
    }
    
    public static void downloadTraining(final Context context, UUID ministryId)
    {
        final Bundle extras = new Bundle(2);
        extras.putInt(EXTRA_TYPE, DOWNLOAD_TRAINING);
        extras.putString(MINISTRY_ID, ministryId.toString());
        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }
    
    private void searchTraining(String ministryId)
    {
        try
        {
            String sessionTicket = sharedPreferences.getString("session_ticket", null);

            GmaApiClient gmaApi = new GmaApiClient(this);
            JSONObject jsonObject = gmaApi.searchTraining(ministryId, sessionTicket);

            if (jsonObject != null)
            {
                TrainingDao trainingDao = TrainingDao.getInstance(this);
                JSONArray jsonArray = new JSONArray(jsonObject.toString());
                trainingDao.saveTrainingFromAPI(jsonArray);
            }
            
            broadcastManager.sendBroadcast(trainingReceivedBroadcast());
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
