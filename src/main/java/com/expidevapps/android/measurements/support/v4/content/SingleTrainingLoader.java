package com.expidevapps.android.measurements.support.v4.content;

import android.content.Context;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Training;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

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
        Training mTraining = null;
        if (mId != Training.INVALID_ID)
        {
            //return mDao.find(Training.class, mId);
            mTraining = mDao.find(Training.class, mId);

            //load training completions and set on training
            mTraining.setCompletions(mDao.get(Training.Completion.class, Contract.Training.Completion.SQL_WHERE_NOT_DELETED_AND_TRAINING_ID, bindValues(mId)));

            return mTraining;
        }
        return null;
    }
}
