package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.MeasurementTypeIds;

/**
 * Created by William.Randall on 2/12/2015.
 */
public class MeasurementTypeIdsMapper extends MeasurementDetailsDataMapper<MeasurementTypeIds>
{
    @Override
    protected void mapField(
        @NonNull ContentValues values,
        @NonNull String field,
        @NonNull MeasurementTypeIds measurementTypeIds)
    {
        switch(field)
        {
            case Contract.MeasurementTypeIds.COLUMN_TOTAL_ID:
                values.put(field, measurementTypeIds.getTotal());
                break;
            case Contract.MeasurementTypeIds.COLUMN_LOCAL_ID:
                values.put(field, measurementTypeIds.getLocal());
                break;
            case Contract.MeasurementTypeIds.COLUMN_PERSON_ID:
                values.put(field, measurementTypeIds.getPerson());
                break;
            default:
                super.mapField(values, field, measurementTypeIds);
                break;
        }
    }

    @NonNull
    @Override
    protected MeasurementTypeIds newObject(@NonNull Cursor cursor)
    {
        return new MeasurementTypeIds();
    }

    @NonNull
    @Override
    public MeasurementTypeIds toObject(@NonNull Cursor cursor)
    {
        final MeasurementTypeIds measurementTypeIds = super.toObject(cursor);

        measurementTypeIds.setTotal(getString(cursor, Contract.MeasurementTypeIds.COLUMN_TOTAL_ID));
        measurementTypeIds.setLocal(getString(cursor, Contract.MeasurementTypeIds.COLUMN_LOCAL_ID));
        measurementTypeIds.setPerson(getString(cursor, Contract.MeasurementTypeIds.COLUMN_PERSON_ID));

        return measurementTypeIds;
    }
}
