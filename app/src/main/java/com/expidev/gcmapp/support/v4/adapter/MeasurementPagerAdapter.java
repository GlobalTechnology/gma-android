package com.expidev.gcmapp.support.v4.adapter;

import static com.expidev.gcmapp.model.measurement.MeasurementValue.TYPE_LOCAL;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.TYPE_PERSONAL;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expidev.gcmapp.MeasurementDetailsActivity;
import com.expidev.gcmapp.R;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.GmaDao;
import com.expidev.gcmapp.model.Ministry.Mcc;
import com.expidev.gcmapp.model.measurement.MeasurementValue;
import com.expidev.gcmapp.model.measurement.MeasurementValue.ValueType;
import com.expidev.gcmapp.model.measurement.MinistryMeasurement;
import com.expidev.gcmapp.model.measurement.PersonalMeasurement;
import com.expidev.gcmapp.service.GmaSyncService;
import com.expidev.gcmapp.support.v4.adapter.MeasurementPagerAdapter.ViewHolder;

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
                }
            }
        }
    }

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_measurement_value, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, @NonNull final Cursor c) {
        super.onBindViewHolder(holder, c);
        holder.mPermLink = CursorUtils.getString(c, Contract.MeasurementType.COLUMN_PERM_LINK_STUB, null);
        holder.mName = CursorUtils.getString(c, Contract.MeasurementType.COLUMN_NAME, null);
        holder.mTotalId = CursorUtils.getString(c, Contract.MeasurementType.COLUMN_TOTAL_ID, null);

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
        holder.mTotalId = null;
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
        String mTotalId;

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
            if (mPermLink != null && mTotalId != null) {
                MeasurementDetailsActivity.start(mContext, mMinistryId, mMcc, mPermLink, mPeriod, mTotalId, mName);
            }
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
}
