package com.expidevapps.android.measurements.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Church.Development;
import com.expidevapps.android.measurements.model.Church.Security;
import com.expidevapps.android.measurements.model.Ministry;

import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_CONTACT_EMAIL;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_CONTACT_MOBILE;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_CONTACT_NAME;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_DEVELOPMENT;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_END_DATE;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_ID;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_NAME;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_NEW;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_PARENT;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_SECURITY;
import static com.expidevapps.android.measurements.db.Contract.Church.COLUMN_SIZE;
import static com.expidevapps.android.measurements.model.Church.SECURITY_DEFAULT;

public class ChurchMapper extends LocationMapper<Church> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Church church) {
        switch (field) {
            case COLUMN_ID:
                values.put(field, church.getId());
                break;
            case COLUMN_PARENT:
                //values.put(field, church.hasParent() ? church.getParentId() : null);
                //ParentId should be Church.INVALID_ID if NOT defined already
                values.put(field, church.hasParent() ? church.getParentId() : Church.INVALID_ID);
                break;
            case COLUMN_MINISTRY_ID:
                values.put(field, church.getMinistryId());
                break;
            case COLUMN_NAME:
                values.put(field, church.getName());
                break;
            case COLUMN_CONTACT_NAME:
                values.put(field, church.getContactName());
                break;
            case COLUMN_CONTACT_EMAIL:
                values.put(field, church.getContactEmail());
                break;
            case COLUMN_CONTACT_MOBILE:
                values.put(field, church.getContactMobile());
                break;
            case COLUMN_DEVELOPMENT:
                values.put(field, church.getDevelopment().id);
                break;
            case COLUMN_SIZE:
                values.put(field, church.getSize());
                break;
            case COLUMN_SECURITY:
                values.put(field, church.getSecurity().id);
                break;
            case COLUMN_END_DATE:
                values.put(field, church.getEndDate() != null ? church.getEndDate().toString() : null);
                break;
            case COLUMN_NEW:
                values.put(field, church.isNew());
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

        church.setMinistryId(getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
        church.setId(getLong(c, COLUMN_ID, Church.INVALID_ID));
        church.setParentId(getLong(c, COLUMN_PARENT, Church.INVALID_ID));
        church.setName(getString(c, COLUMN_NAME, null));
        church.setContactName(getString(c, COLUMN_CONTACT_NAME, null));
        church.setContactEmail(getString(c, COLUMN_CONTACT_EMAIL, null));
        church.setContactMobile(getString(c, COLUMN_CONTACT_MOBILE, null));
        church.setDevelopment(Development.fromRaw(getInt(c, COLUMN_DEVELOPMENT, Development.UNKNOWN.id)));
        church.setSize(getInt(c, COLUMN_SIZE, 0));
        church.setSecurity(Security.fromRaw(getInt(c, COLUMN_SECURITY, SECURITY_DEFAULT)));
        church.setEndDate(getLocalDate(c, COLUMN_END_DATE, null));
        church.setNew(getBool(c, COLUMN_NEW, false));

        return church;
    }
}
