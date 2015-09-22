package com.expidevapps.android.measurements.support.v4.fragment;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.PREFS_SETTINGS;
import static com.expidevapps.android.measurements.Constants.PREF_MAP_LAYER_CHURCH_CHURCH;
import static com.expidevapps.android.measurements.Constants.PREF_MAP_LAYER_CHURCH_GROUP;
import static com.expidevapps.android.measurements.Constants.PREF_MAP_LAYER_CHURCH_MULTIPLYING;
import static com.expidevapps.android.measurements.Constants.PREF_MAP_LAYER_CHURCH_PARENTS;
import static com.expidevapps.android.measurements.Constants.PREF_MAP_LAYER_CHURCH_TARGET;
import static com.expidevapps.android.measurements.Constants.PREF_MAP_LAYER_TRAINING;
import static com.expidevapps.android.measurements.model.Task.CREATE_CHURCH;
import static com.expidevapps.android.measurements.model.Task.CREATE_TRAINING;
import static com.expidevapps.android.measurements.model.Task.VIEW_CHURCH;
import static com.expidevapps.android.measurements.model.Task.VIEW_TRAINING;
import static com.expidevapps.android.measurements.support.v4.content.CurrentAssignmentLoader.ARG_LOAD_MINISTRY;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AlertDialog;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.map.ChurchItem;
import com.expidevapps.android.measurements.map.GmaItem;
import com.expidevapps.android.measurements.map.GmaRenderer;
import com.expidevapps.android.measurements.map.TrainingItem;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Location;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.content.ChurchesLoader;
import com.expidevapps.android.measurements.support.v4.content.CurrentAssignmentLoader;
import com.expidevapps.android.measurements.support.v4.content.TrainingsLoader;
import com.expidevapps.android.measurements.sync.GmaSyncService;
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
    private static final int MAP_LAYER_CHURCH_PARENTS = 5;

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
    @Nullable
    private GmaRenderer mRenderer;
    private final boolean[] mMapLayers = new boolean[6];

    @NonNull
    private /* final */ String mGuid;
    @Nullable
    private Assignment mAssignment;
    @Nullable
    private Ministry mMinistry;
    @Nullable
    private List<Training> mTrainings;
    @Nullable
    private List<Church> mChurches;
    @NonNull
    private LongSparseArray<ChurchItem> mVisibleChurches = new LongSparseArray<>();

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

        // update the visible churches
        updateVisibleChurches();

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
                GmaSyncService.syncChurches(getActivity(), mGuid, mAssignment.getMinistryId(), force);
            }
            GmaSyncService
                    .syncTrainings(getActivity(), mGuid, mAssignment.getMinistryId(), mAssignment.getMcc(), force);
        }
    }

    private void startLoaders() {
        // build the args used for various loaders
        final Bundle args = new Bundle(1);
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
            mRenderer = new GmaRenderer(context, mMap, mClusterManager);
            mRenderer.setMarkerDragListener(new MarkerDragListener());
            mClusterManager.setRenderer(mRenderer);
            mClusterManager.setOnClusterItemInfoWindowClickListener(
                    new ClusterManager.OnClusterItemInfoWindowClickListener<GmaItem>() {
                        @Override
                        public void onClusterItemInfoWindowClick(final GmaItem item) {
                            if (item instanceof ChurchItem) {
                                showEditChurch(((ChurchItem) item).getObject());
                            } else if (item instanceof TrainingItem) {
                                showEditTraining(((TrainingItem) item).getObject());
                            }
                        }
                    });
            mMap.setOnCameraChangeListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);
            mMap.setOnInfoWindowClickListener(mClusterManager);
            mMap.setOnMarkerDragListener(mClusterManager.getMarkerManager());
            mMap.setOnMapLongClickListener(new MapLongClickListener());
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);

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
        final SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_SETTINGS(mGuid), Context.MODE_PRIVATE);
        mMapLayers[MAP_LAYER_TRAINING] = prefs.getBoolean(PREF_MAP_LAYER_TRAINING, true);
        mMapLayers[MAP_LAYER_TARGET] = prefs.getBoolean(PREF_MAP_LAYER_CHURCH_TARGET, true);
        mMapLayers[MAP_LAYER_GROUP] = prefs.getBoolean(PREF_MAP_LAYER_CHURCH_GROUP, true);
        mMapLayers[MAP_LAYER_CHURCH] = prefs.getBoolean(PREF_MAP_LAYER_CHURCH_CHURCH, true);
        mMapLayers[MAP_LAYER_MULTIPLYING_CHURCH] = prefs.getBoolean(PREF_MAP_LAYER_CHURCH_MULTIPLYING, true);
        mMapLayers[MAP_LAYER_CHURCH_PARENTS] = prefs.getBoolean(PREF_MAP_LAYER_CHURCH_PARENTS, true);

        updateVisibleChurches();
    }

    private void updateVisibleChurches() {
        final LongSparseArray<ChurchItem> visibleChurches = new LongSparseArray<>();
        if (mAssignment != null && mChurches != null) {
            for (final Church church : mChurches) {
                if (mAssignment.can(VIEW_CHURCH, church) && church.hasLocation()) {
                    final boolean visible;
                    switch (church.getDevelopment()) {
                        case TARGET:
                            visible = mMapLayers[MAP_LAYER_TARGET];
                            break;
                        case GROUP:
                            visible = mMapLayers[MAP_LAYER_GROUP];
                            break;
                        case CHURCH:
                            visible = mMapLayers[MAP_LAYER_CHURCH];
                            break;
                        case MULTIPLYING_CHURCH:
                            visible = mMapLayers[MAP_LAYER_MULTIPLYING_CHURCH];
                            break;
                        default:
                            visible = false;
                            break;
                    }

                    // create/update the church item if visible
                    if (visible) {
                        visibleChurches.put(church.getId(), new ChurchItem(mAssignment, church));
                    }
                }
            }

            // set parents for all visible churches
            for (int i = 0; i < visibleChurches.size(); i++) {
                final ChurchItem item = visibleChurches.valueAt(i);
                final Church church = item.getObject();
                item.setParent(mMapLayers[MAP_LAYER_CHURCH_PARENTS] && church.hasParent() ?
                                       visibleChurches.get(church.getParentId()) : null);
            }
        }

        // replace the visible churches sparse array
        mVisibleChurches = visibleChurches;
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

            // add Church Markers to the map
            for (int i = 0; i < mVisibleChurches.size(); i++) {
                mClusterManager.addItem(mVisibleChurches.valueAt(i));
            }

            // add training markers to the map
            addTrainingMarkersToMap();

            // force a recluster
            mClusterManager.cluster();
        }
    }

    private void addTrainingMarkersToMap() {
        assert mClusterManager != null : "mClusterManager should be set before calling addTrainingMarkersToMap";

        if (mMapLayers[MAP_LAYER_TRAINING] && mAssignment != null && mAssignment.can(VIEW_TRAINING) &&
                mTrainings != null) {
            for (final Training training : mTrainings) {
                if (training.hasLocation()) {
                    mClusterManager.addItem(new TrainingItem(mAssignment, training));
                }
            }
        }
    }

    void showCreateChurch(@NonNull final LatLng pos) {
        if (mAssignment != null && mAssignment.can(CREATE_CHURCH)) {
            final FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag("createChurch") == null) {
                final CreateChurchFragment fragment =
                        CreateChurchFragment.newInstance(mGuid, mAssignment.getMinistryId(), mAssignment.getRole(), pos);
                fragment.show(fm.beginTransaction().addToBackStack("createChurch"), "createChurch");
            }
        }
    }

    void showEditChurch(@NonNull final Church church) {
        final boolean editable = church.canEdit(mAssignment);
        if (mAssignment != null && (editable || mAssignment.can(VIEW_CHURCH, church))) {
            final FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag("editChurch") == null) {
                final EditChurchFragment fragment = EditChurchFragment
                        .newInstance(mGuid, church.getId(), mAssignment.getMinistryId(), mAssignment.getRole());
                fragment.show(fm.beginTransaction().addToBackStack("editChurch"), "editChurch");
            }
        }
    }

    void showCreateTraining(@NonNull final LatLng pos) {
        if (mAssignment != null && mAssignment.can(CREATE_TRAINING)) {
            final FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag("createTraining") == null) {
                final CreateTrainingFragment fragment =
                        CreateTrainingFragment.newInstance(mGuid, mAssignment.getMinistryId(), mAssignment.getMcc(), pos);
                fragment.show(fm.beginTransaction().addToBackStack("createTraining"), "createTraining");
            }
        }
    }

    void showEditTraining(@NonNull final Training training) {
        final boolean editable = training.canEdit(mAssignment);
        if (mAssignment != null && (editable || mAssignment.can(VIEW_TRAINING))) {
            final FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag("editTraining") == null) {
                final EditTrainingFragment fragment =
                        EditTrainingFragment.newInstance(mGuid, training.getId(), mAssignment.getRole());
                fragment.show(fm.beginTransaction().addToBackStack("editTraining"), "editTraining");
            }
        }
    }

    private boolean canMoveMarker(GmaItem item) {
        boolean canMove = false;
        switch (mAssignment.getRole()) {
            case ADMIN:
            case INHERITED_ADMIN:
            case LEADER:
            case INHERITED_LEADER:
                canMove = true;
                break;
            case SELF_ASSIGNED:
            case MEMBER:
                if (item instanceof ChurchItem) {
                    return isOwnerOfChurch((ChurchItem) item);
                }
                else if (item instanceof  TrainingItem) {
                    return isOwnerOfTraining((TrainingItem) item);
                }
                break;
            default:
                canMove = false;
        }
        return canMove;
    }

    private class MarkerDragListener implements GmaRenderer.OnMarkerDragListener<GmaItem> {
        @Override
        public void onMarkerDragStart(@NonNull GmaItem item, @NonNull Marker marker) {
            // perform haptic feedback to let user know something is happening
            if (mMapFrame != null) {
                mMapFrame.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
        }

        @Override
        public void onMarkerDragEnd(@NonNull final GmaItem item, @NonNull final Marker marker) {
            final Location obj = item.getObject();
            if (mAssignment != null && canMoveMarker(item)) {
                // update location in the database
                final LatLng position = marker.getPosition();
                AsyncTaskCompat.execute(
                        new UpdateLocationRunnable(getActivity().getApplicationContext(), mGuid, obj, position));

                // update the currently loaded backing object
                obj.setLatitude(position.latitude);
                obj.setLongitude(position.longitude);

                // update map markers to their new state
                // XXX: this currently recreates all map markers, which is incredibly heavy to update a single node
                updateMapMarkers();
            } else {
                // reset position because you weren't actually allowed to move the item
                marker.setPosition(item.getPosition());
            }
        }
    }

    private boolean isOwnerOfChurch(ChurchItem church) {
        boolean editMode = false;
        String personId = GmaApiClient.getUserId(getActivity());
        if(personId != null && church.getCreatedBy() != null) {
            editMode = personId.equalsIgnoreCase(church.getCreatedBy());
        }
        return editMode;
    }

    private boolean isOwnerOfTraining(TrainingItem training) {
        boolean editMode = false;
        String personId = GmaApiClient.getUserId(getActivity());
        if(personId != null && training.getCreatedBy() != null) {
            editMode = personId.equalsIgnoreCase(training.getCreatedBy());
        }
        return editMode;
    }

    private static class UpdateLocationRunnable implements Runnable {
        @NonNull
        private final Context mContext;
        @NonNull
        private final GoogleAnalyticsManager mGoogleAnalytics;
        @NonNull
        private final String mGuid;
        @NonNull
        private final Location mObj;
        @NonNull
        private final LatLng mLocation;

        UpdateLocationRunnable(@NonNull final Context context, @NonNull final String guid, @NonNull final Location obj,
                               @NonNull final LatLng location) {
            mContext = context.getApplicationContext();
            mGoogleAnalytics = GoogleAnalyticsManager.getInstance(mContext);
            mGuid = guid;
            mObj = obj;
            mLocation = location;
        }

        @Override
        public void run() {
            final GmaDao dao = GmaDao.getInstance(mContext);
            final Transaction tx = dao.newTransaction();
            try {
                tx.beginTransactionNonExclusive();

                final Location fresh = dao.refresh(mObj);
                if (fresh != null) {
                    // update the location of this church
                    fresh.trackingChanges(true);
                    fresh.setLatitude(mLocation.latitude);
                    fresh.setLongitude(mLocation.longitude);
                    fresh.trackingChanges(false);

                    // store the update
                    dao.update(fresh, new String[] {
                            Contract.Location.COLUMN_LATITUDE, Contract.Location.COLUMN_LONGITUDE,
                            Contract.Base.COLUMN_DIRTY});
                }

                tx.setTransactionSuccessful();
            } finally {
                tx.endTransaction();
            }

            // track movement and sync the updated object back to the cloud
            if (mObj instanceof Church) {
                mGoogleAnalytics.sendMoveChurchEvent(mGuid, ((Church) mObj).getMinistryId(), ((Church) mObj).getId());
                GmaSyncService.syncDirtyChurches(mContext, mGuid);
            } else if (mObj instanceof Training) {
                mGoogleAnalytics.sendMoveTrainingEvent(mGuid, ((Training) mObj).getMinistryId(),
                                                       ((Training) mObj).getMcc(), ((Training) mObj).getId());
                GmaSyncService.syncDirtyTrainings(mContext, mGuid);
            }
        }
    }

    private class MapLongClickListener implements GoogleMap.OnMapLongClickListener {
        @Override
        public void onMapLongClick(final LatLng pos) {
            if (mMapFrame != null) {
                mMapFrame.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

            if (mAssignment.getMcc() == Ministry.Mcc.UNKNOWN ) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.validation_mcc_not_defined)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).show();
            }
            else {
                if (mAssignment.getMcc() == Ministry.Mcc.GCM) {
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.title_dialog_map_create_item)
                            .setPositiveButton(R.string.btn_dialog_map_create_training, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    showCreateTraining(pos);
                                }
                            })
                            .setNegativeButton(R.string.btn_dialog_map_create_church, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    showCreateChurch(pos);
                                }
                            }).show();
                }
                else {
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.title_dialog_map_create_item)
                            .setPositiveButton(R.string.btn_dialog_map_create_training, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    showCreateTraining(pos);
                                }
                            }).show();
                }
            }
        }
    }

    private class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment> {
        @Nullable
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle bundle) {
            switch (id) {
                case LOADER_CURRENT_ASSIGNMENT:
                    return new CurrentAssignmentLoader(getActivity(), mGuid, bundle);
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
                    return new TrainingsLoader(getActivity(), bundle);
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
