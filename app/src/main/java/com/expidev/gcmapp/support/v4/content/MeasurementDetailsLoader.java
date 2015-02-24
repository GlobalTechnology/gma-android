package com.expidev.gcmapp.support.v4.content;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.Constants;
import com.expidev.gcmapp.db.MeasurementDao;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by William.Randall on 2/23/2015.
 */
public class MeasurementDetailsLoader extends AsyncTaskBroadcastReceiverLoader<MeasurementDetails>
{
    private final MeasurementDao measurementDao;

    @Nullable
    private String measurementId;
    @Nullable
    private String ministryId;
    @Nullable
    private String mcc;
    @Nullable
    private String period;

    public MeasurementDetailsLoader(
        @NonNull final Context context,
        @Nullable final Bundle args,
        @NonNull final IntentFilter... filters)
    {
        this(
            context,
            args != null ? args.getString(Constants.ARG_MEASUREMENT_ID) : null,
            args != null ? args.getString(Constants.ARG_MINISTRY_ID) : null,
            args != null ? args.getString(Constants.ARG_MCC) : null,
            args != null ? args.getString(Constants.ARG_PERIOD) : null,
            filters
        );
    }

    public MeasurementDetailsLoader(
        @NonNull final Context context,
        @Nullable String measurementId,
        @Nullable String ministryId,
        @Nullable String mcc,
        @Nullable String period,
        @NonNull final IntentFilter... filters)
    {
        super(context, filters);

        measurementDao = MeasurementDao.getInstance(context);

        if(measurementId != null && ministryId != null && mcc != null)
        {
            if(period == null)
            {
                this.period = getCurrentPeriod();
            }
            else
            {
                this.period = period;
            }

            this.measurementId = measurementId;
            this.ministryId = ministryId;
            this.mcc = mcc;
        }
    }

    private String getCurrentPeriod()
    {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public MeasurementDetails loadInBackground()
    {
        if(measurementId != null && ministryId != null && mcc != null && period != null)
        {
            return measurementDao.loadMeasurementDetails(measurementId, ministryId, mcc, period);
        }

        return null;
    }
}
