package com.expidev.gcmapp.support.v4.content;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.Constants;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MeasurementDao;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by William.Randall on 2/23/2015.
 */
public class MeasurementsLoader extends AsyncTaskBroadcastReceiverLoader<List<Measurement>>
{
    private final MeasurementDao measurementDao;

    @Nullable
    private String ministryId;
    @Nullable
    private String mcc;
    @Nullable
    private String currentPeriod;

    public MeasurementsLoader(
        @NonNull final Context context,
        @Nullable final Bundle args,
        @NonNull final IntentFilter... filters)
    {
        this(
            context,
            args != null ? args.getString(Constants.ARG_MINISTRY_ID) : null,
            args != null ? args.getString(Constants.ARG_MCC) : null,
            args != null ? args.getString(Constants.ARG_PERIOD) : null,
            filters);
    }

    public MeasurementsLoader(
        @NonNull final Context context,
        @Nullable final String ministryId,
        @Nullable final String mcc,
        @Nullable final String period,
        @NonNull final IntentFilter... filters)
    {
        super(context, filters);

        measurementDao = MeasurementDao.getInstance(context);

        if(ministryId != null && mcc != null)
        {
            this.ministryId = ministryId;
            this.mcc = mcc;
            this.currentPeriod = period != null ? period : getCurrentPeriod();
            addIntentFilter(BroadcastUtils.updateMeasurementsFilter());
        }
    }

    private String getCurrentPeriod()
    {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public List<Measurement> loadInBackground()
    {
        if(ministryId != null && mcc != null && currentPeriod != null)
        {
            return measurementDao.get(
                Measurement.class,
                Contract.Measurement.SQL_WHERE_MINISTRY_MCC_PERIOD,
                new String[] { ministryId, mcc, currentPeriod });
        }

        return null;
    }
}
