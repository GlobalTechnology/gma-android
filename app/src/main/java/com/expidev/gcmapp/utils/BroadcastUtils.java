package com.expidev.gcmapp.utils;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Measurement;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.service.TrainingService;
import com.expidev.gcmapp.service.Type;

import java.util.ArrayList;

import static com.expidev.gcmapp.Constants.EXTRA_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.EXTRA_TRAINING_IDS;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public final class BroadcastUtils
{
    private static final Uri URI_ASSIGNMENTS = Uri.parse("gma://assignments/");
    private static final Uri URI_MINISTRIES = Uri.parse("gma://ministries/");
    private static final Uri URI_TRAINING = Uri.parse("gma://training/");

    private static final String ACTION_UPDATE_ASSIGNMENTS = MinistriesService.class.getName() + ".ACTION_UPDATE_ASSIGNMENTS";
    private static final String ACTION_UPDATE_MINISTRIES = MinistriesService.class.getName() + ".ACTION_UPDATE_MINISTRIES";
    private static final String ACTION_UPDATE_TRAINING = TrainingService.class.getName() + ".ACTION_UPDATE_TRAINING";

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
    
    public static Intent updateTrainingBroadcast(@NonNull final long... ids)
    {
        return updateTrainingBroadcast(null, ids);        
    }
    
    public static Intent updateTrainingBroadcast(@NonNull final String ministryId, @NonNull final long... ids)
    {
        final Intent intent = new Intent(ACTION_UPDATE_TRAINING);
        intent.putExtra(EXTRA_MINISTRY_ID, ministryId);
        intent.putExtra(EXTRA_TRAINING_IDS, ids);
        return intent;
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
    
    public static IntentFilter updateTrainingFilter()
    {
        return new IntentFilter(ACTION_UPDATE_TRAINING);
    }
}
