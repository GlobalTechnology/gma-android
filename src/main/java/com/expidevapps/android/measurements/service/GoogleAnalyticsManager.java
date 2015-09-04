package com.expidevapps.android.measurements.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.joda.time.YearMonth;

import static com.expidevapps.android.measurements.BuildConfig.GOOGLE_ANALYTICS_CLIENT_ID;

public class GoogleAnalyticsManager {
    private static final int DIMEN_GUID = 1;
    private static final int DIMEN_MINISTRY_ID = 2;
    private static final int DIMEN_MCC = 3;
    private static final int DIMEN_PERIOD = 4;
    private static final int DIMEN_PERM_LINK = 5;
    private static final int DIMEN_CHURCH_ID = 6;
    private static final int DIMEN_TRAINING_ID = 7;

    private static final String SCREEN_NAME_LOGIN = "Login";
    private static final String SCREEN_NAME_MAP = "Map";
    private static final String SCREEN_NAME_JOIN_MINISTRY = "Join Ministry";
    private static final String SCREEN_NAME_SETTINGS = "Settings";
    private static final String SCREEN_NAME_MEASUREMENTS = "Measurements";
    private static final String SCREEN_NAME_MEASUREMENT_DETAILS = "Measurement Details";

    /* Auth events */
    private static final String CATEGORY_AUTH = "auth";
    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_LOGOUT = "logout";

    /* Assignment events */
    private static final String CATEGORY_ASSIGNMENT = "assignments";
    private static final String ACTION_JOIN_MINISTRY = "join ministry";

    /* Church & Training events */
    private static final String CATEGORY_CHURCH = "church";
    private static final String CATEGORY_TRAINING = "training";
    private static final String ACTION_CREATE = "create";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_MOVE = "move";

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

    @NonNull
    private HitBuilders.ScreenViewBuilder screen() {
        return new HitBuilders.ScreenViewBuilder();
    }

    @NonNull
    private HitBuilders.ScreenViewBuilder screen(@Nullable final String guid) {
        return screen().setCustomDimension(DIMEN_GUID, guid);
    }

    @NonNull
    private HitBuilders.ScreenViewBuilder screen(@Nullable final String guid, @NonNull final String ministryId,
                                                 @NonNull final Mcc mcc) {
        return screen(guid).setCustomDimension(DIMEN_MINISTRY_ID, ministryId)
                .setCustomDimension(DIMEN_MCC, mcc.toString());
    }

    public void sendLoginScreen() {
        mTracker.setScreenName(SCREEN_NAME_LOGIN);
        mTracker.send(screen().build());
    }

    public void sendMapScreen(@Nullable final String guid) {
        mTracker.setScreenName(SCREEN_NAME_MAP);
        mTracker.send(screen(guid).build());
    }

    public void sendJoinMinistryScreen(@NonNull final String guid) {
        mTracker.setScreenName(SCREEN_NAME_JOIN_MINISTRY);
        mTracker.send(screen(guid).build());
    }

    public void sendSettingsScreen(@NonNull final String guid) {
        mTracker.setScreenName(SCREEN_NAME_SETTINGS);
        mTracker.send(screen(guid).build());
    }

    public void sendMeasurementsScreen(@NonNull final String guid, @NonNull final String ministryId,
                                       @NonNull final Mcc mcc, @NonNull final YearMonth period) {
        mTracker.setScreenName(SCREEN_NAME_MEASUREMENTS);
        mTracker.send(screen(guid, ministryId, mcc).setCustomDimension(DIMEN_PERIOD, period.toString()).build());
    }

    public void sendMeasurementDetailsScreen(@NonNull final String guid, @NonNull final String ministryId,
                                             @NonNull final Mcc mcc, @NonNull final String permLink,
                                             @NonNull final YearMonth period) {
        mTracker.setScreenName(SCREEN_NAME_MEASUREMENT_DETAILS);
        mTracker.send(screen(guid, ministryId, mcc).setCustomDimension(DIMEN_PERM_LINK, permLink)
                              .setCustomDimension(DIMEN_PERIOD, period.toString()).build());
    }

    @NonNull
    private HitBuilders.EventBuilder event(@NonNull final String category, @NonNull final String action,
                                           @NonNull final String guid) {
        return new HitBuilders.EventBuilder(category, action).setCustomDimension(DIMEN_GUID, guid);
    }

    @NonNull
    private HitBuilders.EventBuilder event(@NonNull final String category, @NonNull final String action,
                                           @NonNull final String guid, @NonNull final String ministryId) {
        return event(category, action, guid).setCustomDimension(DIMEN_MINISTRY_ID, ministryId);
    }

    @NonNull
    private HitBuilders.EventBuilder event(@NonNull final String category, @NonNull final String action,
                                           @NonNull final String guid, @NonNull final String ministryId,
                                           @NonNull final Mcc mcc) {
        return event(category, action, guid, ministryId).setCustomDimension(DIMEN_MCC, mcc.toString());
    }

    public void sendLoginEvent(@NonNull final String guid) {
        mTracker.send(event(CATEGORY_AUTH, ACTION_LOGIN, guid).build());
    }

    public void sendLogoutEvent(@NonNull final String guid) {
        mTracker.send(event(CATEGORY_AUTH, ACTION_LOGOUT, guid).build());
    }

    public void sendJoinMinistryEvent(@NonNull final String guid, @NonNull final String ministryId) {
        mTracker.send(event(CATEGORY_ASSIGNMENT, ACTION_JOIN_MINISTRY, guid, ministryId).build());
    }

    @NonNull
    private HitBuilders.EventBuilder churchEvent(@NonNull final String action, @NonNull final String guid,
                                                 @NonNull final String ministryId) {
        return event(CATEGORY_CHURCH, action, guid, ministryId);
    }

    @NonNull
    private HitBuilders.EventBuilder churchEvent(@NonNull final String action, @NonNull final String guid,
                                                 @NonNull final String ministryId, final long churchId) {
        return churchEvent(action, guid, ministryId).setCustomDimension(DIMEN_CHURCH_ID, Long.toString(churchId));
    }

    public void sendCreateChurchEvent(@NonNull final String guid, @NonNull final String ministryId) {
        mTracker.send(churchEvent(ACTION_CREATE, guid, ministryId).build());
    }

    public void sendMoveChurchEvent(@NonNull final String guid, @NonNull final String ministryId, final long churchId) {
        mTracker.send(churchEvent(ACTION_MOVE, guid, ministryId, churchId).build());
    }

    public void sendUpdateChurchEvent(@NonNull final String guid, @NonNull final String ministryId,
                                      final long churchId) {
        mTracker.send(churchEvent(ACTION_UPDATE, guid, ministryId, churchId).build());
    }

    @NonNull
    private HitBuilders.EventBuilder trainingEvent(@NonNull final String action, @NonNull final String guid,
                                                 @NonNull final String ministryId, @NonNull final Mcc mcc) {
        return event(CATEGORY_TRAINING, action, guid, ministryId, mcc);
    }

    @NonNull
    private HitBuilders.EventBuilder trainingEvent(@NonNull final String action, @NonNull final String guid,
                                                   @NonNull final String ministryId, @NonNull final Mcc mcc,
                                                   final long trainingId) {
        return event(CATEGORY_TRAINING, action, guid, ministryId, mcc)
                .setCustomDimension(DIMEN_TRAINING_ID, Long.toString(trainingId));
    }

    public void sendMoveTrainingEvent(@NonNull final String guid, @NonNull final String ministryId,
                                      @NonNull final Mcc mcc, final long trainingId) {
        mTracker.send(trainingEvent(ACTION_MOVE, guid, ministryId, mcc, trainingId).build());
    }

    public void sendCreateTrainingEvent(@NonNull final String guid, @NonNull final String ministryId, @NonNull final Mcc mcc) {
        mTracker.send(trainingEvent(ACTION_CREATE, guid, ministryId, mcc).build());
    }

    public void sendUpdateTrainingEvent(@NonNull final String guid, @NonNull final String ministryId,
                                        @NonNull final Mcc mcc, final long trainingId) {
        mTracker.send(trainingEvent(ACTION_UPDATE, guid, ministryId, mcc, trainingId).build());
    }
}
