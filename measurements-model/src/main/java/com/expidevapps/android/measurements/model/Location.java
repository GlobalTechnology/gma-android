package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Location extends Base {
    public static final String JSON_LOCATION = "location";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LONGITUDE = "longitude";

    private double latitude = Double.NaN;
    private double longitude = Double.NaN;

    protected Location() {
    }

    protected Location(@NonNull final Location location) {
        super(location);
        this.latitude = location.latitude;
        this.longitude = location.longitude;
    }

    @Override
    void populateFromJson(@NonNull final JSONObject json) throws JSONException {
        super.populateFromJson(json);

        // parse the latitude & longitude
        final JSONObject location = json.optJSONObject(JSON_LOCATION);
        if (location != null) {
            // ministries and stories nest latitude and longitude in location node
            latitude = location.optDouble(JSON_LATITUDE, latitude);
            longitude = location.optDouble(JSON_LONGITUDE, longitude);
        }
        // default to latitude and longitude at the root level
        latitude = json.optDouble(JSON_LATITUDE, latitude);
        longitude = json.optDouble(JSON_LONGITUDE, longitude);
    }

    public boolean canEdit(@Nullable final Assignment assignment) {
        return false;
    }

    public final boolean hasLocation() {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude);
    }

    public final double getLatitude() {
        return latitude;
    }

    public final void setLatitude(final double latitude) {
        if (mTrackingChanges && this.latitude != latitude) {
            mDirty.add(JSON_LATITUDE);
        }

        this.latitude = latitude;
    }

    public final double getLongitude() {
        return longitude;
    }

    public final void setLongitude(final double longitude) {
        if (mTrackingChanges && this.longitude != longitude) {
            mDirty.add(JSON_LONGITUDE);
        }

        this.longitude = longitude;
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        final JSONObject json = super.toJson();
        if (!Double.isNaN(latitude)) {
            json.put(JSON_LATITUDE, this.latitude);
        }
        if (!Double.isNaN(this.longitude)) {
            json.put(JSON_LONGITUDE, this.longitude);
        }
        return json;
    }
}
