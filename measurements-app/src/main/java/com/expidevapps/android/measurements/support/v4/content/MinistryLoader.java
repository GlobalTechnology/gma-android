package com.expidevapps.android.measurements.support.v4.content;

import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

public class MinistryLoader extends AsyncTaskBroadcastReceiverLoader<Ministry> {
    @NonNull
    private final GmaDao mDao;

    @NonNull
    private final String mMinistryId;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public MinistryLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);
        mDao = GmaDao.getInstance(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mMinistryId = args != null ? args.getString(ARG_MINISTRY_ID, Ministry.INVALID_ID) : Ministry.INVALID_ID;
        } else {
            final String ministryId = args != null ? args.getString(ARG_MINISTRY_ID) : null;
            mMinistryId = ministryId != null ? ministryId : Ministry.INVALID_ID;
        }
    }

    @Override
    public Ministry loadInBackground() {
        if (!Ministry.INVALID_ID.equals(mMinistryId)) {
            return mDao.find(Ministry.class, mMinistryId);
        }

        return null;
    }
}
