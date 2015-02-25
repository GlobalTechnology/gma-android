package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class AssociatedMinistry extends Ministry
{
    @Nullable
    private String parentMinistryId;
    private String ministryCode;
    @NonNull
    private final EnumSet<Mcc> mccs = EnumSet.noneOf(Mcc.class);
    private double latitude;
    private double longitude;
    private int locationZoom;

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

    @NonNull
    public EnumSet<Mcc> getMccs() {
        return EnumSet.copyOf(this.mccs);
    }

    public boolean hasMcc(@NonNull final Mcc mcc) {
        return this.mccs.contains(mcc);
    }

    public void setMccs(final Collection<Mcc> mccs) {
        this.mccs.clear();
        if (mccs != null) {
            this.mccs.addAll(mccs);
        }
    }

    public boolean hasSlm()
    {
        return hasMcc(Mcc.SLM);
    }

    public void setHasSlm(boolean hasSlm)
    {
        if (hasSlm) {
            this.mccs.add(Mcc.SLM);
        } else {
            this.mccs.remove(Mcc.SLM);
        }
    }

    public boolean hasLlm()
    {
        return hasMcc(Mcc.LLM);
    }

    public void setHasLlm(boolean hasLlm)
    {
        if (hasLlm) {
            this.mccs.add(Mcc.LLM);
        } else {
            this.mccs.remove(Mcc.LLM);
        }
    }

    public boolean hasDs()
    {
        return hasMcc(Mcc.DS);
    }

    public void setHasDs(boolean hasDs)
    {
        if (hasDs) {
            this.mccs.add(Mcc.DS);
        } else {
            this.mccs.remove(Mcc.DS);
        }
    }

    public boolean hasGcm()
    {
        return hasMcc(Mcc.GCM);
    }

    public void setHasGcm(boolean hasGcm)
    {
        if (hasGcm) {
            this.mccs.add(Mcc.GCM);
        } else {
            this.mccs.remove(Mcc.GCM);
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public int getLocationZoom() {
        return locationZoom;
    }

    public void setLocationZoom(final int locationZoom) {
        this.locationZoom = locationZoom;
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
