package com.expidev.gcmapp.activity;

import static com.expidev.gcmapp.Constants.EXTRA_GUID;
import static com.expidev.gcmapp.Constants.EXTRA_MCC;
import static com.expidev.gcmapp.Constants.EXTRA_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.EXTRA_PERIOD;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.support.v4.fragment.measurement.ColumnsListFragment;

import org.joda.time.YearMonth;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class MeasurementsActivity extends ActionBarActivity {
    private static final YearMonth NOW = YearMonth.now();

    @NonNull
    private String mGuid;
    @NonNull
    private String mMinistryId = Ministry.INVALID_ID;
    @NonNull
    private Ministry.Mcc mMcc = Ministry.Mcc.UNKNOWN;
    @NonNull
    private YearMonth mPeriod = NOW;

    @Optional
    @Nullable
    @InjectView(R.id.currentPeriod)
    TextView mPeriodView;

    public static void start(@NonNull final Context context, @NonNull final String guid,
                             @NonNull final String ministryId, @NonNull final Ministry.Mcc mcc) {
        start(context, guid, ministryId, mcc, null);
    }

    public static void start(@NonNull final Context context, @NonNull final String guid,
                             @NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                             @Nullable final YearMonth period) {
        final Intent intent = new Intent(context, MeasurementsActivity.class);
        intent.putExtra(EXTRA_GUID, guid);
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        intent.putExtra(EXTRA_MCC, mcc.toString());
        intent.putExtra(EXTRA_PERIOD, (period != null ? period : YearMonth.now()).toString());
        context.startActivity(intent);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_measurements_frags);
        ButterKnife.inject(this);

        final Intent intent = this.getIntent();
        mGuid = intent.getStringExtra(EXTRA_GUID);
        mMinistryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
        mMcc = Ministry.Mcc.fromRaw(intent.getStringExtra(EXTRA_MCC));
        mPeriod = YearMonth.parse(intent.getStringExtra(EXTRA_PERIOD));

        // load savedState
        if (savedState != null) {
            if (savedState.containsKey(EXTRA_PERIOD)) {
                mPeriod = YearMonth.parse(savedState.getString(EXTRA_PERIOD));
            }
        }

        updatePeriodView();
        loadMeasurementColumnsFragment();
    }

    @Optional
    @OnClick(R.id.nextPeriod)
    void onNextPeriod() {
        if (mPeriod.isBefore(NOW)) {
            mPeriod = mPeriod.plusMonths(1);

            updatePeriodView();
            loadMeasurementColumnsFragment();
        }
    }

    @Optional
    @OnClick(R.id.previousPeriod)
    void onPrevPeriod() {
        mPeriod = mPeriod.minusMonths(1);

        updatePeriodView();
        loadMeasurementColumnsFragment();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_PERIOD, mPeriod.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void updatePeriodView() {
        if (mPeriodView != null) {
            mPeriodView.setText(mPeriod.toString("MMM yyyy", Locale.getDefault()));
        }
    }

    private void loadMeasurementColumnsFragment() {
        final ColumnsListFragment fragment = ColumnsListFragment.newInstance(mGuid, mMinistryId, mMcc, mPeriod);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment).commit();
    }
}
