package com.expidevapps.android.measurements.support.v4.fragment.measurement;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.ARG_PERIOD;
import static com.expidevapps.android.measurements.Constants.ARG_TYPE;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_NONE;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementValue.ValueType;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;

import org.ccci.gto.android.common.util.ViewCompat;
import org.ccci.gto.android.common.widget.AccordionView;
import org.joda.time.YearMonth;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class ColumnsListFragment extends Fragment {
    @Nullable
    @Optional
    @InjectView(R.id.accordion)
    AccordionView mAccordion;

    @ValueType
    private /* final */ int mType = TYPE_NONE;
    @NonNull
    private /* final */ String mGuid;
    @NonNull
    private /* final */ String mMinistryId = Ministry.INVALID_ID;
    @NonNull
    private /* final */ Mcc mMcc = Mcc.UNKNOWN;
    @NonNull
    private /* final */ YearMonth mPeriod;

    public static ColumnsListFragment newInstance(@ValueType final int type, @NonNull final String guid,
                                                  @NonNull final String ministryId, @NonNull final Mcc mcc,
                                                  @NonNull final YearMonth period) {
        final ColumnsListFragment fragment = new ColumnsListFragment();

        final Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_GUID, guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putString(ARG_MCC, mcc.toString());
        args.putString(ARG_PERIOD, period.toString());
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
        mPeriod = YearMonth.parse(args.getString(ARG_PERIOD));
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
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void setupAccordion() {
        if (mAccordion != null) {
            mAccordion.setAdapter(new AccordionAdapter());
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

        final int mPagerId;

        public ViewHolder(@NonNull final View header, @NonNull final View content) {
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
        private MeasurementType.Column[] mColumns = {MeasurementType.Column.FAITH, MeasurementType.Column.FRUIT,
                MeasurementType.Column.OUTCOME};

        @Override
        public int getCount() {
            return mColumns.length;
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
            // update label and logo based on column
            final int label;
            final int logoTop;
            final int logoBottom;
            switch (mColumns[position]) {
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
                    logoTop = R.drawable.ic_header_measurements_faith;
                    logoBottom = R.drawable.ic_header_measurements_faith_under;
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
                if (fragment == null) {
                    fm.beginTransaction().replace(holder.mPagerId, MeasurementsPagerFragment
                            .newInstance(mType, mGuid, mMinistryId, mMcc, mPeriod, mColumns[position])).commit();
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
