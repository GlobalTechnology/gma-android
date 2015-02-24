package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.Measurement;

/**
 * Created by William.Randall on 2/11/2015.
 */
public class MeasurementMapper extends BaseMapper<Measurement>
{
    @Override
    protected void mapField(
        @NonNull final ContentValues values,
        @NonNull final String field,
        @NonNull final Measurement measurement)
    {
        switch(field)
        {
            case Contract.Measurement.COLUMN_MEASUREMENT_ID:
                values.put(field, measurement.getMeasurementId());
                break;
            case Contract.Measurement.COLUMN_NAME:
                values.put(field, measurement.getName());
                break;
            case Contract.Measurement.COLUMN_PERM_LINK:
                values.put(field, measurement.getPermLink());
                break;
            case Contract.Measurement.COLUMN_CUSTOM:
                values.put(field, measurement.isCustom() ? 1 : 0);
                break;
            case Contract.Measurement.COLUMN_SECTION:
                values.put(field, measurement.getSection());
                break;
            case Contract.Measurement.COLUMN_COLUMN:
                values.put(field, measurement.getColumn());
                break;
            case Contract.Measurement.COLUMN_TOTAL:
                values.put(field, measurement.getTotal());
                break;
            case Contract.Measurement.COLUMN_MINISTRY_ID:
                values.put(field, measurement.getMinistryId());
                break;
            case Contract.Measurement.COLUMN_MCC:
                values.put(field, measurement.getMcc());
                break;
            case Contract.Measurement.COLUMN_PERIOD:
                values.put(field, measurement.getPeriod());
                break;
            default:
                super.mapField(values, field, measurement);
                break;
        }
    }

    @NonNull
    @Override
    protected Measurement newObject(@NonNull final Cursor cursor)
    {
        return new Measurement();
    }

    @NonNull
    @Override
    public Measurement toObject(@NonNull final Cursor cursor)
    {
        final Measurement measurement = super.toObject(cursor);

        measurement.setMeasurementId(this.getString(cursor, Contract.Measurement.COLUMN_MEASUREMENT_ID, "NO ID"));
        measurement.setName(this.getString(cursor, Contract.Measurement.COLUMN_NAME, null));
        measurement.setPermLink(this.getString(cursor, Contract.Measurement.COLUMN_PERM_LINK, null));
        measurement.setCustom(this.getBool(cursor, Contract.Measurement.COLUMN_CUSTOM, false));
        measurement.setSection(this.getString(cursor, Contract.Measurement.COLUMN_SECTION, null));
        measurement.setColumn(this.getString(cursor, Contract.Measurement.COLUMN_COLUMN, null));
        measurement.setTotal(this.getInt(cursor, Contract.Measurement.COLUMN_TOTAL, 0));
        measurement.setMinistryId(this.getString(cursor, Contract.Measurement.COLUMN_MINISTRY_ID, null));
        measurement.setMcc(this.getString(cursor, Contract.Measurement.COLUMN_MCC, null));
        measurement.setPeriod(this.getString(cursor, Contract.Measurement.COLUMN_PERIOD, null));

        return measurement;
    }
}
