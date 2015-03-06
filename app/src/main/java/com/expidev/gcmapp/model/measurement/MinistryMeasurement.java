package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;
import org.json.JSONException;
import org.json.JSONObject;

public class MinistryMeasurement extends MeasurementValue {
    static final String JSON_VALUE = "local";

    public MinistryMeasurement(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                               @NonNull final String measurementId, @NonNull final YearMonth period) {
        super(ministryId, mcc, measurementId, period);
    }

    @NonNull
    public static MinistryMeasurement fromJson(@NonNull final JSONObject json, @NonNull final String ministryId,
                                               @NonNull final Ministry.Mcc mcc, @NonNull final YearMonth period)
            throws JSONException {
        final MinistryMeasurement measurement =
                new MinistryMeasurement(ministryId, mcc, json.getString(MeasurementType.JSON_MEASUREMENT_ID), period);
        measurement.setValue(json.getInt(JSON_VALUE));
        return measurement;
    }
}
