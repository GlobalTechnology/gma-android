package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Ministry extends Location {
    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_NAME = "name";
    public static final String JSON_CODE = "min_code";
    public static final String JSON_MCCS = "mccs";
    private static final String JSON_MCC_SLM = "slm";
    private static final String JSON_MCC_LLM = "llm";
    private static final String JSON_MCC_DS = "ds";
    private static final String JSON_MCC_GCM = "gcm";
    public static final String JSON_LMI_SHOW = "lmi_show";
    public static final String JSON_LMI_HIDE = "lmi_hide";
    public static final String JSON_LOCATION = "location";
    public static final String JSON_LOCATION_ZOOM = "location_zoom";

    public enum Mcc {
        UNKNOWN(""), SLM(JSON_MCC_SLM), LLM(JSON_MCC_LLM), DS(JSON_MCC_DS), GCM(JSON_MCC_GCM);

        @NonNull
        public final String mJson;
        @NonNull
        @Deprecated
        public final String raw;

        Mcc(@NonNull final String json) {
            mJson = json;
            this.raw = json;
        }

        @NonNull
        @Deprecated
        public static Mcc fromRaw(@Nullable final String raw) {
            return fromJson(raw);
        }

        @NonNull
        public static Mcc fromJson(@Nullable final String json) {
            if (json != null) {
                switch (json.toLowerCase(Locale.US)) {
                    case JSON_MCC_SLM:
                        return SLM;
                    case JSON_MCC_LLM:
                        return LLM;
                    case JSON_MCC_DS:
                        return DS;
                    case JSON_MCC_GCM:
                        return GCM;
                }
            }

            return UNKNOWN;
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
    @Nullable
    private Collection<String> lmiShow;
    @Nullable
    private Collection<String> lmiHide;

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

        // load location data
        final JSONObject location = json.optJSONObject(JSON_LOCATION);
        if (location != null) {
            // location JSON currently broke in getMinistry
            ministry.setLatitude(location.optDouble(JSON_LATITUDE));
            ministry.setLongitude(location.optDouble(JSON_LONGITUDE));
        }
        ministry.setLocationZoom(json.optInt(JSON_LOCATION_ZOOM));

        // parse lmi visibility
        final JSONArray show = json.optJSONArray(JSON_LMI_SHOW);
        if (show != null) {
            ministry.lmiShow = new HashSet<>();
            for (int i = 0; i < show.length(); i++) {
                final String lmi = show.optString(i);
                if (lmi != null) {
                    ministry.lmiShow.add(lmi);
                }
            }
        }
        final JSONArray hide = json.optJSONArray(JSON_LMI_HIDE);
        if (hide != null) {
            ministry.lmiHide = new HashSet<>();
            for (int i = 0; i < hide.length(); i++) {
                final String lmi = hide.optString(i);
                if (lmi != null) {
                    ministry.lmiHide.add(lmi);
                }
            }
        }

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

    @Nullable
    public Collection<String> getLmiShow() {
        return lmiShow;
    }

    @Nullable
    public Collection<String> getLmiHide() {
        return lmiHide;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
