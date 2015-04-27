package com.expidevapps.android.measurements.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Location;

public abstract class LocationMapper<T extends Location> extends BaseMapper<T> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field, @NonNull final T obj) {
        switch (field) {
            case Contract.Location.COLUMN_LATITUDE:
                values.put(field, obj.getLatitude());
                break;
            case Contract.Location.COLUMN_LONGITUDE:
                values.put(field, obj.getLongitude());
                break;
            default:
                super.mapField(values, field, obj);
                break;
        }
    }

    @NonNull
    @Override
    public T toObject(@NonNull final Cursor c) {
        final T obj = super.toObject(c);
        obj.setLatitude(getDouble(c, Contract.Location.COLUMN_LATITUDE, Double.NaN));
        obj.setLongitude(getDouble(c, Contract.Location.COLUMN_LONGITUDE, Double.NaN));
        return obj;
    }
}
