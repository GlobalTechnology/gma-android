package com.expidev.gcmapp;

import static com.expidev.gcmapp.BuildConfig.THEKEY_CLIENTID;
import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;
import static com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader.ARG_LOAD_MINISTRY;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.expidev.gcmapp.activity.SettingsActivity;
import com.expidev.gcmapp.map.ChurchMarker;
import com.expidev.gcmapp.map.Marker;
import com.expidev.gcmapp.map.MarkerRender;
import com.expidev.gcmapp.map.TrainingMarker;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.service.GmaSyncService;
import com.expidev.gcmapp.service.MeasurementsService;
import com.expidev.gcmapp.service.TrainingService;
import com.expidev.gcmapp.support.v4.content.ChurchesLoader;
import com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader;
import com.expidev.gcmapp.support.v4.content.TrainingLoader;
import com.expidev.gcmapp.support.v4.fragment.CreateChurchFragment;
import com.expidev.gcmapp.support.v4.fragment.EditChurchFragment;
import com.expidev.gcmapp.support.v4.fragment.EditTrainingFragment;
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
import org.joda.time.YearMonth;

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
    private static final int LOADER_CHURCHES = 3;
    private static final int LOADER_TRAINING = 4;
    private static final int LOADER_CURRENT_ASSIGNMENT = 6;

    private static final int MAP_LAYER_TRAINING = 0;
    private static final int MAP_LAYER_TARGET = 1;
    private static final int MAP_LAYER_GROUP = 2;
    private static final int MAP_LAYER_CHURCH = 3;
    private static final int MAP_LAYER_MULTIPLYING_CHURCH = 4;
    private static final int MAP_LAYER_CAMPUSES = 5;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    TheKey mTheKey;
    private LocalBroadcastManager manager;
    private GcmBroadcastReceiver gcmBroadcastReceiver;
    private ActionBar actionBar;
    private SharedPreferences preferences;

    /* Loader callback objects */
    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();
    private final AttributesLoaderCallbacks mLoaderCallbacksAttributes = new AttributesLoaderCallbacks();
    private final ChurchesLoaderCallbacks mLoaderCallbacksChurches = new ChurchesLoaderCallbacks();
    private final TrainingLoaderCallbacks mLoaderCallbacksTraining = new TrainingLoaderCallbacks();

    /* map related objects */
    private TextView mapOverlayText;
    @Nullable
    private GoogleMap map;
    private ClusterManager<Marker> clusterManager;
    private final boolean[] mMapLayers = new boolean[6];

    @Nullable
    private Assignment mAssignment;
    @Nullable
    private Ministry mCurrentMinistry;
    @Nullable
    private List<Training> allTraining;
    @Nullable
    private List<Church> mChurches;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        
        mapOverlayText = (TextView) findViewById(R.id.map_text);

        preferences = getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);

        mTheKey = TheKeyImpl.getInstance(this);

        manager = LocalBroadcastManager.getInstance(getApplicationContext());
        gcmBroadcastReceiver = new GcmBroadcastReceiver(mTheKey, this);
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
        if (mTheKey.getGuid() == null)
        {
            showLogin();
        }
        else
        {
            // trigger background syncing of data
            GmaSyncService.syncMinistries(this);
            GmaSyncService.syncAssignments(this, mTheKey.getGuid());
            GmaSyncService.syncMeasurementTypes(this);
            if (mAssignment != null) {
                GmaSyncService.syncMeasurements(this, mAssignment.getMinistryId(), mAssignment.getMcc(),
                                                YearMonth.now());
                MeasurementsService.syncMeasurements(
                    this,
                    mAssignment.getMinistryId(),
                    mAssignment.getMcc(),
                    null,
                    mAssignment.getRole());
            }
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
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_refresh:
                GmaSyncService.syncMinistries(this, true);
                GmaSyncService.syncAssignments(this, mTheKey.getGuid(), true);
                GmaSyncService.syncMeasurementTypes(this);
                if (mAssignment != null) {
                    GmaSyncService.syncChurches(this, mAssignment.getMinistryId());
                    GmaSyncService.syncMeasurements(this, mAssignment.getMinistryId(), mAssignment.getMcc(),
                                                    YearMonth.now());
                    MeasurementsService.syncMeasurements(
                        this,
                        mAssignment.getMinistryId(),
                        mAssignment.getMcc(),
                        null,
                        mAssignment.getRole());
                    TrainingService.syncTraining(this, mAssignment.getMinistryId(), mAssignment.getMcc());
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
     * This event is triggered when a fresh assignment object is loaded
     *
     * @param assignment the new assignment object
     */
    void onLoadCurrentAssignment(@Nullable final Assignment assignment) {
        mAssignment = assignment;

        // store the current ministry
        final Ministry old = mCurrentMinistry;
        mCurrentMinistry = assignment != null ? assignment.getMinistry() : null;

        // determine if the current ministry changed
        final String oldId = old != null ? old.getMinistryId() : Ministry.INVALID_ID;
        final String newId = mCurrentMinistry != null ? mCurrentMinistry.getMinistryId() : Ministry.INVALID_ID;
        final boolean changed = !oldId.equals(newId);

        // trigger some additional actions if we are changing our current ministry
        if (changed) {
            onChangeCurrentAssignment();
        }

        // update the map
        updateMap(changed);
    }

    /**
     * This event is triggered when the current ministry is changing from one to another
     */
    void onChangeCurrentAssignment() {
        // sync churches & trainings
        if (mAssignment != null) {
            GmaSyncService.syncChurches(this, mAssignment.getMinistryId());
            TrainingService.syncTraining(this, mAssignment.getMinistryId(), mAssignment.getMcc());
            GmaSyncService.syncMeasurements(this, mAssignment.getMinistryId(), mAssignment.getMcc(),
                                            YearMonth.now());
            MeasurementsService.syncMeasurements(
                this,
                mAssignment.getMinistryId(),
                mAssignment.getMcc(),
                null,
                mAssignment.getRole());
        }

        // restart Loaders based off the current ministry
        restartCurrentMinistryBasedLoaders();
    }
    
    void onLoadTraining(@Nullable final List<Training> trainings)
    {
        allTraining = trainings;
        updateMap(false);
    }

    void onLoadChurches(@Nullable final List<Church> churches) {
        mChurches = churches;

        // update the map
        updateMap(false);
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        Log.i(TAG, "Resuming");
        
        if (map != null) map.clear();

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

        // build the args used for various loaders
        final Bundle args = new Bundle(1);
        args.putString(ARG_GUID, mTheKey.getGuid());
        args.putBoolean(ARG_LOAD_MINISTRY, true);

        manager.initLoader(LOADER_THEKEY_ATTRIBUTES, null, mLoaderCallbacksAttributes);
        manager.initLoader(LOADER_CURRENT_ASSIGNMENT, args, mLoaderCallbacksAssignment);
        restartCurrentMinistryBasedLoaders();
    }

    private void restartCurrentMinistryBasedLoaders() {
        final LoaderManager manager = this.getSupportLoaderManager();

        // build the args used for ministry based loaders
        final Bundle args = new Bundle(1);
        args.putString(ARG_MINISTRY_ID, mAssignment != null ? mAssignment.getMinistryId() : Ministry.INVALID_ID);

        // restart these loaders in case the ministry id has changed since the last start
        manager.restartLoader(LOADER_CHURCHES, args, mLoaderCallbacksChurches);
        manager.restartLoader(LOADER_TRAINING, args, mLoaderCallbacksTraining);
    }

    private void updateMap(final boolean zoom) {
        // update map overlay text
        if (mapOverlayText != null) {
            mapOverlayText.setText(mCurrentMinistry != null ? mCurrentMinistry.getName() : null);
        }

        // update map itself if it exists
        if (map != null) {
            // refresh the list of map layers to display
            loadVisibleMapLayers();

            // update map zoom
            if (zoom) {
                zoomToLocation();
            }

            // update Markers on the map
            if (clusterManager != null) {
                // clear any previous items
                clusterManager.clearItems();

                // add various Markers to the map
                addChurchMarkersToMap();
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
            if (mTheKey.getGuid() == null)
            {
                showLogin();
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

    void showCreateChurch(@NonNull final LatLng pos) {
        if (mAssignment != null) {
            final FragmentManager fm = getSupportFragmentManager();
            if (fm.findFragmentByTag("createChurch") == null && canEditChurchOrTraining()) {
                CreateChurchFragment fragment = CreateChurchFragment.newInstance(mAssignment.getMinistryId(), pos);
                fragment.show(fm.beginTransaction().addToBackStack("createChurch"), "createChurch");
            }
        }
    }

    void showEditChurch(final long churchId) {
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("editChurch") == null && canEditChurchOrTraining()) {
            EditChurchFragment fragment = EditChurchFragment.newInstance(churchId);
            fragment.show(fm.beginTransaction().addToBackStack("editChurch"), "editChurch");
        }
    }

    private boolean canEditChurchOrTraining()
    {
        return mAssignment != null && mAssignment.isLeadership();
    }
    
    void showEditTraining(final long trainingId)
    {
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("editTraining") == null && canEditChurchOrTraining())
        {
            EditTrainingFragment fragment = EditTrainingFragment.newInstance(trainingId);
            fragment.show(fm.beginTransaction().addToBackStack("editTraining"), "editTraining");
        }
    }

    public void goToMeasurements(MenuItem menuItem)
    {
        if (mAssignment != null) {
            if (!mAssignment.isBlocked()) {
                com.expidev.gcmapp.activity.MeasurementsActivity
                        .start(this, mAssignment.getGuid(), mAssignment.getMinistryId(), mAssignment.getMcc());
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

    public void logout(MenuItem menuItem)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mTheKey.logout();
                        dialog.dismiss();
                        showLogin();
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

    private void loadVisibleMapLayers() {
        mMapLayers[MAP_LAYER_TRAINING] = preferences.getBoolean("trainingActivities", true);
        mMapLayers[MAP_LAYER_TARGET] = preferences.getBoolean("targets", true);
        mMapLayers[MAP_LAYER_GROUP] = preferences.getBoolean("groups", true);
        mMapLayers[MAP_LAYER_CHURCH] = preferences.getBoolean("churches", true);
        mMapLayers[MAP_LAYER_MULTIPLYING_CHURCH] = preferences.getBoolean("multiplyingChurches", true);
        mMapLayers[MAP_LAYER_CAMPUSES] = preferences.getBoolean("campuses", true);
    }

    private void showLogin() {
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null) {
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
        clusterManager.setOnClusterItemInfoWindowClickListener(
                new ClusterManager.OnClusterItemInfoWindowClickListener<Marker>() {
                    @Override
                    public void onClusterItemInfoWindowClick(final Marker marker) {
                        if (marker instanceof ChurchMarker) {
                            showEditChurch(((ChurchMarker) marker).getChurchId());
                        }
                        else if (marker instanceof TrainingMarker)
                        {
                            showEditTraining(((TrainingMarker) marker).getTrainingId());
                        }
                    }
                });
        map.setOnCameraChangeListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnInfoWindowClickListener(clusterManager);
        map.setOnMapLongClickListener(new MapLongClickListener());

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
        // show training activities when the Training layer is enabled and we have trainings
        if (mMapLayers[MAP_LAYER_TRAINING] && allTraining != null
            && mAssignment != null && canViewTraining()) {

            for (Training training : allTraining)
            {
                if (training.hasLocation()) {
                    clusterManager.addItem(new TrainingMarker(training));
                }
            }
        }
    }

    private boolean canViewTraining()
    {
        return mAssignment != null && !(mAssignment.isBlocked() || mAssignment.isSelfAssigned());
    }

    private void addChurchMarkersToMap() {
        if (mChurches != null) {
            for (final Church church : mChurches) {
                boolean render = false;
                switch (church.getDevelopment()) {
                    case TARGET:
                        render = mMapLayers[MAP_LAYER_TARGET];
                        break;
                    case GROUP:
                        render = mMapLayers[MAP_LAYER_GROUP];
                        break;
                    case CHURCH:
                        render = mMapLayers[MAP_LAYER_CHURCH];
                        break;
                    case MULTIPLYING_CHURCH:
                        render = mMapLayers[MAP_LAYER_MULTIPLYING_CHURCH];
                        break;
                }

                if (render && canViewChurch(church.getSecurity()) && church.hasLocation()) {
                    clusterManager.addItem(new ChurchMarker(church));
                }
            }
        }
    }

    private boolean canViewChurch(@NonNull final Church.Security security) {
        if (mAssignment != null) {
            switch (security) {
                case LOCAL_PRIVATE:
                    return mAssignment.isLeader() || mAssignment.isMember();
                case PRIVATE:
                    return mAssignment.isLeadership();
                case PUBLIC:
                    return true;
            }
        }
        return false;
    }

    private void removeBroadcastReceivers()
    {
        manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(gcmBroadcastReceiver);
        gcmBroadcastReceiver = null;
    }

    private class MapLongClickListener implements GoogleMap.OnMapLongClickListener {
        @Override
        public void onMapLongClick(final LatLng pos) {
            showCreateChurch(pos);
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
    
    private class TrainingLoaderCallbacks extends SimpleLoaderCallbacks<List<Training>>
    {
        @Override
        public Loader<List<Training>> onCreateLoader(int id, @Nullable Bundle bundle)
        {
            switch (id)
            {
                case LOADER_TRAINING:
                    return new TrainingLoader(MainActivity.this, bundle);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<Training>> listLoader, @Nullable List<Training> trainings)
        {
            switch (listLoader.getId())
            {
                case LOADER_TRAINING:
                    onLoadTraining(trainings);
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

    private class ChurchesLoaderCallbacks extends SimpleLoaderCallbacks<List<Church>> {
        @Override
        public Loader<List<Church>> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_CHURCHES:
                    return new ChurchesLoader(MainActivity.this, args);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<List<Church>> loader, @Nullable final List<Church> churches) {
            switch (loader.getId()) {
                case LOADER_CHURCHES:
                    onLoadChurches(churches);
            }
        }
    }
}
