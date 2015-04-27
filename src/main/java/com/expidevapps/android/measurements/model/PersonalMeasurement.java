package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;

import org.joda.time.YearMonth;
import org.json.JSONException;
import org.json.JSONObject;

public class PersonalMeasurement extends MeasurementValue {
    private static final String JSON_ASSIGNMENT_ID = "assignment_id";
    static final String JSON_VALUE = "person";

    @NonNull
    private final String guid;

    @Nullable
    private Assignment assignment;

    public PersonalMeasurement(@NonNull final String guid, @NonNull final String ministryId,
                               @NonNull final Ministry.Mcc mcc, @NonNull final String permLink,
                               @NonNull final YearMonth period) {
        super(ministryId, mcc, permLink, period);
        this.guid = guid;
    }

    @NonNull
    public static PersonalMeasurement fromJson(@NonNull final JSONObject json, @NonNull final String guid,
                                               @NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                                               @NonNull final YearMonth period) throws JSONException {
        final PersonalMeasurement measurement =
                new PersonalMeasurement(guid, ministryId, mcc, json.getString(MeasurementType.JSON_PERM_LINK_STUB), period);
        measurement.setValue(json.getInt(JSON_VALUE));
        return measurement;
    }

    @NonNull
    public String getGuid() {
        return guid;
    }

    @Nullable
    public Assignment getAssignment() {
        return this.assignment;
    }

    public void setAssignment(@Nullable final Assignment assignment) {
        this.assignment = assignment;
    }

    @NonNull
    @Override
    public JSONObject toUpdateJson(@NonNull final String source) throws JSONException {
        if (assignment == null) {
            throw new JSONException("Assignment is unavailable");
        }
        final MeasurementType type = getType();
        if (type == null) {
            throw new JSONException("MeasurementType is unavailable");
        }

        final JSONObject json = super.toUpdateJson(source);
        json.put(JSON_ASSIGNMENT_ID, assignment.getId());
        json.put(MeasurementType.JSON_TYPE_ID, type.getPersonalId());
        return json;
    }
}
