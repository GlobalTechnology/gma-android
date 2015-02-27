package com.expidev.gcmapp.service;

import static com.expidev.gcmapp.service.Type.DOWNLOAD_TRAINING;
import static com.expidev.gcmapp.service.Type.SYNC_DIRTY_TRAINING;
import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.TrainingDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.AbstractDao.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class TrainingService extends IntentService
{
    private final String TAG = getClass().getSimpleName();

    private static final String EXTRA_TYPE = TrainingService.class.getName() + ".EXTRA_TYPE";
    private static final String MINISTRY_ID = TrainingService.class.getName() + ".MINISTRY_ID";
    private static final String MINISTRY_MCC = TrainingService.class.getName() + ".MCC";

    private static final String EXTRA_SYNCTYPE = "type";

    private final String PREF_NAME = "gcm_prefs";

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
                    searchTraining(intent);
                    break;
                case SYNC_TRAINING:
                    syncTraining(intent);
                    break;
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

    public static Intent baseIntent(final Context context, final Bundle extras)
    {
        final Intent intent = new Intent(context, TrainingService.class);
        if (extras != null)
        {
            intent.putExtras(extras);
        }
        return intent;
    }

    public static void downloadTraining(@NonNull final Context context, @NonNull final String ministryId,
                                        @NonNull final Ministry.Mcc mcc) {
        final Bundle extras = new Bundle(3);
        extras.putSerializable(EXTRA_TYPE, DOWNLOAD_TRAINING);
        extras.putString(MINISTRY_ID, ministryId);
        extras.putString(MINISTRY_MCC, mcc.toString());
        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }
    
    public static void syncDirtyTraining(@NonNull final Context context)
    {
        final Intent intent = new Intent(context, TrainingService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNC_DIRTY_TRAINING);
        context.startService(intent);
    }

    private void searchTraining(@NonNull final Intent intent) {
        String ministryId = intent.getStringExtra(MINISTRY_ID);
        if (ministryId == null) {
            ministryId = Ministry.INVALID_ID;
        }
        final Ministry.Mcc mcc = Ministry.Mcc.fromRaw(intent.getStringExtra(MINISTRY_MCC));

        try
        {
            final List<Training> trainings = mApi.searchTraining(ministryId, mcc);

            if (trainings != null) {
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
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    
    private void syncTraining(final Intent intent) throws ApiException
    {
        String ministryId = intent.getStringExtra(MINISTRY_ID);
        if (ministryId == null) {
            ministryId = Ministry.INVALID_ID;
        }
        final Ministry.Mcc mcc = Ministry.Mcc.fromRaw(intent.getStringExtra(MINISTRY_MCC));

        final Transaction tx = mDao.newTransaction();
        tx.begin();
        
        try
        {
            // get list of training from api
            final List<Training> trainings = mApi.searchTraining(ministryId, mcc);

            if (trainings != null) {
                final LongSparseArray<Training> current = new LongSparseArray<>();
                for (final Training training : mDao.get(Training.class, Contract.Training.SQL_WHERE_MINISTRY_ID_MCC,
                                                        bindValues(ministryId, mcc))) {
                    current.put(training.getId(), training);
                }

                long[] ids = new long[current.size() + trainings.size()];
                int j = 0;
                for (final Training training : trainings) {
                    training.setLastSynced(new Date());
                    current.remove(training.getId());
                    ids[j++] = training.getId();
                }

                for (int i = 0; i < current.size(); i++) {
                    final Training training = current.valueAt(i);
                    mDao.delete(training);
                    ids[j++] = training.getId();
                }

                tx.setSuccessful();

                broadcastManager.sendBroadcast(
                        BroadcastUtils.updateTrainingBroadcast(ministryId, Arrays.copyOf(ids, j)));
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
