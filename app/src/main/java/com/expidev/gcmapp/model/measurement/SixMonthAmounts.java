package com.expidev.gcmapp.model.measurement;

import java.io.Serializable;

/**
 * Created by William.Randall on 2/11/2015.
 */
public class SixMonthAmounts extends MeasurementDetailsData implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String month;
    private int amount;
    private String amountType;

    public String getMonth()
    {
        return month;
    }

    public void setMonth(String month)
    {
        this.month = month;
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
