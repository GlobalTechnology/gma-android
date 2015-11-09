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

    static final String EXTRA_FILTERS = StorySyncTasks.class.getName() + ".EXTRA_FILTERS";
    static final String EXTRA_PAGE = StorySyncTasks.class.getName() + ".EXTRA_PAGE";
    static final String EXTRA_PAGE_SIZE = StorySyncTasks.class.getName() + ".EXTRA_PAGE_SIZE";

    private static final long STALE_DURATION_STORIES = DAY_IN_MS;

    private static final Object LOCK_DIRTY_STORIES = new Object();

    static boolean syncStories(@NonNull final Context context, @NonNull final String guid, @NonNull final Bundle args,
                               @NonNull final SyncResult result) throws ApiException {
        // short-circuit if there isn't a valid ministry specified
        final String ministryId = BundleCompat.getString(args, EXTRA_MINISTRY_ID, Ministry.INVALID_ID);
        if (ministryId.equals(Ministry.INVALID_ID)) {
            return false;
        }

        // fetch extras from the args bundle
        final Bundle filters = args.getBundle(EXTRA_FILTERS);
        final int page = args.getInt(EXTRA_PAGE, 1);
        final int pageSize = args.getInt(EXTRA_PAGE_SIZE, 20);

        // short-circuit if we aren't forcing a sync and the data isn't stale
        //TODO: support filters in sync key
        final boolean force = isForced(args);
        final GmaDao dao = GmaDao.getInstance(context);
        final Object[] syncKey = new Object[] {SYNC_TIME_STORIES, ministryId, page, pageSize};
        if (!force && System.currentTimeMillis() - dao.getLastSyncTime(syncKey) < STALE_DURATION_STORIES) {
            return true;
        }

        // short-circuit if we fail to fetch stories
        final List<Story> stories =
                StoriesManager.getInstance(context).fetchStories(guid, ministryId, filters, page, pageSize);
        if (stories == null) {
            return false;
        }

        // update the sync time in the database
        dao.updateLastSyncTime(syncKey);

        return true;
    }
}
