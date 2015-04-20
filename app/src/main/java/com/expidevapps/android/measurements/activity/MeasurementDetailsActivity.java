package com.expidevapps.android.measurements.activity;

import static com.expidev.gcmapp.Constants.EXTRA_GUID;
import static com.expidev.gcmapp.Constants.EXTRA_MCC;
import static com.expidev.gcmapp.Constants.EXTRA_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.EXTRA_PERIOD;
import static com.expidev.gcmapp.Constants.EXTRA_PERMLINK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.support.v4.fragment.measurement.MeasurementDetailsFragment;

import org.joda.time.YearMonth;

public class MeasurementDetailsActivity extends ActionBarActivity {
    private static final String TAG_DETAILS = "measurementDetails";

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

    public static void start(@NonNull final Context context, @NonNull final String guid,
                             @NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                             @NonNull final String permLink, @NonNull final YearMonth period) {
        final Intent intent = new Intent(context, MeasurementDetailsActivity.class);
        populateIntent(intent, guid, ministryId, mcc, permLink, period);
        context.startActivity(intent);
    }

    public static void populateIntent(@NonNull final Intent intent, @NonNull final String guid,
                                      @NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                                      @NonNull final String permLink, @NonNull final YearMonth period) {
        intent.putExtra(EXTRA_GUID, guid);
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        intent.putExtra(EXTRA_MCC, mcc.toString());
        intent.putExtra(EXTRA_PERMLINK, permLink);
        intent.putExtra(EXTRA_PERIOD, period.toString());
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_measurement_details);

        final Intent intent = this.getIntent();
        mGuid = intent.getStringExtra(EXTRA_GUID);
        mMinistryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
        mMcc = Ministry.Mcc.fromRaw(intent.getStringExtra(EXTRA_MCC));
        mPermLink = intent.getStringExtra(EXTRA_PERMLINK);
        mPeriod = YearMonth.parse(intent.getStringExtra(EXTRA_PERIOD));

        createDetailsFragmentsIfNeeded();
    }

    /* END lifecycle */

    private void createDetailsFragmentsIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();

        // only create fragment if it doesn't exist
        Fragment fragment = fm.findFragmentByTag(TAG_DETAILS);
        if (fragment == null) {
            fragment = MeasurementDetailsFragment.newInstance(mGuid, mMinistryId, mMcc, mPermLink, mPeriod);
            fm.beginTransaction().replace(R.id.details, fragment, TAG_DETAILS).commit();
        }
    }
}
