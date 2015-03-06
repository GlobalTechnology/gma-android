package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;
import org.json.JSONException;
import org.json.JSONObject;

public class PersonalMeasurement extends MeasurementValue {
    static final String JSON_VALUE = "personal";

    @NonNull
    private final String guid;

    public PersonalMeasurement(@NonNull final String guid, @NonNull final String ministryId,
                                  @NonNull final Ministry.Mcc mcc, @NonNull final String measurementId,
                                  @NonNull final YearMonth period) {
        super(ministryId, mcc, measurementId, period);
        this.guid = guid;
    }

    @NonNull
    public static PersonalMeasurement fromJson(@NonNull final JSONObject json, @NonNull final String guid,
                                               @NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                                               @NonNull final YearMonth period) throws JSONException {
        final PersonalMeasurement measurement =
                new PersonalMeasurement(guid, ministryId, mcc, json.getString(MeasurementType.JSON_MEASUREMENT_ID),
                                        period);
        measurement.setValue(json.getInt(JSON_VALUE));
        return measurement;
    }

    @NonNull
    public String getGuid() {
        return guid;
    }
}
