package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.Measurement;

import org.joda.time.YearMonth;

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
                values.put(field, measurement.getMcc().toString());
                break;
            case Contract.Measurement.COLUMN_PERIOD:
                values.put(field, measurement.getPeriod().toString());
                break;
            case Contract.Measurement.COLUMN_SORT_ORDER:
                values.put(field, measurement.getSortOrder());
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
        measurement.setPermLink(this.getString(cursor, Contract.Measurement.COLUMN_PERM_LINK, null));
        measurement.setCustom(this.getBool(cursor, Contract.Measurement.COLUMN_CUSTOM, false));
        measurement.setSection(this.getString(cursor, Contract.Measurement.COLUMN_SECTION, null));
        measurement.setColumn(this.getString(cursor, Contract.Measurement.COLUMN_COLUMN, null));
        measurement.setTotal(this.getInt(cursor, Contract.Measurement.COLUMN_TOTAL, 0));
        measurement
                .setMinistryId(getNonNullString(cursor, Contract.Measurement.COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
        measurement.setMcc(this.getString(cursor, Contract.Measurement.COLUMN_MCC, null));
        measurement.setPeriod(getNonNullYearMonth(cursor, Contract.Measurement.COLUMN_PERIOD, YearMonth.now()));
        measurement.setSortOrder(this.getInt(cursor, Contract.Measurement.COLUMN_SORT_ORDER, 0));

        return measurement;
    }
}
