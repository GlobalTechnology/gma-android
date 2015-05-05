package com.expidevapps.android.measurements.sync;

import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;
import static com.expidevapps.android.measurements.Constants.EXTRA_MCC;
import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERMLINK;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Ministry.Mcc;

class BaseSyncTasks {
    static final long HOUR_IN_MS = 60 * 60 * 1000;
    static final long DAY_IN_MS = 24 * HOUR_IN_MS;
    static final long WEEK_IN_MS = 7 * DAY_IN_MS;

    @NonNull
    static Bundle baseExtras(@NonNull final String guid, final boolean force) {
        final Bundle extras = new Bundle(2);
        extras.putString(EXTRA_GUID, guid);
        extras.putBoolean(SYNC_EXTRAS_MANUAL, force);
        return extras;
    }

    @NonNull
    static Bundle ministryExtras(@NonNull final String guid, @NonNull final String ministryId, final boolean force) {
        final Bundle extras = baseExtras(guid, force);
        extras.putString(EXTRA_MINISTRY_ID, ministryId);
        return extras;
    }

    @NonNull
    static Bundle ministryExtras(@NonNull final String guid, @NonNull final String ministryId, @NonNull final Mcc mcc,
                                 final boolean force) {
        final Bundle extras = ministryExtras(guid, ministryId, force);
        extras.putString(EXTRA_MCC, mcc.toString());
        return extras;
    }

    @NonNull
    static Bundle measurementExtras(@NonNull final String guid, @NonNull final String ministryId,
                                    @NonNull final Mcc mcc, @NonNull final String permLink, final boolean force) {
        final Bundle extras = ministryExtras(guid, ministryId, mcc, force);
        extras.putString(EXTRA_PERMLINK, permLink);
        return extras;
    }

    static boolean isForced(@NonNull final Bundle extras) {
        return extras.getBoolean(SYNC_EXTRAS_MANUAL, false);
    }
}
