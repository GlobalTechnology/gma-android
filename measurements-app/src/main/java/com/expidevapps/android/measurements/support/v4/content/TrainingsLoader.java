package com.expidevapps.android.measurements.support.v4.content;

import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.sync.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;
import org.ccci.gto.android.common.util.BundleCompat;

import java.util.List;

public class TrainingsLoader extends AsyncTaskBroadcastReceiverLoader<List<Training>> {
    @NonNull
    private final GmaDao mDao;
    @Nullable
    private final String mMinistryId;
    @NonNull
    private final Mcc mMcc;

    public TrainingsLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);
        mDao = GmaDao.getInstance(context);
        if (args != null) {
            mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, Ministry.INVALID_ID);
            mMcc = Mcc.fromRaw(args.getString(ARG_MCC));
        } else {
            mMinistryId = Ministry.INVALID_ID;
            mMcc = Mcc.UNKNOWN;
        }

        // configure Loader if we have a valid ministryId (& MCC)
        if (!mMinistryId.equals(Ministry.INVALID_ID) && mMcc != Mcc.UNKNOWN) {
            addIntentFilter(BroadcastUtils.updateTrainingFilter(/* mMinistryId */));
        }
    }

    @Nullable
    @Override
    public List<Training> loadInBackground() {
        if (mMinistryId != null && mMcc != Mcc.UNKNOWN) {
            return mDao.get(Training.class, Contract.Training.SQL_WHERE_MINISTRY_MCC_NOT_DELETED,
                            bindValues(mMinistryId, mMcc));
        }
        return null;
    }
}
