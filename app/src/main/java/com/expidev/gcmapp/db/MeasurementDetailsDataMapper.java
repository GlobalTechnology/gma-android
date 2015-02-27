package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementDetailsData;

/**
 * Created by William.Randall on 2/12/2015.
 */
public abstract class MeasurementDetailsDataMapper<T extends MeasurementDetailsData> extends BaseMapper<T>
{
    @Override
    protected void mapField(@NonNull ContentValues values, @NonNull String field, @NonNull T obj)
    {
        switch(field)
        {
            case Contract.MeasurementDetailsData.COLUMN_MEASUREMENT_ID:
                values.put(field, obj.getMeasurementId());
                break;
            case Contract.MeasurementDetailsData.COLUMN_MINISTRY_ID:
                values.put(field, obj.getMinistryId());
                break;
            case Contract.MeasurementDetailsData.COLUMN_MCC:
                values.put(field, obj.getMcc().toString());
                break;
            case Contract.MeasurementDetailsData.COLUMN_PERIOD:
                values.put(field, obj.getPeriod());
                break;
            default:
                super.mapField(values, field, obj);
                break;
        }
    }

    @NonNull
    @Override
    public T toObject(@NonNull Cursor cursor)
    {
        final T obj = super.toObject(cursor);
        obj.setMeasurementId(this.getString(cursor, Contract.MeasurementDetailsData.COLUMN_MEASUREMENT_ID, null));
        obj.setMinistryId(this.getString(cursor, Contract.MeasurementDetailsData.COLUMN_MINISTRY_ID, null));
        obj.setMcc(Ministry.Mcc.fromRaw(getString(cursor, Contract.MeasurementDetailsData.COLUMN_MCC, null)));
        obj.setPeriod(this.getString(cursor, Contract.MeasurementDetailsData.COLUMN_PERIOD, null));
        return obj;
    }
}
