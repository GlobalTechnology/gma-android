package com.expidev.gcmapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.expidev.gcmapp.model.Measurement;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.service.MeasurementsService;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.service.Type;
import com.expidev.gcmapp.utils.BroadcastUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class MeasurementsActivity extends ActionBarActivity
{
    private final String TAG = this.getClass().getSimpleName();

    private final String PREF_NAME = "gcm_prefs";

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurements);

        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        setupBroadcastReceivers();
        MinistriesService.retrieveMinistries(this);

    }

    private void setupBroadcastReceivers()
    {
        broadcastManager = LocalBroadcastManager.getInstance(this);

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (BroadcastUtils.ACTION_START.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Started");
                }
                else if (BroadcastUtils.ACTION_RUNNING.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Running");
                }
                else if (BroadcastUtils.ACTION_STOP.equals(intent.getAction()))
                {
                    Type type = (Type) intent.getSerializableExtra(BroadcastUtils.ACTION_TYPE);

                    switch(type)
                    {
                        case SEARCH_MEASUREMENTS:
                            Serializable measurementsData = intent.getSerializableExtra("measurements");

                            if(measurementsData != null)
                            {
                                List<Measurement> measurements = (ArrayList<Measurement>) measurementsData;

                                //TODO: Do stuff with measurements
                                for(Measurement measurement : measurements)
                                {
                                    Log.i(TAG, "Measurements: " + measurement.getName());
                                }
                            }
                            else
                            {
                                Log.w(TAG, "No measurement data");
                            }
                            break;
                        case RETRIEVE_ASSOCIATED_MINISTRIES:
                            Log.i(TAG, "Associated Ministries retrieved");

                            Serializable ministriesData = intent.getSerializableExtra("associatedMinistries");

                            if(ministriesData != null)
                            {
                                List<Ministry> associatedMinistries = (ArrayList<Ministry>) ministriesData;
                                Ministry chosenMinistry = getMinistryForName(
                                    associatedMinistries,
                                    preferences.getString("chosen_ministry", null));

                                MeasurementsService.searchMeasurements(
                                    getApplicationContext(),
                                    chosenMinistry.getMinistryId(),
                                    preferences.getString("chosen_mcc", null),
                                    null,
                                    preferences.getString("session_ticket", null));
                            }
                            else
                            {
                                Log.w(TAG, "No associated ministries");
                            }



                            break;
                        default:
                            Log.i(TAG, "Unhandled Type: " + type);
                            break;
                    }
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtils.startFilter());
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtils.runningFilter());
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtils.stopFilter());
    }

    private Ministry getMinistryForName(List<Ministry> ministryList, String ministryName)
    {
        if(ministryName == null)
        {
            return null;
        }

        for(Ministry ministry : ministryList)
        {
            if(ministryName.equals(ministry.getName()))
            {
                return ministry;
            }
        }

        return null;
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        cleanupBroadcastReceivers();
    }

    private void cleanupBroadcastReceivers()
    {
        broadcastManager = LocalBroadcastManager.getInstance(this);

        broadcastManager.unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_measurements, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void drillIntoMeasurementDetails(View selectedMeasurement)
    {
        //TODO: Integrate measurement details branch
        Log.i(TAG, "drilling into measurement");
    }
}
