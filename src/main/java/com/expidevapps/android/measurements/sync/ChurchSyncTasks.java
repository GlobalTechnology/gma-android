package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Ministry;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChurchSyncTasks extends BaseSyncTasks {
    private static final String SYNC_TIME_CHURCHES = "last_synced.churches";

    private static final long STALE_DURATION_CHURCHES = DAY_IN_MS;

    private static final Object LOCK_DIRTY_CHURCHES = new Object();

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

    @SuppressWarnings("AccessToStaticFieldLockedOnInstance")
    static void syncDirtyChurches(@NonNull final Context context, @NonNull final String guid,
                                  @NonNull final Bundle args, @NonNull final SyncResult result) throws ApiException {
        synchronized (LOCK_DIRTY_CHURCHES) {
            // short-circuit if there aren't any churches to process
            final GmaDao dao = GmaDao.getInstance(context);
            final List<Church> churches = dao.get(Church.class, Contract.Church.SQL_WHERE_DIRTY, null);
            if (churches.isEmpty()) {
                return;
            }

            // ministry_id => church_id
            final Multimap<String, Long> broadcasts = HashMultimap.create();

            // process all churches that are dirty
            final GmaApiClient api = GmaApiClient.getInstance(context, guid);
            for (final Church church : churches) {
                try {
                    if (church.isNew()) {
                        // try creating the church
                        final Church newChurch = api.createChurch(church);

                        // update id of church
                        if (newChurch != null) {
                            dao.delete(church);
                            newChurch.setLastSynced(new Date());
                            dao.updateOrInsert(newChurch, PROJECTION_GET_CHURCHES_DATA);

                            // add church to list of broadcasts
                            broadcasts.put(church.getMinistryId(), church.getId());
                            broadcasts.put(church.getMinistryId(), newChurch.getId());

                            // increment the insert counter
                            result.stats.numInserts++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    } else if (church.isDirty()) {
                        // generate dirty JSON
                        final JSONObject json = church.dirtyToJson();

                        // update the church
                        final boolean success = api.updateChurch(church.getId(), json);

                        // was successful update?
                        if (success) {
                            // clear dirty attributes
                            church.setDirty(null);
                            dao.update(church, new String[] {Contract.Church.COLUMN_DIRTY});

                            // add church to list of broadcasts
                            broadcasts.put(church.getMinistryId(), church.getId());

                            // increment update counter
                            result.stats.numUpdates++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    }
                } catch (final JSONException ignored) {
                    // this shouldn't happen when generating json
                }
            }

            // send broadcasts for each ministryId with churches that were changed
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            for (final String ministryId : broadcasts.keySet()) {
                broadcastManager.sendBroadcast(
                        BroadcastUtils.updateChurchesBroadcast(ministryId, Longs.toArray(broadcasts.get(ministryId))));
            }
        }
    }
}
