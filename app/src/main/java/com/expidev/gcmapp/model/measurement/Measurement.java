package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.model.Base;
import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Measurement extends Base implements Serializable
{
    private static final long serialVersionUID = 0L;

    private static final String JSON_NAME = "name";
    private static final String JSON_MEASUREMENT_ID = "measurement_id";
    private static final String JSON_PERM_LINK = "perm_link";
    private static final String JSON_CUSTOM = "is_custom";
    private static final String JSON_SECTION = "section";
    private static final String JSON_COLUMN = "column";
    private static final String JSON_TOTAL = "total";
    private static final String JSON_SORT_ORDER = "sort_order";

    @Nullable
    private MeasurementType type;
    @Nullable
    private MinistryMeasurement ministryMeasurement;
    @Nullable
    private PersonalMeasurement personalMeasurement;
    private String name;
    private String measurementId;
    private String permLink;
    private boolean custom;
    private String section;
    private String column;
    private int total;
    private MeasurementDetails measurementDetails;
    @NonNull
    private YearMonth period = YearMonth.now();
    @NonNull
    private String ministryId = Ministry.INVALID_ID;
    @NonNull
    private Ministry.Mcc mcc = Ministry.Mcc.UNKNOWN;
    private int sortOrder;

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
        measurement.ministryId = ministryId;
        measurement.mcc = mcc;
        measurement.period = period;

        measurement.type = MeasurementType.fromJson(json);
        if (json.has(MinistryMeasurement.JSON_VALUE)) {
            measurement.ministryMeasurement = MinistryMeasurement.fromJson(json, ministryId, mcc, period);
        }
        if (json.has(PersonalMeasurement.JSON_VALUE)) {
            measurement.personalMeasurement = PersonalMeasurement.fromJson(json, guid, ministryId, mcc, period);
        }

        measurement.measurementId = json.getString(JSON_MEASUREMENT_ID);
        measurement.permLink = json.getString(JSON_PERM_LINK);
        measurement.custom = json.getBoolean(JSON_CUSTOM);
        measurement.section = json.getString(JSON_SECTION);
        measurement.column = json.getString(JSON_COLUMN);
        measurement.total = json.optInt(JSON_TOTAL);
        measurement.sortOrder = json.optInt(JSON_SORT_ORDER);

        return measurement;
    }

    @Nullable
    public MeasurementType getType() {
        return type;
    }

    public void setType(@Nullable final MeasurementType type) {
        this.type = type;
    }

    @Nullable
    public MinistryMeasurement getMinistryMeasurement() {
        return ministryMeasurement;
    }

    public void setMinistryMeasurement(@Nullable final MinistryMeasurement ministryMeasurement) {
        this.ministryMeasurement = ministryMeasurement;
    }

    @Nullable
    public PersonalMeasurement getPersonalMeasurement() {
        return personalMeasurement;
    }

    public void setPersonalMeasurement(@Nullable final PersonalMeasurement personalMeasurement) {
        this.personalMeasurement = personalMeasurement;
    }

    public String getMeasurementId()
    {
        return measurementId;
    }

    public void setMeasurementId(String measurementId)
    {
        this.measurementId = measurementId;
    }

    public String getPermLink()
    {
        return permLink;
    }

    public void setPermLink(String permLink)
    {
        this.permLink = permLink;
    }

    public boolean isCustom()
    {
        return custom;
    }

    public void setCustom(boolean custom)
    {
        this.custom = custom;
    }

    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    public String getColumn()
    {
        return column;
    }

    public void setColumn(String column)
    {
        this.column = column;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public MeasurementDetails getMeasurementDetails()
    {
        return measurementDetails;
    }

    public void setMeasurementDetails(MeasurementDetails measurementDetails)
    {
        this.measurementDetails = measurementDetails;
    }

    @NonNull
    public YearMonth getPeriod() {
        return period;
    }

    public void setPeriod(@NonNull final YearMonth period) {
        this.period = period;
    }

    @NonNull
    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(@NonNull final String ministryId) {
        this.ministryId = ministryId;
    }

    @NonNull
    public Ministry.Mcc getMcc() {
        return this.mcc;
    }

    public void setMcc(@Nullable final String mcc) {
        setMcc(Ministry.Mcc.fromRaw(mcc));
    }

    public void setMcc(@NonNull final Ministry.Mcc mcc) {
        this.mcc = mcc;
    }

    public int getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder)
    {
        this.sortOrder = sortOrder;
    }
}
