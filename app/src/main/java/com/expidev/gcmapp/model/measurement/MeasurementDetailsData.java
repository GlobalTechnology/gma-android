package com.expidev.gcmapp.model.measurement;

import com.expidev.gcmapp.model.Base;

import java.io.Serializable;

/**
 * Created by William.Randall on 2/11/2015.
 */
public class MeasurementDetailsData extends Base implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String measurementId;

    public String getMeasurementId()
    {
        return measurementId;
    }

    public void setMeasurementId(String measurementId)
    {
        this.measurementId = measurementId;
    }
}
