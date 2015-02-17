package com.expidev.gcmapp.model.measurement;

import com.expidev.gcmapp.model.Base;

import java.io.Serializable;

/**
 * Created by William.Randall on 1/29/2015.
 */
public class MeasurementTypeIds extends MeasurementDetailsData implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String total;
    private String local;
    private String person;

    public String getTotal()
    {
        return total;
    }

    public void setTotal(String total)
    {
        this.total = total;
    }

    public String getLocal()
    {
        return local;
    }

    public void setLocal(String local)
    {
        this.local = local;
    }

    public String getPerson()
    {
        return person;
    }

    public void setPerson(String person)
    {
        this.person = person;
    }
}
