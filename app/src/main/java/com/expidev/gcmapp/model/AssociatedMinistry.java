package com.expidev.gcmapp.model;

import java.util.List;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class AssociatedMinistry extends Ministry
{
    private String ministryCode;
    private boolean hasSlm;
    private boolean hasLlm;
    private boolean hasDs;
    private boolean hasGcm;
    private List<AssociatedMinistry> subMinistries;

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

    public List<AssociatedMinistry> getSubMinistries()
    {
        return subMinistries;
    }

    public void setSubMinistries(List<AssociatedMinistry> subMinistries)
    {
        this.subMinistries = subMinistries;
    }
}
