package com.expidev.gcmapp.model;

import java.io.Serializable;

/**
 * Created by William.Randall on 2/4/2015.
 */
public class Measurement extends Base implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String name;
    private String measurementId;
    private String permLink;
    private boolean custom;
    private String section;
    private String column;
    private int total;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getMeasurementId()
    {
        return measurementId;
    }

    public void setMeasurementId(String measurementId)
    {
        this.measurementId = measurementId;
    }

    public String getPermLink()
    {
        return permLink;
    }

    public void setPermLink(String permLink)
    {
        this.permLink = permLink;
    }

    public boolean isCustom()
    {
        return custom;
    }

    public void setCustom(boolean custom)
    {
        this.custom = custom;
    }

    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    public String getColumn()
    {
        return column;
    }

    public void setColumn(String column)
    {
        this.column = column;
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
