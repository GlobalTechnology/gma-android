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
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementType;
import com.expidev.gcmapp.model.measurement.MeasurementValue;
import com.expidev.gcmapp.model.measurement.PersonalMeasurement;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;
import org.joda.time.YearMonth;

import java.util.ArrayList;
import java.util.List;

public class MeasurementValueLoader<T extends MeasurementValue> extends AsyncTaskBroadcastReceiverLoader<T> {
    @NonNull
    private final GmaDao mDao;

    @NonNull
    private final Class<T> mClass;
    @Nullable
    private final Object[] mKey;
    private final boolean mLoadType;

    public MeasurementValueLoader(@NonNull final Context context, @NonNull final Class<T> clazz,
                                  @Nullable final Bundle args) {
        super(context);
        mDao = GmaDao.getInstance(context);
        mClass = clazz;

        // process arguments
        if (args != null) {
            final List<Object> key = new ArrayList<>();
            if (PersonalMeasurement.class.equals(mClass)) {
                key.add(args.getString(ARG_GUID));
            }
            key.add(args.getString(ARG_MINISTRY_ID));
            key.add(Ministry.Mcc.fromRaw(args.getString(ARG_MCC)));
            key.add(args.getString(ARG_PERMLINK));
            key.add(YearMonth.parse(args.getString(ARG_PERIOD)));

            // export key
            mKey = key.toArray(new Object[key.size()]);
        } else {
            mKey = null;
        }
        mLoadType = true;
    }

    @Nullable
    @Override
    public T loadInBackground() {
        if (mKey != null) {
            final T value = mDao.find(mClass, mKey);
            if(value != null) {
                value.setType(mDao.find(MeasurementType.class, value.getPermLink()));
            }
            return value;
        } else {
            return null;
        }
    }
}
