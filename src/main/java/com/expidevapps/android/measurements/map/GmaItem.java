package com.expidevapps.android.measurements.map;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Location;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public abstract class GmaItem<T extends Location> implements ClusterItem {
    @Nullable
    private Assignment mAssignment;
    @NonNull
    protected T mObj;
    @Nullable
    private GmaItem<?> mParent;

    protected GmaItem(@Nullable final Assignment assignment, @NonNull final T obj) {
        mAssignment = assignment;
        mObj = obj;
    }

    public final void setAssignment(@Nullable final Assignment assignment) {
        mAssignment = assignment;
    }

    @NonNull
    public final T getObject() {
        return mObj;
    }

    public final void setObject(@NonNull final T obj) {
        mObj = obj;
    }

    @Nullable
    public GmaItem<?> getParent() {
        return mParent;
    }

    public void setParent(@Nullable final GmaItem<?> parent) {
        mParent = parent;
    }

    public abstract String getName();

    public abstract String getSnippet();

    @DrawableRes
    public abstract int getItemImage();

    @NonNull
    @Override
    public final LatLng getPosition() {
        final LatLng location = mObj.getLocation();
        if (location == null) {
            throw new IllegalStateException("Location object needs to have a location to be rendered");
        }

        return location;
    }

    public boolean isDraggable() {
        return mObj.canEdit(mAssignment);
    }
}
