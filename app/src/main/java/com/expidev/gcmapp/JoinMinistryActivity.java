package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.http.MinistriesTask;
import com.expidev.gcmapp.ministries.MinistryJsonParser;
import com.expidev.gcmapp.model.Ministry;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;


public class JoinMinistryActivity extends ActionBarActivity
{
    private final String TAG = this.getClass().getSimpleName();
    private final String PREF_NAME = "gcm_prefs";

    Map<String, String> ministryMap;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        setContentView(R.layout.activity_join_ministry);

        GmaApiClient.getAllMinistries(preferences.getString("session_ticket", null), new MinistriesTask.MinistriesTaskHandler()
        {
            @Override
            public void taskComplete(JSONArray array)
            {
                Log.i(TAG, "Task Complete");
                setContentView(R.layout.activity_join_ministry);
                Log.i(TAG, "Array: " + array);

                List<Ministry> ministryTeamList = MinistryJsonParser.parseMinistriesJson(array);
                ministryMap = MinistryJsonParser.parseMinistriesAsMap(array);

                ArrayAdapter<Ministry> ministryTeamAdapter = new ArrayAdapter<Ministry>(
                    getApplicationContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    ministryTeamList
                );

                AutoCompleteTextView ministryTeamAutoComplete =
                    (AutoCompleteTextView) findViewById(R.id.ministry_team_autocomplete);
                ministryTeamAutoComplete.setAdapter(ministryTeamAdapter);

                int i = 1;
                for(Ministry ministry : ministryTeamList)
                {
                    Log.i(TAG, "Ministry " + i + ": " + ministry.getName());
                    i++;
                }
            }

            @Override
            public void taskFailed(String reason)
            {
                Log.e(TAG, "Failed to retrieve ministries: " + reason);
                finish();
            }
        });
    }

    public void joinMinistry(View view)
    {
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.ministry_team_autocomplete);

        String ministryName = autoCompleteTextView.getText().toString();
        String ministryId = ministryMap.get(ministryName);

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
