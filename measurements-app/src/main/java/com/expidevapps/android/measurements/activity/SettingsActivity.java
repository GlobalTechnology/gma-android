package com.expidevapps.android.measurements.activity;

import static com.expidevapps.android.measurements.Constants.EXTRA_GUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.expidevapps.android.measurements.Constants;
import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG_SETTINGS = "settings";

    @NonNull
    private /* final */ GoogleAnalyticsManager mGoogleAnalytics;

    @NonNull
    private /* final */ String mGuid;

    public static void start(@NonNull final Context context, @NonNull final String guid) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_GUID, guid);
        context.startActivity(intent);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mGoogleAnalytics = GoogleAnalyticsManager.getInstance(this);

        // process the intent
        final Intent intent = this.getIntent();
        final String guid = intent.getStringExtra(EXTRA_GUID);
        mGuid = guid != null ? guid : Constants.INVALID_GUID;

        // finish activity if we didn't receive a valid intent
        if (Constants.INVALID_GUID.equals(mGuid)) {
            finish();
            return;
        }

        loadSettingsFragment(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleAnalytics.sendSettingsScreen(mGuid);
    }

    /* END lifecycle */

    private void loadSettingsFragment(final boolean force) {
        // only load the settings fragment if it's not currently loaded
        final FragmentManager fm = getSupportFragmentManager();
        if (force || fm.findFragmentByTag(TAG_SETTINGS) == null) {
            fm.beginTransaction().replace(R.id.settings, SettingsFragment.newInstance(mGuid), TAG_SETTINGS).commit();
        }
    }
}
