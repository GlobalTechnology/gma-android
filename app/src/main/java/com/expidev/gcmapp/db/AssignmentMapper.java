package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Assignment;

public class AssignmentMapper extends BaseMapper<Assignment> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Assignment assignment) {
        super.mapField(values, field, assignment);

        switch (field) {
            case Contract.Assignment.COLUMN_ID:
                values.put(field, assignment.getId());
                break;
            case Contract.Assignment.COLUMN_ROLE:
                values.put(field, assignment.getRole().raw);
                break;
            case Contract.Assignment.COLUMN_MINISTRY_ID:
                values.put(field, assignment.getMinistryId());
                break;
            case Contract.Assignment.COLUMN_LATITUDE:
                values.put(field, assignment.getLatitude());
                break;
            case Contract.Assignment.COLUMN_LONGITUDE:
                values.put(field, assignment.getLongitude());
                break;
            case Contract.Assignment.COLUMN_LOCATION_ZOOM:
                values.put(field, assignment.getLocationZoom());
                break;
            default:
                super.mapField(values, field, assignment);
                break;
        }
    }

    @NonNull
    @Override
    protected Assignment newObject(@NonNull final Cursor cursor) {
        return new Assignment();
    }

    @NonNull
    @Override
    public Assignment toObject(@NonNull final Cursor c) {
        final Assignment assignment = super.toObject(c);
        assignment.setId(this.getString(c, Contract.Assignment.COLUMN_ID, null));
        assignment.setRole(this.getString(c, Contract.Assignment.COLUMN_ROLE, null));
        assignment.setMinistryId(this.getString(c, Contract.Assignment.COLUMN_MINISTRY_ID, null));
        assignment.setLatitude(this.getDouble(c, Contract.Assignment.COLUMN_LATITUDE, 0));
        assignment.setLongitude(this.getDouble(c, Contract.Assignment.COLUMN_LONGITUDE, 0));
        assignment.setLocationZoom(this.getInt(c, Contract.Assignment.COLUMN_LOCATION_ZOOM));
        return assignment;
    }
}
