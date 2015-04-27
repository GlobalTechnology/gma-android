package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Base;
import com.expidevapps.android.measurements.model.Ministry;

import org.joda.time.YearMonth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Measurement extends Base {
    @Nullable
    private MeasurementType type;
    @Nullable
    private MinistryMeasurement ministryMeasurement;
    @Nullable
    private PersonalMeasurement personalMeasurement;

    @NonNull
    public static List<Measurement> listFromJson(@NonNull final JSONArray json, @NonNull final String guid,
                                                 @NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                                                 @NonNull final YearMonth period) throws JSONException {
        final List<Measurement> measurements = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            measurements.add(Measurement.fromJson(json.getJSONObject(i), guid, ministryId, mcc, period));
        }
        return measurements;
    }

    @NonNull
    public static Measurement fromJson(@NonNull final JSONObject json, @NonNull final String guid,
                                       @NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                                       @NonNull final YearMonth period) throws JSONException {
        final Measurement measurement = new Measurement();

        measurement.type = MeasurementType.fromJson(json);
        if (json.has(MinistryMeasurement.JSON_VALUE)) {
            measurement.ministryMeasurement = MinistryMeasurement.fromJson(json, ministryId, mcc, period);
        }
        if (json.has(PersonalMeasurement.JSON_VALUE)) {
            measurement.personalMeasurement = PersonalMeasurement.fromJson(json, guid, ministryId, mcc, period);
        }

        return measurement;
    }

    @Nullable
    public MeasurementType getType() {
        return type;
    }

    @Nullable
    public MinistryMeasurement getMinistryMeasurement() {
        return ministryMeasurement;
    }

    @Nullable
    public PersonalMeasurement getPersonalMeasurement() {
        return personalMeasurement;
    }
}
