package com.expidev.gcmapp.map;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Location;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public abstract class Marker<T extends Location> implements ClusterItem {
    @NonNull
    protected final T mObj;
    @NonNull
    private final LatLng mPosition;

    protected Marker(@NonNull final T obj) {
        mObj = obj;
        if (!obj.hasLocation()) {
            throw new IllegalArgumentException("Location object needs to have a location to be rendered");
        }
        assert obj.getLocation() != null;
        mPosition = obj.getLocation();
    }

    @NonNull
    public final T getObject() {
        return mObj;
    }

    public abstract String getName();

    public abstract String getSnippet();

    @DrawableRes
    public abstract int getItemImage();

    @NonNull
    @Override
    public final LatLng getPosition() {
        return mPosition;
    }

    public boolean isDraggable() {
        return false;
    }
}
