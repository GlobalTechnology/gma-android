package com.expidevapps.android.measurements.map;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Location;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public abstract class GmaItem<T extends Location> implements ClusterItem {
    @Nullable
    private final Assignment mAssignment;
    @NonNull
    protected final T mObj;
    @Nullable
    private GmaItem<?> mParent;
    @NonNull
    private final LatLng mPosition;

    protected GmaItem(@Nullable final Assignment assignment, @NonNull final T obj) {
        mAssignment = assignment;
        mObj = obj;

        final LatLng location = mObj.getLocation();
        if (location == null) {
            throw new IllegalStateException("Location object needs to have a location to be rendered");
        }
        mPosition = location;
    }

    @NonNull
    public final T getObject() {
        return mObj;
    }

    @Nullable
    public GmaItem<?> getParent() {
        return mParent;
    }

    public void setParent(@Nullable final GmaItem<?> parent) {
        mParent = parent;
    }

    public abstract String getName();

    public abstract String getSnippet(@NonNull final Context context);

    @DrawableRes
    public abstract int getItemImage();

    @NonNull
    @Override
    public final LatLng getPosition() {
        return mPosition;
    }

    public boolean isDraggable() {
        return mObj.canEdit(mAssignment);
    }
}
