package com.expidevapps.android.measurements.sync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.google.common.collect.Maps;

import org.ccci.gto.android.common.api.ApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MeasurementSyncTasks extends BaseSyncTasks {
    private static final String SYNC_TIME_MEASUREMENT_TYPES = "last_synced.measurementTypes";

    private static final long STALE_DURATION_MEASUREMENT_TYPES = 7 * Constants.DAY_IN_MS;

    private static final String[] PROJECTION_SYNC_MEASUREMENT_TYPES_TYPE =
            {Contract.MeasurementType.COLUMN_NAME, Contract.MeasurementType.COLUMN_DESCRIPTION,
                    Contract.MeasurementType.COLUMN_SECTION, Contract.MeasurementType.COLUMN_COLUMN,
                    Contract.MeasurementType.COLUMN_SORT_ORDER, Contract.MeasurementType.COLUMN_PERSONAL_ID,
                    Contract.MeasurementType.COLUMN_LOCAL_ID, Contract.MeasurementType.COLUMN_TOTAL_ID,
                    Contract.MeasurementType.COLUMN_LAST_SYNCED};

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
}
