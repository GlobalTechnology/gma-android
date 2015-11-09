package com.expidevapps.android.measurements.sync.service;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;

import com.expidevapps.android.measurements.BuildConfig;
import com.expidevapps.android.measurements.Constants;
import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.PagedList;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.sync.BroadcastUtils;
import com.google.common.primitives.Longs;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StoriesManager {
    public static String[] PROJECTION_GET_STORY_DATA =
            {Contract.Story.COLUMN_TITLE, Contract.Story.COLUMN_CONTENT, Contract.Story.COLUMN_IMAGE,
                    Contract.Story.COLUMN_MINISTRY_ID, Contract.Story.COLUMN_MCC, Contract.Story.COLUMN_LONGITUDE,
                    Contract.Story.COLUMN_LATITUDE, Contract.Story.COLUMN_PRIVACY, Contract.Story.COLUMN_STATE,
                    Contract.Story.COLUMN_CREATED_BY, Contract.Story.COLUMN_CREATED};

    @NonNull
    private final Context mContext;
    @NonNull
    private final GmaDao mDao;

    @Nullable
    private static StoriesManager INSTANCE;

    private StoriesManager(@NonNull final Context context) {
        mContext = context;
        mDao = GmaDao.getInstance(mContext);
    }

    public static StoriesManager getInstance(@NonNull final Context context) {
        synchronized (StoriesManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new StoriesManager(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    private GmaApiClient getApi(@NonNull final String guid) {
        return GmaApiClient.getInstance(mContext, BuildConfig.GMA_API_BASE_URI, BuildConfig.GMA_API_VERSION,
                                        Constants.MEASUREMENTS_SOURCE, guid);
    }

    @Nullable
    @WorkerThread
    public PagedList<Story> fetchStories(@NonNull final String guid, @NonNull final String ministryId,
                                         @Nullable final Bundle filters, final int page, final int pageSize)
            throws ApiException {
        // fetch & process the requested stories
        final PagedList<Story> stories = getApi(guid).getStories(ministryId, filters, page, pageSize);
        if (stories != null) {
            updateStoriesFromApi(stories);
        }

        // return the retrieved stories
        return stories;
    }

    @WorkerThread
    public void updateStoriesFromApi(@NonNull final List<Story> stories) {
        final Collection<Long> updated = new ArrayList<>();

        // iterate over all the provided stories
        for (final Story story : stories) {
            updated.add(story.getId());
            final Transaction tx = mDao.newTransaction();
            try {
                tx.beginTransactionNonExclusive();

                // only update/insert stories that don't exist, aren't new and aren't dirty
                final Story existing = mDao.refresh(story);
                if (existing == null) {
                    mDao.insert(story);
                } else if (!existing.isNew() && !existing.isDirty()) {
                    mDao.update(story, PROJECTION_GET_STORY_DATA);
                }

                tx.setTransactionSuccessful();
            } finally {
                tx.endTransaction();
            }
        }

        // broadcast that we updated stories
        LocalBroadcastManager.getInstance(mContext)
                .sendBroadcast(BroadcastUtils.updateStoriesBroadcast(Longs.toArray(updated)));
    }
}
