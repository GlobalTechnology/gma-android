package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.ARG_MCC;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.ARG_PERIOD;
import static com.expidev.gcmapp.Constants.ARG_PERMLINK;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.db.GmaDao;
import com.expidev.gcmapp.model.MeasurementDetails;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.Ministry.Mcc;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;
import org.ccci.gto.android.common.util.BundleCompat;
import org.joda.time.YearMonth;

public class MeasurementDetailsLoader extends AsyncTaskBroadcastReceiverLoader<MeasurementDetails> {
    private final GmaDao mDao;

    @Nullable
    private final String mGuid;
    @NonNull
    private final String mMinistryId;
    @NonNull
    private final Mcc mMcc;
    @Nullable
    private final String mPermLink;
    @NonNull
    private final YearMonth mPeriod;

    public MeasurementDetailsLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);
        mDao = GmaDao.getInstance(context);

        if (args != null) {
            mGuid = args.getString(ARG_GUID);
            mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, Ministry.INVALID_ID);
            mMcc = Mcc.fromRaw(args.getString(ARG_MCC));
            mPermLink = args.getString(ARG_PERMLINK);
            final String rawPeriod = args.getString(ARG_PERIOD);
            mPeriod = rawPeriod != null ? YearMonth.parse(rawPeriod) : YearMonth.now();
        } else {
            mGuid = null;
            mMinistryId = Ministry.INVALID_ID;
            mMcc = Mcc.UNKNOWN;
            mPermLink = null;
            mPeriod = YearMonth.now();
        }
    }

    @Override
    public MeasurementDetails loadInBackground() {
        if (mGuid != null && !Ministry.INVALID_ID.equals(mMinistryId) && mMcc != Mcc.UNKNOWN && mPermLink != null) {
            return mDao.find(MeasurementDetails.class, mGuid, mMinistryId, mMcc, mPermLink, mPeriod);
        }
        return null;
    }
}
