package com.expidev.gcmapp.support.v4.adapter;

import static com.expidev.gcmapp.model.measurement.MeasurementValue.ValueType;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.support.v4.fragment.measurement.MeasurementValueFragment;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.joda.time.YearMonth;

public class MeasurementFragmentPagerAdapter extends FragmentPagerAdapter {
    @ValueType
    private final int mType;
    @NonNull
    private final String mGuid;
    @NonNull
    private final String mMinistryId;
    @NonNull
    private final Ministry.Mcc mMcc;
    @NonNull
    private final YearMonth mPeriod;

    @Nullable
    private Cursor mCursor = null;

    public MeasurementFragmentPagerAdapter(@NonNull final FragmentManager fm, @ValueType final int type,
                                           @NonNull final String guid, @NonNull final String ministryId,
                                           @NonNull final Ministry.Mcc mcc, @NonNull final YearMonth period) {
        super(fm);
        mType = type;
        mGuid = guid;
        mMinistryId = ministryId;
        mMcc = mcc;
        mPeriod = period;
    }

    public void swapCursor(@Nullable final Cursor c) {
        mCursor = c;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final Object item = super.instantiateItem(container, position);
        // XXX: setUserVisibleHint(true) to work around https://code.google.com/p/android/issues/detail?id=69586
        if (item instanceof Fragment) {
            ((Fragment) item).setUserVisibleHint(true);
        }
        return item;
    }

    @Override
    public Fragment getItem(final int position) {
        assert mCursor != null : "This should never be called unless we have items, which means we have a Cursor";

        mCursor.moveToPosition(position);

        return MeasurementValueFragment.newInstance(mType, mGuid, mMinistryId, mMcc, CursorUtils.getNonNullString(
                mCursor, Contract.MeasurementValue.COLUMN_PERM_LINK_STUB, ""), mPeriod);
    }

    @Override
    public long getItemId(final int position) {
        assert mCursor != null : "This should never be called unless we have items, which means we have a Cursor";

        mCursor.moveToPosition(position);
        return CursorUtils.getLong(mCursor, Contract.MeasurementValue.COLUMN_ROWID, super.getItemId(position));
    }
}
