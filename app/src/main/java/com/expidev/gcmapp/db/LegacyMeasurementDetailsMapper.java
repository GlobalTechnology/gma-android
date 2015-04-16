package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;

/**
 * Created by William.Randall on 2/11/2015.
 */
@Deprecated
public class LegacyMeasurementDetailsMapper extends BaseMapper<MeasurementDetails> {
    @Override
    protected void mapField(
        @NonNull final ContentValues values,
        @NonNull final String field,
        @NonNull final MeasurementDetails measurementDetails)
    {
        switch(field)
        {
            case Contract.LegacyMeasurementDetails.COLUMN_MEASUREMENT_ID:
                values.put(field, measurementDetails.getMeasurementId());
                break;
            case Contract.LegacyMeasurementDetails.COLUMN_MINISTRY_ID:
                values.put(field, measurementDetails.getMinistryId());
                break;
            case Contract.LegacyMeasurementDetails.COLUMN_MCC:
                values.put(field, measurementDetails.getMcc().toString());
                break;
            case Contract.LegacyMeasurementDetails.COLUMN_PERIOD:
                values.put(field, measurementDetails.getPeriod());
                break;
            case Contract.LegacyMeasurementDetails.COLUMN_LOCAL_AMOUNT:
                values.put(field, measurementDetails.getLocalValue());
                break;
            case Contract.LegacyMeasurementDetails.COLUMN_PERSONAL_AMOUNT:
                values.put(field, measurementDetails.getPersonalValue());
                break;
            default:
                super.mapField(values, field, measurementDetails);
                break;
        }
    }

    @NonNull
    @Override
    protected MeasurementDetails newObject(@NonNull Cursor cursor)
    {
        return new MeasurementDetails();
    }

    @NonNull
    @Override
    public MeasurementDetails toObject(@NonNull Cursor cursor)
    {
        final MeasurementDetails measurementDetails = super.toObject(cursor);

        measurementDetails.setMeasurementId(this.getString(cursor, Contract.LegacyMeasurementDetails.COLUMN_MEASUREMENT_ID, null));
        measurementDetails.setMinistryId(
                getNonNullString(cursor, Contract.LegacyMeasurementDetails.COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
        measurementDetails.setMcc(this.getString(cursor, Contract.LegacyMeasurementDetails.COLUMN_MCC, null));
        measurementDetails.setPeriod(this.getString(cursor, Contract.LegacyMeasurementDetails.COLUMN_PERIOD, null));
        measurementDetails.setLocalValue(this.getInt(cursor, Contract.LegacyMeasurementDetails.COLUMN_LOCAL_AMOUNT, 0));
        measurementDetails.setPersonalValue(this.getInt(cursor, Contract.LegacyMeasurementDetails.COLUMN_PERSONAL_AMOUNT, 0));

        return measurementDetails;
    }
}
