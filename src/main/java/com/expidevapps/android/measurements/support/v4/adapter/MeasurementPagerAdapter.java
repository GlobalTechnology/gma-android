package com.expidevapps.android.measurements.support.v4.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.activity.MeasurementDetailsActivity;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementValue;
import com.expidevapps.android.measurements.model.MeasurementValue.ValueType;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.MinistryMeasurement;
import com.expidevapps.android.measurements.model.PersonalMeasurement;
import com.expidevapps.android.measurements.support.v4.adapter.MeasurementPagerAdapter.ViewHolder;
import com.expidevapps.android.measurements.sync.GmaSyncService;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.support.v4.adapter.CursorPagerAdapter;
import org.ccci.gto.android.common.support.v4.adapter.ViewHolderPagerAdapter;
import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.ccci.gto.android.common.widget.RepeatingClickTouchListener;
import org.joda.time.YearMonth;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Optional;

import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_LOCAL;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_PERSONAL;

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
    private final Assignment.Role mRole;
    @NonNull
    private final YearMonth mPeriod;

    // we cache changed values to prevent display glitches when updates haven't hit the db yet
    @NonNull
    private final LongSparseArray<Integer> mValues = new LongSparseArray<>();
    @NonNull
    private final LongSparseArray<Boolean> mDirty = new LongSparseArray<>();
    @NonNull
    private final LongSparseArray<Boolean> mFavourites = new LongSparseArray<>();

    public MeasurementPagerAdapter(@NonNull final Context context, @ValueType final int type,
                                   @NonNull final String guid, @NonNull final String ministryId, @NonNull final Mcc mcc,
                                   @NonNull final Assignment.Role role, @NonNull final YearMonth period) {
        mContext = context;
        mDao = GmaDao.getInstance(context);

        mType = type;
        mGuid = guid;
        mMinistryId = ministryId;
        mMcc = mcc;
        mRole = role;
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
        holder.mFavourite = CursorUtils.getBool(c, Contract.MeasurementType.COLUMN_FAVOURITE, false);

        // update views
        if (holder.mNameView != null) {
            holder.mNameView.setText(holder.mName);
        }
        if (holder.mFavouriteView != null) {
            holder.mFavouriteView.setChecked(holder.mFavourite);
            holder.mFavouriteView.setText(holder.mFavourite ? mContext.getResources().getString(R.string.label_make_measurement_non_favourite) :
                    mContext.getResources().getString(R.string.label_make_measurement_favourite));
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
        @InjectView(R.id.favourite)
        CheckBox mFavouriteView;
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

        boolean mFavourite;

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
                AsyncTaskCompat.execute(new UpdateDeltaRunnable(mContext, mDao, mGuid, value, delta));
            }

            // update the value view
            updateValueView();
        }

        private void updateFavourite(final int favourite, final String permLink) {
            final long id = getId();
            mFavourites.put(id, favourite > 0);

            // update favourite status of measurement
            final MeasurementValue value = getValue();
            if (value != null) {
                AsyncTaskCompat.execute(new UpdateFavouriteRunnable(mDao, permLink, favourite > 0));
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
        void onIncrementValue() {
            updateValueDelta(1);
        }

        @Optional
        @OnClick(R.id.decrement)
        void onDecrementValue() {
            updateValueDelta(-1);
        }

        @Optional
        @OnClick(R.id.value)
        void onClickValue() {
            if (mPermLink != null) {
                MeasurementDetailsActivity.start(mContext, mGuid, mMinistryId, mMcc, mRole, mPermLink, mPeriod);
            }
        }

        @Optional
        @OnCheckedChanged(R.id.favourite)
        void onChecked(boolean checked) {
            mFavouriteView.setText(checked ? mContext.getResources().getString(R.string.label_make_measurement_non_favourite) :
                    mContext.getResources().getString(R.string.label_make_measurement_favourite));
            updateFavourite(checked ? 1 : 0, mPermLink);
        }
    }

    private static final class UpdateDeltaRunnable implements Runnable {
        @NonNull
        private final Context mContext;
        @NonNull
        private final GmaDao mDao;
        @NonNull
        private final String mGuid;
        @NonNull
        private final MeasurementValue mValue;
        private final int mDelta;

        public UpdateDeltaRunnable(@NonNull final Context context, @NonNull final GmaDao dao,
                                   @NonNull final String guid, @NonNull final MeasurementValue value, final int delta) {
            mContext = context;
            mDao = dao;
            mGuid = guid;
            mValue = value;
            mDelta = delta;
        }

        @Override
        public void run() {
            // store the delta change in the local database
            mDao.updateMeasurementValueDelta(mValue, mDelta);

            // trigger a sync of dirty measurements
            GmaSyncService.syncDirtyMeasurements(mContext, mGuid, mValue.getMinistryId(), mValue.getMcc(),
                                                 mValue.getPeriod());
        }
    }

    private static final class UpdateFavouriteRunnable implements Runnable {
        @NonNull
        private final GmaDao mDao;
        @NonNull
        private final String mPermLink;
        private final boolean mFavourite;

        public UpdateFavouriteRunnable(@NonNull final GmaDao dao,
                                   @NonNull final String  permLink, final boolean favourite) {
            mDao = dao;
            mPermLink = permLink;
            mFavourite = favourite;
        }

        @Override
        public void run() {
            try {
                final MeasurementType measurementType = mDao.find(MeasurementType.class, mPermLink);
                if (measurementType == null) {
                    return;
                }
                measurementType.setFavourite(mFavourite);
                mDao.update(measurementType, new String[]{Contract.MeasurementType.COLUMN_FAVOURITE});
            }
            catch (final SQLException e) {
                Log.d("SQLException", "error updating measurementType", e);
            }
        }
    }
}
