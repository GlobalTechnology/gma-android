package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.MeasurementDetails;

/**
 * Created by William.Randall on 2/11/2015.
 */
public class MeasurementDetailsMapper extends BaseMapper<MeasurementDetails>
{
    @Override
    protected void mapField(
        @NonNull final ContentValues values,
        @NonNull final String field,
        @NonNull final MeasurementDetails measurementDetails)
    {
        switch(field)
        {
            case Contract.MeasurementDetails.COLUMN_MEASUREMENT_ID:
                values.put(field, measurementDetails.getMeasurementId());
                break;
            case Contract.MeasurementDetails.COLUMN_MINISTRY_ID:
                values.put(field, measurementDetails.getMinistryId());
                break;
            case Contract.MeasurementDetails.COLUMN_MCC:
                values.put(field, measurementDetails.getMcc());
                break;
            case Contract.MeasurementDetails.COLUMN_PERIOD:
                values.put(field, measurementDetails.getPeriod());
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

        measurementDetails.setMeasurementId(this.getString(cursor, Contract.MeasurementDetails.COLUMN_MEASUREMENT_ID, null));
        measurementDetails.setMinistryId(this.getString(cursor, Contract.MeasurementDetails.COLUMN_MINISTRY_ID, null));
        measurementDetails.setMcc(this.getString(cursor, Contract.MeasurementDetails.COLUMN_MCC, null));
        measurementDetails.setPeriod(this.getString(cursor, Contract.MeasurementDetails.COLUMN_PERIOD, null));

        return measurementDetails;
    }
}
