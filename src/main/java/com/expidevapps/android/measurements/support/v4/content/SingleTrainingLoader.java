package com.expidevapps.android.measurements.support.v4.content;

import android.content.Context;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Training;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

/**
 * Created by matthewfrederick on 2/24/15.
 */
public class SingleTrainingLoader extends AsyncTaskBroadcastReceiverLoader<Training>
{
    private final GmaDao mDao;
    private final long mId;
    
    public SingleTrainingLoader(@NonNull final Context context, final long trainingId)
    {
        super(context);
        setBroadcastReceiver(new TrainingLoaderBroadcastReceiver(this, trainingId));
        mDao = GmaDao.getInstance(context);
        mId = trainingId;
    }

    @Override
    public Training loadInBackground()
    {
        if (mId != Training.INVALID_ID)
        {
            return mDao.find(Training.class, mId);
        }
        return null;
    }
}
