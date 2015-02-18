package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.MeasurementDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.AbstractDao;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.expidev.gcmapp.service.Type.SAVE_MEASUREMENTS;
import static com.expidev.gcmapp.service.Type.SEARCH_MEASUREMENTS;
import static com.expidev.gcmapp.service.Type.RETRIEVE_MEASUREMENT_DETAILS;
import static com.expidev.gcmapp.service.Type.SYNC_MEASUREMENTS;
import static com.expidev.gcmapp.utils.BroadcastUtils.measurementDetailsReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.measurementsReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.updateMeasurementsBroadcast;

/**
 * Created by William.Randall on 2/4/2015.
 */
public class MeasurementsService extends IntentService
{
    private static final String TAG = MeasurementsService.class.getSimpleName();

    private static final String PREFS_SYNC = "gma_sync";
    private static final String PREF_SYNC_TIME_MEASUREMENTS = "last_synced.measurements";
    private static final String EXTRA_FORCE = MeasurementsService.class.getName() + ".EXTRA_FORCE";

    private static final long HOUR_IN_MS = 60 * 60 * 1000;
    private static final long DAY_IN_MS = 24 * HOUR_IN_MS;
    private static final long STALE_DURATION_MEASUREMENTS = DAY_IN_MS;

    @NonNull
    private MeasurementDao measurementDao;

    private LocalBroadcastManager broadcastManager;

    public MeasurementsService()
    {
        super("MeasurementsService");
    }


    /////////////////////////////////////////////////////
    //           Lifecycle Handlers                   //
    ////////////////////////////////////////////////////
    @Override
    public void onCreate()
    {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcast(startBroadcast());
        measurementDao = MeasurementDao.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        broadcastManager.sendBroadcast(runningBroadcast());
        final Type type = (Type) intent.getSerializableExtra("type");

        try {
            switch (type) {
                case SEARCH_MEASUREMENTS:
                    searchMeasurements(intent);
                    break;
                case RETRIEVE_MEASUREMENT_DETAILS:
                    retrieveDetailsForMeasurement(intent);
                    break;
                case SAVE_MEASUREMENTS:
                    saveMeasurementsToDatabase(intent);
                    break;
                case SYNC_MEASUREMENTS:
                    syncMeasurements(intent);
                    break;
                default:
                    Log.i(TAG, "Unhandled Type: " + type);
                    break;
            }
        } catch (final ApiException e) {
            // XXX: ignore for now, maybe eventually broadcast something on specific ApiExceptions
        }
    }


    /////////////////////////////////////////////////////
    //           Service API                          //
    ////////////////////////////////////////////////////
    private static Intent baseIntent(final Context context, Bundle extras)
    {
        final Intent intent = new Intent(context, MeasurementsService.class);

        if(extras != null)
        {
            intent.putExtras(extras);
        }

        return intent;
    }

    public static void searchMeasurements(
        final Context context,
        String ministryId,
        String mcc,
        String period)
    {
        Bundle extras = new Bundle(4);
        extras.putSerializable("type", SEARCH_MEASUREMENTS);
        extras.putString("ministryId", ministryId);
        extras.putString("mcc", mcc);

        if(period != null)
        {
            extras.putString("period", period);
        }

        context.startService(baseIntent(context, extras));
    }

    public static void retrieveDetailsForMeasurement(
        final Context context,
        String measurementId,
        String ministryId,
        String mcc,
        String period)
    {
        Bundle extras = new Bundle(5);
        extras.putSerializable("type", RETRIEVE_MEASUREMENT_DETAILS);
        extras.putString("measurementId", measurementId);
        extras.putString("ministryId", ministryId);
        extras.putString("mcc", mcc);

        if(period != null)
        {
            extras.putString("period", period);
        }

        context.startService(baseIntent(context, extras));
    }

    public static void saveMeasurementsToDatabase(final Context context, List<Measurement> measurements)
    {
        Bundle extras = new Bundle(2);
        extras.putSerializable("type", SAVE_MEASUREMENTS);
        extras.putSerializable("measurements", (ArrayList<Measurement>) measurements);
        context.startService(baseIntent(context, extras));
    }

    public static void syncMeasurements(final Context context)
    {
        syncMeasurements(context, false);
    }

    public static void syncMeasurements(final Context context, final boolean force)
    {
        Bundle extras = new Bundle(2);
        extras.putSerializable("type", SYNC_MEASUREMENTS);
        extras.putBoolean(EXTRA_FORCE, force);

        context.startService(baseIntent(context, extras));
    }


    /////////////////////////////////////////////////////
    //           Actions                              //
    ////////////////////////////////////////////////////
    private void searchMeasurements(Intent intent) throws ApiException
    {
        String ministryId = intent.getStringExtra("ministryId");
        String mcc = intent.getStringExtra("mcc");
        String period = intent.getStringExtra("period");

        List<Measurement> measurements = searchMeasurements(ministryId, mcc, period);

        if(measurements == null)
        {
            broadcastManager.sendBroadcast(measurementsReceivedBroadcast(null));
        }
        else
        {
            broadcastManager.sendBroadcast(measurementsReceivedBroadcast((ArrayList<Measurement>) measurements));
        }
    }

    private List<Measurement> searchMeasurements(String ministryId, String mcc, String period) throws ApiException
    {
        final GmaApiClient apiClient = GmaApiClient.getInstance(this);
        JSONArray results = apiClient.searchMeasurements(ministryId, mcc, period);

        if(results == null)
        {
            Log.e(TAG, "No measurement results!");
            return null;
        }
        else
        {
            return MeasurementsJsonParser.parseMeasurements(results, ministryId, mcc, period);
        }
    }

    private void retrieveDetailsForMeasurement(Intent intent) throws ApiException
    {
        String measurementId = intent.getStringExtra("measurementId");
        String ministryId = intent.getStringExtra("ministryId");
        String mcc = intent.getStringExtra("mcc");
        String period = intent.getStringExtra("period");

        GmaApiClient apiClient = GmaApiClient.getInstance(this);
        JSONObject json = apiClient.getDetailsForMeasurement(measurementId, ministryId, mcc, period);

        if(json == null)
        {
            Log.e(TAG, "No measurement details!");
        }
        else
        {
            MeasurementDetails measurementDetails = MeasurementsJsonParser.parseMeasurementDetails(json);
            broadcastManager.sendBroadcast(measurementDetailsReceivedBroadcast(measurementDetails));
        }
    }

    private void saveMeasurementsToDatabase(Intent intent)
    {
        List<Measurement> measurements = (ArrayList<Measurement>) intent.getSerializableExtra("measurements");

        if(measurements != null)
        {
            updateMeasurements(measurements);
        }
    }

    private void syncMeasurements(Intent intent) throws ApiException
    {
        final SharedPreferences prefs = this.getSharedPreferences(PREFS_SYNC, MODE_PRIVATE);
        final boolean force = intent.getBooleanExtra(EXTRA_FORCE, false);
        final boolean stale =
            System.currentTimeMillis() - prefs.getLong(PREF_SYNC_TIME_MEASUREMENTS, 0) > STALE_DURATION_MEASUREMENTS;

        // only sync if being forced or the data is stale
        if(force || stale)
        {
            String ministryId = intent.getStringExtra("ministryId");
            String mcc = intent.getStringExtra("mcc");
            String period = intent.getStringExtra("period");

            List<Measurement> measurements = searchMeasurements(ministryId, mcc, period);

            // only update the saved measurements if we received any back
            if(measurements != null)
            {
                updateMeasurements(measurements);
            }
        }
    }

    private void updateMeasurements(List<Measurement> measurements)
    {
        // save measurements for the given period, ministry, and mcc to the database
        final AbstractDao.Transaction transaction = measurementDao.newTransaction();

        try
        {
            transaction.begin();

            // update measurements in local database
            for(final Measurement measurement : measurements)
            {
                measurementDao.saveMeasurement(measurement);

                //TODO: update measurement details in local database
            }

            transaction.setSuccessful();

            // update the sync time
            this.getSharedPreferences(PREFS_SYNC, MODE_PRIVATE).edit()
                .putLong(PREF_SYNC_TIME_MEASUREMENTS, System.currentTimeMillis()).apply();

            // send broadcasts for updated data
            broadcastManager.sendBroadcast(stopBroadcast(SAVE_MEASUREMENTS));
            broadcastManager.sendBroadcast(updateMeasurementsBroadcast());
        }
        catch(final SQLException e)
        {
            Log.e(TAG, "Error syncing measurements", e);
        }
        finally
        {
            transaction.end();
        }
    }
}
