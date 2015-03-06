package com.expidev.gcmapp.db;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementType;
import com.expidev.gcmapp.model.measurement.MinistryMeasurement;

import org.joda.time.YearMonth;

public class MinistryMeasurementMapper extends MeasurementValueMapper<MinistryMeasurement> {
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
}
