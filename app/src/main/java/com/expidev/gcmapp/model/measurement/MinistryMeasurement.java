package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Base;
import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;
import org.json.JSONException;
import org.json.JSONObject;

public class MinistryMeasurement extends Base {
    static final String JSON_VALUE = "local";

    @NonNull
    private final String measurementId;
    @NonNull
    private final String ministryId;
    @NonNull
    private final Ministry.Mcc mcc;
    @NonNull
    private final YearMonth period;
    private int value = 0;

    public MinistryMeasurement(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                               @NonNull final String measurementId, @NonNull final YearMonth period) {
        this.ministryId = ministryId;
        this.mcc = mcc;
        this.measurementId = measurementId;
        this.period = period;
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

    @NonNull
    public String getMeasurementId() {
        return measurementId;
    }

    @NonNull
    public String getMinistryId() {
        return ministryId;
    }

    @NonNull
    public Ministry.Mcc getMcc() {
        return mcc;
    }

    @NonNull
    public YearMonth getPeriod() {
        return period;
    }

    public int getValue() {
        return value;
    }

    public void setValue(final int value) {
        this.value = value;
    }
}
