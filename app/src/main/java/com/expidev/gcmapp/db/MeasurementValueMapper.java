package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.MeasurementValue;

public abstract class MeasurementValueMapper<T extends MeasurementValue> extends BaseMapper<T> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final T measurement) {
        switch (field) {
            case Contract.MeasurementValue.COLUMN_MINISTRY_ID:
                values.put(field, measurement.getMinistryId());
                break;
            case Contract.MeasurementValue.COLUMN_MCC:
                values.put(field, measurement.getMcc().toString());
                break;
            case Contract.MeasurementValue.COLUMN_MEASUREMENT_TYPE_ID:
                values.put(field, measurement.getMeasurementId());
                break;
            case Contract.MeasurementValue.COLUMN_PERIOD:
                values.put(field, measurement.getPeriod().toString());
                break;
            case Contract.MeasurementValue.COLUMN_VALUE:
                values.put(field, measurement.getValue());
                break;
            default:
                super.mapField(values, field, measurement);
                break;
        }
    }

    @NonNull
    @Override
    public T toObject(@NonNull final Cursor c) {
        final T measurement = super.toObject(c);
        measurement.setValue(getInt(c, Contract.MeasurementValue.COLUMN_VALUE, 0));
        return measurement;
    }
}
