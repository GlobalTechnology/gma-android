package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
}
