package com.expidevapps.android.measurements.support.v4.fragment;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.INVALID_GUID;
import static com.expidevapps.android.measurements.api.GmaApiClient.DEFAULT_STORIES_PER_PAGE;
import static com.expidevapps.android.measurements.db.Contract.Story.FIELD_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.Story.FIELD_PRIVACY;
import static com.expidevapps.android.measurements.db.Contract.Story.FIELD_STATE;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_ORDER_BY;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_WHERE;
import static org.ccci.gto.android.common.db.Expression.bind;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.PagedList;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.model.Story.Privacy;
import com.expidevapps.android.measurements.model.Story.State;
import com.expidevapps.android.measurements.support.v7.adapter.StoryCursorRecyclerViewAdapter;
import com.expidevapps.android.measurements.sync.BroadcastUtils;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.expidevapps.android.measurements.sync.service.StoriesManager;

import org.ccci.gto.android.common.db.support.v4.content.DaoCursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.recyclerview.decorator.DividerItemDecoration;
import org.ccci.gto.android.common.recyclerview.listener.LoadMoreOnScrollListener;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.util.BundleCompat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class StoriesFragment extends Fragment {
    private static final int LOADER_STORIES = 1;

    private final CursorLoaderCallbacks mLoaderCallbacksCursor = new CursorLoaderCallbacks();

    @Optional
    @Nullable
    @InjectView(R.id.refresh)
    SwipeRefreshLayout mSwipeRefresh;
    @Optional
    @Nullable
    @InjectView(R.id.stories)
    RecyclerView mStoriesView;
    @Nullable
    private StoryCursorRecyclerViewAdapter mStoriesAdapter;
    @Nullable
    private LoadMoreOnScrollListener mLoadMoreListener;

    // these properties should be treated as final and only set/modified in onCreate()
    @NonNull
    private /* final */ String mGuid = INVALID_GUID;
    @NonNull
    private /* final */ String mMinistryId = Ministry.INVALID_ID;

    @Nullable
    private Cursor mStories = null;
    @NonNull
    private final Bundle mFilters = new Bundle();
    @Nullable
    private FetchStoriesTask mLoadTask = null;

    private int mLastLoadedStory = 0;
    private boolean mHasMore = true;

    public static StoriesFragment newInstance(@NonNull final String guid, @NonNull final String ministryId) {
        final StoriesFragment fragment = new StoriesFragment();

        final Bundle args = new Bundle(2);
        args.putString(ARG_GUID, guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        setHasOptionsMenu(true);

        // process arguments
        final Bundle args = this.getArguments();
        if (args != null) {
            mGuid = BundleCompat.getString(args, ARG_GUID, mGuid);
            mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, mMinistryId);
        }

        // trigger an initial fetch of stories
        fetchStories(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedState) {
        super.onViewCreated(view, savedState);
        ButterKnife.inject(this, view);
        setupSwipeRefresh();
        setupStoriesView();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_stories, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();
    }

    void onLoadStories(@Nullable final Cursor c) {
        mStories = c;
        updateStoriesView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                onRefreshStories();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onRefreshStories() {
        // reset has more and last page loaded
        mHasMore = true;
        mLastLoadedStory = 0;

        // trigger a load
        fetchStories(true);
    }

    void onFinishedFetching(@NonNull final FetchStoriesTask task, @Nullable final PagedList<Story> stories) {
        if (mLoadTask == task) {
            mLoadTask = null;

            // tell load more listener we finished loading
            if (mLoadMoreListener != null) {
                mLoadMoreListener.doneLoading();
            }

            // track stats for loaded stories
            mHasMore = stories != null && stories.hasMore();
            if (stories != null) {
                mLastLoadedStory = stories.getTo();
            }

            // possibly trigger another load if there are still more stories
            possiblyLoadMore();
        }

        // update refresh view
        updateRefreshView();
    }

    /* END lifecycle */

    private void setupSwipeRefresh() {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshStories();
                }
            });
            updateRefreshView();
        }
    }

    private void updateRefreshView() {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setRefreshing(mLoadTask != null);
        }
    }

    private void setupStoriesView() {
        if (mStoriesView != null) {
            final FragmentActivity activity = getActivity();
            mStoriesView.setHasFixedSize(false);
            mStoriesView.setLayoutManager(new LinearLayoutManager(activity));
            mStoriesView.addItemDecoration(new DividerItemDecoration(activity, VERTICAL));
            mLoadMoreListener = new LoadMoreOnScrollListener() {
                @Override
                protected void onLoadMore() {
                    fetchStories(false);
                }
            };
            mStoriesView.addOnScrollListener(mLoadMoreListener);

            mStoriesAdapter = new StoryCursorRecyclerViewAdapter();
            mStoriesView.setAdapter(mStoriesAdapter);

            updateStoriesView();
        }
    }

    private void updateStoriesView() {
        // update the ministries adapter cursor
        if (mStoriesAdapter != null) {
            mStoriesAdapter.swapCursor(mStories);
        }
    }

    private void fetchStories(final boolean force) {
        if (mHasMore) {
            // replace previous load stories task
            mLoadTask = new FetchStoriesTask(getActivity(), mGuid, mMinistryId, new Bundle(mFilters),
                                             (mLastLoadedStory + 1) / DEFAULT_STORIES_PER_PAGE,
                                             DEFAULT_STORIES_PER_PAGE, force);
            AsyncTaskCompat.executeParallel(mLoadTask);

            // update swipe refresh view
            updateRefreshView();
        }
    }

    private void possiblyLoadMore() {
        if (mHasMore && mLoadTask == null) {
            if (mLoadMoreListener != null && mStoriesView != null) {
                // XXX: not necessarily the cleanest, but trigger a "fake" scroll to check if more stories are still needed
                mLoadMoreListener.onScrolled(mStoriesView, 0, 0);
            }
        }
    }

    private void startLoaders() {
        final LoaderManager manager = getLoaderManager();

        // build the args for the MeasurementDetails loader
        final Bundle args = new Bundle();
        args.putParcelable(ARG_WHERE, FIELD_STATE.eq(bind(State.PUBLISHED))
                .and(FIELD_MINISTRY_ID.eq(bind(mMinistryId)).or(FIELD_PRIVACY.eq(bind(Privacy.PUBLIC)))));
        args.putString(ARG_ORDER_BY, Contract.Story.COLUMN_CREATED + " DESC");

        // start the Stories Cursor loader
        manager.initLoader(LOADER_STORIES, args, mLoaderCallbacksCursor);
    }

    private class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_STORIES:
                    final Context context = getActivity();
                    final CursorBroadcastReceiverLoader loader =
                            new DaoCursorBroadcastReceiverLoader<>(context, GmaDao.getInstance(context), Story.class,
                                                                   args);
                    loader.addIntentFilter(BroadcastUtils.updateStoriesFilter());
                    return loader;
                default:
                    return null;
            }
        }

        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_STORIES:
                    onLoadStories(cursor);
                    break;
            }
        }
    }

    private class FetchStoriesTask extends AsyncTask<Void, Void, PagedList<Story>> {
        @NonNull
        private final Context mContext;
        @NonNull
        private final String mGuid;
        @NonNull
        private final String mMinistryId;
        @Nullable
        private final Bundle mFilters;
        private final int mPage;
        private final int mPageSize;
        private final boolean mForce;

        public FetchStoriesTask(@NonNull final Context context, @NonNull final String guid,
                                @NonNull final String ministryId, @Nullable final Bundle filters, final int page,
                                final int pageSize, final boolean force) {
            mContext = context.getApplicationContext();
            mGuid = guid;
            mMinistryId = ministryId;
            mFilters = filters;
            mPage = page;
            mPageSize = pageSize;
            mForce = force;
        }

        @Override
        protected PagedList<Story> doInBackground(final Void... ignore) {
            try {
                return StoriesManager.getInstance(mContext)
                        .fetchStories(mGuid, mMinistryId, mFilters, mPage, mPageSize);
            } catch (final Exception e) {
                // we had an error fetching the stories, hand the request off to the sync service
                GmaSyncService.syncStories(mContext, mGuid, mMinistryId, mFilters, mPage, mPageSize, mForce);
            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable final PagedList<Story> stories) {
            super.onPostExecute(stories);
            onFinishedFetching(this, stories);
        }
    }
}
