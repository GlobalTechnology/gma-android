package com.expidevapps.android.measurements.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.fragment.measurement.MeasurementDetailsFragment;

import org.joda.time.YearMonth;

import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;
import static com.expidevapps.android.measurements.Constants.EXTRA_MCC;
import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERIOD;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERMLINK;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_NONE;

public class MeasurementDetailsActivity extends AppCompatActivity {
    private static final String TAG_DETAILS = "measurementDetails";

    @NonNull
    private /* final */ GoogleAnalyticsManager mGoogleAnalytics;

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

        mGoogleAnalytics = GoogleAnalyticsManager.getInstance(this);

        final Intent intent = this.getIntent();
        mGuid = intent.getStringExtra(EXTRA_GUID);
        mMinistryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
        mMcc = Ministry.Mcc.fromRaw(intent.getStringExtra(EXTRA_MCC));
        mPermLink = intent.getStringExtra(EXTRA_PERMLINK);
        mPeriod = YearMonth.parse(intent.getStringExtra(EXTRA_PERIOD));

        createDetailsFragmentsIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleAnalytics.sendMeasurementDetailsScreen(mGuid, mMinistryId, mMcc, mPermLink, mPeriod);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final Intent upIntent = NavUtils.getParentActivityIntent(this);
                MeasurementsActivity.populateIntent(upIntent, mGuid, mMinistryId, mMcc, TYPE_NONE, false, mPeriod);

                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
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
