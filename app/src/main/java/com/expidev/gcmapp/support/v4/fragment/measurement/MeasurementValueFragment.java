package com.expidev.gcmapp.support.v4.fragment.measurement;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.ARG_MCC;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.ARG_PERIOD;
import static com.expidev.gcmapp.Constants.ARG_PERMLINK;
import static com.expidev.gcmapp.Constants.ARG_TYPE;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.ValueType;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expidev.gcmapp.MeasurementDetailsActivity;
import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementType;
import com.expidev.gcmapp.model.measurement.MeasurementValue;
import com.expidev.gcmapp.model.measurement.MinistryMeasurement;
import com.expidev.gcmapp.model.measurement.PersonalMeasurement;
import com.expidev.gcmapp.support.v4.content.MeasurementValueLoader;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.joda.time.YearMonth;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class MeasurementValueFragment extends Fragment {
    private static final int LOADER_MEASUREMENT_VALUE = 1;

    @NonNull
    private MeasurementValueCallbacks mLoaderCallbacksMeasurement;

    @ValueType
    private int mValueType;
    @NonNull
    private String mGuid;
    @NonNull
    private String mMinistryId;
    @NonNull
    private Ministry.Mcc mMcc;
    @NonNull
    private String mPermLink;
    @NonNull
    private YearMonth mPeriod;

    @Optional
    @Nullable
    @InjectView(R.id.name)
    TextView mNameView;
    @Optional
    @Nullable
    @InjectView(R.id.value)
    TextView mValueView;

    @Nullable
    private MeasurementType mType = null;
    @Nullable
    private MeasurementValue mValue = null;

    public static MeasurementValueFragment newInstance(@ValueType final int type, @NonNull final String guid,
                                                       @NonNull final String ministryId,
                                                       @NonNull final Ministry.Mcc mcc, @NonNull final String permLink,
                                                       @NonNull final YearMonth period) {
        final MeasurementValueFragment fragment = new MeasurementValueFragment();

        final Bundle args = new Bundle(6);
        args.putInt(ARG_TYPE, type);
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
        //noinspection ResourceType
        mValueType = args.getInt(ARG_TYPE);
        mGuid = args.getString(ARG_GUID);
        mMinistryId = args.getString(ARG_MINISTRY_ID);
        mMcc = Ministry.Mcc.fromRaw(args.getString(ARG_MCC));
        mPermLink = args.getString(ARG_PERMLINK);
        mPeriod = YearMonth.parse(args.getString(ARG_PERIOD));

        // set the loader callbacks
        mLoaderCallbacksMeasurement = new MeasurementValueCallbacks<>(
                mValueType == MeasurementValue.TYPE_LOCAL ? MinistryMeasurement.class : PersonalMeasurement.class);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        final View view = inflater.inflate(R.layout.fragment_measurement_value, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedState) {
        super.onActivityCreated(savedState);
        startLoaders();
    }

    void onLoadMeasurementValue(@Nullable final MeasurementValue value) {
        mValue = value;
        mType = mValue != null ? mValue.getType() : null;
        updateViews();
    }

    @Optional
    @OnClick(R.id.value)
    void onClickValue() {
        if (mType != null) {
            MeasurementDetailsActivity
                    .start(getActivity(), mMinistryId, mMcc, mPermLink, mPeriod, mType.getTotalId(), mType.getName());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void startLoaders() {
        // build loader args
        final Bundle args = new Bundle(5);
        args.putString(ARG_GUID, mGuid);
        args.putString(ARG_MINISTRY_ID, mMinistryId);
        args.putString(ARG_MCC, mMcc.toString());
        args.putString(ARG_PERMLINK, mPermLink);
        args.putString(ARG_PERIOD, mPeriod.toString());

        // start loaders
        final LoaderManager manager = this.getLoaderManager();
        manager.initLoader(LOADER_MEASUREMENT_VALUE, args, mLoaderCallbacksMeasurement);
    }

    private void updateViews() {
        if (mNameView != null) {
            mNameView.setText(mType != null ? mType.getName() : "");
        }
        if (mValueView != null) {
            mValueView.setText(mValue != null ? Integer.toString(mValue.getValue()) : "");
        }
    }

    private class MeasurementValueCallbacks<T extends MeasurementValue> extends SimpleLoaderCallbacks<T> {
        private final Class<T> mClass;

        private MeasurementValueCallbacks(@NonNull final Class<T> clazz) {
            this.mClass = clazz;
        }

        @Override
        public Loader<T> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MEASUREMENT_VALUE:
                    return new MeasurementValueLoader<>(getActivity(), mClass, args);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<T> loader, @Nullable final T value) {
            switch (loader.getId()) {
                case LOADER_MEASUREMENT_VALUE:
                    onLoadMeasurementValue(value);
                    break;
            }
        }
    }
}
