package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Mcc.COLUMN_MCC;
import static com.expidevapps.android.measurements.db.Contract.MeasurementPermLink.COLUMN_PERM_LINK_STUB;
import static com.expidevapps.android.measurements.db.Contract.MeasurementValue.COLUMN_DELTA;
import static com.expidevapps.android.measurements.db.Contract.MeasurementValue.COLUMN_VALUE;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.Period.COLUMN_PERIOD;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.MeasurementValue;

abstract class MeasurementValueMapper<T extends MeasurementValue> extends BaseMapper<T> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final T measurement) {
        switch (field) {
            case COLUMN_MINISTRY_ID:
                values.put(field, measurement.getMinistryId());
                break;
            case COLUMN_MCC:
                values.put(field, measurement.getMcc().toString());
                break;
            case COLUMN_PERM_LINK_STUB:
                values.put(field, measurement.getPermLinkStub());
                break;
            case COLUMN_PERIOD:
                values.put(field, measurement.getPeriod().toString());
                break;
            case COLUMN_VALUE:
                values.put(field, measurement.getValue());
                break;
            case COLUMN_DELTA:
                values.put(field, measurement.getDelta());
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

        measurement.setValue(getInt(c, COLUMN_VALUE, 0));
        measurement.setDelta(getInt(c, COLUMN_DELTA, 0));

        return measurement;
    }
}
