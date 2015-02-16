package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.model.Measurement;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.expidev.gcmapp.service.Type.SEARCH_MEASUREMENTS;
import static com.expidev.gcmapp.utils.BroadcastUtils.measurementsReceivedBroadcast;

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

    @Override
    public void onCreate()
    {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        final Type type = (Type)intent.getSerializableExtra("type");

        switch(type)
        {
            case SEARCH_MEASUREMENTS:
                searchMeasurements(intent);
                break;
            default:
                break;
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
        String period,
        String sessionTicket)
    {
        Bundle extras = new Bundle(4);
        extras.putSerializable("type", SEARCH_MEASUREMENTS);
        extras.putString("ministryId", ministryId);
        extras.putString("mcc", mcc);
        extras.putString("sessionTicket", sessionTicket);

        if(period != null)
        {
            extras.putString("period", period);
        }

        context.startService(baseIntent(context, extras));
    }


    /////////////////////////////////////////////////////
    //           Actions                              //
    ////////////////////////////////////////////////////
    private void searchMeasurements(Intent intent)
    {
        String ministryId = intent.getStringExtra("ministryId");
        String mcc = intent.getStringExtra("mcc");
        String period = intent.getStringExtra("period");
        String sessionTicket = intent.getStringExtra("sessionTicket");

        GmaApiClient apiClient = new GmaApiClient(this);
        JSONArray results = apiClient.searchMeasurements(ministryId, mcc, period, sessionTicket);

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
}
