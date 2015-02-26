package com.expidev.gcmapp.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.Constants;
import com.expidev.gcmapp.db.MeasurementDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.ccci.gto.android.common.db.AbstractDao;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.expidev.gcmapp.service.Type.RETRIEVE_AND_SAVE_MEASUREMENTS;
import static com.expidev.gcmapp.service.Type.SAVE_MEASUREMENTS;
import static com.expidev.gcmapp.service.Type.SAVE_MEASUREMENT_DETAILS;
import static com.expidev.gcmapp.service.Type.SYNC_MEASUREMENTS;
import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.updateMeasurementDetailsBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.updateMeasurementsBroadcast;

/**
 * Created by William.Randall on 2/4/2015.
 */
public class MeasurementsService extends ThreadedIntentService
{
    private static final String TAG = MeasurementsService.class.getSimpleName();

    private static final String PREFS_SYNC = "gma_sync";
    private static final String PREF_SYNC_TIME_MEASUREMENTS = "last_synced.measurements";
    private static final String PREF_SYNC_TIME_MEASUREMENT_DETAILS = "last_synced.measurement_details";
    private static final String EXTRA_FORCE = MeasurementsService.class.getName() + ".EXTRA_FORCE";
    private static final String EXTRA_TYPE = "type";

    private static final long HOUR_IN_MS = 60 * 60 * 1000;
    private static final long DAY_IN_MS = 24 * HOUR_IN_MS;
    private static final long STALE_DURATION_MEASUREMENTS = DAY_IN_MS;

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @NonNull
    private MeasurementDao measurementDao;

    private LocalBroadcastManager broadcastManager;

    public MeasurementsService()
    {
        super("MeasurementsService", 5);
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
        Log.i(TAG, "Action Started");
        measurementDao = MeasurementDao.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        broadcastManager.sendBroadcast(runningBroadcast());
        Log.i(TAG, "Action Running");
        final Type type = (Type) intent.getSerializableExtra(EXTRA_TYPE);

        try {
            switch (type) {
                case SAVE_MEASUREMENTS:
                    saveMeasurementsToDatabase(intent);
                    break;
                case SYNC_MEASUREMENTS:
                    syncMeasurements(intent);
                    break;
                case RETRIEVE_AND_SAVE_MEASUREMENTS:
                    retrieveAndSaveInitialMeasurementsAndDetails(intent);
                    break;
                case SAVE_MEASUREMENT_DETAILS:
                    saveMeasurementDetailsToDatabase(intent);
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

    public static void saveMeasurementsToDatabase(final Context context, List<Measurement> measurements)
    {
        Bundle extras = new Bundle(2);
        extras.putSerializable(EXTRA_TYPE, SAVE_MEASUREMENTS);
        extras.putSerializable("measurements", (ArrayList<Measurement>) measurements);
        context.startService(baseIntent(context, extras));
    }

    public static void syncMeasurements(final Context context, String ministryId, String mcc, String period)
    {
        syncMeasurements(context, ministryId, mcc, period, false);
    }

    public static void syncMeasurements(
        final Context context,
        String ministryId,
        String mcc,
        String period,
        final boolean force)
    {
        Bundle extras = new Bundle(5);
        extras.putSerializable(EXTRA_TYPE, SYNC_MEASUREMENTS);
        extras.putString(Constants.ARG_MINISTRY_ID, ministryId);
        extras.putString(Constants.ARG_MCC, mcc);
        extras.putString(Constants.ARG_PERIOD, setPeriodToCurrentIfNecessary(period));
        extras.putBoolean(EXTRA_FORCE, force);

        context.startService(baseIntent(context, extras));
    }

    public static void retrieveAndSaveInitialMeasurements(
        final Context context,
        String ministryId,
        String mcc,
        String period)
    {
        Bundle extras = new Bundle(4);

        extras.putSerializable(EXTRA_TYPE, RETRIEVE_AND_SAVE_MEASUREMENTS);
        extras.putString(Constants.ARG_MINISTRY_ID, ministryId);
        extras.putString(Constants.ARG_MCC, mcc);
        extras.putString(Constants.ARG_PERIOD, setPeriodToCurrentIfNecessary(period));

        context.startService(baseIntent(context, extras));
    }

    public static void saveMeasurementDetailsToDatabase(final Context context, MeasurementDetails measurementDetails)
    {
        Bundle extras = new Bundle(2);
        extras.putSerializable(EXTRA_TYPE, SAVE_MEASUREMENT_DETAILS);
        extras.putSerializable("measurementDetails", measurementDetails);
        context.startService(baseIntent(context, extras));
    }

    private static String setPeriodToCurrentIfNecessary(String period)
    {
        if(period == null)
        {
            Calendar calendar = Calendar.getInstance();
            period = dateFormat.format(calendar.getTime());
        }
        return period;
    }


    /////////////////////////////////////////////////////
    //           Actions                              //
    ////////////////////////////////////////////////////

    private List<Measurement> searchMeasurements(String ministryId, String mcc, String period) throws ApiException
    {
        final GmaApiClient apiClient = GmaApiClient.getInstance(this);
        period = setPeriodToCurrentIfNecessary(period);
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

    private MeasurementDetails retrieveDetailsForMeasurement(
        String measurementId,
        String ministryId,
        String mcc,
        String period) throws ApiException
    {
        GmaApiClient apiClient = GmaApiClient.getInstance(this);
        JSONObject json = apiClient.getDetailsForMeasurement(measurementId, ministryId, mcc, period);

        if(json == null)
        {
            Log.e(TAG, "No measurement details!");
            return null;
        }
        else
        {
            Log.i(TAG, "Measurement details retrieved: " + json);
            MeasurementDetails measurementDetails = MeasurementsJsonParser.parseMeasurementDetails(json);

            measurementDetails.setMeasurementId(measurementId);
            measurementDetails.setMinistryId(ministryId);
            measurementDetails.setMcc(mcc);
            measurementDetails.setPeriod(period);

            return measurementDetails;
        }
    }

    private void saveMeasurementsToDatabase(Intent intent)
    {
        @SuppressWarnings(value = "unchecked")
        List<Measurement> measurements = (ArrayList<Measurement>) intent.getSerializableExtra("measurements");

        if(measurements != null)
        {
            updateMeasurements(measurements);
        }
        Log.i(TAG, "Measurements saved to the database");
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
            String ministryId = intent.getStringExtra(Constants.ARG_MINISTRY_ID);
            String mcc = intent.getStringExtra(Constants.ARG_MCC);
            String period = intent.getStringExtra(Constants.ARG_PERIOD);

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
                measurement.setLastSynced(new Date());
                measurementDao.saveMeasurement(measurement);
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
            Log.e(TAG, "Error updating measurements", e);
        }
        finally
        {
            transaction.end();
        }
    }

    private void retrieveAndSaveInitialMeasurementsAndDetails(Intent intent) throws ApiException
    {
        String ministryId = intent.getStringExtra(Constants.ARG_MINISTRY_ID);
        String mcc = intent.getStringExtra(Constants.ARG_MCC);
        String period = intent.getStringExtra(Constants.ARG_PERIOD);

        if(ministryId == null || mcc == null)
        {
            String logMessage = "Null";
            if(ministryId == null && mcc == null) logMessage += " Ministry ID and MCC";
            else if(ministryId == null) logMessage += " Ministry ID";
            else logMessage += " MCC";

            Log.w(TAG, logMessage);
            return;
        }

        Calendar previousMonth = Calendar.getInstance();
        previousMonth.add(Calendar.MONTH, -1);

        String previousPeriod = dateFormat.format(previousMonth.getTime());

        // retrieve and save the measurements for the current and previous period
        List<Measurement> measurements = searchMeasurements(ministryId, mcc, period);
        List<Measurement> previousPeriodMeasurements = searchMeasurements(ministryId, mcc, previousPeriod);
        if(previousPeriodMeasurements != null)
        {
            measurements.addAll(previousPeriodMeasurements);
        }

        if(!measurements.isEmpty())
        {
            updateMeasurements(measurements);
        }

        List<MeasurementDetails> measurementDetailsList = new ArrayList<>();

        // retrieve and save all measurement details for the measurements retrieved
        for(Measurement measurement : measurements)
        {
            MeasurementDetails measurementDetails = retrieveDetailsForMeasurement(
                measurement.getMeasurementId(),
                measurement.getMinistryId(),
                measurement.getMcc(),
                measurement.getPeriod());

            if(measurementDetails != null)
            {
                measurementDetailsList.add(measurementDetails);
            }
        }

        if(!measurementDetailsList.isEmpty())
        {
            Log.d(TAG, "Updating measurement details...");
            updateMeasurementDetails(measurementDetailsList);
        }
    }

    private void updateMeasurementDetails(List<MeasurementDetails> measurementDetailsList) throws ApiException
    {
        final AbstractDao.Transaction transaction = measurementDao.newTransaction();

        try
        {
            transaction.begin();

            for(MeasurementDetails measurementDetails : measurementDetailsList)
            {
                measurementDetails.setLastSynced(new Date());
                measurementDao.saveMeasurementDetails(measurementDetails);
            }

            transaction.setSuccessful();

            // update the sync time
            this.getSharedPreferences(PREFS_SYNC, MODE_PRIVATE).edit()
                .putLong(PREF_SYNC_TIME_MEASUREMENT_DETAILS, System.currentTimeMillis()).apply();

            // send broadcasts for updated data
            broadcastManager.sendBroadcast(stopBroadcast(SAVE_MEASUREMENT_DETAILS));
            broadcastManager.sendBroadcast(updateMeasurementDetailsBroadcast());
        }
        catch(final SQLException e)
        {
            Log.e(TAG, "Error updating measurement details", e);
        }
        finally
        {
            transaction.end();
        }
    }

    private void saveMeasurementDetailsToDatabase(Intent intent) throws ApiException
    {
        MeasurementDetails measurementDetails = (MeasurementDetails) intent.getSerializableExtra("measurementDetails");

        if(measurementDetails != null)
        {
            List<MeasurementDetails> rowsToSave = new ArrayList<>();
            rowsToSave.add(measurementDetails);
            updateMeasurementDetails(rowsToSave);
        }
        Log.i(TAG, "Measurement details saved to local storage");
    }
}
