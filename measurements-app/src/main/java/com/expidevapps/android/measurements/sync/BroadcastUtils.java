package com.expidevapps.android.measurements.sync;

import static com.expidevapps.android.measurements.Constants.EXTRA_CHURCH_IDS;
import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.EXTRA_PERMLINKS;
import static com.expidevapps.android.measurements.Constants.EXTRA_PREFERENCES;
import static com.expidevapps.android.measurements.Constants.EXTRA_TRAINING_IDS;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Ministry.Mcc;

import org.joda.time.YearMonth;

import java.util.Locale;

public final class BroadcastUtils {
    private static final Uri URI_ASSIGNMENTS = Uri.parse("gma://assignments/");
    private static final Uri URI_CHURCHES = Uri.parse("gma://churches/");
    private static final Uri URI_MINISTRIES = Uri.parse("gma://ministries/");
    private static final Uri URI_TRAINING = Uri.parse("gma://training/");
    private static final Uri URI_MEASUREMENTS = Uri.parse("gma://measurements/");
    private static final Uri URI_PREFERENCES = Uri.parse("gma://preferences/");

    private static final String ACTION_NO_ASSIGNMENTS = AssignmentSyncTasks.class.getName() + ".ACTION_NO_ASSIGNMENTS";
    private static final String ACTION_UPDATE_ASSIGNMENTS =
            AssignmentSyncTasks.class.getName() + ".ACTION_UPDATE_ASSIGNMENTS";
    private static final String ACTION_UPDATE_CHURCHES = GmaSyncService.class.getName() + ".ACTION_UPDATE_CHURCHES";
    private static final String ACTION_UPDATE_MINISTRIES = GmaSyncService.class.getName() + ".ACTION_UPDATE_MINISTRIES";
    private static final String ACTION_UPDATE_TRAINING = GmaSyncService.class.getName() + ".ACTION_UPDATE_TRAINING";
    private static final String ACTION_UPDATE_FAVORITE_MEASUREMENTS =
            GmaSyncService.class.getName() + ".ACTION_UPDATE_FAVORITE_MEASUREMENTS";
    private static final String ACTION_UPDATE_MEASUREMENT_TYPES =
            GmaSyncService.class.getName() + ".ACTION_UPDATE_MEASUREMENT_TYPES";
    private static final String ACTION_UPDATE_MEASUREMENT_VALUES =
            GmaSyncService.class.getName() + ".ACTION_UPDATE_MEASUREMENT_VALUES";
    private static final String ACTION_UPDATE_MEASUREMENT_DETAILS =
            GmaSyncService.class.getName() + ".ACTION_UPDATE_MEASUREMENT_DETAILS";
    private static final String ACTION_UPDATE_PREFERENCES =
            UserPreferenceSyncTasks.class.getName() + ".ACTION_UPDATE_PREFERENCES";

    private static Uri assignmentsUri() {
        return URI_ASSIGNMENTS;
    }

    private static Uri.Builder assignmentsUriBuilder() {
        return URI_ASSIGNMENTS.buildUpon();
    }

    private static Uri assignmentsUri(@NonNull final String guid) {
        return assignmentsUriBuilder().appendPath(guid.toUpperCase(Locale.US)).build();
    }

    private static Uri churchesUri() {
        return URI_CHURCHES;
    }

    private static Uri.Builder churchesUriBuilder() {
        return URI_CHURCHES.buildUpon();
    }

    private static Uri churchesUri(@NonNull final String ministryId) {
        return churchesUriBuilder().appendPath(ministryId.toUpperCase(Locale.US)).build();
    }

    private static Uri ministriesUri() {
        return URI_MINISTRIES;
    }
    
    private static Uri trainingUri()
    {
        return URI_TRAINING;
    }

    private static Uri.Builder trainingUriBuilder() {
        return URI_TRAINING.buildUpon();
    }

    private static Uri trainingUri(@NonNull final String ministryId) {
        return trainingUriBuilder().appendPath(ministryId.toUpperCase(Locale.US)).build();
    }

    /* BEGIN measurement uri generation */

    private static Uri measurementsUri(@NonNull final String guid, @NonNull final String ministryId,
                                       @NonNull final Mcc mcc) {
        return URI_MEASUREMENTS.buildUpon().appendPath(guid).appendPath(ministryId).appendPath(mcc.toString()).build();
    }

    private static Uri.Builder measurementsUriBuilder(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                                      @NonNull final YearMonth period) {
        return URI_MEASUREMENTS.buildUpon().appendPath(ministryId).appendPath(mcc.toString())
                .appendPath(period.toString());
    }

    private static Uri.Builder measurementsUriBuilder(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                                      @NonNull final YearMonth period, @NonNull final String guid) {
        return measurementsUriBuilder(ministryId, mcc, period).appendPath(guid);
    }

    private static Uri measurementsUri(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                       @NonNull final YearMonth period) {
        return measurementsUriBuilder(ministryId, mcc, period).build();
    }

    private static Uri measurementsUri(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                       @NonNull final YearMonth period, @NonNull final String guid) {
        return measurementsUriBuilder(ministryId, mcc, period, guid).build();
    }

    private static Uri measurementsUri(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                       @NonNull final YearMonth period, @NonNull final String guid,
                                       @NonNull final String permLink) {
        return measurementsUriBuilder(ministryId, mcc, period, guid).appendPath(permLink).build();
    }

    /* END measurement uri generation */

    private static Uri preferencesUri(@NonNull final String guid) {
        return URI_PREFERENCES.buildUpon().appendPath(guid.toUpperCase(Locale.US)).build();
    }

    /* Intent Filter generation methods */

    private static void addDataUri(final IntentFilter filter, final Uri uri, final int type) {
        final String scheme = uri.getScheme();
        if (scheme != null) {
            filter.addDataScheme(scheme);
        }
        final String host = uri.getHost();
        if (host != null) {
            filter.addDataAuthority(host, null);
        }
        final String path = uri.getPath();
        if (path != null) {
            filter.addDataPath(path, type);
        }
    }

    /* BEGIN Assignment broadcasts */

    public static Intent noAssignmentsBroadcast(@NonNull final String guid) {
        return new Intent(ACTION_NO_ASSIGNMENTS, assignmentsUri(guid));
    }

    public static Intent updateAssignmentsBroadcast() {
        return new Intent(ACTION_UPDATE_ASSIGNMENTS, assignmentsUri());
    }

    public static Intent updateAssignmentsBroadcast(@NonNull final String guid) {
        return new Intent(ACTION_UPDATE_ASSIGNMENTS, assignmentsUri(guid));
    }

    /* END Assignment broadcasts */

    /* BEGIN Church broadcasts */

    public static Intent updateChurchesBroadcast(@NonNull final long... ids) {
        return updateChurchesBroadcast(null, ids);
    }

    public static Intent updateChurchesBroadcast(@Nullable final String ministryId, @NonNull final long... ids) {
        final Intent intent =
                new Intent(ACTION_UPDATE_CHURCHES, ministryId != null ? churchesUri(ministryId) : churchesUri());
        intent.putExtra(EXTRA_CHURCH_IDS, ids);
        return intent;
    }

    /* END Church broadcasts */

    public static Intent updateMinistriesBroadcast() {
        return new Intent(ACTION_UPDATE_MINISTRIES, ministriesUri());
    }

    public static Intent updateTrainingBroadcast(@NonNull final long... ids) {
        return updateTrainingBroadcast(null, ids);        
    }

    public static Intent updateTrainingBroadcast(@Nullable final String ministryId, @NonNull final long... ids) {
        final Intent intent =
                new Intent(ACTION_UPDATE_TRAINING, ministryId != null ? trainingUri(ministryId) : trainingUri());
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        intent.putExtra(EXTRA_TRAINING_IDS, ids);
        return intent;
    }

    /* BEGIN Measurement broadcasts */

    public static Intent updateFavoriteMeasurementsBroadcast(@NonNull final String guid,
                                                             @NonNull final String ministryId, @NonNull final Mcc mcc,
                                                             @NonNull final String... permLinks) {
        final Intent intent = new Intent(ACTION_UPDATE_FAVORITE_MEASUREMENTS, measurementsUri(guid, ministryId, mcc));
        intent.putExtra(EXTRA_PERMLINKS, permLinks);
        return intent;
    }

    public static Intent updateMeasurementTypesBroadcast(@NonNull final String... permLinks) {
        final Intent intent = new Intent(ACTION_UPDATE_MEASUREMENT_TYPES);
        intent.putExtra(EXTRA_PERMLINKS, permLinks);
        return intent;
    }

    public static Intent updateMeasurementValuesBroadcast(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                                          @NonNull final YearMonth period, @NonNull final String guid,
                                                          @NonNull final String... permLinks) {
        final Intent intent =
                new Intent(ACTION_UPDATE_MEASUREMENT_VALUES, measurementsUri(ministryId, mcc, period, guid));
        intent.putExtra(EXTRA_PERMLINKS, permLinks);
        return intent;
    }

    public static Intent updateMeasurementDetailsBroadcast(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                                           @NonNull final YearMonth period, @NonNull final String guid,
                                                           @NonNull final String permLink) {
        return new Intent(ACTION_UPDATE_MEASUREMENT_DETAILS, measurementsUri(ministryId, mcc, period, guid, permLink));
    }

    /* END Measurement broadcasts */

    /* BEGIN User Preference broadcasts */

    public static Intent updatePreferencesBroadcast(@NonNull final String guid, @NonNull final String... prefs) {
        final Intent intent = new Intent(ACTION_UPDATE_PREFERENCES, preferencesUri(guid));
        intent.putExtra(EXTRA_PREFERENCES, prefs);
        return intent;
    }

    /* END User Preference broadcasts */

    /* BEGIN Assignment filters */

    public static IntentFilter noAssignmentsFilter(@NonNull final String guid) {
        final IntentFilter filter = new IntentFilter(ACTION_NO_ASSIGNMENTS);
        addDataUri(filter, assignmentsUri(guid), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }

    public static IntentFilter updateAssignmentsFilter() {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_ASSIGNMENTS);
        addDataUri(filter, assignmentsUri(), PatternMatcher.PATTERN_PREFIX);
        return filter;
    }

    public static IntentFilter updateAssignmentsFilter(@NonNull final String guid) {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_ASSIGNMENTS);
        addDataUri(filter, assignmentsUri(guid), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }

    /* END Assignment filters */

    /* BEGIN Church filters */

    public static IntentFilter updateChurchesFilter() {
        return updateChurchesFilter(null);
    }

    public static IntentFilter updateChurchesFilter(@Nullable final String ministryId) {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_CHURCHES);
        if (ministryId == null) {
            addDataUri(filter, churchesUri(), PatternMatcher.PATTERN_PREFIX);
        } else {
            addDataUri(filter, churchesUri(ministryId), PatternMatcher.PATTERN_LITERAL);
        }
        return filter;
    }

    /* END Church filters */

    public static IntentFilter updateMinistriesFilter() {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_MINISTRIES);
        addDataUri(filter, ministriesUri(), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }
    
    public static IntentFilter updateTrainingFilter()
    {
        return updateTrainingFilter(null);
    }

    public static IntentFilter updateTrainingFilter(@Nullable final String ministryId) {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_TRAINING);
        if (ministryId == null) {
            addDataUri(filter, trainingUri(), PatternMatcher.PATTERN_PREFIX);
        } else {
            addDataUri(filter, trainingUri(ministryId), PatternMatcher.PATTERN_LITERAL);
        }
        return filter;
    }

    /* BEGIN Measurement filters */

    @NonNull
    public static IntentFilter updateMeasurementTypesFilter() {
        return new IntentFilter(ACTION_UPDATE_MEASUREMENT_TYPES);
    }

    public static IntentFilter updateMeasurementValuesFilter(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                                             @NonNull final YearMonth period) {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_MEASUREMENT_VALUES);
        addDataUri(filter, measurementsUri(ministryId, mcc, period), PatternMatcher.PATTERN_PREFIX);
        return filter;
    }

    public static IntentFilter updateMeasurementValuesFilter(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                                             @NonNull final YearMonth period,
                                                             @NonNull final String guid) {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_MEASUREMENT_VALUES);
        addDataUri(filter, measurementsUri(ministryId, mcc, period, guid), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }

    public static IntentFilter updateMeasurementDetailsFilter(@NonNull final String ministryId, @NonNull final Mcc mcc,
                                                              @NonNull final YearMonth period,
                                                              @NonNull final String guid,
                                                              @NonNull final String permLink) {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_MEASUREMENT_DETAILS);
        addDataUri(filter, measurementsUri(ministryId, mcc, period, guid, permLink), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }

    public static IntentFilter updateFavoriteMeasurementsFilter(@NonNull final String guid,
                                                                @NonNull final String ministryId,
                                                                @NonNull final Mcc mcc) {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_FAVORITE_MEASUREMENTS);
        addDataUri(filter, measurementsUri(guid, ministryId, mcc), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }

    /* END Measurement filters */

    /* BEGIN User Preference filters */

    public static IntentFilter updatePreferencesFilter(@NonNull final String guid) {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_PREFERENCES);
        addDataUri(filter, preferencesUri(guid), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }

    /* END User Preference filters */
}
