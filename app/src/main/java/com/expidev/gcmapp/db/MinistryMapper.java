package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;

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
            case Contract.Ministry.COLUMN_HAS_SLM:
                values.put(field, ministry.hasSlm() ? 1 : 0);
                break;
            case Contract.Ministry.COLUMN_HAS_LLM:
                values.put(field, ministry.hasLlm() ? 1 : 0);
                break;
            case Contract.Ministry.COLUMN_HAS_DS:
                values.put(field, ministry.hasDs() ? 1 : 0);
                break;
            case Contract.Ministry.COLUMN_HAS_GCM:
                values.put(field, ministry.hasGcm() ? 1 : 0);
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
        ministry.setHasGcm(getBool(c, Contract.Ministry.COLUMN_HAS_GCM, false));
        ministry.setHasSlm(getBool(c, Contract.Ministry.COLUMN_HAS_SLM, false));
        ministry.setHasDs(getBool(c, Contract.Ministry.COLUMN_HAS_DS, false));
        ministry.setHasLlm(getBool(c, Contract.Ministry.COLUMN_HAS_LLM, false));
        ministry.setLatitude(getDouble(c, Contract.Ministry.COLUMN_LATITUDE, 0));
        ministry.setLongitude(getDouble(c, Contract.Ministry.COLUMN_LONGITUDE, 0));
        ministry.setLocationZoom(getInt(c, Contract.Ministry.COLUMN_LOCATION_ZOOM, 0));

        return ministry;
    }
}
