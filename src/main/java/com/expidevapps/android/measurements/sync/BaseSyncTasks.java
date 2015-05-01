package com.expidevapps.android.measurements.sync;

import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;

import android.os.Bundle;
import android.support.annotation.NonNull;

class BaseSyncTasks {
    @NonNull
    static Bundle baseExtras(@NonNull final String guid, final boolean force) {
        final Bundle extras = new Bundle(2);
        extras.putString(EXTRA_GUID, guid);
        extras.putBoolean(SYNC_EXTRAS_MANUAL, force);
        return extras;
    }

    static boolean isForced(@NonNull final Bundle extras) {
        return extras.getBoolean(SYNC_EXTRAS_MANUAL, false);
    }
}
