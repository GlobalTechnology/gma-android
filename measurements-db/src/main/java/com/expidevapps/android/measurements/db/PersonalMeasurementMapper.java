package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Guid.COLUMN_GUID;
import static com.expidevapps.android.measurements.db.Contract.Mcc.COLUMN_MCC;
import static com.expidevapps.android.measurements.db.Contract.MeasurementPermLink.COLUMN_PERM_LINK_STUB;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.Period.COLUMN_PERIOD;
import static com.expidevapps.android.measurements.model.MeasurementType.INVALID_PERM_LINK_STUB;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.PersonalMeasurement;

import org.joda.time.YearMonth;

class PersonalMeasurementMapper extends MeasurementValueMapper<PersonalMeasurement> {
    @Override
    protected void mapField(@NonNull ContentValues values, @NonNull String field,
                            @NonNull PersonalMeasurement measurement) {
        switch (field) {
            case COLUMN_GUID:
                values.put(field, measurement.getGuid());
                break;
            default:
                super.mapField(values, field, measurement);
                break;
        }
    }

    @NonNull
    @Override
    protected PersonalMeasurement newObject(@NonNull final Cursor c) {
        return new PersonalMeasurement(getNonNullString(c, COLUMN_GUID, ""),
                                       getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID),
                                       Mcc.fromRaw(getString(c, COLUMN_MCC)),
                                       getNonNullString(c, COLUMN_PERM_LINK_STUB, INVALID_PERM_LINK_STUB),
                                       getNonNullYearMonth(c, COLUMN_PERIOD, YearMonth.now()));
    }
}
