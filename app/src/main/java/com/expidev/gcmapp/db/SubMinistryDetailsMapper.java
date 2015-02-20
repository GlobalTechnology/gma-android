package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.SubMinistryDetails;

/**
 * Created by William.Randall on 2/12/2015.
 */
public class SubMinistryDetailsMapper extends MeasurementDetailsDataMapper<SubMinistryDetails>
{
    @Override
    protected void mapField(
        @NonNull ContentValues values,
        @NonNull String field,
        @NonNull SubMinistryDetails subMinistryDetails)
    {
        switch(field)
        {
            case Contract.SubMinistryDetails.COLUMN_NAME:
                values.put(field, subMinistryDetails.getName());
                break;
            case Contract.SubMinistryDetails.COLUMN_SUB_MINISTRY_ID:
                values.put(field, subMinistryDetails.getSubMinistryId());
                break;
            case Contract.SubMinistryDetails.COLUMN_TOTAL:
                values.put(field, subMinistryDetails.getTotal());
                break;
            default:
                super.mapField(values, field, subMinistryDetails);
                break;
        }
    }

    @NonNull
    @Override
    protected SubMinistryDetails newObject(@NonNull Cursor c)
    {
        return new SubMinistryDetails();
    }

    @NonNull
    @Override
    public SubMinistryDetails toObject(@NonNull Cursor cursor)
    {
        final SubMinistryDetails subMinistryDetails = super.toObject(cursor);

        subMinistryDetails.setName(this.getString(cursor, Contract.SubMinistryDetails.COLUMN_NAME));
        subMinistryDetails.setSubMinistryId(this.getString(cursor, Contract.SubMinistryDetails.COLUMN_SUB_MINISTRY_ID));
        subMinistryDetails.setTotal(this.getInt(cursor, Contract.SubMinistryDetails.COLUMN_TOTAL));

        return subMinistryDetails;
    }
}
