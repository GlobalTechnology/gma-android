package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;

import org.ccci.gto.android.common.api.ApiException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.expidev.gcmapp.service.Type.SEARCH_MEASUREMENTS;
import static com.expidev.gcmapp.service.Type.RETRIEVE_MEASUREMENT_DETAILS;
import static com.expidev.gcmapp.utils.BroadcastUtils.measurementDetailsReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.measurementsReceivedBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;

/**
 * Created by William.Randall on 2/4/2015.
 */
public class MeasurementsService extends IntentService
{
    private static final String TAG = MeasurementsService.class.getSimpleName();

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


    /////////////////////////////////////////////////////
    //           Actions                              //
    ////////////////////////////////////////////////////
    private void searchMeasurements(Intent intent) throws ApiException
    {
        String ministryId = intent.getStringExtra("ministryId");
        String mcc = intent.getStringExtra("mcc");
        String period = intent.getStringExtra("period");

        final GmaApiClient apiClient = GmaApiClient.getInstance(this);
        JSONArray results = apiClient.searchMeasurements(ministryId, mcc, period);

        if(results == null)
        {
            Log.e(TAG, "No measurement results!");
            broadcastManager.sendBroadcast(measurementsReceivedBroadcast(null));
        }
        else
        {
            List<Measurement> measurementList = MeasurementsJsonParser.parseMeasurements(results);
            broadcastManager.sendBroadcast(measurementsReceivedBroadcast((ArrayList<Measurement>) measurementList));
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
}
