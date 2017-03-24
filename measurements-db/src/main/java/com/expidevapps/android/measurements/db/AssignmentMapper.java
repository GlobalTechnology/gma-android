package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Assignment.COLUMN_ID;
import static com.expidevapps.android.measurements.db.Contract.Assignment.COLUMN_PERSON_ID;
import static com.expidevapps.android.measurements.db.Contract.Assignment.COLUMN_ROLE;
import static com.expidevapps.android.measurements.db.Contract.Guid.COLUMN_GUID;
import static com.expidevapps.android.measurements.db.Contract.Mcc.COLUMN_MCC;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;

class AssignmentMapper extends BaseMapper<Assignment> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Assignment assignment) {
        switch (field) {
            case COLUMN_GUID:
                values.put(field, assignment.getGuid());
                break;
            case COLUMN_ID:
                values.put(field, assignment.getId());
                break;
            case COLUMN_ROLE:
                values.put(field, assignment.getRole().raw);
                break;
            case COLUMN_MINISTRY_ID:
                values.put(field, assignment.getMinistryId());
                break;
            case COLUMN_MCC:
                values.put(field, assignment.getMcc().toString());
                break;
            case COLUMN_PERSON_ID:
                values.put(field, assignment.getPersonId());
                break;
            default:
                super.mapField(values, field, assignment);
                break;
        }
    }

    @NonNull
    @Override
    protected Assignment newObject(@NonNull final Cursor c) {
        return new Assignment(getNonNullString(c, COLUMN_GUID, ""),
                              getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
    }

    @NonNull
    @Override
    public Assignment toObject(@NonNull final Cursor c) {
        final Assignment assignment = super.toObject(c);

        assignment.setPersonId(getString(c, COLUMN_PERSON_ID, null));
        assignment.setId(getString(c, COLUMN_ID, null));
        assignment.setRole(getString(c, COLUMN_ROLE, null));
        assignment.setMcc(Ministry.Mcc.fromRaw(getString(c, COLUMN_MCC, null)));

        return assignment;
    }
}
