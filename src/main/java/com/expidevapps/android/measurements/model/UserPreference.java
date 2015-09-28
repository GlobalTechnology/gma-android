package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class UserPreference extends Base {
    @NonNull
    private final String mGuid;
    @NonNull
    private final String mName;
    @Nullable
    private String mValue;

    public UserPreference(@NonNull final String guid, @NonNull final String name) {
        mGuid = guid;
        mName = name;
    }

    @NonNull
    public static Map<String, UserPreference> mapFromJson(@NonNull final JSONObject json, @NonNull final String guid)
            throws JSONException {
        final Map<String, UserPreference> prefs = new ArrayMap<>(json.length());
        final Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (key != null) {
                final UserPreference pref = new UserPreference(guid, key);
                pref.mValue = json.getString(key);
                prefs.put(key, pref);
            }
        }
        return prefs;
    }

    @NonNull
    public String getGuid() {
        return mGuid;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @Nullable
    public String getValue() {
        return mValue;
    }

    @Nullable
    public Boolean getValueAsBoolean() {
        if ("1".equals(mValue)) {
            return Boolean.TRUE;
        } else if ("0".equals(mValue)) {
            return Boolean.FALSE;
        }

        return null;
    }

    public void setValue(@Nullable final String value) {
        if (mTrackingChanges) {
            mDirty.add("value");
        }
        mValue = value;
    }

    public void setValue(final boolean value) {
        setValue(value ? "1" : "0");
    }
}
