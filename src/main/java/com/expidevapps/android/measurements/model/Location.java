package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Location extends Base {
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

    @Nullable
    public final LatLng getLocation() {
        return hasLocation() ? new LatLng(latitude, longitude) : null;
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
