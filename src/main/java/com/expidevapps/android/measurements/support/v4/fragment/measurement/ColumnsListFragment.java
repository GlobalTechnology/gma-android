package com.expidevapps.android.measurements.support.v4.fragment.measurement;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementType.Column;
import com.expidevapps.android.measurements.model.MeasurementValue.ValueType;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.sync.BroadcastUtils;

import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.db.support.v4.content.DaoCursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.util.ViewCompat;
import org.ccci.gto.android.common.widget.AccordionView;
import org.joda.time.YearMonth;

import java.util.EnumSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.ARG_PERIOD;
import static com.expidevapps.android.measurements.Constants.ARG_ROLE;
import static com.expidevapps.android.measurements.Constants.ARG_SUPPORTED_STAFF;
import static com.expidevapps.android.measurements.Constants.ARG_TYPE;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_NONE;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_DISTINCT;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_JOINS;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_PROJECTION;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_WHERE;
import static org.ccci.gto.android.common.db.Expression.raw;

public class ColumnsListFragment extends Fragment {
    static final int LOADER_COLUMNS = 1;

    private final CursorLoaderCallbacks mLoaderCallbacksCursor = new CursorLoaderCallbacks();

    @Nullable
    @Optional
    @InjectView(R.id.accordion)
    AccordionView mAccordion;
    @Nullable
    private AccordionAdapter mAccordionAdapter;

    @ValueType
    private /* final */ int mType = TYPE_NONE;
    @NonNull
    private /* final */ String mGuid;
    @NonNull
    private /* final */ String mMinistryId = Ministry.INVALID_ID;
    @NonNull
    private /* final */ Mcc mMcc = Mcc.UNKNOWN;
    @NonNull
    private /* final */ Assignment.Role mRole = Assignment.Role.UNKNOWN;
    @NonNull
    private /* final */ YearMonth mPeriod;
    @ValueType
    private boolean mSupportedStaff = false;

    public static ColumnsListFragment newInstance(@ValueType final int type, @NonNull final String guid,
                                                  @NonNull final String ministryId, @NonNull final Mcc mcc,
                                                  @NonNull final YearMonth period, @NonNull final Assignment.Role role,
                                                  @ValueType final boolean supportedStaff) {
        final ColumnsListFragment fragment = new ColumnsListFragment();

        final Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_GUID, guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putString(ARG_ROLE, role.toString());
        args.putString(ARG_MCC, mcc.toString());
        args.putString(ARG_PERIOD, period.toString());
        args.putBoolean(ARG_SUPPORTED_STAFF, supportedStaff);
        fragment.setArguments(args);

        return fragment;
    }

    @ValueType
    public int getType() {
        return mType;
    }

    @NonNull
    public YearMonth getPeriod() {
        return mPeriod;
    }

    /* BEGIN lifecycle */

    @Override
    @SuppressWarnings("ResourceType")
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = this.getArguments();
        mType = args.getInt(ARG_TYPE, mType);
        mGuid = args.getString(ARG_GUID);
        mMinistryId = args.getString(ARG_MINISTRY_ID);
        mMcc = Mcc.fromRaw(args.getString(ARG_MCC));
        mRole = Assignment.Role.fromRaw(args.getString(ARG_ROLE));
        mPeriod = YearMonth.parse(args.getString(ARG_PERIOD));
        mSupportedStaff = args.getBoolean(ARG_SUPPORTED_STAFF, mSupportedStaff);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        return inflater.inflate(R.layout.fragment_measurements_accordion, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedState) {
        super.onViewCreated(view, savedState);
        ButterKnife.inject(this, view);
        setupAccordion();
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();
    }

    void onLoadColumns(@Nullable final Cursor cursor) {
        if (mAccordionAdapter != null) {
            // convert Cursor to Columns EnumSet
            final EnumSet<Column> columns = EnumSet.noneOf(Column.class);
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    columns.add(Column.fromRaw(CursorUtils.getString(cursor, Contract.MeasurementType.COLUMN_COLUMN)));
                }
            }
            columns.remove(Column.UNKNOWN);

            // set Columns on the Accordion
            mAccordionAdapter.setColumns(columns.toArray(new Column[columns.size()]));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void setupAccordion() {
        if (mAccordion != null) {
            mAccordionAdapter = new AccordionAdapter();
            mAccordion.setAdapter(mAccordionAdapter);
        }
    }

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();

        // start the Columns Cursor loader
        manager.initLoader(LOADER_COLUMNS, getLoaderArgsColumns(), mLoaderCallbacksCursor);
    }

    private static final Join<MeasurementType, Contract.MeasurementVisibility> JOIN_MEASUREMENT_VISIBILITY =
            Contract.MeasurementType.JOIN_MEASUREMENT_VISIBILITY.type("LEFT");

    @NonNull
    private Bundle getLoaderArgsColumns() {
        final Bundle args = new Bundle(4);
        args.putBoolean(ARG_DISTINCT, true);
        args.putParcelableArray(ARG_JOINS, new Join[] {JOIN_MEASUREMENT_VISIBILITY
                .andOn(raw(Contract.MeasurementVisibility.SQL_WHERE_MINISTRY, mMinistryId))});
        args.putStringArray(ARG_PROJECTION, new String[] {Contract.MeasurementType.COLUMN_COLUMN});
        args.putString(ARG_WHERE, Contract.MeasurementType.SQL_WHERE_VISIBLE);
        return args;
    }

    private class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_COLUMNS:
                    final Context context = getActivity();
                    final CursorBroadcastReceiverLoader loader =
                            new DaoCursorBroadcastReceiverLoader<>(context, GmaDao.getInstance(context),
                                                                   MeasurementType.class, args);
                    loader.addIntentFilter(BroadcastUtils.updateMeasurementTypesFilter());
                    return loader;
                default:
                    return null;
            }
        }

        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_COLUMNS:
                    onLoadColumns(cursor);
                    break;
            }
        }
    }

    static class ViewHolder extends AccordionView.ViewHolder {
        @Optional
        @Nullable
        @InjectView(R.id.logo)
        ImageView mLogoTop;
        @Optional
        @Nullable
        @InjectView(R.id.logo_bottom)
        ImageView mLogoBottom;
        @Optional
        @Nullable
        @InjectView(R.id.name)
        TextView mTitleView;
        @Optional
        @Nullable
        @InjectView(R.id.pagerFragment)
        View mPagerFrame;

        @NonNull
        Column mColumn = Column.UNKNOWN;
        final int mPagerId;

        ViewHolder(@NonNull final View header, @NonNull final View content) {
            super();
            mPagerId = ViewCompat.generateViewId();
            ButterKnife.inject(this, content);

            // ButterKnife.inject only works with one root view. So, check headerContainer for missing header Views
            mLogoTop = ButterKnife.findById(header, R.id.logo);
            mTitleView = ButterKnife.findById(header, R.id.name);

            // set a unique pager id if we found the Pager
            if (mPagerFrame != null) {
                mPagerFrame.setId(mPagerId);
            }
        }
    }

    private class AccordionAdapter extends AccordionView.Adapter<ViewHolder> {
        @NonNull
        private Column[] mColumns = new Column[0];

        void setColumns(@Nullable final Column[] columns) {
            mColumns = columns != null ? columns : new Column[0];
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mColumns.length;
        }

        @Override
        public int getPosition(@NonNull final ViewHolder holder) {
            for (int i = 0; i < mColumns.length; i++) {
                if (holder.mColumn == mColumns[i]) {
                    return i;
                }
            }
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull final ViewGroup headerParent,
                                             @NonNull final ViewGroup contentParent, int position) {
            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View header = inflater.inflate(R.layout.accordion_measurements_header, headerParent);
            final View content = inflater.inflate(R.layout.accordion_measurements_content_pager, contentParent);
            return new ViewHolder(header, content);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            // update mColumn
            final Column oldColumn = holder.mColumn;
            holder.mColumn = mColumns[position];

            // update label and logo based on column
            final int label;
            final int logoTop;
            final int logoBottom;
            switch (holder.mColumn) {
                case FRUIT:
                    label = R.string.label_measurements_column_fruit;
                    logoTop = R.drawable.ic_header_measurements_fruit;
                    logoBottom = R.drawable.ic_header_measurements_fruit_under;
                    break;
                case OUTCOME:
                    label = R.string.label_measurements_column_outcomes;
                    logoTop = R.drawable.ic_header_measurements_outcomes;
                    logoBottom = R.drawable.ic_header_measurements_outcomes_under;
                    break;
                case FAITH:
                    label = R.string.label_measurements_column_faith;
                    logoTop = R.drawable.ic_header_measurements_faith;
                    logoBottom = R.drawable.ic_header_measurements_faith_under;
                    break;
                case OTHER:
                default:
                    label = R.string.label_measurements_column_other;
                    //TODO get separate header graphics for Other measurements
                    logoTop = R.drawable.ic_header_measurements_outcomes;
                    logoBottom = R.drawable.ic_header_measurements_outcomes_under;
                    break;
            }

            if (holder.mTitleView != null) {
                holder.mTitleView.setText(label);
            }
            if (holder.mLogoTop != null) {
                holder.mLogoTop.setImageResource(logoTop);
            }
            if (holder.mLogoBottom != null) {
                holder.mLogoBottom.setImageResource(logoBottom);
            }

            // create fragment if we don't currently have one
            if (holder.mPagerFrame != null) {
                final FragmentManager fm = getChildFragmentManager();
                final Fragment fragment = fm.findFragmentById(holder.mPagerId);
                if (fragment == null || oldColumn != holder.mColumn) {
                    fm.beginTransaction().replace(holder.mPagerId, MeasurementsPagerFragment
                            .newInstance(mType, mGuid, mMinistryId, mMcc, mPeriod, mRole, mSupportedStaff, holder.mColumn)).commit();
                }
            }
        }

        @Override
        public void onDestroyViewHolder(@NonNull final ViewHolder holder) {
            super.onDestroyViewHolder(holder);

            // remove the fragment if it exists
            final FragmentManager fm = getChildFragmentManager();
            final Fragment fragment = fm.findFragmentById(holder.mPagerId);
            if (fragment != null) {
                fm.beginTransaction().remove(fragment).commit();
            }
        }
    }
}
