package com.expidev.gcmapp.support.v4.fragment;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.ARG_MCC;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;
import static com.expidev.gcmapp.model.Task.CREATE_CHURCH;
import static com.expidev.gcmapp.model.Task.EDIT_CHURCH;
import static com.expidev.gcmapp.model.Task.EDIT_TRAINING;
import static com.expidev.gcmapp.model.Task.VIEW_CHURCH;
import static com.expidev.gcmapp.model.Task.VIEW_TRAINING;
import static com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader.ARG_LOAD_MINISTRY;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.expidev.gcmapp.MapSettings;
import com.expidev.gcmapp.R;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.GmaDao;
import com.expidev.gcmapp.map.ChurchItem;
import com.expidev.gcmapp.map.GmaItem;
import com.expidev.gcmapp.map.MarkerRender;
import com.expidev.gcmapp.map.TrainingItem;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.Task;
import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.service.GmaSyncService;
import com.expidev.gcmapp.service.TrainingService;
import com.expidev.gcmapp.support.v4.content.ChurchesLoader;
import com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader;
import com.expidev.gcmapp.support.v4.content.TrainingLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.common.base.Objects;
import com.google.maps.android.clustering.ClusterManager;

import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.AsyncTaskCompat;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {
    private static final int LOADER_CURRENT_ASSIGNMENT = 1;
    private static final int LOADER_CHURCHES = 2;
    private static final int LOADER_TRAININGS = 3;

    private static final int MAP_LAYER_TRAINING = 0;
    private static final int MAP_LAYER_TARGET = 1;
    private static final int MAP_LAYER_GROUP = 2;
    private static final int MAP_LAYER_CHURCH = 3;
    private static final int MAP_LAYER_MULTIPLYING_CHURCH = 4;
    private static final int MAP_LAYER_CAMPUSES = 5;

    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();
    private final ChurchesLoaderCallbacks mLoaderCallbacksChurches = new ChurchesLoaderCallbacks();
    private final TrainingsLoaderCallbacks mLoaderCallbacksTraining = new TrainingsLoaderCallbacks();

    @Optional
    @Nullable
    @InjectView(R.id.map)
    FrameLayout mMapFrame;
    @Optional
    @Nullable
    @InjectView(R.id.name)
    TextView mMinistryNameView;

    @Nullable
    private GoogleMap mMap;
    private boolean mMapInitialized = false;
    @Nullable
    private ClusterManager<GmaItem> mClusterManager;
    private final boolean[] mMapLayers = new boolean[6];

    @NonNull
    private String mGuid = "";
    @Nullable
    private Assignment mAssignment;
    @Nullable
    private Ministry mMinistry;
    @Nullable
    private List<Training> mTrainings;
    @Nullable
    private List<Church> mChurches;

    public static MapFragment newInstance(@NonNull final String guid) {
        final MapFragment fragment = new MapFragment();

        final Bundle args = new Bundle(1);
        args.putString(ARG_GUID, guid);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onAttach(@NonNull final Activity activity) {
        super.onAttach(activity);

        // get the GoogleMap as soon as it's available
        if (checkPlayServices()) {
            getMapAsync(this);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);
        setHasOptionsMenu(true);

        mGuid = getArguments().getString(ARG_GUID);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map, container, false);
        final FrameLayout frame = ButterKnife.findById(view, R.id.map);
        frame.addView(super.onCreateView(inflater, frame, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedState) {
        super.onViewCreated(view, savedState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVisibleMapLayers();
        updateMapMarkers();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                syncData(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This event is triggered when a fresh assignment object is loaded
     *
     * @param assignment the new assignment object
     */
    void onLoadCurrentAssignment(@Nullable final Assignment assignment) {
        final Assignment old = mAssignment;
        final Ministry oldMinistry = mMinistry;
        mAssignment = assignment;
        mMinistry = mAssignment != null ? mAssignment.getMinistry() : null;

        // determine what has changed
        final String oldId;
        final Ministry.Mcc oldMcc;
        final String newId;
        final Ministry.Mcc newMcc;
        if (old != null) {
            oldId = old.getMinistryId();
            oldMcc = old.getMcc();
        } else {
            oldId = Ministry.INVALID_ID;
            oldMcc = Ministry.Mcc.UNKNOWN;
        }
        if (mAssignment != null) {
            newId = mAssignment.getMinistryId();
            newMcc = mAssignment.getMcc();
        } else {
            newId = Ministry.INVALID_ID;
            newMcc = Ministry.Mcc.UNKNOWN;
        }

        final LatLng oldLocation;
        final int oldZoom;
        final LatLng newLocation;
        final int newZoom;
        if (oldMinistry != null) {
            oldLocation = oldMinistry.getLocation();
            oldZoom = oldMinistry.getLocationZoom();
        } else {
            oldLocation = null;
            oldZoom = 0;
        }
        if (mMinistry != null) {
            newLocation = mMinistry.getLocation();
            newZoom = mMinistry.getLocationZoom();
        } else {
            newLocation = null;
            newZoom = 0;
        }

        final boolean changed = !oldId.equals(newId);
        final boolean changedMcc = changed || oldMcc != newMcc;
        final boolean changedLocation = changed || !Objects.equal(oldLocation, newLocation) || oldZoom != newZoom;

        // sync updated data
        if (changed || changedMcc) {
            syncData(false);
        }

        // restart loaders if necessary
        if (changed || changedMcc) {
            // destroy old loaders
            final LoaderManager manager = getLoaderManager();
            manager.destroyLoader(LOADER_TRAININGS);
            manager.destroyLoader(LOADER_CHURCHES);

            // start loaders for current assignment
            startLoaders(mAssignment);
        }

        // update views
        updateViews();

        // update map location when changing ministries
        if (changedLocation) {
            updateMapLocation();
        }
    }

    void onLoadChurches(@Nullable final List<Church> churches) {
        mChurches = churches;

        // update the map markers
        updateMapMarkers();
    }

    void onLoadTrainings(@Nullable final List<Training> trainings) {
        mTrainings = trainings;

        // update the map markers
        updateMapMarkers();
    }

    @Override
    public void onMapReady(@NonNull final GoogleMap map) {
        // is the map still initialized? Only true if it's been initialized and the map isn't changing
        mMapInitialized = mMapInitialized && map == mMap;

        mMap = map;
        initMap();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void syncData(final boolean force) {
        // trigger background syncing of data
        if (mAssignment != null) {
            if (mAssignment.getMcc() == Ministry.Mcc.GCM) {
                GmaSyncService.syncChurches(getActivity(), mAssignment.getMinistryId());
            }
            TrainingService.syncTraining(getActivity(), mAssignment.getMinistryId(), mAssignment.getMcc());
        }
    }

    private void startLoaders() {
        // build the args used for various loaders
        final Bundle args = new Bundle(2);
        args.putString(ARG_GUID, mGuid);
        args.putBoolean(ARG_LOAD_MINISTRY, true);

        // start loader
        final LoaderManager manager = this.getLoaderManager();
        manager.initLoader(LOADER_CURRENT_ASSIGNMENT, args, mLoaderCallbacksAssignment);

        // start loaders related to the current assignment
        startLoaders(mAssignment);
    }

    private void startLoaders(@Nullable final Assignment assignment) {
        if (assignment != null) {
            // build the args used for various loaders
            final Bundle args = new Bundle(3);
            args.putString(ARG_GUID, mGuid);
            args.putString(ARG_MINISTRY_ID, assignment.getMinistryId());
            args.putString(ARG_MCC, assignment.getMcc().toString());

            // start loaders
            final LoaderManager manager = this.getLoaderManager();
            if (assignment.getMcc() == Ministry.Mcc.GCM) {
                manager.initLoader(LOADER_CHURCHES, args, mLoaderCallbacksChurches);
            }
            if (assignment.getMcc() != Ministry.Mcc.UNKNOWN) {
                manager.initLoader(LOADER_TRAININGS, args, mLoaderCallbacksTraining);
            }
        }
    }

    private boolean checkPlayServices() {
        final int code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (code != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(code)) {
                GooglePlayServicesUtil.showErrorDialogFragment(code, getActivity(), 0);
            }
            return false;
        }

        return true;
    }

    private void initMap() {
        final Context context = getActivity();
        if (!mMapInitialized && context != null && mMap != null) {
            mClusterManager = new ClusterManager<>(context, mMap);
            final MarkerRender renderer = new MarkerRender(context, mMap, mClusterManager);
            renderer.setMarkerDragListener(new MarkerDragListener());
            mClusterManager.setRenderer(renderer);
            mClusterManager.setOnClusterItemInfoWindowClickListener(
                    new ClusterManager.OnClusterItemInfoWindowClickListener<GmaItem>() {
                        @Override
                        public void onClusterItemInfoWindowClick(final GmaItem item) {
                            if (item instanceof ChurchItem) {
                                showEditChurch(((ChurchItem) item).getChurchId());
                            } else if (item instanceof TrainingItem) {
                                showEditTraining(((TrainingItem) item).getTrainingId());
                            }
                        }
                    });
            mMap.setOnCameraChangeListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);
            mMap.setOnInfoWindowClickListener(mClusterManager);
            mMap.setOnMarkerDragListener(mClusterManager.getMarkerManager());
            mMap.setOnMapLongClickListener(new MapLongClickListener());

            loadVisibleMapLayers();
            updateMapLocation();
            updateMapMarkers();

            mMapInitialized = true;
        }
    }

    private void updateViews() {
        if (mMinistryNameView != null) {
            mMinistryNameView.setText(mMinistry != null ? mMinistry.getName() : "");
        }
    }

    private void loadVisibleMapLayers() {
        final SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
        mMapLayers[MAP_LAYER_TRAINING] = prefs.getBoolean("trainingActivities", true);
        mMapLayers[MAP_LAYER_TARGET] = prefs.getBoolean("targets", true);
        mMapLayers[MAP_LAYER_GROUP] = prefs.getBoolean("groups", true);
        mMapLayers[MAP_LAYER_CHURCH] = prefs.getBoolean("churches", true);
        mMapLayers[MAP_LAYER_MULTIPLYING_CHURCH] = prefs.getBoolean("multiplyingChurches", true);
        mMapLayers[MAP_LAYER_CAMPUSES] = prefs.getBoolean("campuses", true);
    }

    private void updateMapLocation() {
        if (mMap != null && mMinistry != null && mMinistry.hasLocation()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMinistry.getLocation(), mMinistry.getLocationZoom()));
        }
    }

    private void updateMapMarkers() {
        // update Markers on the map
        if (mClusterManager != null) {
            // clear any previous items
            mClusterManager.clearItems();

            // add various Markers to the map
            addChurchMarkersToMap();
            addTrainingMarkersToMap();

            // force a recluster
            mClusterManager.cluster();
        }
    }

    private void addChurchMarkersToMap() {
        assert mClusterManager != null : "mClusterManager should be set before calling addChurchMarkersToMap";
        if (mAssignment != null && mChurches != null) {
            for (final Church church : mChurches) {
                if (mAssignment.can(VIEW_CHURCH, church) && church.hasLocation()) {
                    final boolean render;
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
                        default:
                            render = false;
                            break;
                    }

                    if (render) {
                        mClusterManager.addItem(new ChurchItem(church));
                    }
                }
            }
        }
    }

    private void addTrainingMarkersToMap() {
        assert mClusterManager != null : "mClusterManager should be set before calling addTrainingMarkersToMap";

        if (mMapLayers[MAP_LAYER_TRAINING] && mAssignment != null && mAssignment.can(VIEW_TRAINING) &&
                mTrainings != null) {
            for (final Training training : mTrainings) {
                if (training.hasLocation()) {
                    mClusterManager.addItem(new TrainingItem(training));
                }
            }
        }
    }

    @Optional
    @OnClick(R.id.map_settings)
    void showMapSettings() {
        startActivity(new Intent(getActivity(), MapSettings.class));
    }

    void showCreateChurch(@NonNull final LatLng pos) {
        if (mAssignment != null && mAssignment.can(CREATE_CHURCH)) {
            final FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag("createChurch") == null) {
                CreateChurchFragment fragment = CreateChurchFragment.newInstance(mAssignment.getMinistryId(), pos);
                fragment.show(fm.beginTransaction().addToBackStack("createChurch"), "createChurch");
            }
        }
    }

    void showEditChurch(final long churchId) {
        if (mAssignment != null && mAssignment.can(EDIT_CHURCH)) {
            final FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag("editChurch") == null) {
                EditChurchFragment fragment = EditChurchFragment.newInstance(churchId);
                fragment.show(fm.beginTransaction().addToBackStack("editChurch"), "editChurch");
            }
        }
    }

    void showEditTraining(final long trainingId) {
        if (mAssignment != null && mAssignment.can(EDIT_TRAINING)) {
            final FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag("editTraining") == null) {
                EditTrainingFragment fragment = EditTrainingFragment.newInstance(trainingId);
                fragment.show(fm.beginTransaction().addToBackStack("editTraining"), "editTraining");
            }
        }
    }

    private class MarkerDragListener implements MarkerRender.OnMarkerDragListener<GmaItem> {
        @Override
        public void onMarkerDragStart(@NonNull GmaItem item, @NonNull Marker marker) {
            // perform haptic feedback to let user know something is happening
            if (mMapFrame != null) {
                mMapFrame.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        }

        @Override
        public void onMarkerDragEnd(@NonNull final GmaItem item, @NonNull final Marker marker) {
            if (item instanceof ChurchItem) {
                final Church church = ((ChurchItem) item).getObject();
                if (mAssignment != null && mAssignment.can(Task.EDIT_CHURCH, church)) {
                    // update church location in the database
                    final LatLng position = marker.getPosition();
                    AsyncTaskCompat.execute(
                            new ChurchLocationRunnable(getActivity(), ((ChurchItem) item).getChurchId(), position));

                    // update the currently loaded backing church object
                    church.setLatitude(position.latitude);
                    church.setLongitude(position.longitude);

                    // update map markers to their new state
                    // XXX: this currently recreates all map markers, which is incredibly heavy to update a single node
                    updateMapMarkers();
                } else {
                    // reset position because you weren't actually allowed to move the item
                    marker.setPosition(item.getPosition());
                }
            } else {
                // reset position because you weren't actually allowed to move the item
                marker.setPosition(item.getPosition());
            }
        }
    }

    private static class ChurchLocationRunnable implements Runnable {
        private Context mContext;
        private long mId;
        private LatLng mLocation;

        public ChurchLocationRunnable(@NonNull final Context context, final long id, @NonNull final LatLng location) {
            mContext = context.getApplicationContext();
            mId = id;
            mLocation = location;
        }

        @Override
        public void run() {
            final GmaDao dao = GmaDao.getInstance(mContext);
            final Transaction tx = dao.newTransaction();
            try {
                tx.beginTransactionNonExclusive();

                final Church church = dao.find(Church.class, mId);
                if (church != null) {
                    // update the location of this church
                    church.trackingChanges(true);
                    church.setLatitude(mLocation.latitude);
                    church.setLongitude(mLocation.longitude);
                    church.trackingChanges(false);

                    // store the update
                    dao.update(church, new String[] {Contract.Church.COLUMN_LATITUDE, Contract.Church.COLUMN_LONGITUDE,
                            Contract.Church.COLUMN_DIRTY});

                    // sync the updated church back to the cloud
                    GmaSyncService.syncDirtyChurches(mContext);
                }

                tx.setTransactionSuccessful();
            } finally {
                tx.endTransaction();
            }
        }
    }

    private class MapLongClickListener implements GoogleMap.OnMapLongClickListener {
        @Override
        public void onMapLongClick(final LatLng pos) {
            if (mMapFrame != null) {
                mMapFrame.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            showCreateChurch(pos);
        }
    }

    private class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment> {
        @Nullable
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle bundle) {
            switch (id) {
                case LOADER_CURRENT_ASSIGNMENT:
                    return new CurrentAssignmentLoader(getActivity(), bundle);
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

    private class ChurchesLoaderCallbacks extends SimpleLoaderCallbacks<List<Church>> {
        @Override
        public Loader<List<Church>> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_CHURCHES:
                    return new ChurchesLoader(getActivity(), args);
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

    private class TrainingsLoaderCallbacks extends SimpleLoaderCallbacks<List<Training>> {
        @Override
        public Loader<List<Training>> onCreateLoader(int id, @Nullable Bundle bundle) {
            switch (id) {
                case LOADER_TRAININGS:
                    return new TrainingLoader(getActivity(), bundle);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<Training>> listLoader, @Nullable List<Training> trainings) {
            switch (listLoader.getId()) {
                case LOADER_TRAININGS:
                    onLoadTrainings(trainings);
            }
        }
    }
}
