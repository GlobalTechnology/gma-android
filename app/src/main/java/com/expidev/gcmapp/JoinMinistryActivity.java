package com.expidev.gcmapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;


public class JoinMinistryActivity extends ActionBarActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_ministry);

        List<String> ministryTeamList = new ArrayList<String>();
        //TODO: get ministries from endpoint
        ministryTeamList.add("Purdue Campus Ministry");

        ArrayAdapter<String> ministryTeamAdapter = new ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            ministryTeamList);


        AutoCompleteTextView ministryTeamAutoComplete =
            (AutoCompleteTextView)findViewById(R.id.ministry_team_autocomplete);
        ministryTeamAutoComplete.setAdapter(ministryTeamAdapter);
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
