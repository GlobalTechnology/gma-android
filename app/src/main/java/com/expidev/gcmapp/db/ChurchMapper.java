package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Church;

public class ChurchMapper extends LocationMapper<Church> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Church church) {
        switch (field) {
            case Contract.Church.COLUMN_ID:
                values.put(field, church.getId());
                break;
            case Contract.Church.COLUMN_MINISTRY_ID:
                values.put(field, church.getMinistryId());
                break;
            case Contract.Church.COLUMN_NAME:
                values.put(field, church.getName());
                break;
            case Contract.Church.COLUMN_CONTACT_NAME:
                values.put(field, church.getContactName());
                break;
            case Contract.Church.COLUMN_CONTACT_EMAIL:
                values.put(field, church.getContactEmail());
                break;
            case Contract.Church.COLUMN_DEVELOPMENT:
                values.put(field, church.getDevelopment().id);
                break;
            case Contract.Church.COLUMN_SIZE:
                values.put(field, church.getSize());
                break;
            case Contract.Church.COLUMN_SECURITY:
                values.put(field, church.getSecurity());
                break;
            case Contract.Church.COLUMN_DIRTY:
                values.put(field, church.getDirty());
                break;
            default:
                super.mapField(values, field, church);
                break;
        }
    }

    @NonNull
    @Override
    protected Church newObject(@NonNull final Cursor cursor) {
        return new Church();
    }

    @NonNull
    @Override
    public Church toObject(@NonNull final Cursor c) {
        final Church church = super.toObject(c);
        final String ministryId = getString(c, Contract.Church.COLUMN_MINISTRY_ID);
        church.setMinistryId(ministryId != null ? ministryId : "");
        church.setId(getLong(c, Contract.Church.COLUMN_ID, Church.INVALID_ID));
        church.setName(getString(c, Contract.Church.COLUMN_NAME, null));
        church.setContactName(getString(c, Contract.Church.COLUMN_CONTACT_NAME, null));
        church.setContactEmail(getString(c, Contract.Church.COLUMN_CONTACT_EMAIL, null));
        church.setDevelopment(getInt(c, Contract.Church.COLUMN_DEVELOPMENT, Church.Development.UNKNOWN.id));
        church.setSize(getInt(c, Contract.Church.COLUMN_SIZE, 0));
        church.setSecurity(getInt(c, Contract.Church.COLUMN_SECURITY, 2));
        church.setDirty(getString(c, Contract.Church.COLUMN_DIRTY, null));
        return church;
    }
}
