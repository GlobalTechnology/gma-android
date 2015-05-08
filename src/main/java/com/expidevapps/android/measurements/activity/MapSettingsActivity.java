package com.expidevapps.android.measurements.activity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.expidevapps.android.measurements.R;

public class MapSettingsActivity extends AppCompatActivity implements CheckBox.OnCheckedChangeListener
{
    private final String TAG = getClass().getSimpleName();

    private final String PREF_NAME = "gcm_prefs";
    
    private boolean targets;
    private boolean groups;
    private boolean churches;
    private boolean multiplyingChurches;
    private boolean trainingActivities;
    private boolean campuses;
    
    private CheckBox targetsCB;
    private CheckBox groupCB;
    private CheckBox churchCB;
    private CheckBox multiChurchCB;
    private CheckBox trainingCB;
    private CheckBox campusCB;

    private SharedPreferences preferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        ActionBar actionBar = getSupportActionBar();
        // when this is set to true, it reloads the map page, which cuts down on performance
        // using the phones back button calls the onPostResume method, which is much better.
        actionBar.setDisplayHomeAsUpEnabled(false);
        
        targetsCB = (CheckBox) findViewById(R.id.cb_targets);
        groupCB = (CheckBox) findViewById(R.id.cb_groups);
        churchCB = (CheckBox) findViewById(R.id.cb_churches);
        multiChurchCB = (CheckBox) findViewById(R.id.cb_mult_churches);
        trainingCB = (CheckBox) findViewById(R.id.cb_training);
        campusCB = (CheckBox) findViewById(R.id.cb_campus);
        
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        targets = preferences.getBoolean("targets", true);
        targetsCB.setChecked(targets);
        targetsCB.setOnCheckedChangeListener(this);
        
        groups = preferences.getBoolean("groups", true);
        groupCB.setChecked(groups);
        groupCB.setOnCheckedChangeListener(this);
        
        churches = preferences.getBoolean("churches", true);
        churchCB.setChecked(churches);
        churchCB.setOnCheckedChangeListener(this);
        
        multiplyingChurches = preferences.getBoolean("multiplyingChurches", true);
        multiChurchCB.setChecked(multiplyingChurches);
        multiChurchCB.setOnCheckedChangeListener(this);
        
        trainingActivities = preferences.getBoolean("trainingActivities", true);
        trainingCB.setChecked(trainingActivities);
        trainingCB.setOnCheckedChangeListener(this);
        
        campuses = preferences.getBoolean("campuses", true);
        campusCB.setChecked(campuses);
        campusCB.setOnCheckedChangeListener(this);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        SharedPreferences.Editor editor = preferences.edit();
        
        switch (buttonView.getId())
        {
            case R.id.cb_targets:
                editor.putBoolean("targets", isChecked);
                break;
            case R.id.cb_groups:
                editor.putBoolean("groups", isChecked);
                break;
            case R.id.cb_churches:
                editor.putBoolean("churches", isChecked);
                break;
            case R.id.cb_mult_churches:
                editor.putBoolean("multiplyingChurches", isChecked);
                break;
            case R.id.cb_training:
                editor.putBoolean("trainingActivities", isChecked);
                break;
            case R.id.cb_campus:
                editor.putBoolean("campuses", isChecked);
                break;
        }

        editor.apply();
    }
}
