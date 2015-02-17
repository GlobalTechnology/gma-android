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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.service.Type;
import com.expidev.gcmapp.tasks.CreateAssignmentTask;
import com.expidev.gcmapp.utils.BroadcastUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class JoinMinistryActivity extends ActionBarActivity
{
    private final String TAG = this.getClass().getSimpleName();
    private final String PREF_NAME = "gcm_prefs";

    List<Ministry> ministryTeamList;
    private LocalBroadcastManager manager;
    private BroadcastReceiver broadcastReceiver;
    private SharedPreferences preferences;

    /* BEGIN lifecycle */

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
        MinistriesService.loadAllMinistriesFromLocalStorage(this);
    }

    private void setupBroadcastReceivers()
    {
        Log.i(TAG, "Setting up broadcast receivers");

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
                        case LOAD_ALL_MINISTRIES:
                            Serializable data = intent.getSerializableExtra("allMinistries");

                            if(data != null)
                            {
                                ministryTeamList = (ArrayList<Ministry>) data;
                                ArrayAdapter<Ministry> ministryTeamAdapter = new ArrayAdapter<Ministry>(
                                    getApplicationContext(),
                                    android.R.layout.simple_dropdown_item_1line,
                                    ministryTeamList
                                );

                                AutoCompleteTextView ministryTeamAutoComplete =
                                    (AutoCompleteTextView) findViewById(R.id.ministry_team_autocomplete);
                                ministryTeamAutoComplete.setAdapter(ministryTeamAdapter);
                            }
                            else
                            {
                                //TODO: Should we try to load from the API in this case?
                                Log.e(TAG, "Failed to retrieve ministries");
                                finish();
                            }
                            break;
                        case SAVE_ASSIGNMENT:
                            Log.i(TAG, "Added user to assignment");
                            break;
                        case RETRIEVE_ALL_MINISTRIES:
                            Serializable ministriesToSave = intent.getSerializableExtra("allMinistries");

                            if(ministriesToSave != null)
                            {
                                List<Ministry> allMinistries = (ArrayList<Ministry>) ministriesToSave;
                                MinistriesService.saveAllMinistries(getApplicationContext(), allMinistries);
                            }
                            else
                            {
                                Log.e(TAG, "Failed to retrieve ministries");
                                finish();
                            }
                            break;
                        default:
                            Log.i(TAG, "Unhandled Type: " + type);
                    }
                }
            }
        };
        manager.registerReceiver(broadcastReceiver, BroadcastUtils.startFilter());
        manager.registerReceiver(broadcastReceiver, BroadcastUtils.runningFilter());
        manager.registerReceiver(broadcastReceiver, BroadcastUtils.stopFilter());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        cleanupBroadcastReceivers();
    }

    /* END lifecycle */

    private void cleanupBroadcastReceivers()
    {
        Log.i(TAG, "Cleaning up broadcast receivers");
        manager.unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    public void joinMinistry(View view)
    {
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.ministry_team_autocomplete);

        String ministryName = autoCompleteTextView.getText().toString();
        AssociatedMinistry chosenMinistry = getMinistryByName(ministryTeamList, ministryName);
        String ministryId = chosenMinistry.getMinistryId();

        new CreateAssignmentTask(this, ministryId, Assignment.Role.SELF_ASSIGNED) {
            @Override
            protected void onPostExecute(final Assignment assignment) {
                super.onPostExecute(assignment);

                if (assignment != null) {
                    // display dialog on success
                    // TODO: we should display the dialog when starting and change state to complete when we finish
                    final AssociatedMinistry ministry = assignment.getMinistry();
                    new AlertDialog.Builder(JoinMinistryActivity.this).setTitle("Join Ministry")
                            .setMessage("You have joined " + ministry.getName() + " with a ministry ID of: " +
                                                assignment.getMinistryId()).setNeutralButton(
                            "OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).create().show();
                }

                // TODO: we need to handle failed requests
            }
        }.execute();
    }

    private AssociatedMinistry getMinistryByName(List<Ministry> ministryList, String name)
    {
        for(Ministry ministry : ministryList)
        {
            if(ministry.getName().equalsIgnoreCase(name))
            {
                AssociatedMinistry associatedMinistry = new AssociatedMinistry();
                associatedMinistry.setMinistryId(ministry.getMinistryId());
                associatedMinistry.setName(ministry.getName());
                associatedMinistry.setLastSynced(ministry.getLastSynced());
                return associatedMinistry;
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
