package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.UserPreference.COLUMN_GUID;
import static com.expidevapps.android.measurements.db.Contract.UserPreference.COLUMN_NAME;
import static com.expidevapps.android.measurements.db.Contract.UserPreference.COLUMN_VALUE;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.UserPreference;

class UserPreferenceMapper extends BaseMapper<UserPreference> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final UserPreference pref) {
        switch (field) {
            case COLUMN_GUID:
                values.put(field, pref.getGuid());
                break;
            case COLUMN_NAME:
                values.put(field, pref.getName());
                break;
            case COLUMN_VALUE:
                values.put(field, pref.getValue());
                break;
            default:
                super.mapField(values, field, pref);
                break;
        }
    }

    @NonNull
    @Override
    protected UserPreference newObject(@NonNull final Cursor c) {
        return new UserPreference(getNonNullString(c, COLUMN_GUID, ""), getNonNullString(c, COLUMN_NAME, ""));
    }

    @NonNull
    @Override
    public UserPreference toObject(@NonNull Cursor c) {
        final UserPreference pref = super.toObject(c);

        pref.setValue(getString(c, COLUMN_VALUE, null));

        return pref;
    }
}
