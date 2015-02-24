package com.expidev.gcmapp.model.measurement;

import java.io.Serializable;

/**
 * Created by William.Randall on 1/29/2015.
 */
public class SubMinistryDetails extends MeasurementDetailsData implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String name;
    private String subMinistryId;
    private int total;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSubMinistryId()
    {
        return subMinistryId;
    }

    public void setSubMinistryId(String subMinistryId)
    {
        this.subMinistryId = subMinistryId;
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
