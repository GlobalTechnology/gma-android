package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementType;
import com.expidev.gcmapp.model.measurement.MinistryMeasurement;

import org.joda.time.YearMonth;

public class MinistryMeasurementMapper extends BaseMapper<MinistryMeasurement> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final MinistryMeasurement measurement) {
        switch (field) {
            case Contract.MinistryMeasurement.COLUMN_MINISTRY_ID:
                values.put(field, measurement.getMinistryId());
                break;
            case Contract.MinistryMeasurement.COLUMN_MCC:
                values.put(field, measurement.getMcc().toString());
                break;
            case Contract.MinistryMeasurement.COLUMN_MEASUREMENT_TYPE_ID:
                values.put(field, measurement.getMeasurementId());
                break;
            case Contract.MinistryMeasurement.COLUMN_PERIOD:
                values.put(field, measurement.getPeriod().toString());
                break;
            case Contract.MinistryMeasurement.COLUMN_VALUE:
                values.put(field, measurement.getValue());
                break;
            default:
                super.mapField(values, field, measurement);
                break;
        }
    }

    @NonNull
    @Override
    protected MinistryMeasurement newObject(@NonNull final Cursor c) {
        final String measurementId = getNonNullString(c, Contract.MinistryMeasurement.COLUMN_MEASUREMENT_TYPE_ID,
                                                      MeasurementType.INVALID_ID);
        final String ministryId = getNonNullString(c, Contract.MinistryMeasurement.COLUMN_MINISTRY_ID,
                                                   Ministry.INVALID_ID);
        final Ministry.Mcc mcc = Ministry.Mcc.fromRaw(getString(c, Contract.MinistryMeasurement.COLUMN_MCC));
        final YearMonth period = getNonNullYearMonth(c, Contract.MinistryMeasurement.COLUMN_PERIOD,
                                                     YearMonth.now());
        return new MinistryMeasurement(ministryId, mcc, measurementId, period);
    }

    @NonNull
    @Override
    public MinistryMeasurement toObject(@NonNull final Cursor c) {
        final MinistryMeasurement measurement = super.toObject(c);
        measurement.setValue(getInt(c, Contract.MinistryMeasurement.COLUMN_VALUE, 0));
        return measurement;
    }
}
