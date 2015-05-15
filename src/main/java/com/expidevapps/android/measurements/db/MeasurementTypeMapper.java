package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_CUSTOM;
import static com.expidevapps.android.measurements.model.MeasurementType.DEFAULT_CUSTOM;
import static com.expidevapps.android.measurements.model.MeasurementType.DEFAULT_SORT_ORDER;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.MeasurementType;

public class MeasurementTypeMapper extends BaseMapper<MeasurementType> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final MeasurementType type) {
        switch (field) {
            case Contract.MeasurementType.COLUMN_PERM_LINK_STUB:
                values.put(field, type.getPermLinkStub());
                break;
            case Contract.MeasurementType.COLUMN_PERSONAL_ID:
                values.put(field, type.getPersonalId());
                break;
            case Contract.MeasurementType.COLUMN_LOCAL_ID:
                values.put(field, type.getLocalId());
                break;
            case Contract.MeasurementType.COLUMN_TOTAL_ID:
                values.put(field, type.getTotalId());
                break;
            case Contract.MeasurementType.COLUMN_NAME:
                values.put(field, type.getName());
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
            case COLUMN_CUSTOM:
                values.put(field, type.isCustom());
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
        return new MeasurementType(getNonNullString(c, Contract.MeasurementType.COLUMN_PERM_LINK_STUB,
                                                    MeasurementType.INVALID_PERM_LINK_STUB));
    }

    @NonNull
    @Override
    public MeasurementType toObject(@NonNull final Cursor c) {
        final MeasurementType type = super.toObject(c);
        type.setPersonalId(
                getNonNullString(c, Contract.MeasurementType.COLUMN_PERSONAL_ID, MeasurementType.INVALID_ID));
        type.setLocalId(getNonNullString(c, Contract.MeasurementType.COLUMN_LOCAL_ID, MeasurementType.INVALID_ID));
        type.setTotalId(getNonNullString(c, Contract.MeasurementType.COLUMN_TOTAL_ID, MeasurementType.INVALID_ID));
        type.setName(getString(c, Contract.MeasurementType.COLUMN_NAME, null));
        type.setDescription(getString(c, Contract.MeasurementType.COLUMN_DESCRIPTION, null));
        type.setSection(MeasurementType.Section.valueOf(getString(c, Contract.MeasurementType.COLUMN_SECTION, null)));
        type.setColumn(MeasurementType.Column.fromRaw(getString(c, Contract.MeasurementType.COLUMN_COLUMN, null)));
        type.setCustom(getBool(c, COLUMN_CUSTOM, DEFAULT_CUSTOM));
        type.setSortOrder(getInt(c, Contract.MeasurementType.COLUMN_SORT_ORDER, DEFAULT_SORT_ORDER));

        return type;
    }
}
