package com.expidevapps.android.measurements.activity;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;
import static com.expidevapps.android.measurements.Constants.EXTRA_MCC;
import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERIOD;
import static com.expidevapps.android.measurements.Constants.EXTRA_TYPE;
import static com.expidevapps.android.measurements.model.Measurement.SHOW_ALL;
import static com.expidevapps.android.measurements.model.Measurement.SHOW_FAVOURITE;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_LOCAL;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_NONE;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_PERSONAL;
import static com.expidevapps.android.measurements.model.Task.UPDATE_MINISTRY_MEASUREMENTS;
import static com.expidevapps.android.measurements.model.Task.UPDATE_PERSONAL_MEASUREMENTS;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.MeasurementValue.ValueType;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.content.AssignmentLoader;
import com.expidevapps.android.measurements.support.v4.fragment.measurement.ColumnsListFragment;
import com.expidevapps.android.measurements.sync.GmaSyncService;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.joda.time.YearMonth;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class MeasurementsActivity extends AppCompatActivity {
    private static final String TAG_FRAGMENT_MEASUREMENT_COLUMNS = "measurementColumns";
    private static final YearMonth NOW = YearMonth.now();

    private static final int LOADER_ASSIGNMENT = 1;

    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();

    @NonNull
    private /* final */ GoogleAnalyticsManager mGoogleAnalytics;

    @NonNull
    private /* final */ String mGuid;
    @NonNull
    private /* final */ String mMinistryId = Ministry.INVALID_ID;
    @NonNull
    private /* final */ Mcc mMcc = Mcc.UNKNOWN;

    @Optional
    @Nullable
    @InjectView(R.id.currentPeriod)
    TextView mPeriodView;

    private boolean mPaused = false;
    @Nullable
    private Assignment mAssignment;
    @ValueType
    private int mType = TYPE_NONE;
    @NonNull
    private YearMonth mPeriod = NOW;

    private int mShowMeasurement = SHOW_ALL;

    public static void start(@NonNull final Context context, @NonNull final String guid,
                             @NonNull final String ministryId, @NonNull final Mcc mcc, @ValueType final int type) {
        start(context, guid, ministryId, mcc, type, null);
    }

    public static void start(@NonNull final Context context, @NonNull final String guid,
                             @NonNull final String ministryId, @NonNull final Mcc mcc, @ValueType final int type,
                             @Nullable final YearMonth period) {
        final Intent intent = new Intent(context, MeasurementsActivity.class);
        populateIntent(intent, guid, ministryId, mcc, type, period);
        context.startActivity(intent);
    }

    public static void populateIntent(@NonNull final Intent intent, @NonNull final String guid,
                                      @NonNull final String ministryId, @NonNull final Mcc mcc,
                                      @ValueType final int type, @Nullable final YearMonth period) {
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_GUID, guid);
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        intent.putExtra(EXTRA_MCC, mcc.toString());
        intent.putExtra(EXTRA_PERIOD, (period != null ? period : NOW).toString());
    }

    /* BEGIN lifecycle */

    @Override
    @SuppressWarnings("ResourceType")
    protected void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_measurements_frags);

        mGoogleAnalytics = GoogleAnalyticsManager.getInstance(this);

        ButterKnife.inject(this);

        final Intent intent = this.getIntent();
        mType = intent.getIntExtra(EXTRA_TYPE, mType);
        mGuid = intent.getStringExtra(EXTRA_GUID);
        mMinistryId = intent.getStringExtra(EXTRA_MINISTRY_ID);
        mMcc = Mcc.fromRaw(intent.getStringExtra(EXTRA_MCC));
        mPeriod = YearMonth.parse(intent.getStringExtra(EXTRA_PERIOD));

        // load savedState
        if (savedState != null) {
            mType = savedState.getInt(EXTRA_TYPE, mType);
            if (savedState.containsKey(EXTRA_PERIOD)) {
                mPeriod = YearMonth.parse(savedState.getString(EXTRA_PERIOD));
            }
        }

        syncAdjacentPeriods(false);
        startLoaders();
        updateTitle();
        updateViews();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_measurements, menu);

        // display appropriate measurement type toggles
        if (mAssignment != null) {
            if (mType != TYPE_LOCAL && mAssignment.can(UPDATE_MINISTRY_MEASUREMENTS)) {
                // show type options when available
                final MenuItem item = menu.findItem(R.id.action_measurements_ministry);
                if (item != null) {
                    item.setVisible(true);
                }
            }
            if (mType != TYPE_PERSONAL && mAssignment.can(UPDATE_PERSONAL_MEASUREMENTS)) {
                // show type options when available
                final MenuItem item = menu.findItem(R.id.action_measurements_personal);
                if (item != null) {
                    item.setVisible(true);
                }
            }

            if (mShowMeasurement == SHOW_ALL) {
                // show favourite options when available
                final MenuItem item = menu.findItem(R.id.action_show_favourite_measurements);
                if (item != null) {
                    item.setVisible(true);
                }
            }

            if (mShowMeasurement == SHOW_FAVOURITE) {
                // show favourite options when available
                final MenuItem item = menu.findItem(R.id.action_show_all_measurements);
                if (item != null) {
                    item.setVisible(true);
                }
            }
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // we update the fragment here to guarantee state has been restored
        // see: http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
        loadMeasurementColumnsFragmentIfNeeded();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_measurements_ministry:
                onChangeType(TYPE_LOCAL);
                return true;
            case R.id.action_measurements_personal:
                onChangeType(TYPE_PERSONAL);
                return true;
            case R.id.action_show_favourite_measurements:
                onChangeFeavouriteFilter(SHOW_FAVOURITE);
                return true;
            case R.id.action_show_all_measurements:
                onChangeFeavouriteFilter(SHOW_ALL);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onLoadAssignment(@Nullable final Assignment assignment, final boolean resetting) {
        mAssignment = assignment;

        // stop this activity if an assignment wasn't found
        if (mAssignment == null && !resetting) {
            finish();
        }

        // assignment permissions could have changed, so trigger a type change to the current type
        onChangeType(mType);

        // stop this activity if we have an assignment, but aren't editing a valid measurement value type
        if (mAssignment != null && mType == TYPE_NONE) {
            finish();
        }
    }

    void onChangeType(@ValueType final int type) {
        mType = sanitizeType(type);

        updateTitle();
        supportInvalidateOptionsMenu();
        loadMeasurementColumnsFragmentIfNeeded();
    }

    void onChangeFeavouriteFilter(final int showMeasurement) {
        mShowMeasurement = showMeasurement;
        invalidateOptionsMenu();
        loadMeasurementColumnsFragmentIfNeeded();
    }

    @Optional
    @OnClick(R.id.nextPeriod)
    void onNextPeriod() {
        onChangePeriod(mPeriod.plusMonths(1));
    }

    @Optional
    @OnClick(R.id.previousPeriod)
    void onPrevPeriod() {
        onChangePeriod(mPeriod.minusMonths(1));
    }

    private void onChangePeriod(@NonNull YearMonth period) {
        // don't allow navigating into the future
        if (period.isAfter(NOW)) {
            period = NOW;
        }

        // check if the period is changing
        final boolean changing = !mPeriod.isEqual(period);

        // update period
        mPeriod = period;

        if (changing) {
            // start a data sync
            syncAdjacentPeriods(false);

            // update Period views
            updateViews();

            // reload the measurement columns fragment for the current period
            loadMeasurementColumnsFragmentIfNeeded();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_TYPE, mType);
        outState.putString(EXTRA_PERIOD, mPeriod.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = getSupportLoaderManager();

        final Bundle args = new Bundle(2);
        args.putString(ARG_GUID, mGuid);
        args.putString(ARG_MINISTRY_ID, mMinistryId);

        manager.initLoader(LOADER_ASSIGNMENT, args, mLoaderCallbacksAssignment);
    }

    private void syncAdjacentPeriods(final boolean force) {
        GmaSyncService.syncMeasurements(this, mGuid, mMinistryId, mMcc, mPeriod, force);
        GmaSyncService.syncMeasurements(this, mGuid, mMinistryId, mMcc, mPeriod.minusMonths(1), false);
        if (mPeriod.isBefore(NOW)) {
            GmaSyncService.syncMeasurements(this, mGuid, mMinistryId, mMcc, mPeriod.plusMonths(1), false);
        }
    }

    @ValueType
    private int sanitizeType(@ValueType final int type) {
        if (mAssignment != null) {
            for (@ValueType final int potential : new int[] {type, TYPE_PERSONAL, TYPE_LOCAL}) {
                switch (potential) {
                    case TYPE_PERSONAL:
                        if (mAssignment.can(UPDATE_PERSONAL_MEASUREMENTS)) {
                            return potential;
                        }
                        break;
                    case TYPE_LOCAL:
                        if (mAssignment.can(UPDATE_MINISTRY_MEASUREMENTS)) {
                            return potential;
                        }
                        break;
                }
            }

            return TYPE_NONE;
        } else {
            return type;
        }
    }

    private void updateTitle() {
        setTitle(mType == TYPE_PERSONAL ? R.string.title_activity_measurements_personal :
                         mType == TYPE_LOCAL ? R.string.title_activity_measurements_local :
                                 R.string.title_activity_measurements);
    }

    private void updateViews() {
        if (mPeriodView != null) {
            mPeriodView.setText(mPeriod.toString("MMM yyyy", Locale.getDefault()));
        }
    }

    private void loadMeasurementColumnsFragmentIfNeeded() {
        // short-circuit if we are currently paused to prevent state loss
        if (mPaused) {
            // fragment state will be updated the next time we resume
            return;
        }

        // update the current screen to reflect the current period
        mGoogleAnalytics.sendMeasurementsScreen(mGuid, mMinistryId, mMcc, mPeriod);

        // check for the current fragment
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment existing = fm.findFragmentByTag(TAG_FRAGMENT_MEASUREMENT_COLUMNS);
        if (existing instanceof ColumnsListFragment) {
            final ColumnsListFragment fragment = (ColumnsListFragment) existing;
            if (mType == fragment.getType() && mPeriod.equals(fragment.getPeriod()) && mShowMeasurement == fragment.getShowMeasurement()) {
                // no need to update the fragment
                return;
            }
        }

        // create a new ColumnsListFragment
        // XXX: we use commitAllowingStateLoss because we already prevent state loss by checking mPaused
        final ColumnsListFragment fragment =
                ColumnsListFragment.newInstance(mType, mGuid, mMinistryId, mMcc, mPeriod, mShowMeasurement);
        fm.beginTransaction().replace(R.id.frame_content, fragment, TAG_FRAGMENT_MEASUREMENT_COLUMNS)
                .commitAllowingStateLoss();
    }

    private final class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment> {
        @Nullable
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_ASSIGNMENT:
                    return new AssignmentLoader(MeasurementsActivity.this, args);
            }
            return null;
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Assignment> loader, @Nullable final Assignment assignment) {
            switch (loader.getId()) {
                case LOADER_ASSIGNMENT:
                    onLoadAssignment(assignment, false);
                    break;
            }
        }

        @Override
        public void onLoaderReset(@NonNull final Loader<Assignment> loader) {
            switch (loader.getId()) {
                case LOADER_ASSIGNMENT:
                    onLoadAssignment(null, true);
                    break;
                default:
                    super.onLoaderReset(loader);
            }
        }
    }
}
