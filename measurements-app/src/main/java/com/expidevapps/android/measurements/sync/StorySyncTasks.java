package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;

import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.sync.service.StoriesManager;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.util.BundleCompat;

import java.util.List;

@WorkerThread
class StorySyncTasks extends BaseSyncTasks {
    private static final String SYNC_TIME_STORIES = "last_synced.stories";

    static final String EXTRA_SELFONLY = StorySyncTasks.class.getName() + ".EXTRA_SELFONLY";

    private static final long STALE_DURATION_STORIES = DAY_IN_MS;

    private static final Object LOCK_DIRTY_STORIES = new Object();

    static boolean syncStories(@NonNull final Context context, @NonNull final String guid, @NonNull final Bundle args,
                               @NonNull final SyncResult result) throws ApiException {
        // short-circuit if there isn't a valid ministry specified
        final String ministryId = BundleCompat.getString(args, EXTRA_MINISTRY_ID, Ministry.INVALID_ID);
        if (ministryId.equals(Ministry.INVALID_ID)) {
            return false;
        }

        // load remaining filters
        final boolean selfOnly = args.getBoolean(EXTRA_SELFONLY, false);

        // short-circuit if we aren't forcing a sync and the data isn't stale
        final boolean force = isForced(args);
        final GmaDao dao = GmaDao.getInstance(context);
        if (!force && System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_STORIES, ministryId, selfOnly) <
                STALE_DURATION_STORIES) {
            return true;
        }

        // short-circuit if we fail to fetch stories
        final GmaApiClient api = getApi(context, guid);
        final List<Story> stories = api.getStories(ministryId, selfOnly, 1, 20);
        if (stories == null) {
            return false;
        }

        // store the stories in the StoriesManager
        StoriesManager.getInstance(context).updateStoriesFromApi(stories);

        // update the sync time in the database
        dao.updateLastSyncTime(SYNC_TIME_STORIES, ministryId, selfOnly);

        return true;
    }
}
