package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;
import org.json.JSONException;
import org.json.JSONObject;

public class MinistryMeasurement extends MeasurementValue {
    private static final String JSON_MINISTRY_ID = "ministry_id";
    static final String JSON_VALUE = "local";

    public MinistryMeasurement(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                               @NonNull final String permLink, @NonNull final YearMonth period) {
        super(ministryId, mcc, permLink, period);
    }

    @NonNull
    public static MinistryMeasurement fromJson(@NonNull final JSONObject json, @NonNull final String ministryId,
                                               @NonNull final Ministry.Mcc mcc, @NonNull final YearMonth period)
            throws JSONException {
        final MinistryMeasurement measurement =
                new MinistryMeasurement(ministryId, mcc, json.getString(MeasurementType.JSON_PERM_LINK_STUB), period);
        measurement.setValue(json.getInt(JSON_VALUE));
        return measurement;
    }

    @NonNull
    @Override
    public JSONObject toJson() throws JSONException {
        final MeasurementType type = getType();
        if (type == null) {
            throw new JSONException("MeasurementType is unavailable");
        }

        final JSONObject json = super.toJson();
        json.put(MeasurementType.JSON_TYPE_ID, type.getLocalId());
        json.put(JSON_MINISTRY_ID, getMinistryId());
        return json;
    }
}
