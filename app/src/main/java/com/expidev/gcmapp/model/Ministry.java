package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;

public class Ministry extends Base implements Serializable
{
    private static final long serialVersionUID = 0L;

    public enum Mcc {
        UNKNOWN(null), SLM("slm"), LLM("llm"), DS("ds"), GCM("gcm");

        @Nullable
        public final String raw;

        private Mcc(final String raw) {
            this.raw = raw;
        }

        @NonNull
        public static Mcc fromRaw(@Nullable final String raw) {
            switch (raw != null ? raw.toLowerCase(Locale.US) : "") {
                case "slm":
                    return SLM;
                case "llm":
                    return LLM;
                case "ds":
                    return DS;
                case "gcm":
                    return GCM;
                default:
                    return UNKNOWN;
            }
        }
    }

    public static final String INVALID_ID = "";

    @NonNull
    private String ministryId = INVALID_ID;
    @Nullable
    private String parentMinistryId;
    private String ministryCode;
    private String name;
    @NonNull
    private final EnumSet<Mcc> mccs = EnumSet.noneOf(Mcc.class);
    private double latitude;
    private double longitude;
    private int locationZoom;

    @NonNull
    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(@NonNull final String ministryId)
    {
        this.ministryId = ministryId;
    }

    @Nullable
    public String getParentMinistryId() {
        return parentMinistryId;
    }

    public void setParentMinistryId(@Nullable final String parentMinistryId) {
        this.parentMinistryId = parentMinistryId;
    }

    public String getMinistryCode() {
        return ministryCode;
    }

    public void setMinistryCode(final String ministryCode) {
        this.ministryCode = ministryCode;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public boolean hasSlm() {
        return hasMcc(Mcc.SLM);
    }

    public void setHasSlm(boolean hasSlm) {
        if (hasSlm) {
            this.mccs.add(Mcc.SLM);
        } else {
            this.mccs.remove(Mcc.SLM);
        }
    }

    public boolean hasLlm() {
        return hasMcc(Mcc.LLM);
    }

    public void setHasLlm(boolean hasLlm) {
        if (hasLlm) {
            this.mccs.add(Mcc.LLM);
        } else {
            this.mccs.remove(Mcc.LLM);
        }
    }

    public boolean hasDs() {
        return hasMcc(Mcc.DS);
    }

    public void setHasDs(boolean hasDs) {
        if (hasDs) {
            this.mccs.add(Mcc.DS);
        } else {
            this.mccs.remove(Mcc.DS);
        }
    }

    public boolean hasGcm() {
        return hasMcc(Mcc.GCM);
    }

    public void setHasGcm(boolean hasGcm) {
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

    @Override
    public String toString()
    {
        return name;
    }
}
