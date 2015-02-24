package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;

public abstract class Location extends Base {
    private double latitude;
    private double longitude;

    protected Location() {
    }

    protected Location(@NonNull final Location location) {
        super(location);
        this.latitude = location.latitude;
        this.longitude = location.longitude;
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
}
