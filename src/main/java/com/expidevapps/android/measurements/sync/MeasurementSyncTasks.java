package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.BuildConfig.GMA_API_VERSION;
import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.ARG_PERIOD;
import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERMLINK;
import static com.expidevapps.android.measurements.sync.Constants.DAY_IN_MS;
import static com.expidevapps.android.measurements.sync.Constants.HOUR_IN_MS;
import static com.expidevapps.android.measurements.sync.Constants.WEEK_IN_MS;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Measurement;
import com.expidevapps.android.measurements.model.MeasurementDetails;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.MinistryMeasurement;
import com.expidevapps.android.measurements.model.PersonalMeasurement;
import com.google.common.collect.Maps;

import org.ccci.gto.android.common.api.ApiException;
import org.joda.time.YearMonth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class MeasurementSyncTasks extends BaseSyncTasks {
    private static final String SYNC_TIME_MEASUREMENT_TYPES = "last_synced.measurement.types";
    private static final String SYNC_TIME_MEASUREMENTS = "last_synced.measurements";

    private static final int PREVIOUS_MONTHS = 2;
    private static final long STALE_DURATION_MEASUREMENT_TYPES = WEEK_IN_MS;
    private static final long STALE_DURATION_MEASUREMENTS_CURRENT = 6 * HOUR_IN_MS;
    private static final long STALE_DURATION_MEASUREMENTS_PREVIOUS = 2 * DAY_IN_MS;
    private static final long STALE_DURATION_MEASUREMENTS_OLD = 2 * WEEK_IN_MS;
    private static final long STALE_DURATION_MEASUREMENT_DETAILS_CURRENT = DAY_IN_MS;
    private static final long STALE_DURATION_MEASUREMENT_DETAILS_PREVIOUS = 2 * DAY_IN_MS;
    private static final long STALE_DURATION_MEASUREMENT_DETAILS_OLD = 7 * DAY_IN_MS;

    private static final String[] PROJECTION_SYNC_MEASUREMENT_TYPES_TYPE =
            {Contract.MeasurementType.COLUMN_NAME, Contract.MeasurementType.COLUMN_DESCRIPTION,
                    Contract.MeasurementType.COLUMN_SECTION, Contract.MeasurementType.COLUMN_COLUMN,
                    Contract.MeasurementType.COLUMN_SORT_ORDER, Contract.MeasurementType.COLUMN_PERSONAL_ID,
                    Contract.MeasurementType.COLUMN_LOCAL_ID, Contract.MeasurementType.COLUMN_TOTAL_ID,
                    Contract.MeasurementType.COLUMN_LAST_SYNCED};
    private static final String[] PROJECTION_SYNC_MEASUREMENTS_TYPE =
            {Contract.MeasurementType.COLUMN_NAME, Contract.MeasurementType.COLUMN_DESCRIPTION,
                    Contract.MeasurementType.COLUMN_SECTION, Contract.MeasurementType.COLUMN_COLUMN,
                    Contract.MeasurementType.COLUMN_SORT_ORDER, Contract.MeasurementType.COLUMN_LOCAL_ID,
                    Contract.MeasurementType.COLUMN_PERSONAL_ID, Contract.MeasurementType.COLUMN_TOTAL_ID,
                    Contract.MeasurementType.COLUMN_LAST_SYNCED};
    private static final String[] PROJECTION_SYNC_MEASUREMENTS_MINISTRY_MEASUREMENT =
            {Contract.MinistryMeasurement.COLUMN_VALUE, Contract.MinistryMeasurement.COLUMN_LAST_SYNCED};
    private static final String[] PROJECTION_SYNC_MEASUREMENTS_PERSONAL_MEASUREMENT =
            {Contract.PersonalMeasurement.COLUMN_VALUE, Contract.PersonalMeasurement.COLUMN_LAST_SYNCED};


    static void syncMeasurementTypes(@NonNull final Context context, @NonNull final String guid,
                                     @NonNull final Bundle args) throws ApiException {
        final boolean force = isForced(args);

        final GmaDao dao = GmaDao.getInstance(context);
        if (force || System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_MEASUREMENT_TYPES) >
                STALE_DURATION_MEASUREMENT_TYPES) {
            final GmaApiClient api = GmaApiClient.getInstance(context, guid);
            final List<MeasurementType> types = api.getMeasurementTypes();
            if (types != null) {
                final List<String> updatedTypes = new ArrayList<>();

                // load all existing measurement types
                final Map<String, MeasurementType> existing = Maps.newHashMap(
                        Maps.uniqueIndex(dao.get(MeasurementType.class), MeasurementType.FUNCTION_PERMLINK));

                // update any returned measurement types
                for (final MeasurementType type : types) {
                    dao.updateOrInsert(type, PROJECTION_SYNC_MEASUREMENT_TYPES_TYPE);
                    existing.remove(type.getPermLinkStub());
                    updatedTypes.add(type.getPermLinkStub());
                }

                // remove any orphaned measurement types
                for (final MeasurementType type : existing.values()) {
                    dao.delete(type);
                    updatedTypes.add(type.getPermLinkStub());
                }

                // update the last sync time for measurement types
                dao.updateLastSyncTime(SYNC_TIME_MEASUREMENT_TYPES);

                // send broadcasts
                if (!updatedTypes.isEmpty()) {
                    final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                    broadcastManager.sendBroadcast(BroadcastUtils.updateMeasurementTypesBroadcast(
                            updatedTypes.toArray(new String[updatedTypes.size()])));
                }
            }
        }
    }

    static void syncMeasurements(@NonNull final Context context, @NonNull final String guid, @NonNull final Bundle args)
            throws ApiException {
        // short-circuit if this request is for an invalid ministry
        final String ministryId = args.getString(EXTRA_MINISTRY_ID);
        if (ministryId == null || ministryId.equals(Ministry.INVALID_ID)) {
            return;
        }

        // short-circuit if this request is for an invalid mcc
        final Mcc mcc = Mcc.fromRaw(args.getString(ARG_MCC));
        if (mcc == Mcc.UNKNOWN) {
            return;
        }

        // determine the period to sync (use the current period if one is not specified)
        final String rawPeriod = args.getString(ARG_PERIOD);
        final YearMonth period = rawPeriod != null ? YearMonth.parse(rawPeriod) : YearMonth.now();

        // short-circuit if we aren't forcing a sync and the data isn't stale
        final GmaDao dao = GmaDao.getInstance(context);
        if (!isForced(args)) {
            final long age = System.currentTimeMillis() -
                    dao.getLastSyncTime(SYNC_TIME_MEASUREMENTS, guid, ministryId, mcc, period);
            if (age < staleDuration(period, STALE_DURATION_MEASUREMENTS_CURRENT, STALE_DURATION_MEASUREMENTS_PREVIOUS,
                                    STALE_DURATION_MEASUREMENTS_OLD)) {
                return;
            }
        }

        // fetch the requested measurements from the api
        final GmaApiClient api = GmaApiClient.getInstance(context, guid);
        final List<Measurement> measurements = api.getMeasurements(ministryId, mcc, period);
        if (measurements != null) {
            saveMeasurements(context, guid, ministryId, mcc, period, measurements, true);
        }
    }

    static void syncMeasurementDetails(@NonNull final Context context, @NonNull final String guid,
                                       @NonNull final Bundle args) throws ApiException {
        // short-circuit if this request is for an invalid ministry
        final String ministryId = args.getString(EXTRA_MINISTRY_ID);
        if (ministryId == null || ministryId.equals(Ministry.INVALID_ID)) {
            return;
        }

        // short-circuit if this request is for an invalid mcc
        final Mcc mcc = Mcc.fromRaw(args.getString(ARG_MCC));
        if (mcc == Mcc.UNKNOWN) {
            return;
        }

        // short-circuit if this request is for an invalid perm_link
        final String permLink = args.getString(EXTRA_PERMLINK);
        if (permLink == null) {
            return;
        }

        // determine the period to sync (use the current period if one is not specified)
        final String rawPeriod = args.getString(ARG_PERIOD);
        final YearMonth period = rawPeriod != null ? YearMonth.parse(rawPeriod) : YearMonth.now();

        // short-circuit if we aren't forcing a sync and the data isn't invalid or stale
        final GmaDao dao = GmaDao.getInstance(context);
        if (!isForced(args)) {
            final MeasurementDetails details =
                    dao.find(MeasurementDetails.class, guid, ministryId, mcc, permLink, period);
            if (details != null && details.getVersion() == GMA_API_VERSION) {
                final long age = System.currentTimeMillis() - details.getLastSynced();
                if (age < staleDuration(period, STALE_DURATION_MEASUREMENT_DETAILS_CURRENT,
                                        STALE_DURATION_MEASUREMENT_DETAILS_PREVIOUS,
                                        STALE_DURATION_MEASUREMENT_DETAILS_OLD)) {
                    return;
                }
            }
        }

        // fetch details from the API
        final GmaApiClient api = GmaApiClient.getInstance(context, guid);
        final MeasurementDetails details = api.getMeasurementDetails(ministryId, mcc, permLink, period);
        if (details != null) {
            details.setLastSynced();
            dao.updateOrInsert(details, new String[] {Contract.MeasurementDetails.COLUMN_JSON,
                    Contract.MeasurementDetails.COLUMN_LAST_SYNCED});

            // broadcast the measurement details sync
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            broadcastManager.sendBroadcast(
                    BroadcastUtils.updateMeasurementDetailsBroadcast(ministryId, mcc, period, guid, permLink));
        }
    }

    static void saveMeasurements(@NonNull final Context context, @NonNull final String guid,
                                 @NonNull final String ministryId, @NonNull final Mcc mcc,
                                 @NonNull final YearMonth period, @NonNull final List<Measurement> measurements,
                                 final boolean sendBroadcasts) {
        // short-circuit if we don't have any measurements to save
        if (measurements.isEmpty()) {
            return;
        }

        final List<String> updatedTypes = new ArrayList<>();
        final Set<String> updatedValues = new HashSet<>();

        // update measurement data in the database
        final GmaDao dao = GmaDao.getInstance(context);
        for (final Measurement measurement : measurements) {
            // update the measurement type data
            final MeasurementType type = measurement.getType();
            if (type != null) {
                type.setLastSynced();
                dao.updateOrInsert(type, PROJECTION_SYNC_MEASUREMENTS_TYPE);
                updatedTypes.add(type.getPermLinkStub());
            }

            // update ministry measurements
            final MinistryMeasurement ministryMeasurement = measurement.getMinistryMeasurement();
            if (ministryMeasurement != null) {
                ministryMeasurement.setLastSynced();
                dao.updateOrInsert(ministryMeasurement, PROJECTION_SYNC_MEASUREMENTS_MINISTRY_MEASUREMENT);
                updatedValues.add(ministryMeasurement.getPermLinkStub());
            }

            // update personal measurements
            final PersonalMeasurement personalMeasurement = measurement.getPersonalMeasurement();
            if (personalMeasurement != null) {
                personalMeasurement.setLastSynced();
                dao.updateOrInsert(personalMeasurement, PROJECTION_SYNC_MEASUREMENTS_PERSONAL_MEASUREMENT);
                updatedValues.add(personalMeasurement.getPermLinkStub());
            }
        }

        // mark measurements for this period as synced
        dao.updateLastSyncTime(SYNC_TIME_MEASUREMENTS, guid, ministryId, mcc, period);

        // send broadcasts if requested
        if (sendBroadcasts) {
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
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

    private static long staleDuration(@NonNull final YearMonth period, final long current, final long previous,
                                      final long old) {
        final YearMonth now = YearMonth.now();
        if (period.equals(now) || period.isAfter(now)) {
            return current;
        } else if (period.isBefore(now.minusMonths(PREVIOUS_MONTHS))) {
            return old;
        } else {
            return previous;
        }
    }
}
