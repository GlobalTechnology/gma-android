package com.expidevapps.android.measurements.service;

import static com.expidevapps.android.measurements.BuildConfig.GOOGLE_ANALYTICS_CLIENT_ID;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.joda.time.YearMonth;

public class GoogleAnalyticsManager {
    private static final int DIMEN_GUID = 1;
    private static final int DIMEN_MINISTRY_ID = 2;
    private static final int DIMEN_MCC = 3;
    private static final int DIMEN_PERIOD = 4;

    private static final String SCREEN_NAME_LOGIN = "Login";
    private static final String SCREEN_NAME_MAP = "Map";
    private static final String SCREEN_NAME_JOIN_MINISTRY = "Join Ministry";
    private static final String SCREEN_NAME_SETTINGS = "Settings";
    private static final String SCREEN_NAME_MEASUREMENTS = "Measurements";

    /* Auth events */
    private static final String CATEGORY_AUTH = "auth";
    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_LOGOUT = "logout";

    /* Assignment events */
    private static final String CATEGORY_ASSIGNMENT = "assignments";
    private static final String ACTION_JOIN_MINISTRY = "join ministry";

    private final Tracker mTracker;

    private static final Object LOCK_INSTANCE = new Object();
    private static GoogleAnalyticsManager INSTANCE;

    private GoogleAnalyticsManager(final Context context) {
        mTracker = GoogleAnalytics.getInstance(context).newTracker(GOOGLE_ANALYTICS_CLIENT_ID);
    }

    @NonNull
    public static GoogleAnalyticsManager getInstance(@NonNull final Context context) {
        synchronized (LOCK_INSTANCE) {
            if (INSTANCE == null) {
                INSTANCE = new GoogleAnalyticsManager(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    public void sendLoginScreen() {
        mTracker.setScreenName(SCREEN_NAME_LOGIN);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void sendMapScreen(@Nullable final String guid) {
        mTracker.setScreenName(SCREEN_NAME_MAP);
        mTracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(DIMEN_GUID, guid).build());
    }

    public void sendJoinMinistryScreen(@NonNull final String guid) {
        mTracker.setScreenName(SCREEN_NAME_JOIN_MINISTRY);
        mTracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(DIMEN_GUID, guid).build());
    }

    public void sendSettingsScreen(@NonNull final String guid) {
        mTracker.setScreenName(SCREEN_NAME_SETTINGS);
        mTracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(DIMEN_GUID, guid).build());
    }

    public void sendMeasurementsScreen(@NonNull final String guid, @NonNull final String ministryId,
                                       @NonNull final Mcc mcc, @NonNull final YearMonth period) {
        mTracker.setScreenName(SCREEN_NAME_MEASUREMENTS);
        mTracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(DIMEN_GUID, guid)
                              .setCustomDimension(DIMEN_MINISTRY_ID, ministryId)
                              .setCustomDimension(DIMEN_MCC, mcc.toString())
                              .setCustomDimension(DIMEN_PERIOD, period.toString()).build());
    }

    public void sendLoginEvent(@NonNull final String guid) {
        mTracker.send(new HitBuilders.EventBuilder(CATEGORY_AUTH, ACTION_LOGIN).setCustomDimension(DIMEN_GUID, guid)
                              .build());
    }

    public void sendLogoutEvent(@NonNull final String guid) {
        mTracker.send(new HitBuilders.EventBuilder(CATEGORY_AUTH, ACTION_LOGOUT).setCustomDimension(DIMEN_GUID, guid)
                              .build());
    }

    public void sendJoinMinistryEvent(@NonNull final String guid, @NonNull final String ministryId) {
        mTracker.send(new HitBuilders.EventBuilder(CATEGORY_ASSIGNMENT, ACTION_JOIN_MINISTRY)
                              .setCustomDimension(DIMEN_GUID, guid).setCustomDimension(DIMEN_MINISTRY_ID, ministryId)
                              .build());
    }
}
