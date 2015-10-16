package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Ministry.COLUMN_LOCATION_ZOOM;
import static com.expidevapps.android.measurements.db.Contract.Ministry.COLUMN_MCCS;
import static com.expidevapps.android.measurements.db.Contract.Ministry.COLUMN_MIN_CODE;
import static com.expidevapps.android.measurements.db.Contract.Ministry.COLUMN_NAME;
import static com.expidevapps.android.measurements.db.Contract.Ministry.COLUMN_PARENT_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;

import java.util.EnumSet;

class MinistryMapper extends LocationMapper<Ministry> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Ministry ministry) {
        switch (field) {
            case COLUMN_MINISTRY_ID:
                values.put(field, ministry.getMinistryId());
                break;
            case COLUMN_NAME:
                values.put(field, ministry.getName());
                break;
            case COLUMN_MIN_CODE:
                values.put(field, ministry.getMinistryCode());
                break;
            case COLUMN_MCCS:
                values.put(field, TextUtils.join(",", ministry.getMccs()));
                break;
            case COLUMN_LOCATION_ZOOM:
                values.put(field, ministry.getLocationZoom());
                break;
            case COLUMN_PARENT_MINISTRY_ID:
                values.put(field, ministry.getParentMinistryId());
                break;
            default:
                super.mapField(values, field, ministry);
                break;
        }
    }

    @NonNull
    @Override
    protected Ministry newObject(@NonNull final Cursor cursor) {
        return new Ministry();
    }

    @NonNull
    @Override
    public Ministry toObject(@NonNull final Cursor c) {
        final Ministry ministry = super.toObject(c);

        ministry.setMinistryId(getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
        ministry.setParentMinistryId(getString(c, COLUMN_PARENT_MINISTRY_ID, null));
        ministry.setName(getString(c, COLUMN_NAME, null));
        ministry.setMinistryCode(getString(c, COLUMN_MIN_CODE, null));
        ministry.setLocationZoom(getInt(c, COLUMN_LOCATION_ZOOM, 0));

        // parse COLUMN_MCCS
        final EnumSet<Mcc> mccs = EnumSet.noneOf(Mcc.class);
        for (final String raw : TextUtils.split(getNonNullString(c, COLUMN_MCCS, ""), ",")) {
            mccs.add(Mcc.fromRaw(raw));
        }
        ministry.setMccs(mccs);

        return ministry;
    }
}
