package com.expidevapps.android.measurements.sync;

import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.content.SyncResult;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.UserPreference;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.Transaction;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WorkerThread
public class UserPreferenceSyncTasks extends BaseSyncTasks {
    private static final Logger LOG = LoggerFactory.getLogger(UserPreferenceSyncTasks.class);

    static final String EXTRA_PREFERENCES = UserPreferenceSyncTasks.class.getName() + ".EXTRA_PREFERENCES";

    private static final String SYNC_TIME_PREFERENCES = "last_synced.preferences";

    private static final long STALE_DURATION_PREFERENCES = DAY_IN_MS;

    static void savePreferences(@NonNull final Context context, @NonNull final String guid, @NonNull final Bundle args,
                                @NonNull final SyncResult result) {
        final String raw = args.getString(EXTRA_PREFERENCES);
        if (raw != null) {
            try {
                updateAllPreferences(context, guid, UserPreference.mapFromJson(new JSONObject(raw), guid));
            } catch (final JSONException e) {
                result.stats.numParseExceptions++;
            }
        }
    }

    static boolean syncPreferences(@NonNull final Context context, @NonNull final String guid,
                                   @NonNull final Bundle args, @NonNull final SyncResult result) throws ApiException {
        // short-circuit if we aren't forcing a sync and the data isn't stale
        final boolean force = isForced(args);
        final GmaDao dao = GmaDao.getInstance(context);
        if (!force && System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_PREFERENCES, guid) <
                STALE_DURATION_PREFERENCES) {
            return true;
        }

        // fetch raw data from API & process it
        final GmaApiClient api = GmaApiClient.getInstance(context, guid);
        final Map<String, UserPreference> prefs = api.getPreferences();
        return prefs != null && updateAllPreferences(context, guid, prefs);
    }

    static boolean syncDirtyPreferences(@NonNull final Context context, @NonNull final String guid,
                                        @NonNull final Bundle args, @NonNull final SyncResult result)
            throws ApiException {
        final GmaDao dao = GmaDao.getInstance(context);

        final Transaction tx = dao.newTransaction();
        try {
            tx.beginTransactionNonExclusive();

            // load any dirty preferences
            final List<UserPreference> dirty =
                    dao.get(UserPreference.class, Contract.UserPreference.SQL_WHERE_GUID_AND_NEW_OR_DIRTY,
                            bindValues(guid));
            if (dirty.size() > 0) {
                // send prefs to the api
                final GmaApiClient api = GmaApiClient.getInstance(context, guid);
                final Map<String, UserPreference> prefs =
                        api.updatePreferences(dirty.toArray(new UserPreference[dirty.size()]));
                if (prefs != null) {
                    final List<String> updated = new ArrayList<>(dirty.size());

                    // mark all dirty prefs as clean
                    for (final UserPreference pref : dirty) {
                        updated.add(pref.getName());
                        pref.setNew(false);
                        pref.setDirty(null);
                        dao.updateOrInsert(pref, new String[] {Contract.UserPreference.COLUMN_NEW,
                                Contract.UserPreference.COLUMN_DIRTY});
                    }

                    // send broadcasts for updated preferences
                    final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                    broadcastManager.sendBroadcast(BroadcastUtils.updatePreferencesBroadcast(guid, updated.toArray(
                            new String[updated.size()])));

                    // update the local preferences based on the response from the API
                    updateAllPreferences(context, guid, prefs);
                }
            }

            tx.setTransactionSuccessful();

            return true;
        } catch (final JSONException e) {
            result.stats.numParseExceptions++;
            return false;
        } finally {
            tx.close();
        }
    }

    private static final String[] PROJECTION_PREFERENCE = {Contract.UserPreference.COLUMN_VALUE};

    private static boolean updateAllPreferences(@NonNull final Context context, @NonNull final String guid,
                                                @NonNull final Map<String, UserPreference> prefs) {
        // wrap entire update in a transaction
        final GmaDao dao = GmaDao.getInstance(context);
        final Transaction tx = dao.newTransaction();
        try {
            tx.beginTransactionNonExclusive();

            // load pre-existing preferences (name => UserPreference)
            final Map<String, UserPreference> existing = new HashMap<>();
            for (final UserPreference pref : dao.get(UserPreference.class, Contract.UserPreference.SQL_WHERE_GUID,
                                                     bindValues(guid))) {
                existing.put(pref.getName(), pref);
            }

            // update preferences in local database
            final List<String> updated = new ArrayList<>();
            for (final UserPreference pref : prefs.values()) {
                // only update preferences that don't exist or aren't modified
                final UserPreference current = existing.get(pref.getName());
                if (current == null || (!current.isNew() && !current.isDirty())) {
                    updated.add(pref.getName());
                    dao.updateOrInsert(pref, PROJECTION_PREFERENCE);
                }

                // remove it from the list of existing preferences
                existing.remove(pref.getName());
            }

            // delete any remaining non-new non-dirty preferences, we don't have them anymore
            for (final UserPreference pref : existing.values()) {
                if (!pref.isNew() && !pref.isDirty()) {
                    updated.add(pref.getName());
                    dao.delete(pref);
                }
            }

            // update the sync time
            dao.updateLastSyncTime(SYNC_TIME_PREFERENCES, guid);

            tx.setSuccessful();

            // send broadcasts for updated data
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            broadcastManager.sendBroadcast(
                    BroadcastUtils.updatePreferencesBroadcast(guid, updated.toArray(new String[updated.size()])));

            return true;
        } catch (final SQLException e) {
            LOG.debug("error updating preferences", e);
            return false;
        } finally {
            tx.end();
        }
    }
}
