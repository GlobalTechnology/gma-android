package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Church extends Location implements Cloneable {
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

    private static final int SECURITY_LOCAL_PRIVATE = 0;
    private static final int SECURITY_PRIVATE = 1;
    private static final int SECURITY_PUBLIC = 2;
    public static final int SECURITY_DEFAULT = SECURITY_PUBLIC;

    public enum Security {
        LOCAL_PRIVATE(SECURITY_LOCAL_PRIVATE), PRIVATE(SECURITY_PRIVATE), PUBLIC(SECURITY_PUBLIC);

        public final int id;

        private Security(final int id) {
            this.id = id;
        }

        @NonNull
        public static Security fromRaw(final int id) {
            switch (id) {
                case SECURITY_LOCAL_PRIVATE:
                    return LOCAL_PRIVATE;
                case SECURITY_PRIVATE:
                    return PRIVATE;
                case SECURITY_PUBLIC:
                default:
                    return PUBLIC;
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
    private String ministryId = Ministry.INVALID_ID;
    @Nullable
    private String name;
    @Nullable
    private String contactEmail;
    @Nullable
    private String contactName;
    @NonNull
    private Development development = Development.UNKNOWN;
    @NonNull
    private Security security = Security.PUBLIC;
    private int size = 0;

    private boolean mNew = false;
    @NonNull
    private final Set<String> mDirty = new HashSet<>();
    private boolean mTrackingChanges = false;

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
        church.setLatitude(json.optDouble(JSON_LATITUDE, Double.NaN));
        church.setLongitude(json.optDouble(JSON_LONGITUDE, Double.NaN));
        church.development = Development.fromRaw(json.optInt(JSON_DEVELOPMENT, DEVELOPMENT_UNKNOWN));
        church.security = Security.fromRaw(json.optInt(JSON_SECURITY, SECURITY_DEFAULT));
        church.size = json.optInt(JSON_SIZE, 0);
        return church;
    }

    public Church() {
    }

    private Church(@NonNull final Church church) {
        super(church);
        this.id = church.id;
        this.ministryId = church.ministryId;
        this.name = church.name;
        this.contactEmail = church.contactEmail;
        this.contactName = church.contactName;
        this.development = church.development;
        this.size = church.size;
        this.security = church.security;
        mDirty.clear();
        mDirty.addAll(church.mDirty);
        mTrackingChanges = church.mTrackingChanges;
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
        if (mTrackingChanges) {
            mDirty.add(JSON_NAME);
        }
    }

    @Nullable
    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(@Nullable final String email) {
        this.contactEmail = email;
        if (mTrackingChanges) {
            mDirty.add(JSON_CONTACT_EMAIL);
        }
    }

    @Nullable
    public String getContactName() {
        return contactName;
    }

    public void setContactName(@Nullable final String name) {
        this.contactName = name;
        if (mTrackingChanges) {
            mDirty.add(JSON_CONTACT_NAME);
        }
    }

    @NonNull
    public Development getDevelopment() {
        return development;
    }

    public void setDevelopment(@NonNull final Development development) {
        this.development = development;
        if (mTrackingChanges) {
            mDirty.add(JSON_DEVELOPMENT);
        }
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
        if(mTrackingChanges) {
            mDirty.add(JSON_SIZE);
        }
    }

    @NonNull
    public Security getSecurity() {
        return security;
    }

    public void setSecurity(@NonNull final Security security) {
        this.security = security;
    }

    public void setNew(final boolean state) {
        mNew = state;
    }

    public boolean isNew() {
        return mNew;
    }

    public void setDirty(@Nullable final String dirty) {
        mDirty.clear();
        if (dirty != null) {
            Collections.addAll(mDirty, TextUtils.split(dirty, ","));
        }
    }

    @NonNull
    public String getDirty() {
        return TextUtils.join(",", mDirty);
    }

    public boolean isDirty() {
        return !mDirty.isEmpty();
    }

    public void trackingChanges(final boolean state) {
        mTrackingChanges = state;
    }

    @Override
    public Church clone() {
        return new Church(this);
    }

    public JSONObject dirtyToJson() throws JSONException {
        final JSONObject json = this.toJson();
        final Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            if (!this.mDirty.contains(keys.next())) {
                keys.remove();
            }
        }
        return json;
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(JSON_MINISTRY_ID, this.ministryId);
        json.put(JSON_NAME, this.name);
        json.put(JSON_CONTACT_NAME, this.contactName);
        json.put(JSON_CONTACT_EMAIL, this.contactEmail);
        json.put(JSON_LATITUDE, this.getLatitude());
        json.put(JSON_LONGITUDE, this.getLongitude());
        if (this.development != Development.UNKNOWN) {
            json.put(JSON_DEVELOPMENT, this.development.id);
        }
        json.put(JSON_SIZE, this.size);
        json.put(JSON_SECURITY, this.security.id);
        return json;
    }
}
