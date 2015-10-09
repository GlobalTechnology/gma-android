package com.expidevapps.android.measurements.support.v4.adapter;

import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_LOCAL;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_PERSONAL;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.activity.MeasurementDetailsActivity;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.MeasurementValue;
import com.expidevapps.android.measurements.model.MeasurementValue.ValueType;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.MinistryMeasurement;
import com.expidevapps.android.measurements.model.PersonalMeasurement;
import com.expidevapps.android.measurements.support.v4.adapter.MeasurementPagerAdapter.ViewHolder;
import com.expidevapps.android.measurements.sync.BroadcastUtils;
import com.expidevapps.android.measurements.sync.GmaSyncService;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.support.v4.adapter.CursorPagerAdapter;
import org.ccci.gto.android.common.support.v4.adapter.ViewHolderPagerAdapter;
import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.ccci.gto.android.common.widget.RepeatingClickTouchListener;
import org.joda.time.YearMonth;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class MeasurementPagerAdapter extends CursorPagerAdapter<ViewHolder> {
    @NonNull
    private final Context mContext;
    @NonNull
    private final GmaDao mDao;

    @ValueType
    private final int mType;
    @NonNull
    private final String mGuid;
    @NonNull
    private final String mMinistryId;
    @NonNull
    private final Mcc mMcc;
    @NonNull
    private final YearMonth mPeriod;

    // we cache changed values to prevent display glitches when updates haven't hit the db yet
    @NonNull
    private final LongSparseArray<Integer> mValues = new LongSparseArray<>();
    @NonNull
    private final LongSparseArray<Boolean> mDirty = new LongSparseArray<>();
    @NonNull
    private final LongSparseArray<Boolean> mFavorites = new LongSparseArray<>();
    @NonNull
    private final LongSparseArray<Boolean> mDirtyFavorites = new LongSparseArray<>();

    public MeasurementPagerAdapter(@NonNull final Context context, @ValueType final int type,
                                   @NonNull final String guid, @NonNull final String ministryId, @NonNull final Mcc mcc,
                                   @NonNull final YearMonth period) {
        mContext = context;
        mDao = GmaDao.getInstance(context);

        mType = type;
        mGuid = guid;
        mMinistryId = ministryId;
        mMcc = mcc;
        mPeriod = period;
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCursorChanged(@Nullable final Cursor old, @Nullable final Cursor c) {
        super.onCursorChanged(old, c);

        // update cached values
        if (c != null) {
            final int idColumn = c.getColumnIndex(BaseColumns._ID);
            if (idColumn >= 0) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    final long id = c.getLong(idColumn);

                    // set the value if there is no value or the value isn't dirty
                    final int value = CursorUtils.getInt(c, Contract.MeasurementValue.COLUMN_VALUE, 0) +
                            CursorUtils.getInt(c, Contract.MeasurementValue.COLUMN_DELTA, 0);
                    if (mValues.get(id) == null || !mDirty.get(id, false)) {
                        mValues.put(id, value);
                    }

                    // update dirty flag based on Cursor value and cached value
                    mDirty.put(id, value != mValues.get(id));

                    // set the favorite flag if it's not currently set or isn't dirty
                    final boolean favorite =
                            CursorUtils.getBool(c, Contract.FavoriteMeasurement.COLUMN_FAVORITE, false);
                    if (mFavorites.get(id) == null || !mDirtyFavorites.get(id, false)) {
                        mFavorites.put(id, favorite);
                    }

                    // update dirty favorite flag based on Cursor value and cached value
                    mDirtyFavorites.put(id, favorite != mFavorites.get(id, false));
                }
            }
        }
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.page_measurements_value, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, @NonNull final Cursor c) {
        super.onBindViewHolder(holder, c);
        holder.mPermLink = CursorUtils.getString(c, Contract.MeasurementType.COLUMN_PERM_LINK_STUB, null);
        holder.mName = CursorUtils.getString(c, Contract.MeasurementType.COLUMN_NAME, null);

        // update views
        if (holder.mNameView != null) {
            holder.mNameView.setText(holder.mName);
        }

        holder.updateValueView();
    }

    @Override
    protected void onViewRecycled(@NonNull final ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mRepeatingClickListener.resetTask();
        holder.mPermLink = null;
        holder.mName = null;
        holder.mValue = null;
    }

    /* END lifecycle */

    final class ViewHolder extends ViewHolderPagerAdapter.ViewHolder {
        final RepeatingClickTouchListener mRepeatingClickListener = new RepeatingClickTouchListener();

        @Optional
        @Nullable
        @InjectView(R.id.name)
        TextView mNameView;
        @Optional
        @Nullable
        @InjectView(R.id.value)
        TextView mValueView;
        @Optional
        @Nullable
        @InjectView(R.id.increment)
        View mIncrementView;
        @Optional
        @Nullable
        @InjectView(R.id.decrement)
        View mDecrementView;

        @Nullable
        String mPermLink;
        @Nullable
        String mName;

        @Nullable
        MeasurementValue mValue = null;

        protected ViewHolder(@NonNull final View view) {
            super(view);
            ButterKnife.inject(this, view);

            if (mIncrementView != null) {
                mIncrementView.setOnTouchListener(mRepeatingClickListener);
            }
            if (mDecrementView != null) {
                mDecrementView.setOnTouchListener(mRepeatingClickListener);
            }
        }

        void updateValueView() {
            if (mValueView != null) {
                mValueView.setText(Integer.toString(mValues.get(getId(), 0)));
            }
        }

        private void updateValueDelta(final int delta) {
            final long id = getId();
            mValues.put(id, Math.max(mValues.get(id, 0) + delta, mType == TYPE_PERSONAL ? 0 : Integer.MIN_VALUE));
            mDirty.put(id, true);

            // update the backing measurement
            final MeasurementValue value = getValue();
            if (value != null) {
                AsyncTaskCompat.execute(new Runnable() {
                    @Override
                    public void run() {
                        // store the delta change in the local database
                        mDao.updateMeasurementValueDelta(value, delta);

                        // trigger a sync of dirty measurements
                        GmaSyncService.syncDirtyMeasurements(mContext, mGuid, mMinistryId, mMcc, mPeriod);
                    }
                });
            }

            // update the value view
            updateValueView();
        }

        private void updateFavorite(final boolean favorite) {
            final long id = getId();
            mFavorites.put(id, favorite);
            mDirtyFavorites.put(id, true);

            // update favourite status of measurement
            if (mPermLink != null) {
                final String permLink = mPermLink;
                AsyncTaskCompat.execute(new Runnable() {
                    @Override
                    public void run() {
                        // store updated favorite measurement flag
                        mDao.setFavoriteMeasurement(mGuid, mMinistryId, mMcc, permLink, favorite);

                        // broadcast that the favorite measurements have changed
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                                BroadcastUtils.updateFavoriteMeasurementsBroadcast(mGuid, mMinistryId, mMcc, permLink));
                    }
                });
            }
        }

        @Nullable
        private MeasurementValue getValue() {
            if (mValue == null && mPermLink != null) {
                switch (mType) {
                    case TYPE_LOCAL:
                        mValue = new MinistryMeasurement(mMinistryId, mMcc, mPermLink, mPeriod);
                        break;
                    case TYPE_PERSONAL:
                        mValue = new PersonalMeasurement(mGuid, mMinistryId, mMcc, mPermLink, mPeriod);
                        break;
                }
            }

            return mValue;
        }

        @Optional
        @OnClick(R.id.increment)
        void incrementValue() {
            updateValueDelta(1);
        }

        @Optional
        @OnClick(R.id.decrement)
        void decrementValue() {
            updateValueDelta(-1);
        }

        @Optional
        @OnClick(R.id.value)
        void showDetails() {
            if (mPermLink != null) {
                MeasurementDetailsActivity.start(mContext, mGuid, mMinistryId, mMcc, mPermLink, mPeriod);
            }
        }

        @Optional
        @OnClick(R.id.page_menu)
        void showPopupMenu(@NonNull final View view) {
            final PopupMenu popup = new PopupMenu(mContext, view);

            // load and customize menu
            final Menu menu = popup.getMenu();
            popup.getMenuInflater().inflate(R.menu.menu_measurements_popup, menu);
            if (mFavorites.get(getId(), false)) {
                final MenuItem menuItem = menu.findItem(R.id.action_remove_favorite);
                if (menuItem != null) {
                    menuItem.setVisible(true);
                }
            } else {
                final MenuItem menuItem = menu.findItem(R.id.action_add_favorite);
                if (menuItem != null) {
                    menuItem.setVisible(true);
                }
            }

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch(item.getItemId()) {
                        case R.id.action_add_favorite:
                            updateFavorite(true);
                            return true;
                        case R.id.action_remove_favorite:
                            updateFavorite(false);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            // show popup menu
            popup.show();
        }
    }
}
