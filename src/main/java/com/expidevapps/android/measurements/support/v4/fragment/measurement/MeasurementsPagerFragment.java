package com.expidevapps.android.measurements.support.v4.fragment.measurement;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.ARG_PERIOD;
import static com.expidevapps.android.measurements.Constants.ARG_TYPE;
import static com.expidevapps.android.measurements.model.MeasurementType.ARG_COLUMN;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_LOCAL;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_NONE;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_PERSONAL;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_JOINS;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_ORDER_BY;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_PROJECTION;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_WHERE;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_WHERE_ARGS;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
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

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementValue.ValueType;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.MinistryMeasurement;
import com.expidevapps.android.measurements.model.PersonalMeasurement;
import com.expidevapps.android.measurements.service.BroadcastUtils;
import com.expidevapps.android.measurements.support.v4.adapter.MeasurementPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.db.support.v4.content.DaoCursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.util.ArrayUtils;
import org.joda.time.YearMonth;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class MeasurementsPagerFragment extends Fragment {
    static final int LOADER_MEASUREMENTS = 1;

    private final CursorLoaderCallbacks mLoaderCallbacksCursor = new CursorLoaderCallbacks();

    @Nullable
    private MeasurementType.Column mColumn;

    @NonNull
    private /* final */ String mGuid;
    @NonNull
    private /* final */ String mMinistryId = Ministry.INVALID_ID;
    @NonNull
    private /* final */ Ministry.Mcc mMcc = Ministry.Mcc.UNKNOWN;
    @NonNull
    private /* final */ YearMonth mPeriod;
    @ValueType
    private /* final */ int mType = TYPE_NONE;

    @Nullable
    @Optional
    @InjectView(R.id.pager)
    ViewPager mPager = null;
    private MeasurementPagerAdapter mAdapter;
    @Nullable
    @Optional
    @InjectView(R.id.indicator)
    CirclePageIndicator mPagerIndicator = null;

    @NonNull
    public static MeasurementsPagerFragment newInstance(@ValueType final int type, @NonNull final String guid,
                                                        @NonNull final String ministryId,
                                                        @NonNull final Ministry.Mcc mcc,
                                                        @NonNull final YearMonth period,
                                                        @NonNull final MeasurementType.Column column) {
        final MeasurementsPagerFragment fragment = new MeasurementsPagerFragment();

        final Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
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
    @SuppressWarnings("ResourceType")
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = this.getArguments();
        mType = args.getInt(ARG_TYPE, TYPE_NONE);
        mGuid = args.getString(ARG_GUID);
        mMinistryId = args.getString(ARG_MINISTRY_ID);
        mMcc = Ministry.Mcc.fromRaw(args.getString(ARG_MCC));
        mPeriod = YearMonth.parse(args.getString(ARG_PERIOD));
        mColumn = MeasurementType.Column.fromRaw(args.getString(ARG_COLUMN));
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        return inflater.inflate(R.layout.fragment_measurements_pager, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedState) {
        super.onViewCreated(view, savedState);
        ButterKnife.inject(this, view);
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
            mAdapter = new MeasurementPagerAdapter(getActivity(), mType, mGuid, mMinistryId, mMcc, mPeriod);
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
        manager.initLoader(LOADER_MEASUREMENTS, getLoaderArgsMeasurements(), mLoaderCallbacksCursor);
    }

    private static final String[] PROJECTION_BASE =
            {Contract.MeasurementType.COLUMN_ROWID, Contract.MeasurementType.COLUMN_PERM_LINK_STUB,
                    Contract.MeasurementType.COLUMN_NAME};
    private static final Join<MeasurementType, PersonalMeasurement> JOIN_PERSONAL_MEASUREMENT =
            Contract.MeasurementType.JOIN_PERSONAL_MEASUREMENT.type("LEFT").andOn(
                    Contract.PersonalMeasurement.SQL_WHERE_GUID_MINISTRY_MCC_PERIOD);
    private static final Join<MeasurementType, MinistryMeasurement> JOIN_MINISTRY_MEASUREMENT =
            Contract.MeasurementType.JOIN_MINISTRY_MEASUREMENT.type("LEFT").andOn(
                    Contract.MinistryMeasurement.SQL_WHERE_MINISTRY_MCC_PERIOD);

    @NonNull
    private Bundle getLoaderArgsMeasurements() {
        final Bundle args = new Bundle(5);

        // generate joins & projections based on measurement type
        switch (mType) {
            case TYPE_LOCAL:
                args.putParcelableArray(ARG_JOINS,
                                        new Join[] {JOIN_MINISTRY_MEASUREMENT.args(mMinistryId, mMcc, mPeriod)});
                args.putStringArray(ARG_PROJECTION, ArrayUtils.merge(String.class, PROJECTION_BASE, new String[] {
                        Contract.MinistryMeasurement.SQL_PREFIX + Contract.MinistryMeasurement.COLUMN_VALUE,
                        Contract.MinistryMeasurement.SQL_PREFIX + Contract.MinistryMeasurement.COLUMN_DELTA}));
                break;
            case TYPE_PERSONAL:
                args.putParcelableArray(ARG_JOINS,
                                        new Join[] {JOIN_PERSONAL_MEASUREMENT.args(mGuid, mMinistryId, mMcc, mPeriod)});
                args.putStringArray(ARG_PROJECTION, ArrayUtils.merge(String.class, PROJECTION_BASE, new String[] {
                        Contract.PersonalMeasurement.SQL_PREFIX + Contract.PersonalMeasurement.COLUMN_VALUE,
                        Contract.PersonalMeasurement.SQL_PREFIX + Contract.PersonalMeasurement.COLUMN_DELTA}));
                break;
            default:
                args.putStringArray(ARG_PROJECTION, PROJECTION_BASE);
                break;
        }

        // set WHERE and ORDER BY clauses
        if (mColumn != null) {
            args.putString(ARG_WHERE, Contract.MeasurementType.SQL_WHERE_COLUMN);
            args.putStringArray(ARG_WHERE_ARGS, bindValues(mColumn));
        }
        args.putString(ARG_ORDER_BY, Contract.MeasurementType.COLUMN_SORT_ORDER);

        return args;
    }

    private class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MEASUREMENTS:
                    final Context context = getActivity();
                    final CursorBroadcastReceiverLoader loader =
                            new DaoCursorBroadcastReceiverLoader<>(context, GmaDao.getInstance(context),
                                                                   MeasurementType.class, args);
                    loader.addIntentFilter(BroadcastUtils.updateMeasurementTypesFilter());
                    switch (mType) {
                        case TYPE_LOCAL:
                            loader.addIntentFilter(
                                    BroadcastUtils.updateMeasurementValuesFilter(mMinistryId, mMcc, mPeriod));
                            break;
                        case TYPE_PERSONAL:
                            loader.addIntentFilter(
                                    BroadcastUtils.updateMeasurementValuesFilter(mMinistryId, mMcc, mPeriod, mGuid));
                            break;
                    }
                    return loader;
                default:
                    return null;
            }
        }

        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_MEASUREMENTS:
                    onLoadMeasurementTypes(cursor);
                    break;
            }
        }
    }
}
