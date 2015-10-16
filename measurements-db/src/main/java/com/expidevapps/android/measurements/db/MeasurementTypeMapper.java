package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.MeasurementPermLink.COLUMN_PERM_LINK_STUB;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_COLUMN;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_CUSTOM;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_DESCRIPTION;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_LEADER_ONLY;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_LOCAL_ID;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_NAME;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_PERSONAL_ID;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_SECTION;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_SORT_ORDER;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_SUPPORTED_STAFF_ONLY;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_TOTAL_ID;
import static com.expidevapps.android.measurements.model.MeasurementType.DEFAULT_CUSTOM;
import static com.expidevapps.android.measurements.model.MeasurementType.DEFAULT_LEADER_ONLY;
import static com.expidevapps.android.measurements.model.MeasurementType.DEFAULT_SORT_ORDER;
import static com.expidevapps.android.measurements.model.MeasurementType.DEFAULT_SUPPORTED_STAFF_ONLY;
import static com.expidevapps.android.measurements.model.MeasurementType.INVALID_PERM_LINK_STUB;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.MeasurementType;

class MeasurementTypeMapper extends BaseMapper<MeasurementType> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final MeasurementType type) {
        switch (field) {
            case COLUMN_PERM_LINK_STUB:
                values.put(field, type.getPermLinkStub());
                break;
            case COLUMN_PERSONAL_ID:
                values.put(field, type.getPersonalId());
                break;
            case COLUMN_LOCAL_ID:
                values.put(field, type.getLocalId());
                break;
            case COLUMN_TOTAL_ID:
                values.put(field, type.getTotalId());
                break;
            case COLUMN_NAME:
                values.put(field, type.getName());
                break;
            case COLUMN_DESCRIPTION:
                values.put(field, type.getDescription());
                break;
            case COLUMN_SECTION:
                values.put(field, type.getSection().name());
                break;
            case COLUMN_COLUMN:
                values.put(field, type.getColumn().name());
                break;
            case COLUMN_CUSTOM:
                values.put(field, type.isCustom());
                break;
            case COLUMN_SORT_ORDER:
                values.put(field, type.getSortOrder());
                break;
            case COLUMN_LEADER_ONLY:
                values.put(field, type.isLeaderOnly());
                break;
            case COLUMN_SUPPORTED_STAFF_ONLY:
                values.put(field, type.isSupportedStaffOnly());
                break;
            default:
                super.mapField(values, field, type);
                break;
        }
    }

    @NonNull
    @Override
    protected MeasurementType newObject(@NonNull final Cursor c) {
        return new MeasurementType(getNonNullString(c, COLUMN_PERM_LINK_STUB, INVALID_PERM_LINK_STUB));
    }

    @NonNull
    @Override
    public MeasurementType toObject(@NonNull final Cursor c) {
        final MeasurementType type = super.toObject(c);

        type.setPersonalId(getNonNullString(c, COLUMN_PERSONAL_ID, MeasurementType.INVALID_ID));
        type.setLocalId(getNonNullString(c, COLUMN_LOCAL_ID, MeasurementType.INVALID_ID));
        type.setTotalId(getNonNullString(c, COLUMN_TOTAL_ID, MeasurementType.INVALID_ID));
        type.setName(getString(c, COLUMN_NAME, null));
        type.setDescription(getString(c, COLUMN_DESCRIPTION, null));
        type.setSection(MeasurementType.Section.valueOf(getString(c, COLUMN_SECTION, null)));
        type.setColumn(MeasurementType.Column.fromRaw(getString(c, COLUMN_COLUMN, null)));
        type.setCustom(getBool(c, COLUMN_CUSTOM, DEFAULT_CUSTOM));
        type.setSortOrder(getInt(c, COLUMN_SORT_ORDER, DEFAULT_SORT_ORDER));
        type.setLeaderOnly(getBool(c, COLUMN_LEADER_ONLY, DEFAULT_LEADER_ONLY));
        type.setSupportedStaffOnly(getBool(c, COLUMN_SUPPORTED_STAFF_ONLY, DEFAULT_SUPPORTED_STAFF_ONLY));

        return type;
    }
}
