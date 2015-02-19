package com.expidev.gcmapp.utils;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.PatternMatcher;

import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.service.MeasurementsService;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.service.TrainingService;
import com.expidev.gcmapp.service.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public final class BroadcastUtils
{
    private static final Uri URI_ASSIGNMENTS = Uri.parse("gma://assignments/");
    private static final Uri URI_MINISTRIES = Uri.parse("gma://ministries/");
    private static final Uri URI_MEASUREMENTS = Uri.parse("gma://measurements/");
    private static final Uri URI_MEASUREMENT_DETAILS = Uri.parse("gma://measurementdetails/");

    private static final String ACTION_UPDATE_ASSIGNMENTS = MinistriesService.class.getName() + ".ACTION_UPDATE_ASSIGNMENTS";
    private static final String ACTION_UPDATE_MINISTRIES = MinistriesService.class.getName() + ".ACTION_UPDATE_MINISTRIES";
    private static final String ACTION_UPDATE_MEASUREMENTS = MeasurementsService.class.getName() + ".ACTION_UPDATE_MEASUREMENTS";
    private static final String ACTION_UPDATE_MEASUREMENT_DETAILS = MeasurementsService.class.getName() + ".ACTION_UPDATE_MEASUREMENT_DETAILS";

    public static final String ACTION_START = BroadcastUtils.class.getName() + ".ACTION_START";
    public static final String ACTION_RUNNING = BroadcastUtils.class.getName() + ".ACTION_RUNNING";
    public static final String ACTION_STOP = BroadcastUtils.class.getName() + ".ACTION_STOP";
    
    public static final String ACTION_TYPE = BroadcastUtils.class.getName() + ".ACTION_TYPE";

    public static final String TRAINING_RECEIVED = TrainingService.class.getName() + ".TRAINING_RECEIVED";

    private static Uri assignmentsUri() {
        return URI_ASSIGNMENTS;
    }

    private static Uri ministriesUri() {
        return URI_MINISTRIES;
    }

    private static Uri measurementsUri()
    {
        return URI_MEASUREMENTS;
    }

    private static Uri measurementDetailsUri()
    {
        return URI_MEASUREMENT_DETAILS;
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

    public static Intent startBroadcast()
    {
        return new Intent(ACTION_START);
    }

    public static Intent runningBroadcast()
    {
        return new Intent(ACTION_RUNNING);
    }

    public static Intent stopBroadcast(Type type)
    {
        Intent intent = new Intent(ACTION_STOP);
        intent.putExtra(ACTION_TYPE, type);
        return intent;
    }

    public static IntentFilter startFilter()
    {
        return new IntentFilter(ACTION_START);
    }

    public static IntentFilter runningFilter()
    {
        return new IntentFilter(ACTION_RUNNING);
    }

    public static IntentFilter stopFilter()
    {
        return new IntentFilter(ACTION_STOP);
    }

    public static Intent trainingReceivedBroadcast()
    {
        return new Intent(TRAINING_RECEIVED);
    }

    public static Intent allMinistriesReceivedBroadcast(ArrayList<Ministry> allMinistries)
    {
        Intent intent = stopBroadcast(Type.RETRIEVE_ALL_MINISTRIES);
        intent.putExtra("allMinistries", allMinistries);
        return intent;
    }

    public static Intent associatedMinistriesReceivedBroadcast(ArrayList<AssociatedMinistry> associatedMinistries)
    {
        Intent intent = stopBroadcast(Type.RETRIEVE_ASSOCIATED_MINISTRIES);
        intent.putExtra("associatedMinistries", associatedMinistries);
        return intent;
    }

    public static Intent measurementsReceivedBroadcast(ArrayList<Measurement> measurements)
    {
        Intent intent = stopBroadcast(Type.SEARCH_MEASUREMENTS);
        intent.putExtra("measurements", measurements);
        return intent;
    }

    public static Intent measurementDetailsReceivedBroadcast(MeasurementDetails measurementDetails)
    {
        Intent intent = stopBroadcast(Type.RETRIEVE_MEASUREMENT_DETAILS);
        intent.putExtra("measurementDetails", measurementDetails);
        return intent;
    }

    public static Intent updateAssignmentsBroadcast() {
        return new Intent(ACTION_UPDATE_ASSIGNMENTS, assignmentsUri());
    }

    public static Intent updateMinistriesBroadcast() {
        return new Intent(ACTION_UPDATE_MINISTRIES, ministriesUri());
    }

    public static IntentFilter updateAssignmentsFilter() {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_ASSIGNMENTS);
        addDataUri(filter, assignmentsUri(), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }

    public static IntentFilter updateMinistriesFilter() {
        final IntentFilter filter = new IntentFilter(ACTION_UPDATE_MINISTRIES);
        addDataUri(filter, ministriesUri(), PatternMatcher.PATTERN_LITERAL);
        return filter;
    }

    public static Intent updateMeasurementsBroadcast()
    {
        return new Intent(ACTION_UPDATE_MEASUREMENTS, measurementsUri());
    }

    public static Intent measurementsLoaded(List<Measurement> measurements, String ministryId, String mcc, String period)
    {
        Intent intent = stopBroadcast(Type.LOAD_MEASUREMENTS);
        intent.putExtra("measurements", (ArrayList<Measurement>) measurements);

        // We put the search values here in case there are 0 results and we need to search from the API
        intent.putExtra("ministryId", ministryId);
        intent.putExtra("mcc", mcc);
        intent.putExtra("period", period);
        return intent;
    }

    public static Intent updateMeasurementDetailsBroadcast()
    {
        return new Intent(ACTION_UPDATE_MEASUREMENT_DETAILS, measurementDetailsUri());
    }
}
