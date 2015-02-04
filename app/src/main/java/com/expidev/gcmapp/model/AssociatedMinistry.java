package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class AssociatedMinistry extends Ministry
{
    @Nullable
    private String parentMinistryId;
    private String ministryCode;
    private boolean hasSlm;
    private boolean hasLlm;
    private boolean hasDs;
    private boolean hasGcm;
    @NonNull
    private final List<AssociatedMinistry> subMinistries = new ArrayList<>();

    @Nullable
    public String getParentMinistryId() {
        return parentMinistryId;
    }

    public void setParentMinistryId(@Nullable final String parentMinistryId) {
        this.parentMinistryId = parentMinistryId;
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

    @NonNull
    public List<AssociatedMinistry> getSubMinistries()
    {
        return Collections.unmodifiableList(subMinistries);
    }

    public void setSubMinistries(@Nullable final List<AssociatedMinistry> ministries) {
        this.subMinistries.clear();
        if (ministries != null) {
            this.subMinistries.addAll(ministries);
        }
    }
}
