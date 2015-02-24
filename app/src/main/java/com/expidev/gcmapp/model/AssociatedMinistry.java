package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class AssociatedMinistry extends Ministry
{
    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_NAME = "name";
    public static final String JSON_CODE = "min_code";
    public static final String JSON_LOCATION = "location";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LONGITUDE = "longitude";
    public static final String JSON_LOCATION_ZOOM = "location_zoom";

    @Nullable
    private String parentMinistryId;
    private String ministryCode;
    @NonNull
    private final EnumSet<Mcc> mccs = EnumSet.noneOf(Mcc.class);
    private double latitude;
    private double longitude;
    private int locationZoom;

    @NonNull
    public static List<AssociatedMinistry> listFromJson(@NonNull final JSONArray json) throws JSONException {
        final List<AssociatedMinistry> ministries = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            ministries.add(fromJson(json.getJSONObject(i)));
        }
        return ministries;
    }

    @NonNull
    public static AssociatedMinistry fromJson(@NonNull final JSONObject json) throws JSONException {
        final AssociatedMinistry ministry = new AssociatedMinistry();
        ministry.setMinistryId(json.getString(JSON_MINISTRY_ID));
        ministry.setName(json.getString(JSON_NAME));
        ministry.setMinistryCode(json.optString(JSON_CODE));
        ministry.setHasSlm(json.optBoolean("has_slm"));
        ministry.setHasLlm(json.optBoolean("has_llm"));
        ministry.setHasDs(json.optBoolean("has_ds"));
        ministry.setHasGcm(json.optBoolean("has_gcm"));

        // load location data
        double latitude = json.optDouble(JSON_LATITUDE);
        double longitude = json.optDouble(JSON_LONGITUDE);
        final JSONObject location = json.optJSONObject(JSON_LOCATION);
        if (location != null) {
            // location JSON currently broke in getMinistry
            latitude = location.optDouble(JSON_LATITUDE, latitude);
            longitude = location.optDouble(JSON_LONGITUDE, longitude);
        }
        ministry.setLatitude(latitude);
        ministry.setLongitude(longitude);
        ministry.setLocationZoom(json.optInt(JSON_LOCATION_ZOOM));

        return ministry;
    }

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
}
