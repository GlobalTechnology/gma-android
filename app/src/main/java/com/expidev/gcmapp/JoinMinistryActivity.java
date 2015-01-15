package com.expidev.gcmapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

    private Context context;
    Map<String, String> ministryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        context = this;

        //TODO: Need token
        GmaApiClient.getAllMinistries("", new MinistriesTask.MinistriesTaskHandler()
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
                    context,
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
