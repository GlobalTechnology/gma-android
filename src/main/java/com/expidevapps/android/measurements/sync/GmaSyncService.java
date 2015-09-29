package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.BuildConfig.ACCOUNT_TYPE;
import static com.expidevapps.android.measurements.BuildConfig.SYNC_AUTHORITY;
import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERIOD;
import static com.expidevapps.android.measurements.sync.AssignmentSyncTasks.EXTRA_ASSIGNMENTS;
import static com.expidevapps.android.measurements.sync.AssignmentSyncTasks.EXTRA_PERSON_ID;
import static com.expidevapps.android.measurements.sync.AssignmentSyncTasks.EXTRA_SUPPORTED_STAFF;
import static com.expidevapps.android.measurements.sync.BaseSyncTasks.baseExtras;
import static com.expidevapps.android.measurements.sync.BaseSyncTasks.measurementExtras;
import static com.expidevapps.android.measurements.sync.BaseSyncTasks.ministryExtras;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.EXTRA_SYNCTYPE;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_ASSIGNMENTS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_CHURCHES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_DIRTY_CHURCHES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_DIRTY_MEASUREMENTS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_DIRTY_TRAININGS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_DIRTY_TRAINING_COMPLETIONS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_MEASUREMENTS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_MEASUREMENT_DETAILS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_MEASUREMENT_TYPES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_MINISTRIES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_NONE;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_PREFERENCES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_SAVE_ASSIGNMENTS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_SAVE_PREFERENCES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_TRAININGS;
import static com.expidevapps.android.measurements.sync.UserPreferenceSyncTasks.EXTRA_PREFERENCES;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Ministry.Mcc;

import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.joda.time.YearMonth;
import org.json.JSONArray;
import org.json.JSONObject;

import me.thekey.android.lib.accounts.AccountUtils;

public class GmaSyncService extends ThreadedIntentService {
    @NonNull
    private /* final */ GmaSyncAdapter mSyncAdapter;

    public GmaSyncService() {
        super("GmaSyncService", 10);
    }

    public static void syncMinistries(final Context context, @NonNull final String guid, final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MINISTRIES);
        intent.putExtras(baseExtras(guid, force));
        context.startService(intent);
    }

    public static void syncPreferences(@NonNull final Context context, @NonNull final String guid,
                                       final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_PREFERENCES);
        intent.putExtras(baseExtras(guid, force));
        context.startService(intent);
    }

    public static void savePreferences(@NonNull final Context context, @NonNull final String guid,
                                       @Nullable final JSONObject preferences) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_SAVE_PREFERENCES);
        intent.putExtras(baseExtras(guid, false));
        intent.putExtra(EXTRA_PREFERENCES, preferences != null ? preferences.toString() : null);
        context.startService(intent);
    }

    public static void syncAssignments(@NonNull final Context context, @NonNull final String guid,
                                       final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_ASSIGNMENTS);
        intent.putExtras(baseExtras(guid, force));
        context.startService(intent);
    }

    public static void saveAssignments(@NonNull final Context context, @NonNull final String guid,
                                       @Nullable final String personId, final int supportedStaff, @Nullable final JSONArray assignments) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_SAVE_ASSIGNMENTS);
        intent.putExtras(baseExtras(guid, false));
        intent.putExtra(EXTRA_PERSON_ID, personId);
        intent.putExtra(EXTRA_SUPPORTED_STAFF, supportedStaff);
        intent.putExtra(EXTRA_ASSIGNMENTS, assignments != null ? assignments.toString() : null);
        context.startService(intent);
    }

    public static void syncChurches(@NonNull final Context context, @NonNull final String guid,
                                    @NonNull final String ministryId, final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_CHURCHES);
        intent.putExtras(ministryExtras(guid, ministryId, force));
        context.startService(intent);
    }

    public static void syncDirtyChurches(@NonNull final Context context, @NonNull final String guid) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_DIRTY_CHURCHES);
        intent.putExtras(baseExtras(guid, false));
        context.startService(intent);
    }

    public static void syncTrainings(@NonNull final Context context, @NonNull final String guid,
                                     @NonNull final String ministryId, @NonNull final Mcc mcc, final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_TRAININGS);
        intent.putExtras(ministryExtras(guid, ministryId, mcc, force));
        context.startService(intent);
    }

    public static void syncDirtyTrainings(@NonNull final Context context, @NonNull final String guid) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_DIRTY_TRAININGS);
        intent.putExtras(baseExtras(guid, false));
        context.startService(intent);
    }

    public static void syncDirtyTrainingCompletions(@NonNull final Context context, @NonNull final String ministryId, @NonNull final String guid) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_DIRTY_TRAINING_COMPLETIONS);
        //intent.putExtras(baseExtras(guid, false));
        intent.putExtras(ministryExtras(guid, ministryId, false));
        context.startService(intent);
    }

    public static void syncMeasurementTypes(@NonNull final Context context, @NonNull final String guid,
                                            @NonNull final String ministryId, final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MEASUREMENT_TYPES);
        intent.putExtras(ministryExtras(guid, ministryId, force));
        context.startService(intent);
    }

    public static void syncMeasurements(@NonNull final Context context, @NonNull final String guid,
                                        @NonNull final String ministryId, @NonNull final Mcc mcc,
                                        @Nullable final YearMonth period, final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MEASUREMENTS);
        intent.putExtras(ministryExtras(guid, ministryId, mcc, force));
        intent.putExtra(EXTRA_PERIOD, period != null ? period.toString() : null);
        context.startService(intent);
    }

    public static void syncDirtyMeasurements(@NonNull final Context context, @NonNull final String guid,
                                             @NonNull final String ministryId, @NonNull final Mcc mcc,
                                             @NonNull final YearMonth period) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_DIRTY_MEASUREMENTS);
        intent.putExtras(ministryExtras(guid, ministryId, mcc, false));
        intent.putExtra(EXTRA_PERIOD, period.toString());
        context.startService(intent);
    }

    public static void syncMeasurementDetails(@NonNull final Context context, @NonNull final String guid,
                                              @NonNull final String ministryId, @NonNull final Mcc mcc,
                                              @NonNull final String permLink, @NonNull final YearMonth period,
                                              final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MEASUREMENT_DETAILS);
        intent.putExtras(measurementExtras(guid, ministryId, mcc, permLink, force));
        intent.putExtra(EXTRA_PERIOD, period.toString());
        context.startService(intent);
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate() {
        super.onCreate();
        mSyncAdapter = GmaSyncAdapter.getInstance(this);
    }

    @Override
    public IBinder onBind(@NonNull final Intent intent) {
        final String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "android.content.SyncAdapter":
                    return mSyncAdapter.getSyncAdapterBinder();
            }
        }
        return super.onBind(intent);
    }

    @Override
    public void onHandleIntent(@NonNull final Intent intent) {
        // short-circuit if we don't have a valid guid
        final String guid = intent.getStringExtra(EXTRA_GUID);
        if (guid == null) {
            return;
        }

        // dispatch sync request
        final int type = intent.getIntExtra(EXTRA_SYNCTYPE, SYNCTYPE_NONE);
        final Bundle extras = intent.getExtras();
        final SyncResult result = new SyncResult();
        mSyncAdapter.dispatchSync(guid, type, extras, result);

        // request a sync next time we are online if we had errors syncing
        if (result.hasError()) {
            final Account account = AccountUtils.getAccount(this, ACCOUNT_TYPE, guid);
            if (account != null) {
                ContentResolver.requestSync(account, SYNC_AUTHORITY, extras);
            }
        }
    }

    /* END lifecycle */
}
