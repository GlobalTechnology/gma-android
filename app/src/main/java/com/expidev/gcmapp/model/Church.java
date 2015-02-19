package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Church extends Base {
    public static final long INVALID_ID = -1;

    private static final int DEVELOPMENT_UNKNOWN = 0;
    private static final int DEVELOPMENT_TARGET = 1;
    private static final int DEVELOPMENT_GROUP = 2;
    private static final int DEVELOPMENT_CHURCH = 3;
    private static final int DEVELOPMENT_MULTIPLYING_CHURCH = 5;

    public enum Development {
        UNKNOWN(DEVELOPMENT_UNKNOWN), TARGET(DEVELOPMENT_TARGET), GROUP(DEVELOPMENT_GROUP), CHURCH(DEVELOPMENT_CHURCH),
        MULTIPLYING_CHURCH(DEVELOPMENT_MULTIPLYING_CHURCH);

        public final int id;

        private Development(final int id) {
            this.id = id;
        }

        @NonNull
        public static Development fromRaw(final int index) {
            switch (index) {
                case DEVELOPMENT_TARGET:
                    return TARGET;
                case DEVELOPMENT_GROUP:
                    return GROUP;
                case DEVELOPMENT_CHURCH:
                    return CHURCH;
                case DEVELOPMENT_MULTIPLYING_CHURCH:
                    return MULTIPLYING_CHURCH;
                default:
                    return UNKNOWN;
            }
        }
    }

    public static final String JSON_ID = "id";
    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_NAME = "name";
    public static final String JSON_CONTACT_EMAIL = "contact_email";
    public static final String JSON_CONTACT_NAME = "contact_name";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LONGITUDE = "longitude";
    public static final String JSON_DEVELOPMENT = "development";
    public static final String JSON_SIZE = "size";
    public static final String JSON_SECURITY = "security";

    private long id = INVALID_ID;
    @NonNull
    private String ministryId;
    @Nullable
    private String name;
    @Nullable
    private String contactEmail;
    @Nullable
    private String contactName;
    private double latitude;
    private double longitude;
    @NonNull
    private Development development = Development.UNKNOWN;
    private int size;
    private int security;

    @NonNull
    public static List<Church> listFromJson(@NonNull final JSONArray json) throws JSONException {
        final List<Church> churches = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            churches.add(fromJson(json.getJSONObject(i)));
        }
        return churches;
    }

    @NonNull
    public static Church fromJson(@NonNull final JSONObject json) throws JSONException {
        final Church church = new Church();
        church.id = json.getLong(JSON_ID);
        church.ministryId = json.getString(JSON_MINISTRY_ID);
        church.name = json.optString(JSON_NAME, null);
        church.contactEmail = json.optString(JSON_CONTACT_EMAIL, null);
        church.contactName = json.optString(JSON_CONTACT_NAME, null);
        church.latitude = json.getDouble(JSON_LATITUDE);
        church.longitude = json.getDouble(JSON_LONGITUDE);
        church.setDevelopment(json.optInt(JSON_DEVELOPMENT, DEVELOPMENT_UNKNOWN));
        church.size = json.optInt(JSON_SIZE, 0);
        church.security = json.optInt(JSON_SECURITY, 2);
        return church;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @NonNull
    public String getMinistryId() {
        return ministryId;
    }

    public void setMinistryId(@NonNull final String ministryId) {
        this.ministryId = ministryId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = name;
    }

    @Nullable
    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(@Nullable final String email) {
        this.contactEmail = email;
    }

    @Nullable
    public String getContactName() {
        return contactName;
    }

    public void setContactName(@Nullable final String name) {
        this.contactName = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    public Development getDevelopment() {
        return development;
    }

    public void setDevelopment(final int development) {
        this.development = Development.fromRaw(development);
    }

    public void setDevelopment(@NonNull final Development development) {
        this.development = development;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(final int security) {
        this.security = security;
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(JSON_MINISTRY_ID, this.ministryId);
        json.put(JSON_NAME, this.name);
        json.put(JSON_CONTACT_NAME, this.contactName);
        json.put(JSON_CONTACT_EMAIL, this.contactEmail);
        json.put(JSON_LATITUDE, this.latitude);
        json.put(JSON_LONGITUDE, this.longitude);
        if (this.development != Development.UNKNOWN) {
            json.put(JSON_DEVELOPMENT, this.development.id);
        }
        json.put(JSON_SIZE, this.size);
        json.put(JSON_SECURITY, this.security);
        return json;
    }
}
