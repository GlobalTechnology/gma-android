package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class Base {
    @NonNull
    protected final Set<String> mDirty = new HashSet<>();
    protected boolean mTrackingChanges = false;

    @NonNull
    private Date lastSynced = new Date(0);

    protected Base() {}

    protected Base(@NonNull final Base base) {
        this.lastSynced = base.lastSynced;
    }

    public final void trackingChanges(final boolean state) {
        mTrackingChanges = state;
    }

    public final void setDirty(@Nullable final String dirty) {
        mDirty.clear();
        if (dirty != null) {
            Collections.addAll(mDirty, TextUtils.split(dirty, ","));
        }
    }

    @NonNull
    public final String getDirty() {
        return TextUtils.join(",", mDirty);
    }

    public final boolean isDirty() {
        return !mDirty.isEmpty();
    }

    public long getLastSynced() {
        return this.lastSynced.getTime();
    }

    public Date getLastSyncedDate() {
        return this.lastSynced;
    }

    public void setLastSynced() {
        this.lastSynced = new Date();
    }

    public void setLastSynced(final long lastSynced) {
        this.lastSynced = new Date(lastSynced);
    }

    public void setLastSynced(final Date lastSynced) {
        this.lastSynced = lastSynced != null ? lastSynced : new Date(0);
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        return new JSONObject();
    }

    @NonNull
    public final JSONObject dirtyToJson() throws JSONException {
        final JSONObject json = this.toJson();
        final Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            if (!mDirty.contains(keys.next())) {
                keys.remove();
            }
        }
        return json;
    }
}
