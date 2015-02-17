package com.expidev.gcmapp.model.measurement;

import java.io.Serializable;

/**
 * Created by William.Randall on 1/29/2015.
 */
public class SubMinistryDetails extends MeasurementDetailsData implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String name;
    private String ministryId;
    private int total;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(String ministryId)
    {
        this.ministryId = ministryId;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }
}
