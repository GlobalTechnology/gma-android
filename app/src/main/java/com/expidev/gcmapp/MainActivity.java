package com.expidev.gcmapp;

import static com.expidev.gcmapp.BuildConfig.THEKEY_CLIENTID;
import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;
import static com.expidev.gcmapp.Constants.PREF_CURRENT_MINISTRY;
import static com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader.ARG_LOAD_MINISTRY;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.expidev.gcmapp.GcmTheKey.GcmBroadcastReceiver;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.map.ChurchMarker;
import com.expidev.gcmapp.map.Marker;
import com.expidev.gcmapp.map.MarkerRender;
import com.expidev.gcmapp.map.TrainingMarker;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.service.MeasurementsService;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.service.TrainingService;
import com.expidev.gcmapp.support.v4.content.ChurchesLoader;
import com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader;
import com.expidev.gcmapp.support.v4.content.MinistriesCursorLoader;
import com.expidev.gcmapp.support.v4.content.TrainingLoader;
import com.expidev.gcmapp.support.v4.fragment.EditChurchFragment;
import com.expidev.gcmapp.support.v4.fragment.EditTrainingFragment;
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
import org.ccci.gto.android.common.util.CursorUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemSelected;
import butterknife.Optional;
import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;
import me.thekey.android.lib.support.v4.dialog.LoginDialogFragment;

public class MainActivity extends ActionBarActivity
    implements OnMapReadyCallback
{
    private final String TAG = this.getClass().getSimpleName();

    private static final int LOADER_CHURCHES = 3;
    private static final int LOADER_TRAINING = 4;
    private static final int LOADER_ASSIGNED_MINISTRIES = 5;
    private static final int LOADER_CURRENT_ASSIGNMENT = 6;

    private static final int MAP_LAYER_TRAINING = 0;
    private static final int MAP_LAYER_TARGET = 1;
    private static final int MAP_LAYER_GROUP = 2;
    private static final int MAP_LAYER_CHURCH = 3;
    private static final int MAP_LAYER_MULTIPLYING_CHURCH = 4;
    private static final int MAP_LAYER_CAMPUSES = 5;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /* Background Services & Utilities */
    TheKey theKey;
    private LocalBroadcastManager manager;
    private GcmBroadcastReceiver gcmBroadcastReceiver;
    private SharedPreferences preferences;

    /* Loader callback objects */
    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();
    private final ChurchesLoaderCallbacks mLoaderCallbacksChurches = new ChurchesLoaderCallbacks();
    private final TrainingLoaderCallbacks mLoaderCallbacksTraining = new TrainingLoaderCallbacks();
    private final CursorLoaderCallbacks mLoaderCallbacksCursor = new CursorLoaderCallbacks();

    /* Data adapters */
    private SimpleCursorAdapter mMinistriesNavAdapter;
    private ArrayAdapter<Ministry.Mcc> mMccsNavAdapter;

    /* Views */
    @Optional
    @Nullable
    @InjectView(R.id.toolbar_actionbar)
    Toolbar mActionBar;
    @Optional
    @Nullable
    @InjectView(R.id.toolbar_spinner_ministries)
    Spinner mMinistriesSpinner;
    @Optional
    @Nullable
    @InjectView(R.id.toolbar_spinner_mccs)
    Spinner mMccsSpinner;

    /* map related objects */
    @Nullable
    private GoogleMap map;
    private ClusterManager<Marker> clusterManager;
    private final boolean[] mMapLayers = new boolean[6];

    /* loaded data */
    @NonNull
    private final LongSparseArray<String> mMinistryIds = new LongSparseArray<>();
    @Nullable
    private Assignment mAssignment;
    @Nullable
    private AssociatedMinistry mCurrentMinistry;
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
        ButterKnife.inject(this);
        setupActionBar();

        preferences = getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);

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
            MinistriesService.syncAssignments(this, theKey.getGuid());

            if (mAssignment != null) {
                MeasurementsService
                        .syncMeasurements(this, mAssignment.getMinistryId(), mAssignment.getMcc().toString(), null);
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
                Intent goToSettings = new Intent(this, SettingsActivity.class);
                startActivity(goToSettings);
                return true;
            case R.id.action_refresh:
                MinistriesService.syncAllMinistries(this, true);
                MinistriesService.syncAssignments(this, theKey.getGuid(), true);
                if (mAssignment != null) {
                    MinistriesService.syncChurches(this, mAssignment.getMinistryId());
                    MeasurementsService.syncMeasurements(
                            this, mAssignment.getMinistryId(), mAssignment.getMcc().toString(), null, true);
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onLoadAssignedMinistries(@Nullable final Cursor cursor) {
        // update the lookup table of ministry ids
        mMinistryIds.clear();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                mMinistryIds.put(CursorUtils.getLong(cursor, Contract.AssociatedMinistry.COLUMN_ROWID), CursorUtils
                        .getString(cursor, Contract.AssociatedMinistry.COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
            }
        }

        // update the Ministry Navigation Spinner
        if (mMinistriesNavAdapter != null) {
            // swap out the Cursor
            mMinistriesNavAdapter.swapCursor(cursor);
        }
    }

    /**
     * This event is triggered when a fresh assignment object is loaded
     *
     * @param assignment the new assignment object
     */
    void onLoadCurrentAssignment(@Nullable final Assignment assignment) {
        mAssignment = assignment;

        // store the current ministry
        final AssociatedMinistry old = mCurrentMinistry;
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
            MinistriesService.syncChurches(this, mAssignment.getMinistryId());
            TrainingService.downloadTraining(this, mAssignment.getMinistryId(), mAssignment.getMcc());
            MeasurementsService.retrieveAndSaveInitialMeasurements(this, mAssignment.getMinistryId(),
                                                                   mAssignment.getMcc().toString(), null);
        }

        // restart Loaders based off the current ministry
        restartCurrentMinistryBasedLoaders();

        // update the navigation spinners
        updateNavSpinners();
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
        cleanupNavSpinners();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void setupActionBar() {
        if (mActionBar != null) {
            setSupportActionBar(mActionBar);
            setupMinistriesSpinner();
            setupMccsSpinner();
            updateNavSpinners();
        }
    }

    private void setupMinistriesSpinner() {
        if (mMinistriesSpinner != null) {
            // create adapter
            mMinistriesNavAdapter = new SimpleCursorAdapter(this, R.layout.toolbar_nav_item_ministry_selected, null,
                                                            new String[] {Contract.AssociatedMinistry.COLUMN_NAME},
                                                            new int[] {R.id.ministryName}, 0);
            mMinistriesNavAdapter.setDropDownViewResource(R.layout.toolbar_nav_item_ministry);
            mMinistriesSpinner.setAdapter(mMinistriesNavAdapter);
        }
    }

    private void setupMccsSpinner() {
        if (mMccsSpinner != null) {
            // create adapter
            mMccsNavAdapter =
                    new ArrayAdapter<>(this, R.layout.toolbar_nav_item_mcc_selected, R.id.mcc,
                                       new ArrayList<Ministry.Mcc>());
            mMccsNavAdapter.setDropDownViewResource(R.layout.toolbar_nav_item_mcc);
            mMccsSpinner.setAdapter(mMccsNavAdapter);
        }
    }

    private void updateNavSpinners() {
        // only update the selected ministry in the ActionBar navigation if we have a loaded Assignment
        if (mMinistriesSpinner != null && mMinistriesNavAdapter != null && mAssignment != null) {
            final String ministryId = mAssignment.getMinistryId();

            // only proceed if the value is currently incorrect
            Cursor c = (Cursor) mMinistriesSpinner.getSelectedItem();
            if (c == null ||
                    !ministryId.equals(CursorUtils.getString(c, Contract.AssociatedMinistry.COLUMN_MINISTRY_ID))) {
                // get current Cursor
                c = mMinistriesNavAdapter.getCursor();
                if (c != null) {
                    c.moveToPosition(-1);
                    while (c.moveToNext()) {
                        if (ministryId
                                .equals(CursorUtils.getString(c, Contract.AssociatedMinistry.COLUMN_MINISTRY_ID))) {
                            final int pos = c.getPosition();
                            mMinistriesSpinner.setSelection(pos);
                            mMinistriesSpinner.setTag(R.id.pos, pos);
                            break;
                        }
                    }
                }
            }
        }

        // update the list of MCCs
        if (mMccsNavAdapter != null) {
            // update the available MCCs
            mMccsNavAdapter.clear();
            if (mCurrentMinistry != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mMccsNavAdapter.addAll(mCurrentMinistry.getMccs());
                } else {
                    for (final Ministry.Mcc mcc : mCurrentMinistry.getMccs()) {
                        mMccsNavAdapter.add(mcc);
                    }
                }
            }

            // update the selected MCC
            if (mMccsSpinner != null && mAssignment != null) {
                final int pos = mMccsNavAdapter.getPosition(mAssignment.getMcc());
                if (pos != -1 && pos != mMccsSpinner.getSelectedItemPosition()) {
                    mMccsSpinner.setSelection(pos);
                    mMccsSpinner.setTag(R.id.pos, pos);
                }
            }
        }
    }

    private void cleanupNavSpinners() {
        mMinistriesNavAdapter = null;
        mMccsNavAdapter = null;
    }

    private void startLoaders() {
        final LoaderManager manager = this.getSupportLoaderManager();

        // build the args used for various loaders
        final Bundle args = new Bundle(1);
        args.putString(ARG_GUID, theKey.getGuid());
        args.putBoolean(ARG_LOAD_MINISTRY, true);

        manager.initLoader(LOADER_ASSIGNED_MINISTRIES, null, mLoaderCallbacksCursor);
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

    @Optional
    @OnItemSelected(R.id.toolbar_spinner_ministries)
    void changeMinistry(final AdapterView<?> parent, final View view, final int position, final long id) {
        // only process if this isn't a change to an automatically set position
        if (!Integer.valueOf(position).equals(parent.getTag(R.id.pos))) {
            // update the currently selected ministry
            preferences.edit().putString(PREF_CURRENT_MINISTRY, mMinistryIds.get(id)).apply();
        }

        // clear out a remaining position indicator
        parent.setTag(R.id.pos, null);
    }

    @Optional
    @OnItemSelected(R.id.toolbar_spinner_mccs)
    void changeMcc(final AdapterView<?> parent, final View view, final int position, final long id) {
        // only process if this isn't a change to an automatically set position
        if (!Integer.valueOf(position).equals(parent.getTag(R.id.pos))) {
            if (mAssignment != null) {
                // update the selected MCC
                final Assignment assignment = mAssignment.clone();
                assignment.setMcc((Ministry.Mcc) parent.getItemAtPosition(position));

                // persist the change in the database
                final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
                final MinistriesDao dao = MinistriesDao.getInstance(this);
                dao.async(new Runnable() {
                    @Override
                    public void run() {
                        dao.updateOrInsert(assignment, new String[] {Contract.Assignment.COLUMN_MCC});

                        // broadcast the update
                        broadcastManager.sendBroadcast(BroadcastUtils.updateAssignmentsBroadcast());
                    }
                });
            }
        }

        // clear out a remaining position indicator
        parent.setTag(R.id.pos, null);
    }

    private void updateMap(final boolean zoom) {
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

    void showEditChurch(final long churchId) {
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("editChurch") == null) {
            EditChurchFragment fragment = EditChurchFragment.newInstance(churchId);
            fragment.show(fm.beginTransaction().addToBackStack("editChurch"), "editChurch");
        }
    }
    
    void showEditTraining(final long trainingId)
    {
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("editTraining") == null)
        {
            EditTrainingFragment fragment = EditTrainingFragment.newInstance(trainingId);
            fragment.show(fm.beginTransaction().addToBackStack("editTraining"), "editTraining");
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

    private void loadVisibleMapLayers() {
        mMapLayers[MAP_LAYER_TRAINING] = preferences.getBoolean("trainingActivities", true);
        mMapLayers[MAP_LAYER_TARGET] = preferences.getBoolean("targets", true);
        mMapLayers[MAP_LAYER_GROUP] = preferences.getBoolean("groups", true);
        mMapLayers[MAP_LAYER_CHURCH] = preferences.getBoolean("churches", true);
        mMapLayers[MAP_LAYER_MULTIPLYING_CHURCH] = preferences.getBoolean("multiplyingChurches", true);
        mMapLayers[MAP_LAYER_CAMPUSES] = preferences.getBoolean("campuses", true);
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
        if (mMapLayers[MAP_LAYER_TRAINING] && allTraining != null) {
            for (Training training : allTraining)
            {
                clusterManager.addItem(new TrainingMarker(training));
            }
        }
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

                if (render) {
                    clusterManager.addItem(new ChurchMarker(church));
                }
            }
        }
    }

    private void removeBroadcastReceivers()
    {
        manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(gcmBroadcastReceiver);
        gcmBroadcastReceiver = null;
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

    private class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_ASSIGNED_MINISTRIES:
                    return new MinistriesCursorLoader(MainActivity.this);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_ASSIGNED_MINISTRIES:
                    onLoadAssignedMinistries(cursor);
                    break;
            }
        }
    }
}
