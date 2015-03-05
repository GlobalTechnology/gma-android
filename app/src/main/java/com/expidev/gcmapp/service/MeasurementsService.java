package com.expidev.gcmapp.service;

import static com.expidev.gcmapp.service.Type.SAVE_MEASUREMENT_DETAILS;
import static com.expidev.gcmapp.service.Type.SYNC_MEASUREMENTS;
import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.updateMeasurementDetailsBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.updateMeasurementsBroadcast;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.Constants;
import com.expidev.gcmapp.db.MeasurementDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.ccci.gto.android.common.db.AbstractDao;
import org.joda.time.YearMonth;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by William.Randall on 2/4/2015.
 */
public class MeasurementsService extends ThreadedIntentService
{
    private static final String TAG = MeasurementsService.class.getSimpleName();

    private static final String PREFS_SYNC = "gma_sync";
    private static final String PREF_SYNC_TIME_MEASUREMENTS = "last_synced.measurements";
    private static final String PREF_SYNC_TIME_MEASUREMENT_DETAILS = "last_synced.measurement_details";
    private static final String EXTRA_TYPE = "type";

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @NonNull
    private GmaApiClient mApi;
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
        mApi = GmaApiClient.getInstance(this);
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
                case SYNC_MEASUREMENTS:
                    syncMeasurements(intent);
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

    public static void syncMeasurements(
        final Context context,
        String ministryId,
        @NonNull final Ministry.Mcc mcc,
        String period,
        Assignment.Role role)
    {
        Bundle extras = new Bundle(4);

        extras.putSerializable(EXTRA_TYPE, SYNC_MEASUREMENTS);
        extras.putString(Constants.ARG_MINISTRY_ID, ministryId);
        extras.putString(Constants.ARG_MCC, mcc.toString());
        extras.putString(Constants.ARG_PERIOD, setPeriodToCurrentIfNecessary(period));
        extras.putSerializable("role", role);

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

    private MeasurementDetails retrieveDetailsForMeasurement(
        String measurementId,
        String ministryId,
        @NonNull final Ministry.Mcc mcc,
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

    private void syncMeasurements(Intent intent) throws ApiException
    {
        String ministryId = intent.getStringExtra(Constants.ARG_MINISTRY_ID);
        if(ministryId == null) {
            ministryId = Ministry.INVALID_ID;
        }
        final Ministry.Mcc mcc = Ministry.Mcc.fromRaw(intent.getStringExtra(Constants.ARG_MCC));
        final String rawPeriod = intent.getStringExtra(Constants.ARG_PERIOD);
        final YearMonth period = rawPeriod != null ? YearMonth.parse(rawPeriod) : YearMonth.now();
        Assignment.Role role = (Assignment.Role) intent.getSerializableExtra("role");

        if(ministryId.equals(Ministry.INVALID_ID) || mcc == Ministry.Mcc.UNKNOWN)
        {
            String logMessage = "Null";
            if(ministryId.equals(Ministry.INVALID_ID) && mcc == Ministry.Mcc.UNKNOWN) logMessage += " Ministry ID and MCC";
            else if(ministryId.equals(Ministry.INVALID_ID)) logMessage += " Ministry ID";
            else logMessage += " MCC";

            Log.w(TAG, logMessage);
            return;
        }

        // retrieve and save the measurements for the current and previous period
        List<Measurement> measurements = mApi.getMeasurements(ministryId, mcc, period);
        List<Measurement> previousPeriodMeasurements = mApi.getMeasurements(ministryId, mcc, period.minusMonths(1));
        if(previousPeriodMeasurements != null)
        {
            measurements.addAll(previousPeriodMeasurements);
        }

        if(measurements == null)
        {
            return;
        }

        if(!measurements.isEmpty())
        {
            updateMeasurements(measurements);
        }

        final List<Assignment.Role> rolesWithDetailsPermissions = Arrays.asList(
            Assignment.Role.LEADER,
            Assignment.Role.INHERITED_LEADER,
            Assignment.Role.MEMBER
        );

        // Skip loading measurement details if it just returns a 401 anyway
        if(rolesWithDetailsPermissions.contains(role))
        {
            List<MeasurementDetails> measurementDetailsList = new ArrayList<>();

            // retrieve and save all measurement details for the measurements retrieved
            for(Measurement measurement : measurements)
            {
                MeasurementDetails measurementDetails = retrieveDetailsForMeasurement(
                    measurement.getMeasurementId(),
                    measurement.getMinistryId(),
                    measurement.getMcc(),
                    measurement.getPeriod().toString());

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
