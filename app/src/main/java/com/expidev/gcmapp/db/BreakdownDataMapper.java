package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.BreakdownData;

/**
 * Created by William.Randall on 2/12/2015.
 */
public class BreakdownDataMapper extends MeasurementDetailsDataMapper<BreakdownData>
{
    @Override
    protected void mapField(
        @NonNull ContentValues values,
        @NonNull String field,
        @NonNull BreakdownData breakdownData)
    {
        switch(field)
        {
            case Contract.BreakdownData.COLUMN_SOURCE:
                values.put(field, breakdownData.getSource());
                break;
            case Contract.BreakdownData.COLUMN_AMOUNT:
                values.put(field, breakdownData.getAmount());
                break;
            case Contract.BreakdownData.COLUMN_TYPE:
                values.put(field, breakdownData.getType());
                break;
            default:
                super.mapField(values, field, breakdownData);
                break;
        }
    }

    @NonNull
    @Override
    protected BreakdownData newObject(@NonNull Cursor cursor)
    {
        return new BreakdownData();
    }

    @NonNull
    @Override
    public BreakdownData toObject(@NonNull Cursor cursor)
    {
        final BreakdownData breakdownData = super.toObject(cursor);

        breakdownData.setSource(this.getString(cursor, Contract.BreakdownData.COLUMN_SOURCE, null));
        breakdownData.setAmount(this.getInt(cursor, Contract.BreakdownData.COLUMN_AMOUNT, 0));
        breakdownData.setType(this.getString(cursor, Contract.BreakdownData.COLUMN_TYPE, null));

        return super.toObject(cursor);
    }
}
