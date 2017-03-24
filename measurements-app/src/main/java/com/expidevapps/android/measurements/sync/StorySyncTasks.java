package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static com.expidevapps.android.measurements.api.GmaApiClient.DEFAULT_STORIES_PER_PAGE;
import static com.expidevapps.android.measurements.db.Contract.Story.SQL_WHERE_DIRTY;
import static com.expidevapps.android.measurements.db.Contract.Story.SQL_WHERE_HAS_PENDING_IMAGE;
import static com.expidevapps.android.measurements.db.Contract.Story.SQL_WHERE_NEW;
import static com.expidevapps.android.measurements.sync.service.StoriesManager.PROJECTION_GET_STORY_DATA;
import static org.ccci.gto.android.common.db.Expression.not;

import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.sync.service.StoriesManager;
import com.google.common.primitives.Longs;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.util.ArrayUtils;
import org.ccci.gto.android.common.util.BundleCompat;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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
        final int pageSize = args.getInt(EXTRA_PAGE_SIZE, DEFAULT_STORIES_PER_PAGE);

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

    static boolean syncDirtyStories(@NonNull final Context context, @NonNull final String guid,
                                    @NonNull final Bundle args, @NonNull final SyncResult result) throws ApiException {
        final Collection<Long> updated = new ArrayList<>();
        synchronized (LOCK_DIRTY_STORIES) {
            final GmaDao dao = GmaDao.getInstance(context);
            final GmaApiClient api = getApi(context, guid);

            // process all stories that are new or dirty
            for (final Story story : dao.get(Query.select(Story.class).where(SQL_WHERE_NEW.or(SQL_WHERE_DIRTY)))) {
                try {
                    if (story.isNew()) {
                        // try creating the story
                        final Story newStory = api.createStory(story);

                        // update id of church
                        if (newStory != null) {
                            final Transaction tx = dao.newTransaction();
                            try {
                                tx.begin();

                                // copy pending image to new Story
                                newStory.setPendingImage(story.getPendingImage());
                                dao.delete(story);
                                dao.updateOrInsert(newStory, ArrayUtils
                                        .merge(String.class, new String[] {Contract.Story.COLUMN_PENDING_IMAGE},
                                               PROJECTION_GET_STORY_DATA));

                                tx.setSuccessful();
                            } finally {
                                tx.end();
                            }

                            // add story to list of broadcasts
                            updated.add(story.getId());
                            updated.add(newStory.getId());

                            // increment the insert counter
                            result.stats.numInserts++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    }
                } catch (final JSONException ignored) {
                    // this shouldn't happen when generating json
                }
            }

            // process any pending image uploads (for non-new stories)
            for (final Story story : dao
                    .get(Query.select(Story.class).where(SQL_WHERE_HAS_PENDING_IMAGE.and(not(SQL_WHERE_NEW))))) {
                final File image = story.getPendingImage();
                if (image != null && image.isFile()) {
                    final Story updatedStory = api.storeImage(story.getId(), image);

                    // did we store the image successfully?
                    if (updatedStory != null) {
                        // update image for this story
                        dao.updateOrInsert(updatedStory, Contract.Story.COLUMN_IMAGE,
                                           Contract.Story.COLUMN_PENDING_IMAGE);

                        // delete the local pending image
                        image.delete();

                        // add story to list of broadcasts
                        updated.add(updatedStory.getId());
                    }
                }
            }
        }

        // send broadcast for updated stories
        if (updated.size() > 0) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(BroadcastUtils.updateStoriesBroadcast(Longs.toArray(updated)));
        }

        return true;
    }
}
