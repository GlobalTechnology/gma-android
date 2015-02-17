package com.expidev.gcmapp.model.measurement;

import java.io.Serializable;

/**
 * Created by William.Randall on 2/11/2015.
 */
public class SixMonthAmounts extends MeasurementDetailsData implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String period;
    private int amount;
    private String amountType;

    public String getPeriod()
    {
        return period;
    }

    public void setPeriod(String period)
    {
        this.period = period;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    public String getAmountType()
    {
        return amountType;
    }

    public void setAmountType(String amountType)
    {
        this.amountType = amountType;
    }
}
