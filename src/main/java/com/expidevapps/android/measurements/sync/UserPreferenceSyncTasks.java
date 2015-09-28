package com.expidevapps.android.measurements.sync;

import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.content.SyncResult;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.UserPreference;

import org.ccci.gto.android.common.db.Transaction;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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

            // column projections for updates
            final String[] PROJECTION_PREFERENCE = {Contract.UserPreference.COLUMN_VALUE};

            // update preferences in local database
            for (final UserPreference pref : prefs.values()) {
                // only update preferences that don't exist or aren't modified
                final UserPreference current = existing.get(pref.getName());
                if (current == null || (!current.isNew() && !current.isDirty())) {
                    dao.updateOrInsert(pref, PROJECTION_PREFERENCE);
                }

                // remove it from the list of existing preferences
                existing.remove(pref.getName());
            }

            // delete any remaining non-new preferences, we don't have them anymore
            for (final UserPreference pref : existing.values()) {
                if (!pref.isNew()) {
                    dao.delete(pref);
                }
            }

            // update the sync time
            dao.updateLastSyncTime(SYNC_TIME_PREFERENCES, guid);

            tx.setSuccessful();

            return true;
        } catch (final SQLException e) {
            LOG.debug("error updating preferences", e);
            return false;
        } finally {
            tx.end();
        }
    }
}
