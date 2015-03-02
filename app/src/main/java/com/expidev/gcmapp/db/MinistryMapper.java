package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.expidev.gcmapp.model.Ministry;

import java.util.EnumSet;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class MinistryMapper extends BaseMapper<Ministry> {
    @Override
    protected void mapField(
        @NonNull final ContentValues values,
        @NonNull final String field,
        @NonNull final Ministry ministry) {
        switch (field)
        {
            case Contract.Ministry.COLUMN_MINISTRY_ID:
                values.put(field, ministry.getMinistryId());
                break;
            case Contract.Ministry.COLUMN_NAME:
                values.put(field, ministry.getName());
                break;
            case Contract.Ministry.COLUMN_MIN_CODE:
                values.put(field, ministry.getMinistryCode());
                break;
            case Contract.Ministry.COLUMN_MCCS:
                values.put(field, TextUtils.join(",", ministry.getMccs()));
                break;
            case Contract.Ministry.COLUMN_LATITUDE:
                values.put(field, ministry.getLatitude());
                break;
            case Contract.Ministry.COLUMN_LONGITUDE:
                values.put(field, ministry.getLongitude());
                break;
            case Contract.Ministry.COLUMN_LOCATION_ZOOM:
                values.put(field, ministry.getLocationZoom());
                break;
            case Contract.Ministry.COLUMN_PARENT_MINISTRY_ID:
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

        ministry.setMinistryId(getNonNullString(c, Contract.Ministry.COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
        ministry.setParentMinistryId(getString(c, Contract.Ministry.COLUMN_PARENT_MINISTRY_ID, null));
        ministry.setName(getString(c, Contract.Ministry.COLUMN_NAME, null));
        ministry.setMinistryCode(getString(c, Contract.Ministry.COLUMN_MIN_CODE, null));
        ministry.setLatitude(getDouble(c, Contract.Ministry.COLUMN_LATITUDE, 0));
        ministry.setLongitude(getDouble(c, Contract.Ministry.COLUMN_LONGITUDE, 0));
        ministry.setLocationZoom(getInt(c, Contract.Ministry.COLUMN_LOCATION_ZOOM, 0));

        // parse COLUMN_MCCS
        final EnumSet<Ministry.Mcc> mccs = EnumSet.noneOf(Ministry.Mcc.class);
        for (final String raw : TextUtils.split(getNonNullString(c, Contract.Ministry.COLUMN_MCCS, ""), ",")) {
            mccs.add(Ministry.Mcc.fromRaw(raw));
        }
        ministry.setMccs(mccs);

        return ministry;
    }
}
