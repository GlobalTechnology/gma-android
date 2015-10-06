package com.expidevapps.android.measurements.support.v4.fragment.measurement;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.ARG_PERIOD;
import static com.expidevapps.android.measurements.Constants.ARG_SHOW_MEASUREMENT;
import static com.expidevapps.android.measurements.Constants.ARG_TYPE;
import static com.expidevapps.android.measurements.db.Contract.Base.COLUMN_ROWID;
import static com.expidevapps.android.measurements.db.Contract.MeasurementPermLink.COLUMN_PERM_LINK_STUB;
import static com.expidevapps.android.measurements.db.Contract.MeasurementType.COLUMN_FAVOURITE;
import static com.expidevapps.android.measurements.db.Contract.MeasurementTypeLocalization.COLUMN_LOCALE;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.model.Measurement.SHOW_ALL;
import static com.expidevapps.android.measurements.model.Measurement.SHOW_FAVOURITE;
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
import static org.ccci.gto.android.common.db.Expression.field;
import static org.ccci.gto.android.common.db.Expression.literal;
import static org.ccci.gto.android.common.db.Expression.raw;

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
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementTypeLocalization;
import com.expidevapps.android.measurements.model.MeasurementValue.ValueType;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.MinistryMeasurement;
import com.expidevapps.android.measurements.model.PersonalMeasurement;
import com.expidevapps.android.measurements.support.v4.adapter.MeasurementPagerAdapter;
import com.expidevapps.android.measurements.support.v4.content.FilteredMeasurementTypeDaoCursorLoader;
import com.expidevapps.android.measurements.sync.BroadcastUtils;
import com.viewpagerindicator.CirclePageIndicator;

import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.db.Table;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.util.ArrayUtils;
import org.ccci.gto.android.common.util.LocaleCompat;
import org.joda.time.YearMonth;

import java.util.ArrayList;
import java.util.Locale;

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
    private int mShowMeasurement = SHOW_ALL;

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
                                                        @NonNull final YearMonth period, final int showMeasurement,
                                                        @NonNull final MeasurementType.Column column) {
        final MeasurementsPagerFragment fragment = new MeasurementsPagerFragment();

        final Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_GUID, guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putString(ARG_MCC, mcc.toString());
        args.putString(ARG_PERIOD, period.toString());
        args.putString(ARG_COLUMN, column.toString());
        args.putInt(ARG_SHOW_MEASUREMENT, showMeasurement);
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
        mShowMeasurement = args.getInt(ARG_SHOW_MEASUREMENT, mShowMeasurement);
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

    private static final String[] PROJECTION_BASE = {COLUMN_ROWID, COLUMN_PERM_LINK_STUB, COLUMN_FAVOURITE};
    private static final Join<MeasurementType, PersonalMeasurement> JOIN_PERSONAL_MEASUREMENT =
            Contract.MeasurementType.JOIN_PERSONAL_MEASUREMENT.type("LEFT");
    private static final Join<MeasurementType, MinistryMeasurement> JOIN_MINISTRY_MEASUREMENT =
            Contract.MeasurementType.JOIN_MINISTRY_MEASUREMENT.type("LEFT");

    @NonNull
    private Bundle getLoaderArgsMeasurements() {
        final Bundle args = new Bundle(5);

        // build dynamic query parts
        final Table<MeasurementType> base = Table.forClass(MeasurementType.class);
        final StringBuilder name = new StringBuilder("COALESCE(");
        final ArrayList<Join<MeasurementType, ?>> joins = new ArrayList<>();
        String[] projection = PROJECTION_BASE;

        // add joins & projections based on measurement type
        switch (mType) {
            case TYPE_LOCAL:
                joins.add(JOIN_MINISTRY_MEASUREMENT.andOn(
                        raw(Contract.MinistryMeasurement.SQL_WHERE_MINISTRY_MCC_PERIOD, mMinistryId, mMcc, mPeriod)));
                projection = ArrayUtils.merge(String.class, projection, new String[] {
                        Contract.MinistryMeasurement.SQL_PREFIX + Contract.MinistryMeasurement.COLUMN_VALUE,
                        Contract.MinistryMeasurement.SQL_PREFIX + Contract.MinistryMeasurement.COLUMN_DELTA});
                break;
            case TYPE_PERSONAL:
                joins.add(JOIN_PERSONAL_MEASUREMENT.andOn(
                        raw(Contract.PersonalMeasurement.SQL_WHERE_GUID_MINISTRY_MCC_PERIOD, mGuid, mMinistryId, mMcc,
                            mPeriod)));
                projection = ArrayUtils.merge(String.class, projection, new String[] {
                        Contract.PersonalMeasurement.SQL_PREFIX + Contract.PersonalMeasurement.COLUMN_VALUE,
                        Contract.PersonalMeasurement.SQL_PREFIX + Contract.PersonalMeasurement.COLUMN_DELTA});
                break;
        }

        // process fallback locales
        final Locale[] locales = LocaleCompat.getFallbacks(Locale.getDefault());
        int i = 0;
        for (final Locale locale : locales) {
            final String alias = "lang" + i;
            final Table<MeasurementTypeLocalization> table =
                    Table.forClass(MeasurementTypeLocalization.class).as(alias);
            name.append(alias).append('.').append(Contract.MeasurementTypeLocalization.COLUMN_NAME).append(',');
            joins.add(Join.create(base, table).type("LEFT")
                              .on(field(base, COLUMN_PERM_LINK_STUB).eq(field(table, COLUMN_PERM_LINK_STUB)))
                              .andOn(field(table, COLUMN_MINISTRY_ID).eq(literal(mMinistryId)))
                              .andOn(field(table, COLUMN_LOCALE).eq(literal(locale))));
            i++;
        }
        name.append(Contract.MeasurementType.SQL_PREFIX).append(Contract.MeasurementType.COLUMN_NAME).append(") AS ")
                .append(Contract.MeasurementType.COLUMN_NAME);
        projection = ArrayUtils.merge(String.class, projection, new String[] {name.toString()});

        // add components to args
        args.putStringArray(ARG_PROJECTION, projection);
        args.putParcelableArray(ARG_JOINS, joins.toArray(new Join[joins.size()]));
        if (mColumn != null) {
            args.putString(ARG_WHERE, Contract.MeasurementType.SQL_WHERE_COLUMN +
                    (mShowMeasurement == SHOW_FAVOURITE ? " AND " + Contract.MeasurementType.SQL_WHERE_FAVOURITE : ""));
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
                    final CursorBroadcastReceiverLoader loader =
                            new FilteredMeasurementTypeDaoCursorLoader(getActivity(), mGuid, mMinistryId, args);
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
