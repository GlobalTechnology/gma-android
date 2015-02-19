package com.expidev.gcmapp;

import static com.expidev.gcmapp.BuildConfig.THEKEY_CLIENTID;
import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.expidev.gcmapp.GcmTheKey.GcmBroadcastReceiver;
import com.expidev.gcmapp.db.TrainingDao;
import com.expidev.gcmapp.map.GcmMarker;
import com.expidev.gcmapp.map.MarkerRender;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.service.TrainingService;
import com.expidev.gcmapp.service.Type;
import com.expidev.gcmapp.support.v4.content.CurrentMinistryLoader;
import com.expidev.gcmapp.utils.BroadcastUtils;
import com.expidev.gcmapp.utils.Device;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;

import java.util.List;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;
import me.thekey.android.lib.support.v4.content.AttributesLoader;
import me.thekey.android.lib.support.v4.dialog.LoginDialogFragment;


public class MainActivity extends ActionBarActivity
    implements OnMapReadyCallback
{
    private final String TAG = this.getClass().getSimpleName();

    private static final int LOADER_THEKEY_ATTRIBUTES = 1;
    private static final int LOADER_CURRENT_MINISTRY = 2;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    TheKey theKey;
    private LocalBroadcastManager manager;
    private GcmBroadcastReceiver gcmBroadcastReceiver;
    private ActionBar actionBar;
    private boolean targets;
    private boolean groups;
    private boolean churches;
    private boolean multiplyingChurches;
    private boolean trainingActivities;
    private boolean campuses;
    private SharedPreferences mapPreferences;
    private SharedPreferences preferences;
    private BroadcastReceiver broadcastReceiver;

    @Nullable
    private GoogleMap map;
    private ClusterManager<GcmMarker> clusterManager;

    @Nullable
    private AssociatedMinistry mCurrentMinistry;

    private TextView mapOverlayText;

    @Nullable
    private List<Training> allTraining;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        actionBar = getSupportActionBar();
        
        mapOverlayText = (TextView) findViewById(R.id.map_text);

        preferences = getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);

        getMapPreferences();
        
        setupBroadcastReceivers();

        theKey = TheKeyImpl.getInstance(getApplicationContext(), THEKEY_CLIENTID);

        manager = LocalBroadcastManager.getInstance(getApplicationContext());
        gcmBroadcastReceiver = new GcmBroadcastReceiver(theKey, this);
        gcmBroadcastReceiver.registerReceiver(manager);
        
        if (Device.isConnected(getApplicationContext()))
        {
            handleLogin();
        }
        else
        {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
        if (checkPlayServices())
        {
            SupportMapFragment map = (SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map);
            map.getMapAsync(this);
        }
    }

    private void handleLogin()
    {
        // check for previous login sessions
        if (theKey.getGuid() == null)
        {
            login();
        }
        else
        {
            // trigger background syncing of data
            MinistriesService.syncAllMinistries(this);
            MinistriesService.syncAssignments(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLoaders();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent goToSettings = new Intent(this, SettingsActivity.class);
                startActivity(goToSettings);
                return true;
            case R.id.action_refresh:
                MinistriesService.syncAllMinistries(this, true);
                MinistriesService.syncAssignments(this, true);
                if (mCurrentMinistry != null) {
                    MinistriesService.syncChurches(this, mCurrentMinistry.getMinistryId());
                }
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
     * This event is triggered when a new currentMinistry object is loaded
     *
     * @param ministry the new current ministry object
     */
    void onLoadCurrentMinistry(@Nullable final AssociatedMinistry ministry) {
        // store the current ministry
        final AssociatedMinistry old = mCurrentMinistry;
        mCurrentMinistry = ministry;

        // determine if the current ministry changed
        final String oldId = old != null ? old.getMinistryId() : null;
        final String newId = ministry != null ? ministry.getMinistryId() : null;
        final boolean changed = oldId != null ? !oldId.equals(newId) : newId != null;

        // trigger some additional actions if we are changing our current ministry
        if (changed) {
            onChangeCurrentMinistry();
        }

        // update the map
        updateMap(changed);
    }

    /**
     * This event is triggered when the current ministry is changing from one to another
     */
    void onChangeCurrentMinistry() {
        // sync churches & trainings
        if (mCurrentMinistry != null) {
            String mcc = getChosenMcc();
            MinistriesService.syncChurches(this, mCurrentMinistry.getMinistryId());
            TrainingService.downloadTraining(this, mCurrentMinistry.getMinistryId(), mcc != null ? mcc : "slm");
        }

        // If we are changing assignments/ministries, we need to reload trainings
        // TODO: this should be handled by a ContentLoader on a background thread eventually
        if (mCurrentMinistry != null) {
            TrainingDao trainingDao = TrainingDao.getInstance(this);
            allTraining = trainingDao.getAllMinistryTraining(mCurrentMinistry.getMinistryId());
        } else {
            allTraining = null;
        }
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        Log.i(TAG, "Resuming");
        
        if (map != null) map.clear();
        
        getMapPreferences();
        updateMap(false);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeBroadcastReceivers();
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = this.getSupportLoaderManager();
        manager.initLoader(LOADER_THEKEY_ATTRIBUTES, null, new AttributesLoaderCallbacks());
        manager.initLoader(LOADER_CURRENT_MINISTRY, null, new AssociatedMinistryLoaderCallbacks());
    }

    private void updateMap(final boolean zoom) {
        // update map overlay text
        if (mapOverlayText != null) {
            mapOverlayText.setText(mCurrentMinistry != null ? mCurrentMinistry.getName() : null);
        }

        // update map itself if it exists
        if (map != null) {
            // update map zoom
            if (zoom) {
                zoomToLocation();
            }

            // update Markers on the map
            if (clusterManager != null) {
                // clear any previous items
                clusterManager.clearItems();

                // add training Markers
                addTrainingMarkersToMap();

                // force a recluster
                clusterManager.cluster();
            }
        }
    }

    public void joinNewMinistry(MenuItem menuItem)
    {
        final Context context = this;
        if (Device.isConnected(getApplicationContext()))
        {
            if (theKey.getGuid() == null)
            {
                login();
            }
            else
            {
                Intent goToJoinMinistryPage = new Intent(context, JoinMinistryActivity.class);
                startActivity(goToJoinMinistryPage);
            }
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Internet Necessary")
                .setMessage("You need Internet access to access this page")
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

            alertDialog.show();
        }
    }

    public void goToMeasurements(MenuItem menuItem)
    {
        startActivity(new Intent(getApplicationContext(), MeasurementsActivity.class));
    }

    public void reset(MenuItem menuItem)
    {
        //TODO: implement reset: clear local data-model, download from server
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Reset")
                .setMessage("Re-downloading information...")
                .setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    public void logout(MenuItem menuItem)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        theKey.logout();
                        dialog.dismiss();
                        login();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();  
    }
    
    private void getMapPreferences()
    {
        targets = preferences.getBoolean("targets", true);
        groups = preferences.getBoolean("groups", true);
        churches = preferences.getBoolean("churches", true);
        multiplyingChurches = preferences.getBoolean("multiplyingChurches", true);
        trainingActivities = preferences.getBoolean("trainingActivities", true);
        campuses = preferences.getBoolean("campuses", true);
    }
    
    private void login()
    {   
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null)
        {
            LoginDialogFragment loginDialogFragment = LoginDialogFragment.builder().clientId(THEKEY_CLIENTID).build();
            loginDialogFragment.show(fm.beginTransaction().addToBackStack("loginDialog"), "loginDialog");
        }
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }
    
    public void mapOptions(View view)
    {
        Log.i(TAG, "Map options");
        Intent intent = new Intent(this, MapSettings.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap) {
        Log.i(TAG, "On Map Ready");
        this.map = googleMap;
        clusterManager = new ClusterManager<>(this, map);
        clusterManager.setRenderer(new MarkerRender(this, map, clusterManager));
        map.setOnCameraChangeListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);

        // update the map
        updateMap(true);
    }

    private void zoomToLocation()
    {
        assert map != null : "map should be set before calling zoomToLocation";
        if (mCurrentMinistry != null) {
            Log.i(TAG, "Zooming to: " + mCurrentMinistry.getLatitude() + ", " + mCurrentMinistry.getLongitude());

            CameraUpdate center = CameraUpdateFactory
                    .newLatLng(new LatLng(mCurrentMinistry.getLatitude(), mCurrentMinistry.getLongitude()));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(mCurrentMinistry.getLocationZoom());

            map.moveCamera(center);
            map.moveCamera(zoom);
        }
    }

    private void addTrainingMarkersToMap() {
        // do not show training activities if turned off in map settings
        Log.i(TAG, "Show training: " + trainingActivities);
        if (trainingActivities && allTraining != null) {
            for (Training training : allTraining)
            {
                GcmMarker marker = new GcmMarker(training.getName(), training.getLatitude(), training.getLongitude());
                clusterManager.addItem(marker);
            }
        }
    }

    private void setupBroadcastReceivers()
    {
        manager = LocalBroadcastManager.getInstance(this);

        this.broadcastReceiver = new BroadcastReceiver()
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
                    
                    switch (type)
                    {
                        case TRAINING:
                            Log.i(TAG, "Training search complete and training saved");
                            
                            TrainingDao trainingDao = TrainingDao.getInstance(context);
                            allTraining = mCurrentMinistry != null ?
                                    trainingDao.getAllMinistryTraining(mCurrentMinistry.getMinistryId()) : null;

                            updateMap(false);

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

    private void removeBroadcastReceivers()
    {
        manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(broadcastReceiver);
        manager.unregisterReceiver(gcmBroadcastReceiver);
        broadcastReceiver = null;
        gcmBroadcastReceiver = null;
    }
    
    @Nullable
    private String getChosenMcc() {
        if (mCurrentMinistry != null) {
            String mcc = preferences.getString("chosen_mcc", null);

            if (mcc == null || "No MCC Options".equals(mcc)) {
                if (mCurrentMinistry.hasSlm()) {
                    mcc = "SLM";
                } else if (mCurrentMinistry.hasGcm()) {
                    mcc = "GCM";
                } else if (mCurrentMinistry.hasLlm()) {
                    mcc = "LLM";
                } else if (mCurrentMinistry.hasDs()) {
                    mcc = "DS";
                }

                preferences.edit().putString("chosen_mcc", mcc).apply();
            }

            return mcc;
        }

        return null;
    }

    private class AssociatedMinistryLoaderCallbacks extends SimpleLoaderCallbacks<AssociatedMinistry> {
        @Override
        public Loader<AssociatedMinistry> onCreateLoader(final int id, final Bundle bundle) {
            switch (id) {
                case LOADER_CURRENT_MINISTRY:
                    return new CurrentMinistryLoader(MainActivity.this);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(final Loader<AssociatedMinistry> loader,
                                   @Nullable final AssociatedMinistry ministry) {
            switch (loader.getId()) {
                case LOADER_CURRENT_MINISTRY:
                    onLoadCurrentMinistry(ministry);
                    break;
            }
        }
    }

    private class AttributesLoaderCallbacks extends SimpleLoaderCallbacks<TheKey.Attributes> {
        @Override
        public Loader<TheKey.Attributes> onCreateLoader(final int id, final Bundle args) {
            switch (id) {
                case LOADER_THEKEY_ATTRIBUTES:
                    return new AttributesLoader(MainActivity.this, theKey);
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
