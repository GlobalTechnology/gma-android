package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expidev.gcmapp.model.Measurement;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.service.MeasurementsService;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.service.Type;
import com.expidev.gcmapp.utils.BroadcastUtils;
import com.expidev.gcmapp.utils.ViewUtils;
import com.expidev.gcmapp.view.TextHeaderView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private Ministry chosenMinistry;
    private String chosenMcc;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurements);

        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        chosenMcc = preferences.getString("chosen_mcc", null);
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
                                drawLayout(chosenMinistry, chosenMcc, measurements);
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
                                //TODO: Handle no chosen ministry (need default somewhere in Main)
                                List<Ministry> associatedMinistries = (ArrayList<Ministry>) ministriesData;
                                chosenMinistry = getMinistryForName(
                                    associatedMinistries,
                                    preferences.getString("chosen_ministry", null));

                                MeasurementsService.searchMeasurements(
                                    getApplicationContext(),
                                    chosenMinistry.getMinistryId(),
                                    chosenMcc,
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

    private void drawLayout(Ministry selectedMinistry, String mcc, List<Measurement> measurements)
    {
        TextView titleView = (TextView) findViewById(R.id.measurement_ministry_name);
        titleView.setText(selectedMinistry.getName() + " (" + mcc + ")");

        List<Measurement> sortedMeasurements = sortMeasurements(measurements);

        LinearLayout dataContainer = (LinearLayout) findViewById(R.id.measurement_data_Layout);

        String previousColumn = sortedMeasurements.get(0).getColumn();
        String firstMeasurementId = sortedMeasurements.get(0).getMeasurementId();

        for(Measurement measurement : sortedMeasurements)
        {
            String column = measurement.getColumn();

            if(!column.equals(previousColumn) || measurement.getMeasurementId().equals(firstMeasurementId))
            {
                // Add the new header and the data
                TextHeaderView headerView = new TextHeaderView(this);
                headerView.setText(column);
                dataContainer.addView(headerView);

                previousColumn = column;
            }
            LinearLayout row = createDataRow(
                measurement.getName(),
                measurement.getTotal(),
                measurement);

            dataContainer.addView(row);
        }
    }

    private LinearLayout createDataRow(String name, int total, final Measurement measurement)
    {
        TextView nameView = createNameView(name);
        TextView totalView = createTotalView(total);
        TextView arrowView = createArrowView();

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        row.addView(nameView);
        row.addView(totalView);
        row.addView(arrowView);
        row.setClickable(true);
        row.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drillIntoMeasurementDetails(measurement);
            }
        });

        return row;
    }

    private TextView createNameView(String name)
    {
        TextView nameView = new TextView(this);
        nameView.setText(name);

        LinearLayout.LayoutParams nameLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameLayoutParams.weight = 1f;
        nameLayoutParams.setMargins(ViewUtils.dpToPixels(this, 10), 0, 0, 0);

        nameView.setLayoutParams(nameLayoutParams);

        return nameView;
    }

    private TextView createTotalView(int total)
    {
        TextView totalView = new TextView(this);
        totalView.setText(Integer.toString(total));

        LinearLayout.LayoutParams totalLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        totalLayoutParams.setMargins(0, 0, ViewUtils.dpToPixels(this, 5), 0);

        totalView.setLayoutParams(totalLayoutParams);

        return totalView;
    }

    private TextView createArrowView()
    {
        TextView arrowView = new TextView(this);
        arrowView.setText(">");

        LinearLayout.LayoutParams arrowLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        arrowLayoutParams.setMargins(0, 0, ViewUtils.dpToPixels(this, 10), 0);

        arrowView.setLayoutParams(arrowLayoutParams);

        return arrowView;
    }

    /**
     * Group measurements together by column (alphabetically), then
     * sort within each group by section (win, build, send)
     */
    List<Measurement> sortMeasurements(List<Measurement> measurements)
    {
        List<Measurement> sortedMeasurements = new ArrayList<>(measurements.size());

        // First create a list of columns
        List<String> distinctColumns = new ArrayList<>();
        for(Measurement measurement : measurements)
        {
            if(!distinctColumns.contains(measurement.getColumn()))
            {
                distinctColumns.add(measurement.getColumn());
            }
        }

        // Sort the columns alphabetically
        Collections.sort(distinctColumns);

        // First group the measurements into their column groups
        for(String column : distinctColumns)
        {
            List<Measurement> measurementGroup = new ArrayList<>();

            for(Measurement measurement : measurements)
            {
                if(measurement.getColumn().equals(column))
                {
                    measurementGroup.add(measurement);
                }
            }

            // Now sort it according to section
            Collections.sort(measurementGroup, sectionComparator());
            sortedMeasurements.addAll(measurementGroup);
        }

        return sortedMeasurements;
    }

    /**
     * Sorts measurements first in order of section: win, build, send
     * Then alphabetically by name, if they have the same section
     */
    private Comparator<Measurement> sectionComparator()
    {
        return new Comparator<Measurement>()
        {
            @Override
            public int compare(Measurement lhs, Measurement rhs)
            {
                if("win".equals(lhs.getSection()))
                {
                    if(rhs.getSection().equals("win"))
                    {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                    else
                    {
                        return -1;
                    }
                }
                else if(lhs.getSection().equals("build"))
                {
                    if(rhs.getSection().equals("win"))
                    {
                        return 1;
                    }
                    else if(rhs.getSection().equals("build"))
                    {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                    else
                    {
                        return -1;
                    }
                }
                else
                {
                    if(rhs.getSection().equals("send"))
                    {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                    else
                    {
                        return 1;
                    }
                }
            }
        };
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

    public void drillIntoMeasurementDetails(Measurement measurement)
    {
        //TODO: Integrate measurement details branch
        Log.i(TAG, "drilling into measurement");
        AlertDialog alertDialog = new AlertDialog.Builder(this)
            .setTitle("Measurement selected")
            .setMessage("Drilling into measurement: " + measurement.getName())
            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            })
            .create();

        alertDialog.show();
    }
}
