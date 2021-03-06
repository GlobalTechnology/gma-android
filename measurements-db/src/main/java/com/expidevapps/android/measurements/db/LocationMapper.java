package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Location.COLUMN_LATITUDE;
import static com.expidevapps.android.measurements.db.Contract.Location.COLUMN_LONGITUDE;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Location;

abstract class LocationMapper<T extends Location> extends BaseMapper<T> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field, @NonNull final T obj) {
        switch (field) {
            case COLUMN_LATITUDE:
                final double latitude = obj.getLatitude();
                values.put(field, Double.isNaN(latitude) ? null : latitude);
                break;
            case COLUMN_LONGITUDE:
                final double longitude = obj.getLongitude();
                values.put(field, Double.isNaN(longitude) ? null : longitude);
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

        obj.setLatitude(getDouble(c, COLUMN_LATITUDE, Double.NaN));
        obj.setLongitude(getDouble(c, COLUMN_LONGITUDE, Double.NaN));

        return obj;
    }
}
