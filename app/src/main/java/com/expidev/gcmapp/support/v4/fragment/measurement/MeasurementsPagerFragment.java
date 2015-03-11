package com.expidev.gcmapp.support.v4.fragment.measurement;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.ARG_MCC;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.ARG_PERIOD;
import static com.expidev.gcmapp.model.measurement.MeasurementType.ARG_COLUMN;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_PROJECTION;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementType;
import com.expidev.gcmapp.model.measurement.MeasurementValue;
import com.expidev.gcmapp.support.v4.adapter.MeasurementPagerAdapter;
import com.expidev.gcmapp.support.v4.content.MeasurementTypesCursorLoader;
import com.viewpagerindicator.CirclePageIndicator;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.joda.time.YearMonth;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class MeasurementsPagerFragment extends Fragment {
    static final int LOADER_MEASUREMENT_TYPES = 1;

    private final CursorLoaderCallbacks mLoaderCallbacksCursor = new CursorLoaderCallbacks();

    @Nullable
    private MeasurementType.Column mColumn;

    @NonNull
    private String mGuid;
    @NonNull
    private String mMinistryId = Ministry.INVALID_ID;
    @NonNull
    private Ministry.Mcc mMcc = Ministry.Mcc.UNKNOWN;
    @NonNull
    private YearMonth mPeriod;

    @Nullable
    @Optional
    @InjectView(R.id.pager)
    ViewPager mPager = null;
    private MeasurementPagerAdapter mAdapter;
    @Nullable
    @Optional
    @InjectView(R.id.indicator)
    CirclePageIndicator mPagerIndicator = null;

    public static MeasurementsPagerFragment newInstance(@NonNull final String guid, @NonNull final String ministryId,
                                                        @NonNull final Ministry.Mcc mcc,
                                                        @NonNull final YearMonth period,
                                                        @NonNull final MeasurementType.Column column) {
        final MeasurementsPagerFragment fragment = new MeasurementsPagerFragment();

        final Bundle args = new Bundle();
        args.putString(ARG_GUID, guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putString(ARG_MCC, mcc.toString());
        args.putString(ARG_PERIOD, period.toString());
        args.putString(ARG_COLUMN, column.toString());
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = this.getArguments();
        mGuid = args.getString(ARG_GUID);
        mMinistryId = args.getString(ARG_MINISTRY_ID);
        mMcc = Ministry.Mcc.fromRaw(args.getString(ARG_MCC));
        mPeriod = YearMonth.parse(args.getString(ARG_PERIOD));
        mColumn = MeasurementType.Column.fromRaw(args.getString(ARG_COLUMN));
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        final View view = inflater.inflate(R.layout.fragment_measurement_pager, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedState) {
        super.onActivityCreated(savedState);
        setupViewPager();
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();
    }

    void onLoadMeasurementTypes(@Nullable final Cursor c) {
        if (mAdapter != null) {
            mAdapter.swapCursor(c);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void setupViewPager() {
        if (mPager != null) {
            mAdapter = new MeasurementPagerAdapter(getChildFragmentManager(), MeasurementValue.TYPE_PERSONAL, mGuid,
                                                   mMinistryId, mMcc, mPeriod);
            mPager.setAdapter(mAdapter);

            // configure view pager indicator
            if (mPagerIndicator != null) {
                mPagerIndicator.setViewPager(mPager);
            }
        }
    }

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();

        // start the MeasurementTypes Cursor loader
        final Bundle measurementTypesArgs = new Bundle(2);
        measurementTypesArgs.putString(ARG_COLUMN, mColumn != null ? mColumn.toString() : null);
        measurementTypesArgs.putStringArray(ARG_PROJECTION, new String[] {Contract.MeasurementType.COLUMN_ROWID,
                Contract.MeasurementType.COLUMN_PERM_LINK});
        manager.initLoader(LOADER_MEASUREMENT_TYPES, measurementTypesArgs, mLoaderCallbacksCursor);
    }

    private class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MEASUREMENT_TYPES:
                    return new MeasurementTypesCursorLoader(getActivity(), args);
                default:
                    return null;
            }
        }

        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_MEASUREMENT_TYPES:
                    onLoadMeasurementTypes(cursor);
                    break;
            }
        }
    }
}
