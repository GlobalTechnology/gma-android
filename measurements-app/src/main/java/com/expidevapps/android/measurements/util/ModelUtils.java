package com.expidevapps.android.measurements.util;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Church.Development;
import com.expidevapps.android.measurements.model.Location;
import com.google.android.gms.maps.model.LatLng;

public class ModelUtils {
    @DrawableRes
    public static int getIcon(@NonNull final Development development) {
        switch (development) {
            case TARGET:
                return R.drawable.ic_church_target;
            case GROUP:
                return R.drawable.ic_church_group;
            case MULTIPLYING_CHURCH:
                return R.drawable.ic_church_multiplying;
            case CHURCH:
            case UNKNOWN:
            default:
                return R.drawable.ic_church_church;
        }
    }

    @Nullable
    public static LatLng getLocation(@NonNull final Location location) {
        return location.hasLocation() ? new LatLng(location.getLatitude(), location.getLongitude()) : null;
    }
}
