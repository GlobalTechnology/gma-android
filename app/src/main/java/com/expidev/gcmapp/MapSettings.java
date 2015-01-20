package com.expidev.gcmapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class MapSettings extends PreferenceActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_map);
    }
}
