package com.expidevapps.android.measurements.sync;

import android.content.ContentResolver;

final class Constants {
    @Deprecated
    static final String EXTRA_FORCE = ContentResolver.SYNC_EXTRAS_MANUAL;

    static final long HOUR_IN_MS = 60 * 60 * 1000;
    static final long DAY_IN_MS = 24 * HOUR_IN_MS;
    static final long WEEK_IN_MS = 7 * DAY_IN_MS;
}
