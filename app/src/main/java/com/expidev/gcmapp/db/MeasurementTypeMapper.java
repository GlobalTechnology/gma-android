package com.expidev.gcmapp.db;

import static com.expidev.gcmapp.model.measurement.MeasurementType.DEFAULT_SORT_ORDER;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.MeasurementType;

public class MeasurementTypeMapper extends BaseMapper<MeasurementType> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final MeasurementType type) {
        switch (field) {
            case Contract.MeasurementType.COLUMN_MEASUREMENT_TYPE_ID:
                values.put(field, type.getMeasurementId());
                break;
            case Contract.MeasurementType.COLUMN_NAME:
                values.put(field, type.getName());
                break;
            case Contract.MeasurementType.COLUMN_PERM_LINK:
                values.put(field, type.getPermLink());
                break;
            case Contract.MeasurementType.COLUMN_DESCRIPTION:
                values.put(field, type.getDescription());
                break;
            case Contract.MeasurementType.COLUMN_SECTION:
                values.put(field, type.getSection().name());
                break;
            case Contract.MeasurementType.COLUMN_COLUMN:
                values.put(field, type.getColumn().name());
                break;
            case Contract.MeasurementType.COLUMN_SORT_ORDER:
                values.put(field, type.getSortOrder());
                break;
            default:
                super.mapField(values, field, type);
                break;
        }
    }

    @NonNull
    @Override
    protected MeasurementType newObject(@NonNull final Cursor c) {
        return new MeasurementType();
    }

    @NonNull
    @Override
    public MeasurementType toObject(@NonNull final Cursor c) {
        final MeasurementType type = super.toObject(c);
        type.setMeasurementId(
                getNonNullString(c, Contract.MeasurementType.COLUMN_MEASUREMENT_TYPE_ID, MeasurementType.INVALID_ID));
        type.setName(getString(c, Contract.MeasurementType.COLUMN_NAME, null));
        type.setPermLink(getString(c, Contract.MeasurementType.COLUMN_PERM_LINK, null));
        type.setDescription(getString(c, Contract.MeasurementType.COLUMN_DESCRIPTION, null));
        type.setSection(MeasurementType.Section.valueOf(getString(c, Contract.MeasurementType.COLUMN_SECTION, null)));
        type.setColumn(MeasurementType.Column.fromRaw(getString(c, Contract.MeasurementType.COLUMN_COLUMN, null)));
        type.setSortOrder(getInt(c, Contract.MeasurementType.COLUMN_SORT_ORDER, DEFAULT_SORT_ORDER));
        return type;
    }
}
