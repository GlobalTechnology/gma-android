package com.expidevapps.android.measurements.db;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.MinistryMeasurement;

import org.joda.time.YearMonth;

public class MinistryMeasurementMapper extends MeasurementValueMapper<MinistryMeasurement> {
    @NonNull
    @Override
    protected MinistryMeasurement newObject(@NonNull final Cursor c) {
        final String permLink =
                getNonNullString(c, Contract.MinistryMeasurement.COLUMN_PERM_LINK_STUB, MeasurementType.INVALID_PERM_LINK_STUB);
        final String ministryId = getNonNullString(c, Contract.MinistryMeasurement.COLUMN_MINISTRY_ID,
                                                   Ministry.INVALID_ID);
        final Ministry.Mcc mcc = Ministry.Mcc.fromRaw(getString(c, Contract.MinistryMeasurement.COLUMN_MCC));
        final YearMonth period = getNonNullYearMonth(c, Contract.MinistryMeasurement.COLUMN_PERIOD,
                                                     YearMonth.now());
        return new MinistryMeasurement(ministryId, mcc, permLink, period);
    }
}
