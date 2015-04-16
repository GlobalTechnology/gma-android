package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.model.Ministry.Mcc;

import org.joda.time.YearMonth;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasurementDetails extends Base {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementDetails.class);

    @NonNull
    private final String guid;
    @NonNull
    private final String ministryId;
    @NonNull
    private final Mcc mcc;
    @NonNull
    private final String permLink;
    @NonNull
    private final YearMonth period;

    @Nullable
    private String rawJson;
    @Nullable
    private JSONObject json;
    private int version;

    public MeasurementDetails(@NonNull final String guid, @NonNull final String ministryId, @NonNull final Mcc mcc,
                              @NonNull final String permLink, @NonNull final YearMonth period) {
        this.guid = guid;
        this.ministryId = ministryId;
        this.mcc = mcc;
        this.permLink = permLink;
        this.period = period;
    }

    @NonNull
    public String getGuid() {
        return guid;
    }

    @NonNull
    public String getMinistryId() {
        return ministryId;
    }

    @NonNull
    public Mcc getMcc() {
        return mcc;
    }

    @NonNull
    public String getPermLink() {
        return permLink;
    }

    public YearMonth getPeriod() {
        return period;
    }

    @Nullable
    public JSONObject getJson() {
        // try parsing raw JSON if we don't have parsed JSON, but we have raw JSON
        if (json == null && rawJson != null) {
            try {
                json = new JSONObject(rawJson);
            } catch (final JSONException e) {
                LOG.error("Error parsing MeasurementDetails JSON", e);
                resetTransients();
            }
        }

        return json;
    }

    @Nullable
    public String getRawJson() {
        if (rawJson == null && json != null) {
            rawJson = json.toString();
        }

        return rawJson;
    }

    public void setJson(@Nullable final String json, final int version) {
        resetTransients();
        this.rawJson = json;
        this.version = version;
    }

    public void setJson(@Nullable final JSONObject json, final int version) {
        resetTransients();
        this.json = json;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    private void resetTransients() {
        this.json = null;
        this.rawJson = null;
        this.version = 0;
    }
}
