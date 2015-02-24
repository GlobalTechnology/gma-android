package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;

public class AssignmentMapper extends BaseMapper<Assignment> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Assignment assignment) {
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
        final String ministryId = getString(c, Contract.Assignment.COLUMN_MINISTRY_ID, Ministry.INVALID_ID);
        assignment.setMinistryId(ministryId != null ? ministryId : Ministry.INVALID_ID);
        return assignment;
    }
}
