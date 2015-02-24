package com.expidev.gcmapp.model.measurement;

import java.io.Serializable;

/**
 * Created by William.Randall on 2/12/2015.
 */
public class BreakdownData extends MeasurementDetailsData implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String source; // One of which is "total"
    private int amount; // The amount for the source
    private String type; // local or self

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
