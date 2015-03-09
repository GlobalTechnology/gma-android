package com.expidev.gcmapp.support.v4.fragment.measurement;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.ARG_MCC;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.ARG_PERIOD;
import static com.expidev.gcmapp.Constants.ARG_PERMLINK;
import static com.expidev.gcmapp.model.measurement.MeasurementValue.ValueType;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;

import butterknife.ButterKnife;

public class MeasurementValueFragment extends Fragment {
    private static final String ARG_TYPE = MeasurementValueFragment.class.getName() + ".ARG_TYPE";

    @ValueType
    private int mType;
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
        mType = args.getInt(ARG_TYPE);
        mGuid = args.getString(ARG_GUID);
        mMinistryId = args.getString(ARG_MINISTRY_ID);
        mMcc = Ministry.Mcc.fromRaw(args.getString(ARG_MCC));
        mPermLink = args.getString(ARG_PERMLINK);
        mPeriod = YearMonth.parse(args.getString(ARG_PERIOD));
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        final View view = inflater.inflate(R.layout.fragment_measurement_value, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    /* END lifecycle */
}
