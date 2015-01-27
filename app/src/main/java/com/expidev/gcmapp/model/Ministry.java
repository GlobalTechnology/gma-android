package com.expidev.gcmapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by William.Randall on 1/9/2015.
 */
public class Ministry implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String ministryId;
    private String name;
    private String ministryCode;
    private boolean hasSlm;
    private boolean hasLlm;
    private boolean hasDs;
    private boolean hasGcm;
    private List<Ministry> subMinistries;

    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(String ministryId)
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

    public String getMinistryCode()
    {
        return ministryCode;
    }

    public void setMinistryCode(String ministryCode)
    {
        this.ministryCode = ministryCode;
    }

    public boolean hasSlm()
    {
        return hasSlm;
    }

    public void setHasSlm(boolean hasSlm)
    {
        this.hasSlm = hasSlm;
    }

    public boolean hasLlm()
    {
        return hasLlm;
    }

    public void setHasLlm(boolean hasLlm)
    {
        this.hasLlm = hasLlm;
    }

    public boolean hasDs()
    {
        return hasDs;
    }

    public void setHasDs(boolean hasDs)
    {
        this.hasDs = hasDs;
    }

    public boolean hasGcm()
    {
        return hasGcm;
    }

    public void setHasGcm(boolean hasGcm)
    {
        this.hasGcm = hasGcm;
    }

    public List<Ministry> getSubMinistries()
    {
        return subMinistries;
    }

    public void setSubMinistries(List<Ministry> subMinistries)
    {
        this.subMinistries = subMinistries;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
