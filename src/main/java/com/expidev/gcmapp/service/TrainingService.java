package com.expidev.gcmapp.service;

import static com.expidev.gcmapp.service.Type.SYNC_DIRTY_TRAINING;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.runningBroadcast;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.startBroadcast;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.TrainingDao;
import com.expidevapps.android.measurements.model.Training;

import org.ccci.gto.android.common.api.ApiException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class TrainingService extends IntentService
{
    private final String TAG = getClass().getSimpleName();

    private static final String EXTRA_SYNCTYPE = "type";

    @NonNull
    private GmaApiClient mApi;
    @NonNull
    private TrainingDao mDao;
    private LocalBroadcastManager broadcastManager;

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

        this.broadcastManager.sendBroadcast(startBroadcast());
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.i(TAG, "Handle Intent");
        broadcastManager.sendBroadcast(runningBroadcast());
        
        final Type type = (Type) intent.getSerializableExtra(EXTRA_SYNCTYPE);
        Log.i(TAG, "Type: " + type);
        try
        {
            switch (type)
            {
                case SYNC_DIRTY_TRAINING:
                    syncDirtyTraining();
                    break;
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void syncDirtyTraining(@NonNull final Context context)
    {
        final Intent intent = new Intent(context, TrainingService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNC_DIRTY_TRAINING);
        context.startService(intent);
    }

    private synchronized void syncDirtyTraining() throws ApiException
    {
        final List<Training> dirty = mDao.get(Training.class, Contract.Training.SQL_WHERE_DIRTY, null);
        
        for (final Training training : dirty)
        {
            try
            {
                final JSONObject json = training.dirtyToJson();
                final boolean success = mApi.updateTraining(training.getId(), json);
                
                if (success)
                {
                    training.setDirty(null);
                    mDao.update(training, new String[] {Contract.Training.COLUMN_DIRTY});
                }
            }
            catch (final JSONException ignored) {
                
            }
        }
    }
}
