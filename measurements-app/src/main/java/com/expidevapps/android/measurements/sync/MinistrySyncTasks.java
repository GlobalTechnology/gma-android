package com.expidevapps.android.measurements.sync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;

import org.ccci.gto.android.common.api.ApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MinistrySyncTasks extends BaseSyncTasks {
    private static final String SYNC_TIME_MINISTRIES = "last_synced.ministries";

    private static final long STALE_DURATION_MINISTRIES = 7 * DAY_IN_MS;

    static void syncMinistries(@NonNull final Context context, @NonNull final String guid, @NonNull final Bundle args)
            throws ApiException {
        final boolean force = isForced(args);

        // only sync if being forced or the data is stale
        final GmaDao dao = GmaDao.getInstance(context);
        if (force ||
                System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_MINISTRIES) > STALE_DURATION_MINISTRIES) {
            // refresh the list of ministries if the load is being forced
            final GmaApiClient api = getApi(context, guid);
            final List<Ministry> ministries = api.getMinistries();

            // only update the saved ministries if we received any back
            if (ministries != null) {
                // load current ministries
                final Map<String, Ministry> current = new HashMap<>();
                for (final Ministry ministry : dao.get(Ministry.class)) {
                    current.put(ministry.getMinistryId(), ministry);
                }

                // update all the ministry names
                for (final Ministry ministry : ministries) {
                    // this is only a very minimal update, so don't log last synced for new ministries
                    ministry.setLastSynced(0);
                    dao.updateOrInsert(ministry, new String[] {Contract.Ministry.COLUMN_NAME});

                    // remove from the list of current ministries
                    current.remove(ministry.getMinistryId());
                }

                // remove any current ministries we didn't see, we can do this because we just retrieved a complete list
                for (final Ministry ministry : current.values()) {
                    dao.delete(ministry);
                }

                // update the last sync time for ministries
                dao.updateLastSyncTime(SYNC_TIME_MINISTRIES);

                // send broadcasts that data has been updated in the database
                final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                broadcastManager.sendBroadcast(BroadcastUtils.updateMinistriesBroadcast());
            }
        }
    }

}
