package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;

import java.util.Date;

public abstract class Base {
    @NonNull
    private Date lastSynced = new Date(0);

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
}
