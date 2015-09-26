package com.expidevapps.android.measurements.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;

import static com.expidevapps.android.measurements.db.Contract.Assignment.COLUMN_PERSON_ID;
import static com.expidevapps.android.measurements.db.Contract.Assignment.COLUMN_SUPPORTED_STAFF;

public class AssignmentMapper extends BaseMapper<Assignment> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Assignment assignment) {
        switch (field) {
            case Contract.Assignment.COLUMN_GUID:
                values.put(field, assignment.getGuid());
                break;
            case Contract.Assignment.COLUMN_ID:
                values.put(field, assignment.getId());
                break;
            case Contract.Assignment.COLUMN_ROLE:
                values.put(field, assignment.getRole().raw);
                break;
            case Contract.Assignment.COLUMN_MINISTRY_ID:
                values.put(field, assignment.getMinistryId());
                break;
            case Contract.Assignment.COLUMN_MCC:
                values.put(field, assignment.getMcc().toString());
                break;
            case COLUMN_PERSON_ID:
                values.put(field, assignment.getPersonId());
                break;
            case COLUMN_SUPPORTED_STAFF:
                values.put(field, assignment.isSupportedStaff());
                break;
            default:
                super.mapField(values, field, assignment);
                break;
        }
    }

    @NonNull
    @Override
    protected Assignment newObject(@NonNull final Cursor c) {
        return new Assignment(getNonNullString(c, Contract.Assignment.COLUMN_GUID, ""),
                              getNonNullString(c, Contract.Assignment.COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
    }

    @NonNull
    @Override
    public Assignment toObject(@NonNull final Cursor c) {
        final Assignment assignment = super.toObject(c);
        assignment.setPersonId(getString(c, COLUMN_PERSON_ID, null));
        assignment.setSupportedStaff(getBool(c, COLUMN_SUPPORTED_STAFF, false));
        assignment.setId(this.getString(c, Contract.Assignment.COLUMN_ID, null));
        assignment.setRole(this.getString(c, Contract.Assignment.COLUMN_ROLE, null));
        assignment.setMcc(Ministry.Mcc.fromRaw(getString(c, Contract.Assignment.COLUMN_MCC, null)));
        return assignment;
    }
}
