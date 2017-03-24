package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Mcc.COLUMN_MCC;
import static com.expidevapps.android.measurements.db.Contract.MeasurementPermLink.COLUMN_PERM_LINK_STUB;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.Period.COLUMN_PERIOD;
import static com.expidevapps.android.measurements.model.MeasurementType.INVALID_PERM_LINK_STUB;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.MinistryMeasurement;

import org.joda.time.YearMonth;

class MinistryMeasurementMapper extends MeasurementValueMapper<MinistryMeasurement> {
    @NonNull
    @Override
    protected MinistryMeasurement newObject(@NonNull final Cursor c) {
        return new MinistryMeasurement(getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID),
                                       Mcc.fromRaw(getString(c, COLUMN_MCC)),
                                       getNonNullString(c, COLUMN_PERM_LINK_STUB, INVALID_PERM_LINK_STUB),
                                       getNonNullYearMonth(c, COLUMN_PERIOD, YearMonth.now()));
    }
}
