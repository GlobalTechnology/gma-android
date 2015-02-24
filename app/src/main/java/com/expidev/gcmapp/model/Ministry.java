package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by William.Randall on 1/9/2015.
 */
public class Ministry extends Base implements Serializable
{
    private static final long serialVersionUID = 0L;

    public static final String INVALID_ID = "";

    @NonNull
    private String ministryId = INVALID_ID;
    private String name;

    @NonNull
    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(@NonNull final String ministryId)
    {
        this.ministryId = ministryId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
