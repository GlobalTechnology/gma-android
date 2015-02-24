package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.support.v4.util.LongSparseArray;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.TrainingDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.TrainingJsonParser;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.AbstractDao.Transaction;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    
    private static final String EXTRA_FORCE = TrainingService.class.getName() + ".EXTRA_FORCE";
    private static final String EXTRA_TRAINING_ID = TrainingService.class.getName() + ".EXTRA_TRAINING_ID";

    private static final String PREF_SYNC_TIME_TRAINING = "last_synced.training";

    // various stale data durations
    private static final long HOUR_IN_MS = 60 * 60 * 1000;
    private static final long DAY_IN_MS = 24 * HOUR_IN_MS;
    private static final long STALE_DURATION_TRAINING = 7 * DAY_IN_MS;

    @NonNull
    private GmaApiClient mApi;
    @NonNull
    private TrainingDao mDao;
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
        mDao = TrainingDao.getInstance(this);

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
                case SYNC_TRAINING:
                    syncTraining(intent);
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
            JSONArray jsonArray = mApi.searchTraining(ministryId, mcc);

            if (jsonArray != null)
            {
                // parse returned trainings
                final List<Training> trainings = TrainingJsonParser.parseTrainings(jsonArray);

                // save all trainings to the database
                TrainingDao trainingDao = TrainingDao.getInstance(this);
                final AbstractDao.Transaction tx = trainingDao.newTransaction();
                try {
                    tx.begin();

                    // save trainings
                    for (final Training training : trainings) {
                        trainingDao.saveTraining(training);
                    }

                    // TODO: remove missing trainings for this ministry & mcc

                    tx.setSuccessful();
                } finally {
                    tx.end();
                }
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
    
    private void syncTraining(final Intent intent) throws ApiException
    {
        final String ministryId = intent.getStringExtra(MINISTRY_ID);
        final String mcc = intent.getStringExtra(MINISTRY_MCC);

        final Transaction tx = mDao.newTransaction();
        tx.begin();
        
        try
        {
            // get list of training from api
            JSONArray jsonArray = mApi.searchTraining(ministryId, mcc);
            
            if (jsonArray != null)
            {
                final List<Training> trainings = TrainingJsonParser.parseTrainings(jsonArray);
                
                if (trainings != null)
                {
                    
                    
                    final LongSparseArray<Training> current = new LongSparseArray<>();
                    for (final Training training : mDao.get(Training.class, Contract.Training.SQL_WHERE_MINISTRY_ID, new String[]{ministryId}))
                    {
                        current.put(training.getId(), training);   
                    }
                    
                    long[] ids = new long[current.size() + trainings.size()];
                    int j = 0;
                    for (final Training training : trainings)
                    {
                        training.setLastSynced(new Date());
                        current.remove(training.getId());
                        ids[j++] = training.getId();
                    }
                    
                    for (int i = 0; i < current.size(); i++)
                    {
                        final Training training = current.valueAt(i);
                        mDao.delete(training);
                        ids[j++] = training.getId();
                    }
                    
                    tx.setSuccessful();
                    
                    broadcastManager.sendBroadcast(BroadcastUtils.updateTrainingBroadcast(ministryId, Arrays.copyOf(ids, j)));
                }
            }
        } catch (Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
        finally
        {
            tx.end();
        }
    }
}
