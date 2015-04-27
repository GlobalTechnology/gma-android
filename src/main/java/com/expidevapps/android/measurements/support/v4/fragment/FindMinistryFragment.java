package com.expidevapps.android.measurements.support.v4.fragment;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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
import com.expidevapps.android.measurements.service.BroadcastUtils;
import com.expidevapps.android.measurements.support.v4.fragment.JoinMinistryDialogFragment.OnJoinMinistryListener;
import com.expidevapps.android.measurements.support.v7.adapter.MinistryCursorRecyclerViewAdapter;

import org.ccci.gto.android.common.recyclerview.adapter.CursorAdapter;
import org.ccci.gto.android.common.recyclerview.decorator.DividerItemDecoration;
import org.ccci.gto.android.common.recyclerview.listener.ItemClickListener;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.ccci.gto.android.common.util.BundleCompat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class FindMinistryFragment extends Fragment implements OnJoinMinistryListener {
    private static final String ARG_QUERY = FindMinistryFragment.class.getName() + ".ARG_QUERY";

    @Nullable
    private SearchView mSearchView;
    @Optional
    @Nullable
    @InjectView(R.id.ministries)
    RecyclerView mMinistriesView;

    @Nullable
    private MinistryCursorRecyclerViewAdapter mMinistriesAdapter;
    @NonNull
    private final MinistriesUpdateBroadcastReceiver mReceiverMinistriesUpdate = new MinistriesUpdateBroadcastReceiver();
    @NonNull
    private final MinistriesQueryListener mListenerQueryText = new MinistriesQueryListener();
    @NonNull
    private final MinistriesOnClickListener mListenerMinistriesOnClick = new MinistriesOnClickListener();

    @NonNull
    private GmaDao mDao;
    @Nullable
    private LoadMinistriesTask mLoadMinistriesTask;

    @NonNull
    private String mGuid;
    @NonNull
    private String mQuery = "";
    @Nullable
    private Cursor mMinistriesCursor;

    public static FindMinistryFragment newInstance(@NonNull final String guid) {
        final FindMinistryFragment fragment = new FindMinistryFragment();

        final Bundle args = new Bundle(1);
        args.putString(ARG_GUID, guid);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        mDao = GmaDao.getInstance(getActivity());
        setHasOptionsMenu(true);

        final Bundle args = this.getArguments();
        mGuid = args.getString(ARG_GUID);

        // load previous saved query
        if (savedState != null) {
            mQuery = BundleCompat.getString(savedState, ARG_QUERY, "");
        }

        // start listening for ministries updates
        startBroadcastReceivers();

        // fetch initial Cursor
        fetchMinistriesCursor();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        return inflater.inflate(R.layout.fragment_find_ministry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedState) {
        super.onViewCreated(view, savedState);
        ButterKnife.inject(this, view);
        setupMinistriesView();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_find_ministry, menu);

        // configure the search view
        setupSearchView(menu);
    }

    void onUpdateQuery(@NonNull final String query) {
        final String old = mQuery;
        mQuery = query;

        // only fetch a new Ministries Cursor if the query has changed
        if (!old.equals(mQuery)) {
            fetchMinistriesCursor();
        }
    }

    @Override
    public void onJoinedMinistry(@NonNull final String ministryId) {
        // cascade event up activity/fragment hierarchy
        final OnJoinMinistryListener listener = FragmentUtils.getListener(this, OnJoinMinistryListener.class);
        if (listener != null) {
            listener.onJoinedMinistry(ministryId);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_QUERY, mQuery);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        cleanupMinistriesView();
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        cleanupSearchView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBroadcastReceivers();
        if(mLoadMinistriesTask != null) {
            mLoadMinistriesTask.cancel(true);
        }
        changeMinistriesCursor(null);
    }

    /* END lifecycle */

    private void setupMinistriesView() {
        if (mMinistriesView != null) {
            mMinistriesView.setHasFixedSize(true);
            mMinistriesView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mMinistriesView.addItemDecoration(new DividerItemDecoration(getActivity(), VERTICAL));

            mMinistriesAdapter = new MinistryCursorRecyclerViewAdapter();
            mMinistriesView.setAdapter(mMinistriesAdapter);

            mMinistriesView.addOnItemTouchListener(new ItemClickListener(getActivity(), mListenerMinistriesOnClick));

            updateMinistriesView();
        }
    }

    private void cleanupMinistriesView() {
        final CursorAdapter adapter = mMinistriesAdapter;
        mMinistriesAdapter = null;
        if (adapter != null) {
            adapter.swapCursor(null);
        }
    }

    private void setupSearchView(@NonNull final Menu menu) {
        // find the search view
        final MenuItem item = menu.findItem(R.id.action_search);
        final View searchView = item != null ? MenuItemCompat.getActionView(item) : null;
        mSearchView = searchView instanceof SearchView ? (SearchView) searchView : null;

        // initialize the search view
        if (mSearchView != null) {
            // expand and populate query if we currently have one
            if (!TextUtils.isEmpty(mQuery)) {
                MenuItemCompat.expandActionView(item);
                mSearchView.setQuery(mQuery, false);
                mSearchView.clearFocus();
            }

            mSearchView.setOnQueryTextListener(mListenerQueryText);
        }
    }

    private void cleanupSearchView() {
        mSearchView = null;
    }

    private void startBroadcastReceivers() {
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        broadcastManager.registerReceiver(mReceiverMinistriesUpdate, BroadcastUtils.updateMinistriesFilter());
    }

    private void stopBroadcastReceivers() {
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        broadcastManager.unregisterReceiver(mReceiverMinistriesUpdate);
    }

    private void updateMinistriesView() {
        // update the ministries adapter cursor
        if (mMinistriesAdapter != null) {
            mMinistriesAdapter.swapCursor(mMinistriesCursor);
        }
    }

    private void fetchMinistriesCursor() {
        final LoadMinistriesTask old = mLoadMinistriesTask;

        // create & execute new task
        mLoadMinistriesTask = new LoadMinistriesTask();
        mLoadMinistriesTask.execute(mQuery);

        // stop old task
        if (old != null) {
            old.cancel(true);
        }
    }

    void changeMinistriesCursor(@Nullable final Cursor cursor) {
        final Cursor old = mMinistriesCursor;
        mMinistriesCursor = cursor;

        // update the ministries adapter cursor
        updateMinistriesView();

        // close the old cursor if it's different then the current cursor
        if (old != null && old != mMinistriesCursor) {
            old.close();
        }
    }

    void showJoinMinistryDialog(@NonNull final String ministryId) {
        if (!Ministry.INVALID_ID.equals(ministryId)) {
            final FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag("joinMinistryDialog") == null) {
                JoinMinistryDialogFragment.newInstance(mGuid, ministryId)
                        .show(fm.beginTransaction().addToBackStack("joinMinistryDialog"), "joinMinistryDialog");
            }
        }
    }

    private final class MinistriesUpdateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            // trigger an update of the displayed ministries
            fetchMinistriesCursor();
        }
    }

    private final class MinistriesQueryListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(@NonNull final String query) {
            onUpdateQuery(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(@NonNull final String newText) {
            onUpdateQuery(newText);
            return false;
        }
    }

    private final class MinistriesOnClickListener implements ItemClickListener.OnItemClickListener {
        @Override
        public void onItemClick(final View view, final int position) {
            if (mMinistriesAdapter != null) {
                showJoinMinistryDialog(mMinistriesAdapter.getMinistryId(position));
            }
        }
    }

    private static final String[] PROJECTION_FIELDS =
            new String[] {Contract.Ministry.COLUMN_ROWID, Contract.Ministry.COLUMN_MINISTRY_ID,
                    Contract.Ministry.COLUMN_NAME};
    private static final String ORDER_BY_NAME = Contract.Ministry.COLUMN_NAME;
    private static final String WHERE_NAME_LIKE = Contract.Ministry.COLUMN_NAME + " LIKE ?";

    private final class LoadMinistriesTask extends AsyncTask<String, Void, Cursor> {
        @NonNull
        @Override
        protected Cursor doInBackground(@NonNull final String... params) {
            if (params.length == 1 && !TextUtils.isEmpty(params[0])) {
                return mDao.getCursor(Ministry.class, PROJECTION_FIELDS, WHERE_NAME_LIKE,
                                      bindValues("%" + params[0] + "%"), ORDER_BY_NAME);
            } else {
                return mDao.getCursor(Ministry.class, PROJECTION_FIELDS, null, null, ORDER_BY_NAME);
            }
        }

        @Override
        protected void onPostExecute(@NonNull final Cursor cursor) {
            super.onPostExecute(cursor);
            changeMinistriesCursor(cursor);
        }

        @Override
        protected void onCancelled(@Nullable final Cursor cursor) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
