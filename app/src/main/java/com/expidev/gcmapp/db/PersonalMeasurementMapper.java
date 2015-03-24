package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementType;
import com.expidev.gcmapp.model.measurement.PersonalMeasurement;

import org.joda.time.YearMonth;

public class PersonalMeasurementMapper extends MeasurementValueMapper<PersonalMeasurement> {
    @Override
    protected void mapField(@NonNull ContentValues values, @NonNull String field,
                            @NonNull PersonalMeasurement measurement) {
        switch (field) {
            case Contract.PersonalMeasurement.COLUMN_GUID:
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
        final String guid = getNonNullString(c, Contract.PersonalMeasurement.COLUMN_GUID, "");
        final String permLink =
                getNonNullString(c, Contract.PersonalMeasurement.COLUMN_PERM_LINK_STUB, MeasurementType.INVALID_PERM_LINK_STUB);
        final String ministryId =
                getNonNullString(c, Contract.PersonalMeasurement.COLUMN_MINISTRY_ID, Ministry.INVALID_ID);
        final Ministry.Mcc mcc = Ministry.Mcc.fromRaw(getString(c, Contract.PersonalMeasurement.COLUMN_MCC));
        final YearMonth period = getNonNullYearMonth(c, Contract.PersonalMeasurement.COLUMN_PERIOD,
                                                     YearMonth.now());
        return new PersonalMeasurement(guid, ministryId, mcc, permLink, period);
    }
}
