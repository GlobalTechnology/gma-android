package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Base;
import com.expidev.gcmapp.model.Ministry;

import java.io.Serializable;

/**
 * Created by William.Randall on 2/11/2015.
 */
public class MeasurementDetailsData extends Base implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String measurementId;
    private String ministryId;
    @NonNull
    private Ministry.Mcc mcc;
    private String period;

    public String getMeasurementId()
    {
        return measurementId;
    }

    public void setMeasurementId(String measurementId)
    {
        this.measurementId = measurementId;
    }

    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(String ministryId)
    {
        this.ministryId = ministryId;
    }

    @NonNull
    public Ministry.Mcc getMcc() {
        return mcc;
    }

    public void setMcc(@NonNull final Ministry.Mcc mcc) {
        this.mcc = mcc;
    }

    public String getPeriod()
    {
        return period;
    }

    public void setPeriod(String period)
    {
        this.period = period;
    }
}
