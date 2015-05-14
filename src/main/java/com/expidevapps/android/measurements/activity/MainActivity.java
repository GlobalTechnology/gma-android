package com.expidevapps.android.measurements.activity;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_LOCAL;
import static com.expidevapps.android.measurements.model.MeasurementValue.TYPE_PERSONAL;
import static com.expidevapps.android.measurements.model.Task.UPDATE_MINISTRY_MEASUREMENTS;
import static com.expidevapps.android.measurements.model.Task.UPDATE_PERSONAL_MEASUREMENTS;
import static com.expidevapps.android.measurements.support.v4.content.CurrentAssignmentLoader.ARG_LOAD_MINISTRY;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.content.CurrentAssignmentLoader;
import com.expidevapps.android.measurements.support.v4.fragment.MapFragment;
import com.expidevapps.android.measurements.sync.BroadcastUtils;
import com.expidevapps.android.measurements.sync.GmaSyncService;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;
import me.thekey.android.lib.content.TheKeyBroadcastReceiver;
import me.thekey.android.lib.support.v4.content.AttributesLoader;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private static final int LOADER_THEKEY_ATTRIBUTES = 1;
    private static final int LOADER_CURRENT_ASSIGNMENT = 6;

    private static final int REQUEST_LOGIN_ACTIVITY = 0;

    @NonNull
    /* final */ GoogleAnalyticsManager mGoogleAnalytics;
    TheKey mTheKey;

    /* BroadcastReceivers */
    private final LoginBroadcastReceiver mBroadcastReceiverLogin = new LoginBroadcastReceiver();
    private final BroadcastReceiver mBroadcastReceiverNoAssignments = new NoAssignmentsBroadcastReceiver();

    /* Loader callback objects */
    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();
    private final AttributesLoaderCallbacks mLoaderCallbacksAttributes = new AttributesLoaderCallbacks();

    @Nullable
    private String mGuid = null;
    @Nullable
    private Assignment mAssignment;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleAnalytics = GoogleAnalyticsManager.getInstance(this);
        mTheKey = TheKeyImpl.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBroadcastReceivers();
        onUpdateGuid(mTheKey.getDefaultSessionGuid());
        mGoogleAnalytics.sendMapScreen(mGuid);
    }

    void onUpdateGuid(@Nullable final String guid) {
        final boolean changed = !TextUtils.equals(guid, mGuid);

        // cleanup after previous user if active user is changing
        if (changed) {
            // destroy current loaders
            final LoaderManager manager = getSupportLoaderManager();
            manager.destroyLoader(LOADER_CURRENT_ASSIGNMENT);
        }

        // update active user
        mGuid = guid;

        if(changed) {
            // update no assignments receiver
            updateNoAssignmentsBroadcastReceiver();
        }

        // show login dialog if there isn't a valid active user
        if (mGuid == null) {
            showLogin();
        }
        // otherwise start processes for current active user
        else if (changed) {
            syncData(false);
            startLoaders();
            loadMap(mGuid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_join_ministry:
                joinNewMinistry();
                return true;
            case R.id.action_settings:
                if (mGuid != null) {
                    SettingsActivity.start(this, mGuid);
                }
                return true;
            case R.id.action_refresh:
                syncData(true);
                break;
            case R.id.action_logout:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onLoadAttributes(@Nullable final TheKey.Attributes attrs) {
        final ActionBar actionBar = getSupportActionBar();
        //TODO: what should be the default text until attributes have been loaded
        actionBar.setTitle(
            "Welcome" + (attrs != null && attrs.getFirstName() != null ? " " + attrs.getFirstName() : ""));
    }

    /**
     * This event is triggered when a fresh assignment object is loaded
     *
     * @param assignment the new assignment object
     */
    void onLoadCurrentAssignment(@Nullable final Assignment assignment) {
        final Assignment old = mAssignment;
        mAssignment = assignment;

        // determine if the current ministry changed
        final String oldId = old != null ? old.getMinistryId() : Ministry.INVALID_ID;
        final String newId = mAssignment != null ? mAssignment.getMinistryId() : Ministry.INVALID_ID;
        final boolean changed = !oldId.equals(newId);

        // trigger some additional actions if we are changing our current ministry
        if (changed && mAssignment != null) {
            syncData(false);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_LOGIN_ACTIVITY:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        finish();
                        break;
                }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cleanupBroadcastReceivers();
    }

    /* END lifecycle */

    private void setupBroadcastReceivers() {
        mBroadcastReceiverLogin.registerReceiver(LocalBroadcastManager.getInstance(this));
        updateNoAssignmentsBroadcastReceiver();
    }

    private void updateNoAssignmentsBroadcastReceiver() {
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(mBroadcastReceiverNoAssignments);
        if (mGuid != null) {
            broadcastManager.registerReceiver(mBroadcastReceiverNoAssignments,
                                              BroadcastUtils.noAssignmentsFilter(mGuid));
        }
    }

    private void cleanupBroadcastReceivers() {
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        mBroadcastReceiverLogin.unregisterReceiver(broadcastManager);
        broadcastManager.unregisterReceiver(mBroadcastReceiverNoAssignments);
    }

    private void startLoaders() {
        final LoaderManager manager = this.getSupportLoaderManager();

        // build the args used for various loaders
        final Bundle args = new Bundle(2);
        args.putString(ARG_GUID, mGuid);
        args.putBoolean(ARG_LOAD_MINISTRY, false);

        manager.initLoader(LOADER_THEKEY_ATTRIBUTES, null, mLoaderCallbacksAttributes);
        manager.initLoader(LOADER_CURRENT_ASSIGNMENT, args, mLoaderCallbacksAssignment);
    }

    private void syncData(final boolean force) {
        if (mGuid != null) {
            // trigger background syncing of data
            GmaSyncService.syncAssignments(this, mGuid, force);
            GmaSyncService.syncMinistries(this, mGuid, force);
            GmaSyncService.syncMeasurementTypes(this, mGuid, force);
        }
    }

    void joinNewMinistry() {
        if (mGuid != null) {
            JoinMinistryActivity.start(this, mGuid);
        }
    }

    public void goToMeasurements(MenuItem menuItem)
    {
        if (mAssignment != null) {
            if (mAssignment.can(UPDATE_PERSONAL_MEASUREMENTS) || mAssignment.can(UPDATE_MINISTRY_MEASUREMENTS)) {
                MeasurementsActivity
                        .start(this, mAssignment.getGuid(), mAssignment.getMinistryId(), mAssignment.getMcc(),
                               mAssignment.can(UPDATE_PERSONAL_MEASUREMENTS) ? TYPE_PERSONAL : TYPE_LOCAL);
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.title_dialog_blocked))
                        .setMessage(getString(R.string.disallowed_measurements))
                        .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();

                alertDialog.show();
            }
        }
    }

    public void logout() {
        if (mGuid != null) {
            new AlertDialog.Builder(this).setTitle(R.string.logout).setMessage(
                    R.string.logout_message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mGoogleAnalytics.sendLogoutEvent(mGuid);
                    mTheKey.logout(mGuid);
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    private void showLogin() {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN_ACTIVITY);
    }

    private void loadMap(@NonNull final String guid) {
        getSupportFragmentManager().beginTransaction().replace(R.id.map, MapFragment.newInstance(guid)).commit();
    }

    private class LoginBroadcastReceiver extends TheKeyBroadcastReceiver {
        @Override
        protected void onLogin(@NonNull final String guid) {
            onUpdateGuid(guid);
        }

        @Override
        protected void onChangeDefaultSession(@NonNull final String guid) {
            onUpdateGuid(guid);
        }

        @Override
        protected void onLogout(final String guid, final boolean changingUser) {
            if (!changingUser) {
                onUpdateGuid(mTheKey.getDefaultSessionGuid());
            }
        }
    }

    private class NoAssignmentsBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            joinNewMinistry();
        }
    }

    private class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment> {
        @Nullable
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle bundle) {
            switch (id) {
                case LOADER_CURRENT_ASSIGNMENT:
                    return new CurrentAssignmentLoader(MainActivity.this, bundle);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Assignment> loader, @Nullable final Assignment assignment) {
            switch (loader.getId()) {
                case LOADER_CURRENT_ASSIGNMENT:
                    onLoadCurrentAssignment(assignment);
                    break;
            }
        }
    }

    private class AttributesLoaderCallbacks extends SimpleLoaderCallbacks<TheKey.Attributes> {
        @Override
        public Loader<TheKey.Attributes> onCreateLoader(final int id, final Bundle args) {
            switch (id) {
                case LOADER_THEKEY_ATTRIBUTES:
                    return new AttributesLoader(MainActivity.this, mTheKey);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<TheKey.Attributes> loader,
                                   @Nullable final TheKey.Attributes attrs) {
            switch (loader.getId()) {
                case LOADER_THEKEY_ATTRIBUTES:
                    onLoadAttributes(attrs);
                    break;
            }
        }
    }
}
