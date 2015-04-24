package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.TrainingDao;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

import java.util.List;

public class TrainingLoader extends AsyncTaskBroadcastReceiverLoader<List<Training>> {
    private final TrainingDao mDao;
    @Nullable
    private final String mMinistyId;

    public TrainingLoader(@NonNull final Context context, @Nullable final Bundle bundle) {
        this(context, bundle != null ? bundle.getString(ARG_MINISTRY_ID) : null);
    }

    public TrainingLoader(@NonNull final Context context, @Nullable final String ministryId) {
        super(context);
        if (ministryId != null)
        {
            setBroadcastReceiver(new TrainingLoaderBroadcastReceiver(this, ministryId));
            addIntentFilter(BroadcastUtils.updateTrainingFilter());
        }
        mDao = TrainingDao.getInstance(context);
        mMinistyId = ministryId;
    }
    
    @Override
    public List<Training> loadInBackground()
    {
        if (mMinistyId != null)
        {
            return mDao.get(Training.class, Contract.Training.SQL_WHERE_MINISTRY, new String[] {mMinistyId});
        }
        return null;
    }
}
