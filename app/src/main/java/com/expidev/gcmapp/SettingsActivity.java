package com.expidev.gcmapp;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.service.Type;
import com.expidev.gcmapp.utils.BroadcastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
{
    private final String TAG = getClass().getSimpleName();

    private BroadcastReceiver broadcastReceiver;
    private final String PREF_NAME = "gcm_prefs";
    private SharedPreferences preferences;
    
    private List<AssociatedMinistry> associatedMinistries;

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setupBroadcastReceivers();
        MinistriesService.retrieveMinistries(this);
    }

    private void setupBroadcastReceivers()
    {
        Log.i(TAG, "Setting up broadcast receivers");
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

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
                    Log.i(TAG, "Action Done");

                    Type type = (Type) intent.getSerializableExtra(BroadcastUtils.ACTION_TYPE);

                    switch(type)
                    {
                        case RETRIEVE_ASSOCIATED_MINISTRIES:
                            Log.i(TAG, "Associated Ministries Received");

                            String chosenMinistry = preferences.getString("chosen_ministry", null);
                            String chosenMcc = preferences.getString("chosen_mcc", null);

                            associatedMinistries =
                                (ArrayList<AssociatedMinistry>) intent.getSerializableExtra("associatedMinistries");

                            populateMinistryListPreference(associatedMinistries, chosenMinistry);

                            if(chosenMinistry != null)
                            {
                                populateMissionCriticalComponentsPreference(
                                    associatedMinistries, chosenMinistry, chosenMcc);
                            }
                            break;
                        default:
                            Log.i(TAG, "Unhandled Type: " + type);
                    }
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtils.stopFilter());
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtils.startFilter());
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtils.runningFilter());
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
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen()
    {
        if (!isSimplePreferences(this))
        {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
    }

    private void populateMinistryListPreference(
        final List<AssociatedMinistry> associatedMinistries,
        String chosenMinistry)
    {
        ListPreference ministryListPreference = (ListPreference) findPreference("ministry_team_list");

        List<String> ministryNames = new ArrayList<String>(associatedMinistries.size());

        for(AssociatedMinistry ministry : associatedMinistries)
        {
            ministryNames.add(ministry.getName());
        }
        
        ministryListPreference.setEntries(ministryNames.toArray(new CharSequence[ministryNames.size()]));
        ministryListPreference.setEntryValues(ministryNames.toArray(new CharSequence[ministryNames.size()]));

        if(chosenMinistry != null)
        {
            bindPreferenceSummaryToValue(ministryListPreference, chosenMinistry);
        }
        else
        {
            bindPreferenceSummaryToValue(ministryListPreference);
        }

        ministryListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                String ministryName = newValue.toString();
                bindPreferenceSummaryToValue(preference, ministryName);

                populateMissionCriticalComponentsPreference(associatedMinistries, ministryName, null);
                preferences
                    .edit()
                    .putString("chosen_ministry", ministryName)
                    .apply();
                return true;
            }
        });
    }

    private void populateMissionCriticalComponentsPreference(
        List<AssociatedMinistry> associatedMinistries,
        String chosenMinistryName,
        String chosenMcc)
    {
        ListPreference mccListPreference = (ListPreference) findPreference("mcc_list");
        List<String> options = new ArrayList<String>();

        if(chosenMinistryName != null)
        {
            AssociatedMinistry chosenMinistry = null;
            for(AssociatedMinistry ministry : associatedMinistries)
            {
                if(chosenMinistryName.equalsIgnoreCase(ministry.getName()))
                {
                    chosenMinistry = ministry;
                    break;
                }
            }

            if(chosenMinistry != null)
            {
                if(chosenMinistry.hasDs()) options.add("DS");
                if(chosenMinistry.hasGcm()) options.add("GCM");
                if(chosenMinistry.hasLlm()) options.add("LLM");
                if(chosenMinistry.hasSlm()) options.add("SLM");
            }
        }

        //TODO: Find out what to do here
        if(options.isEmpty())
        {
            mccListPreference.setEntries(new CharSequence[] { "No MCC Options" });
            mccListPreference.setEntryValues(new CharSequence[] { "No MCC Options" });
        }
        else
        {
            mccListPreference.setEntries(options.toArray(new CharSequence[options.size()]));
            mccListPreference.setEntryValues(options.toArray(new CharSequence[options.size()]));
        }

        mccListPreference.setPersistent(true);

        if(chosenMcc != null)
        {
            bindPreferenceSummaryToValue(mccListPreference, chosenMcc);
        }
        else
        {
            bindPreferenceSummaryToValue(mccListPreference);
        }

        mccListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                String mcc = newValue.toString();
                setMccPreference(preference, mcc);

                return true;
            }
        });
    }

    @Override
    public boolean onIsMultiPane()
    {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context)
    {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target)
    {
        if (!isSimplePreferences(this))
        {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    private void setMccPreference(Preference preference, String mcc)
    {
        bindPreferenceSummaryToValue(preference, mcc);

        preferences
            .edit()
            .putString("chosen_mcc", mcc)
            .apply();
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value)
        {
            bindPreferenceSummaryToValue(preference, value.toString());
            return true;
        }
    };

    /**
     * Using this instead of {@link #bindPreferenceSummaryToValue(android.preference.Preference)}
     * allows the calling method to define its own OnPreferenceChanged listener
     * instead of coupling this logic with it
     */
    private static void bindPreferenceSummaryToValue(Preference preference, String stringValue)
    {
        if (preference instanceof ListPreference)
        {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                index >= 0
                    ? listPreference.getEntries()[index]
                    : null);

        }
        else
        {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference)
    {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }
}
