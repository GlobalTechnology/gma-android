package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class Ministry extends Location implements Serializable {
    private static final long serialVersionUID = 0L;

    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_NAME = "name";
    public static final String JSON_CODE = "min_code";
    public static final String JSON_MCCS = "mccs";
    public static final String JSON_LOCATION = "location";
    public static final String JSON_LOCATION_ZOOM = "location_zoom";

    @Deprecated
    private static final String JSON_HAS_DS = "has_ds";
    @Deprecated
    private static final String JSON_HAS_GCM = "has_gcm";
    @Deprecated
    private static final String JSON_HAS_LLM = "has_llm";
    @Deprecated
    private static final String JSON_HAS_SLM = "has_slm";

    public enum Mcc {
        UNKNOWN(null), SLM("slm"), LLM("llm"), DS("ds"), GCM("gcm");

        @Nullable
        public final String raw;

        Mcc(final String raw) {
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
    private int locationZoom;

    @NonNull
    public static List<Ministry> listFromJson(@NonNull final JSONArray json) throws JSONException {
        final List<Ministry> ministries = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            ministries.add(fromJson(json.getJSONObject(i)));
        }
        return ministries;
    }

    @NonNull
    public static Ministry fromJson(@NonNull final JSONObject json) throws JSONException {
        final Ministry ministry = new Ministry();
        ministry.setMinistryId(json.getString(JSON_MINISTRY_ID));
        ministry.setName(json.getString(JSON_NAME));
        ministry.setMinistryCode(json.optString(JSON_CODE));

        // parse the mccs array
        final JSONArray mccs = json.optJSONArray(JSON_MCCS);
        if (mccs != null) {
            for (int i = 0; i < mccs.length(); i++) {
                ministry.mccs.add(Mcc.fromRaw(mccs.getString(i)));
            }
        }
        // parse legacy has_{mcc} flags if we don't have an mccs array
        else if (BuildConfig.GMA_API_VERSION < 4) {
            if (json.optBoolean(JSON_HAS_DS, false)) {
                ministry.mccs.add(Mcc.DS);
            }
            if (json.optBoolean(JSON_HAS_GCM, false)) {
                ministry.mccs.add(Mcc.GCM);
            }
            if (json.optBoolean(JSON_HAS_LLM, false)) {
                ministry.mccs.add(Mcc.LLM);
            }
            if (json.optBoolean(JSON_HAS_SLM, false)) {
                ministry.mccs.add(Mcc.SLM);
            }
        }

        // load location data
        final JSONObject location = json.optJSONObject(JSON_LOCATION);
        if (location != null) {
            // location JSON currently broke in getMinistry
            ministry.setLatitude(location.optDouble(JSON_LATITUDE));
            ministry.setLongitude(location.optDouble(JSON_LONGITUDE));
        }
        ministry.setLocationZoom(json.optInt(JSON_LOCATION_ZOOM));

        return ministry;
    }

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

    public void addMcc(@NonNull final Mcc mcc) {
        this.mccs.add(mcc);
    }

    public void removeMcc(@NonNull final Mcc mcc) {
        this.mccs.remove(mcc);
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
