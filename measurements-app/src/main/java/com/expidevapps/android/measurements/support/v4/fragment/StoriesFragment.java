package com.expidevapps.android.measurements.support.v4.fragment;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.INVALID_GUID;
import static com.expidevapps.android.measurements.db.Contract.Story.SQL_WHERE_MINISTRY;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_ORDER_BY;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_WHERE;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.support.v7.adapter.StoryCursorRecyclerViewAdapter;

import org.ccci.gto.android.common.db.support.v4.content.DaoCursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.recyclerview.decorator.DividerItemDecoration;
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

    @NonNull
    private /* final */ String mGuid = INVALID_GUID;
    @NonNull
    private /* final */ String mMinistryId = Ministry.INVALID_ID;

    @Nullable
    private Cursor mStories = null;

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

        // process arguments
        final Bundle args = this.getArguments();
        if (args != null) {
            mGuid = BundleCompat.getString(args, ARG_GUID, mGuid);
            mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, mMinistryId);
        }
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
    public void onStart() {
        super.onStart();
        startLoaders();
    }

    void onLoadStories(@Nullable final Cursor c) {
        mStories = c;
        updateStoriesView();
    }

    void onRefreshStories() {
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
        }
    }

    private void setupStoriesView() {
        if (mStoriesView != null) {
            final FragmentActivity activity = getActivity();
            mStoriesView.setHasFixedSize(false);
            mStoriesView.setLayoutManager(new LinearLayoutManager(activity));
            mStoriesView.addItemDecoration(new DividerItemDecoration(activity, VERTICAL));

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

    private void startLoaders() {
        final LoaderManager manager = getLoaderManager();

        // build the args for the MeasurementDetails loader
        final Bundle args = new Bundle();
        args.putParcelable(ARG_WHERE, SQL_WHERE_MINISTRY.args(mMinistryId));
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
}
