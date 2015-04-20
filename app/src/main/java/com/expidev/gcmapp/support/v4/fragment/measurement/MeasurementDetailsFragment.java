package com.expidev.gcmapp.support.v4.fragment.measurement;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.ARG_MCC;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.ARG_PERIOD;
import static com.expidev.gcmapp.Constants.ARG_PERMLINK;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.TYPE_LOCAL;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.TYPE_PERSONAL;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.TYPE_TOTAL;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.MeasurementDetails;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.support.v4.content.MeasurementDetailsLoader;
import com.expidev.gcmapp.support.v7.adapter.MeasurementBreakdownExpandableViewAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import org.ccci.gto.android.common.recyclerview.layoutmanager.LinearLayoutManager;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.joda.time.YearMonth;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class MeasurementDetailsFragment extends Fragment {
    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    private static final int LOADER_MEASUREMENT_DETAILS = 1;

    private final MeasurementDetailsLoaderCallbacks mLoaderCallbacksMeasurementDetails =
            new MeasurementDetailsLoaderCallbacks();

    @NonNull
    private /* final */ String mGuid;
    @NonNull
    private /* final */ String mMinistryId;
    @NonNull
    private /* final */ Ministry.Mcc mMcc;
    @NonNull
    private /* final */ String mPermLink;
    @NonNull
    private /* final */ YearMonth mPeriod;

    @Optional
    @Nullable
    @InjectView(R.id.chart)
    LineChart mChartView;
    @Optional
    @Nullable
    @InjectView(R.id.breakdown)
    RecyclerView mBreakdownView;

    @Nullable
    private RecyclerViewExpandableItemManager mBreakdownExpandableItemManager;
    @Nullable
    private MeasurementBreakdownExpandableViewAdapter mBreakdownAdapter;
    @Nullable
    private RecyclerView.Adapter mWrappedBreakdownAdapter;

    @Nullable
    private MeasurementDetails mDetails;

    @NonNull
    public static MeasurementDetailsFragment newInstance(@NonNull final String guid, @NonNull final String ministryId,
                                                         @NonNull final Ministry.Mcc mcc,
                                                         @NonNull final String permLink,
                                                         @NonNull final YearMonth period) {
        final MeasurementDetailsFragment fragment = new MeasurementDetailsFragment();

        final Bundle args = new Bundle();
        args.putString(ARG_GUID, guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putString(ARG_MCC, mcc.toString());
        args.putString(ARG_PERMLINK, permLink);
        args.putString(ARG_PERIOD, period.toString());
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
        mPermLink = args.getString(ARG_PERMLINK);
        mPeriod = YearMonth.parse(args.getString(ARG_PERIOD));
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        return inflater.inflate(R.layout.fragment_measurement_details, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedState) {
        super.onViewCreated(view, savedState);
        ButterKnife.inject(this, view);
        setupBreakdown(savedState);
        setupChart();
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();
    }

    void onLoadDetails(@Nullable final MeasurementDetails details) {
        mDetails = details;
        updateChart();
        if (mBreakdownAdapter != null) {
            mBreakdownAdapter.updateDetails(details);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state to support screen rotation, etc...
        if (mBreakdownExpandableItemManager != null) {
            outState.putParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                                   mBreakdownExpandableItemManager.getSavedState());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupBreakdown();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();

        // build the args for the MeasurementDetails loader
        final Bundle args = new Bundle();
        args.putString(ARG_GUID, mGuid);
        args.putString(ARG_MINISTRY_ID, mMinistryId);
        args.putString(ARG_MCC, mMcc.toString());
        args.putString(ARG_PERMLINK, mPermLink);
        args.putString(ARG_PERIOD, mPeriod.toString());

        // start the MeasurementTypes Cursor loader
        manager.initLoader(LOADER_MEASUREMENT_DETAILS, args, mLoaderCallbacksMeasurementDetails);
    }

    private void setupBreakdown(@Nullable final Bundle savedState) {
        if (mBreakdownView != null) {
            mBreakdownView.setHasFixedSize(false);
            mBreakdownView.setLayoutManager(new LinearLayoutManager(getActivity()));

            // create and attach adapter
            mBreakdownExpandableItemManager = new RecyclerViewExpandableItemManager(
                    savedState != null ? savedState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null);
            mBreakdownAdapter = new MeasurementBreakdownExpandableViewAdapter();
            mWrappedBreakdownAdapter = mBreakdownExpandableItemManager.createWrappedAdapter(mBreakdownAdapter);
            mBreakdownView.setAdapter(mWrappedBreakdownAdapter);

            // set custom animator
            mBreakdownView.setItemAnimator(new RefactoredDefaultItemAnimator());

            // attach the RecyclerView to the ExpandableItemManager
            mBreakdownExpandableItemManager.attachRecyclerView(mBreakdownView);
        }
    }

    private void cleanupBreakdown() {
        if (mBreakdownView != null) {
            mBreakdownView.setItemAnimator(null);
            mBreakdownView.setAdapter(null);
        }
        if (mBreakdownExpandableItemManager != null) {
            mBreakdownExpandableItemManager.release();
            mBreakdownExpandableItemManager = null;
        }
        if (mWrappedBreakdownAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedBreakdownAdapter);
            mWrappedBreakdownAdapter = null;
        }
    }

    private void setupChart() {
        if (mChartView != null) {
            mChartView.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            mChartView.setTouchEnabled(false);

            updateChart();
        }
    }

    private void updateChart() {
        if (mChartView != null) {
            if (mDetails != null) {
                // generate & attach data set
                final Table<Integer, YearMonth, Integer> history = mDetails.getHistory();
                final List<YearMonth> periods = Lists.newArrayList(history.columnKeySet());
                final EntryFunction converter = new EntryFunction(periods);
                final LineData data = new LineData(Lists.transform(periods, Functions.toStringFunction()));
                for (final int type : history.rowKeySet()) {
                    final LineDataSet dataSet = new LineDataSet(
                            Lists.newArrayList(Iterables.transform(history.row(type).entrySet(), converter)), null);
                    final int rawColor;
                    switch (type) {
                        case TYPE_LOCAL:
                            dataSet.setLabel("Local Team");
                            rawColor = R.color.measurements_graph_local;
                            break;
                        case TYPE_PERSONAL:
                            dataSet.setLabel("Personal");
                            rawColor = R.color.measurements_graph_personal;
                            break;
                        case TYPE_TOTAL:
                        default:
                            dataSet.setLabel("Total");
                            rawColor = R.color.measurements_graph_total;
                            break;
                    }
                    final int color = getActivity().getResources().getColor(rawColor);
                    dataSet.setColor(color);
                    dataSet.setCircleColor(color);
                    data.addDataSet(dataSet);
                }
                mChartView.setData(data);
            } else {
                mChartView.clear();
            }
        }
    }

    private final class MeasurementDetailsLoaderCallbacks extends SimpleLoaderCallbacks<MeasurementDetails> {
        @Nullable
        @Override
        public Loader<MeasurementDetails> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MEASUREMENT_DETAILS:
                    return new MeasurementDetailsLoader(getActivity(), args);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<MeasurementDetails> loader,
                                   @Nullable final MeasurementDetails data) {
            switch (loader.getId()) {
                case LOADER_MEASUREMENT_DETAILS:
                    onLoadDetails(data);
                    break;
            }
        }
    }

    private static final class EntryFunction implements Function<Map.Entry<YearMonth, Integer>, Entry> {
        @NonNull
        private final List<YearMonth> mPeriods;

        EntryFunction(@NonNull final List<YearMonth> periods) {
            mPeriods = periods;
        }

        @Nullable
        @Override
        public Entry apply(@Nullable final Map.Entry<YearMonth, Integer> input) {
            if (input != null) {
                return new Entry(input.getValue(), mPeriods.indexOf(input.getKey()));
            }
            return null;
        }
    }
}
