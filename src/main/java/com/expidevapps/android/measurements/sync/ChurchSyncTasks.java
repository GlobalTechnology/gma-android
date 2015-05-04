package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Ministry;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.Transaction;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChurchSyncTasks extends BaseSyncTasks {
    private static final String SYNC_TIME_CHURCHES = "last_synced.churches";

    private static final long STALE_DURATION_CHURCHES = Constants.DAY_IN_MS;

    private static String[] PROJECTION_GET_CHURCHES_DATA = {Contract.Church.COLUMN_MINISTRY_ID,
            Contract.Church.COLUMN_NAME, Contract.Church.COLUMN_CONTACT_NAME,
            Contract.Church.COLUMN_CONTACT_EMAIL, Contract.Church.COLUMN_LATITUDE,
            Contract.Church.COLUMN_LONGITUDE, Contract.Church.COLUMN_SIZE,
            Contract.Church.COLUMN_DEVELOPMENT, Contract.Church.COLUMN_SECURITY};

    static boolean syncChurches(@NonNull final Context context, @NonNull final String guid, @NonNull final Bundle args)
            throws ApiException {
        // short-circuit if there isn't a valid ministry specified
        final String ministryId = args.getString(EXTRA_MINISTRY_ID);
        if (ministryId == null || ministryId.equals(Ministry.INVALID_ID)) {
            return false;
        }

        // short-circuit if we aren't forcing a sync and the data isn't stale
        final boolean force = isForced(args);
        final GmaDao dao = GmaDao.getInstance(context);
        if (!force && System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_CHURCHES, ministryId) <
                STALE_DURATION_CHURCHES) {
            return true;
        }

        // short-circuit if we fail to fetch churches
        final GmaApiClient api = GmaApiClient.getInstance(context, guid);
        final List<Church> churches = api.getChurches(ministryId);
        if (churches == null) {
            return false;
        }

        // update churches in the database
        final Transaction tx = dao.newTransaction();
        try {
            tx.beginTransactionNonExclusive();

            // load current churches
            final LongSparseArray<Church> current = new LongSparseArray<>();
            for (final Church church : dao
                    .get(Church.class, Contract.Church.SQL_WHERE_MINISTRY, bindValues(ministryId))) {
                current.put(church.getId(), church);
            }

            // process all fetched churches
            long[] ids = new long[current.size() + churches.size()];
            int j = 0;
            for (final Church church : churches) {
                final long id = church.getId();
                final Church existing = current.get(id);

                // persist church in database (if it doesn't exist or (isn't new and isn't dirty))
                if (existing == null || (!existing.isNew() && !existing.isDirty())) {
                    church.setLastSynced(new Date());
                    dao.updateOrInsert(church, PROJECTION_GET_CHURCHES_DATA);

                    // mark this id as having been changed
                    ids[j++] = id;
                }

                // remove this church from the list of churches
                current.remove(id);
            }

            // delete any remaining churches that weren't returned from the API
            for (int i = 0; i < current.size(); i++) {
                final Church church = current.valueAt(i);
                // only delete the church if it isn't new
                if (!church.isNew()) {
                    dao.delete(church);

                    // mark these ids as being updated as well
                    ids[j++] = church.getId();
                }
            }

            // update the sync time in the database
            dao.updateLastSyncTime(SYNC_TIME_CHURCHES, ministryId);

            // mark transaction successful
            tx.setSuccessful();

            // send broadcasts that data has been updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            broadcastManager.sendBroadcast(BroadcastUtils.updateChurchesBroadcast(ministryId, Arrays.copyOf(ids, j)));

            // return success
            return true;
        } finally {
            tx.end();
        }
    }

}
