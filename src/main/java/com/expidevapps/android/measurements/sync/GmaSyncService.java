package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.BuildConfig.GMA_API_VERSION;
import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;
import static com.expidevapps.android.measurements.Constants.EXTRA_MCC;
import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERIOD;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERMLINK;
import static com.expidevapps.android.measurements.model.Task.UPDATE_MINISTRY_MEASUREMENTS;
import static com.expidevapps.android.measurements.model.Task.UPDATE_PERSONAL_MEASUREMENTS;
import static com.expidevapps.android.measurements.sync.AssignmentSyncTasks.EXTRA_ASSIGNMENTS;
import static com.expidevapps.android.measurements.sync.BaseSyncTasks.baseExtras;
import static com.expidevapps.android.measurements.sync.Constants.DAY_IN_MS;
import static com.expidevapps.android.measurements.sync.Constants.EXTRA_FORCE;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.EXTRA_SYNCTYPE;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_ASSIGNMENTS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_CHURCHES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_DIRTY_CHURCHES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_DIRTY_MEASUREMENTS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_MEASUREMENTS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_MEASUREMENT_DETAILS;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_MEASUREMENT_TYPES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_MINISTRIES;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_NONE;
import static com.expidevapps.android.measurements.sync.GmaSyncAdapter.SYNCTYPE_SAVE_ASSIGNMENTS;
import static com.expidevapps.android.measurements.sync.MinistrySyncTasks.ministryExtras;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;

import com.expidevapps.android.measurements.Constants;
import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Measurement;
import com.expidevapps.android.measurements.model.MeasurementDetails;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementValue;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.MinistryMeasurement;
import com.expidevapps.android.measurements.model.PersonalMeasurement;
import com.google.common.collect.Maps;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.ccci.gto.android.common.util.ThreadUtils;
import org.ccci.gto.android.common.util.ThreadUtils.GenericKey;
import org.joda.time.YearMonth;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GmaSyncService extends ThreadedIntentService {

    // various stale data durations
    private static final long STALE_DURATION_MEASUREMENT_DETAILS_CURRENT = DAY_IN_MS;
    private static final long STALE_DURATION_MEASUREMENT_DETAILS_PREVIOUS = 2 * DAY_IN_MS;
    private static final long STALE_DURATION_MEASUREMENT_DETAILS_OLD = 7 * DAY_IN_MS;

    // locks to synchronize various sync types
    private final Object mLockDirtyChurches = new Object();
    private final Map<GenericKey, Object> mLocksDirtyMeasurements = new ArrayMap<>();

    @NonNull
    private /* final */ GmaDao mDao;
    private LocalBroadcastManager broadcastManager;
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

    public static void syncAssignments(@NonNull final Context context, @NonNull final String guid) {
        syncAssignments(context, guid, false);
    }

    public static void syncAssignments(@NonNull final Context context, @NonNull final String guid,
                                       final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_ASSIGNMENTS);
        intent.putExtras(baseExtras(guid, force));
        context.startService(intent);
    }

    public static void saveAssignments(@NonNull final Context context, @NonNull final String guid,
                                       @Nullable final JSONArray assignments) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_SAVE_ASSIGNMENTS);
        intent.putExtras(baseExtras(guid, false));
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

    public static void syncMeasurementTypes(@NonNull final Context context, @NonNull final String guid,
                                            final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MEASUREMENT_TYPES);
        intent.putExtras(baseExtras(guid, force));
        context.startService(intent);
    }

    public static void syncMeasurements(@NonNull final Context context, @NonNull final String ministryId,
                                        @NonNull final Mcc mcc, @Nullable final YearMonth period) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MEASUREMENTS);
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        intent.putExtra(EXTRA_MCC, mcc.toString());
        intent.putExtra(EXTRA_PERIOD, (period != null ? period : YearMonth.now()).toString());
        context.startService(intent);
    }

    public static void syncDirtyMeasurements(@NonNull final Context context, @NonNull final String guid,
                                             @NonNull final String ministryId, @NonNull final Mcc mcc,
                                             @NonNull final YearMonth period) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_DIRTY_MEASUREMENTS);
        intent.putExtras(baseExtras(guid, true));
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        intent.putExtra(EXTRA_MCC, mcc.toString());
        intent.putExtra(EXTRA_PERIOD, period.toString());
        context.startService(intent);
    }

    public static void syncMeasurementDetails(@NonNull final Context context, @NonNull final String guid,
                                              @NonNull final String ministryId, @NonNull final Mcc mcc,
                                              @NonNull final String permLink, @NonNull final YearMonth period,
                                              final boolean force) {
        final Intent intent = new Intent(context, GmaSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MEASUREMENT_DETAILS);
        intent.putExtras(baseExtras(guid, force));
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        intent.putExtra(EXTRA_MCC, mcc.toString());
        intent.putExtra(EXTRA_PERMLINK, permLink);
        intent.putExtra(EXTRA_PERIOD, period.toString());
        context.startService(intent);
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate() {
        super.onCreate();
        mDao = GmaDao.getInstance(this);
        broadcastManager = LocalBroadcastManager.getInstance(this);
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
        final String guid = intent.getStringExtra(EXTRA_GUID);
        final GmaApiClient api = GmaApiClient.getInstance(this, intent.getStringExtra(EXTRA_GUID));

        try {
            final int type = intent.getIntExtra(EXTRA_SYNCTYPE, SYNCTYPE_NONE);
            switch (type) {
                case SYNCTYPE_MEASUREMENTS:
                    syncMeasurements(api, intent);
                    break;
                case SYNCTYPE_DIRTY_MEASUREMENTS:
                    syncDirtyMeasurements(api, intent);
                    break;
                case SYNCTYPE_MEASUREMENT_DETAILS:
                    syncMeasurementDetails(api, intent);
                    break;
                default:
                    if (guid != null) {
                        mSyncAdapter.dispatchSync(guid, type, intent.getExtras(), new SyncResult());
                    }
                    break;
            }
        } catch (final ApiException e) {
            // XXX: ignore for now, maybe eventually broadcast something on specific ApiExceptions
        }
    }

    /* END lifecycle */

    /* BEGIN Measurement sync */

    private static final String[] PROJECTION_SYNC_MEASUREMENTS_TYPE =
            {Contract.MeasurementType.COLUMN_NAME, Contract.MeasurementType.COLUMN_PERM_LINK_STUB,
                    Contract.MeasurementType.COLUMN_DESCRIPTION, Contract.MeasurementType.COLUMN_SECTION,
                    Contract.MeasurementType.COLUMN_COLUMN, Contract.MeasurementType.COLUMN_SORT_ORDER,
                    Contract.MeasurementType.COLUMN_LOCAL_ID, Contract.MeasurementType.COLUMN_PERSONAL_ID,
                    Contract.MeasurementType.COLUMN_TOTAL_ID, Contract.MeasurementType.COLUMN_LAST_SYNCED};
    private static final String[] PROJECTION_SYNC_MEASUREMENTS_MINISTRY_MEASUREMENT =
            {Contract.MinistryMeasurement.COLUMN_VALUE, Contract.MinistryMeasurement.COLUMN_LAST_SYNCED};
    private static final String[] PROJECTION_SYNC_MEASUREMENTS_PERSONAL_MEASUREMENT =
            {Contract.PersonalMeasurement.COLUMN_VALUE, Contract.PersonalMeasurement.COLUMN_LAST_SYNCED};

    private void syncMeasurements(@NonNull final GmaApiClient api, final Intent intent) throws ApiException {
        // get parameters for sync from the intent & sanitize
        final String guid = intent.getStringExtra(EXTRA_GUID);
        final String ministryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
        final Mcc mcc = Mcc.fromRaw(intent.getStringExtra(Constants.ARG_MCC));
        final String rawPeriod = intent.getStringExtra(Constants.ARG_PERIOD);
        final YearMonth period = rawPeriod != null ? YearMonth.parse(rawPeriod) : YearMonth.now();
        if (guid == null) {
            return;
        }
        if (ministryId == null || ministryId.equals(Ministry.INVALID_ID)) {
            return;
        }
        if (mcc == Mcc.UNKNOWN) {
            return;
        }

        // fetch the requested measurements from the api
        final List<Measurement> measurements = api.getMeasurements(ministryId, mcc, period);
        if (measurements != null) {
            saveMeasurements(measurements, guid, ministryId, mcc, period, true);
        }
    }

    private void syncDirtyMeasurements(@NonNull final GmaApiClient api, @NonNull final Intent intent)
            throws ApiException {
        // get parameters for sync from the intent & sanitize
        final String guid = intent.getStringExtra(EXTRA_GUID);
        final String ministryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
        final Mcc mcc = Mcc.fromRaw(intent.getStringExtra(Constants.ARG_MCC));
        final String rawPeriod = intent.getStringExtra(Constants.ARG_PERIOD);
        final YearMonth period = rawPeriod != null ? YearMonth.parse(rawPeriod) : YearMonth.now();
        if (guid == null) {
            return;
        }
        if (ministryId == null || ministryId.equals(Ministry.INVALID_ID)) {
            return;
        }
        if (mcc == Mcc.UNKNOWN) {
            return;
        }

        // synchronize on updating this ministry, mcc & period
        synchronized (ThreadUtils.getLock(mLocksDirtyMeasurements, new GenericKey(guid, ministryId, mcc, period))) {
            // get the current assignment
            final Assignment assignment = mDao.find(Assignment.class, guid, ministryId);
            if (assignment == null) {
                return;
            }

            // check for dirty measurements to update
            List<MeasurementValue> dirty = getDirtyMeasurements(assignment, mcc, period);
            if (dirty.isEmpty()) {
                return;
            }

            // update measurements from server before submitting updates
            List<Measurement> measurements = api.getMeasurements(ministryId, mcc, period);
            if (measurements == null) {
                return;
            }
            saveMeasurements(measurements, guid, ministryId, mcc, period, false);

            // refresh the list of dirty measurements
            dirty = getDirtyMeasurements(assignment, mcc, period);
            if (dirty.isEmpty()) {
                return;
            }

            // populate dirty measurement objects with the MeasurementType and Assignment
            final Map<String, MeasurementType> types =
                    Maps.uniqueIndex(mDao.get(MeasurementType.class), MeasurementType.FUNCTION_PERMLINK);
            for (final MeasurementValue value : dirty) {
                value.setType(types.get(value.getPermLinkStub()));
                if (value instanceof PersonalMeasurement) {
                    ((PersonalMeasurement) value).setAssignment(assignment);
                }
            }

            // sync the measurements
            final boolean synced = api.updateMeasurements(dirty.toArray(new MeasurementValue[dirty.size()]));

            // update database for any synced measurements
            if (synced) {
                // clear dirty values for the measurements updated
                for (final MeasurementValue value : dirty) {
                    mDao.updateMeasurementValueDelta(value, 0 - value.getDelta());

                    // Force a details sync for this updated measurement
                    syncMeasurementDetails(this, guid, ministryId, mcc, value.getPermLinkStub(), period, true);
                }

                // update the measurements one last time
                measurements = api.getMeasurements(ministryId, mcc, period);
                if (measurements != null) {
                    saveMeasurements(measurements, guid, ministryId, mcc, period, true);
                }
            }
        }
    }

    @NonNull
    private List<MeasurementValue> getDirtyMeasurements(@NonNull final Assignment assignment, @NonNull final Mcc mcc,
                                                        @NonNull final YearMonth period) {
        final List<MeasurementValue> dirty = new ArrayList<>();
        if (assignment.can(UPDATE_PERSONAL_MEASUREMENTS)) {
            dirty.addAll(mDao.get(PersonalMeasurement.class, Contract.PersonalMeasurement.SQL_WHERE_DIRTY + " AND " +
                                          Contract.PersonalMeasurement.SQL_WHERE_GUID_MINISTRY_MCC_PERIOD,
                                  bindValues(assignment.getGuid(), assignment.getMinistryId(), mcc, period)));
        }
        if (assignment.can(UPDATE_MINISTRY_MEASUREMENTS)) {
            dirty.addAll(mDao.get(MinistryMeasurement.class, Contract.MinistryMeasurement.SQL_WHERE_DIRTY + " AND " +
                                          Contract.MinistryMeasurement.SQL_WHERE_MINISTRY_MCC_PERIOD,
                                  bindValues(assignment.getMinistryId(), mcc, period)));
        }
        return dirty;
    }

    private void saveMeasurements(@NonNull final List<Measurement> measurements, @NonNull final String guid,
                                  @NonNull final String ministryId, @NonNull final Mcc mcc,
                                  @NonNull final YearMonth period, final boolean sendBroadcasts) {
        final List<String> updatedTypes = new ArrayList<>();
        final Set<String> updatedValues = new HashSet<>();

        // update measurement data in the database
        for (final Measurement measurement : measurements) {
            // update the measurement type data
            final MeasurementType type = measurement.getType();
            if (type != null) {
                type.setLastSynced();
                mDao.updateOrInsert(type, PROJECTION_SYNC_MEASUREMENTS_TYPE);
                updatedTypes.add(type.getPermLinkStub());
            }

            // update ministry measurements
            final MinistryMeasurement ministryMeasurement = measurement.getMinistryMeasurement();
            if (ministryMeasurement != null) {
                ministryMeasurement.setLastSynced();
                mDao.updateOrInsert(ministryMeasurement, PROJECTION_SYNC_MEASUREMENTS_MINISTRY_MEASUREMENT);
                updatedValues.add(ministryMeasurement.getPermLinkStub());
            }

            // update personal measurements
            final PersonalMeasurement personalMeasurement = measurement.getPersonalMeasurement();
            if (personalMeasurement != null) {
                personalMeasurement.setLastSynced();
                mDao.updateOrInsert(personalMeasurement, PROJECTION_SYNC_MEASUREMENTS_PERSONAL_MEASUREMENT);
                updatedValues.add(personalMeasurement.getPermLinkStub());
            }
        }

        if (sendBroadcasts) {
            if (!updatedTypes.isEmpty()) {
                broadcastManager.sendBroadcast(BroadcastUtils.updateMeasurementTypesBroadcast(
                        updatedTypes.toArray(new String[updatedTypes.size()])));
            }

            if (!updatedValues.isEmpty()) {
                broadcastManager.sendBroadcast(BroadcastUtils.updateMeasurementValuesBroadcast(
                        ministryId, mcc, period, guid, updatedValues.toArray(new String[updatedValues.size()])));
            }
        }
    }

    private void syncMeasurementDetails(@NonNull final GmaApiClient api, @NonNull final Intent intent)
            throws ApiException {
        // get parameters for sync from the intent & sanitize
        final String guid = intent.getStringExtra(EXTRA_GUID);
        final String ministryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
        final Mcc mcc = Mcc.fromRaw(intent.getStringExtra(Constants.ARG_MCC));
        final String permLink = intent.getStringExtra(EXTRA_PERMLINK);
        final String rawPeriod = intent.getStringExtra(Constants.ARG_PERIOD);
        final YearMonth period = rawPeriod != null ? YearMonth.parse(rawPeriod) : YearMonth.now();
        boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
        if (guid == null) {
            return;
        }
        if (ministryId == null || ministryId.equals(Ministry.INVALID_ID)) {
            return;
        }
        if (mcc == Mcc.UNKNOWN) {
            return;
        }
        if (permLink == null) {
            return;
        }

        // is the cached data stale?
        boolean stale = false;
        if (!force) {
            // calculate the stale duration for the requested period
            final long staleDuration;
            final int comparison = period.compareTo(YearMonth.now().minusMonths(1));
            if (comparison > 0) {
                staleDuration = STALE_DURATION_MEASUREMENT_DETAILS_CURRENT;
            } else if (comparison == 0) {
                staleDuration = STALE_DURATION_MEASUREMENT_DETAILS_PREVIOUS;
            } else {
                staleDuration = STALE_DURATION_MEASUREMENT_DETAILS_OLD;
            }

            // check if the currently cached measurement details are stale unless we are already planning on syncing
            final MeasurementDetails details =
                    mDao.find(MeasurementDetails.class, guid, ministryId, mcc, permLink, period);
            stale = details == null || details.getVersion() < GMA_API_VERSION ||
                    System.currentTimeMillis() - details.getLastSynced() > staleDuration;
        }

        if (force || stale) {
            // fetch details from the API
            final MeasurementDetails details = api.getMeasurementDetails(ministryId, mcc, permLink, period);
            if (details != null) {
                details.setLastSynced();
                mDao.updateOrInsert(details, new String[] {Contract.MeasurementDetails.COLUMN_JSON,
                        Contract.MeasurementDetails.COLUMN_LAST_SYNCED});

                // broadcast the measurement details sync
                broadcastManager.sendBroadcast(
                        BroadcastUtils.updateMeasurementDetailsBroadcast(ministryId, mcc, period, guid, permLink));
            }
        }
    }

    /* END Measurements sync */
}
