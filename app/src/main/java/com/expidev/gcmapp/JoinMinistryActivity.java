package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.service.AssociatedMinistriesService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class JoinMinistryActivity extends ActionBarActivity
{
    private final String TAG = this.getClass().getSimpleName();
    private final String PREF_NAME = "gcm_prefs";

    List<Ministry> ministryTeamList;
    private LocalBroadcastManager manager;
    private BroadcastReceiver allMinistriesRetrievedReceiver;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_ministry);

        manager = LocalBroadcastManager.getInstance(getApplicationContext());
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        setupBroadcastReceivers();
        GmaApiClient.getAllMinistries(this, preferences.getString("session_ticket", null));
    }

    private void setupBroadcastReceivers()
    {
        Log.i(TAG, "Setting up broadcast receivers");

        allMinistriesRetrievedReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String reason = intent.getStringExtra("reason");
                if(reason != null)
                {
                    Log.e(TAG, "Failed to retrieve ministries: " + reason);
                    finish();
                }
                else
                {
                    Serializable data = intent.getSerializableExtra("ministryTeamList");

                    if(data != null)
                    {
                        ministryTeamList = (ArrayList<Ministry>) data;
                        ArrayAdapter<Ministry> ministryTeamAdapter = new ArrayAdapter<Ministry>(
                            getApplicationContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            ministryTeamList
                        );

                        AutoCompleteTextView ministryTeamAutoComplete =
                            (AutoCompleteTextView)findViewById(R.id.ministry_team_autocomplete);
                        ministryTeamAutoComplete.setAdapter(ministryTeamAdapter);
                    }
                }
            }
        };
        manager.registerReceiver(allMinistriesRetrievedReceiver,
            new IntentFilter(AssociatedMinistriesService.ACTION_RETRIEVE_ALL_MINISTRIES));
    }

    @Override
    public void onStop()
    {
        super.onStop();
        cleanupBroadcastReceivers();
    }

    private void cleanupBroadcastReceivers()
    {
        Log.i(TAG, "Cleaning up broadcast receivers");
        manager.unregisterReceiver(allMinistriesRetrievedReceiver);
        allMinistriesRetrievedReceiver = null;
    }

    public void joinMinistry(View view)
    {
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.ministry_team_autocomplete);

        String ministryName = autoCompleteTextView.getText().toString();
        Ministry chosenMinistry = getMinistryByName(ministryTeamList, ministryName);
        String ministryId = chosenMinistry.getMinistryId();

        AlertDialog alertDialog = new AlertDialog.Builder(this)
            .setTitle("Join Ministry")
            .setMessage("You have joined " + ministryName + " with a ministry ID of: " + ministryId)
            .setNeutralButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    finish();
                }
            })
            .create();

        alertDialog.show();

        //TODO: Give user self assigned team role (save to local storage?)
        //TODO: Send request to join this ministry
        //TODO: Make sure this ministry gets added to the list in Settings
        //TODO: Go back to home page or previous page?
    }

    private Ministry getMinistryByName(List<Ministry> ministryList, String name)
    {
        for(Ministry ministry : ministryList)
        {
            if(ministry.getName().equalsIgnoreCase(name))
            {
                return ministry;
            }
        }
        return null;
    }

    public void cancel(View view)
    {
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join_ministry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
