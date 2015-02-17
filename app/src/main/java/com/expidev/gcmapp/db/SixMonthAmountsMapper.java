package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.SixMonthAmounts;

/**
 * Created by William.Randall on 2/12/2015.
 */
public class SixMonthAmountsMapper extends MeasurementDetailsDataMapper<SixMonthAmounts>
{
    @Override
    protected void mapField(
        @NonNull ContentValues values,
        @NonNull String field,
        @NonNull SixMonthAmounts sixMonthAmounts)
    {
        switch(field)
        {
            case Contract.SixMonthAmounts.COLUMN_PERIOD:
                values.put(field, sixMonthAmounts.getPeriod());
                break;
            case Contract.SixMonthAmounts.COLUMN_AMOUNT:
                values.put(field, sixMonthAmounts.getAmount());
                break;
            case Contract.SixMonthAmounts.COLUMN_AMOUNT_TYPE:
                values.put(field, sixMonthAmounts.getAmountType());
                break;
            default:
                super.mapField(values, field, sixMonthAmounts);
                break;
        }
    }

    @NonNull
    @Override
    protected SixMonthAmounts newObject(@NonNull Cursor c)
    {
        return new SixMonthAmounts();
    }

    @NonNull
    @Override
    public SixMonthAmounts toObject(@NonNull Cursor cursor)
    {
        final SixMonthAmounts sixMonthAmounts = super.toObject(cursor);

        sixMonthAmounts.setPeriod(this.getString(cursor, Contract.SixMonthAmounts.COLUMN_PERIOD, null));
        sixMonthAmounts.setAmount(this.getInt(cursor, Contract.SixMonthAmounts.COLUMN_AMOUNT, 0));
        sixMonthAmounts.setAmountType(this.getString(cursor, Contract.SixMonthAmounts.COLUMN_AMOUNT_TYPE, null));

        return sixMonthAmounts;
    }
}
