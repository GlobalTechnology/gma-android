package com.expidevapps.android.measurements.map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.ImmutableList;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Map;
import java.util.Set;

public class GmaRenderer extends DefaultClusterRenderer<GmaItem> {
    @NonNull
    private final GoogleMap mMap;
    @NonNull
    private final ClusterManager<GmaItem> mClusterManager;

    @Nullable
    private OnMarkerDragListener<GmaItem> mMarkerDragListener;

    @NonNull
    private Map<GmaItem, LatLng> mMarkerLocations = new ArrayMap<>();
    @NonNull
    private Map<GmaItem, Polyline> mParentLines = new ArrayMap<>();

    public GmaRenderer(@NonNull final Context context, @NonNull final GoogleMap map,
                       @NonNull final ClusterManager<GmaItem> clusterManager) {
        super(context, map, clusterManager);
        mMap = map;
        mClusterManager = clusterManager;
    }

    public void setMarkerDragListener(@Nullable final OnMarkerDragListener<GmaItem> listener) {
        mMarkerDragListener = listener;
    }

    /* BEGIN lifecycle */

    @Override
    public void onAdd() {
        super.onAdd();
        mClusterManager.getMarkerCollection().setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull final Marker marker) {
                if (mMarkerDragListener != null) {
                    final GmaItem item = getClusterItem(marker);
                    if (item != null) {
                        mMarkerDragListener.onMarkerDragStart(item, marker);
                    }
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // do nothing
            }

            @Override
            public void onMarkerDragEnd(@NonNull final Marker marker) {
                if (mMarkerDragListener != null) {
                    final GmaItem item = getClusterItem(marker);
                    if (item != null) {
                        mMarkerDragListener.onMarkerDragEnd(item, marker);
                    }
                }
            }
        });
    }

    @Override
    public void onClustersChanged(final Set<? extends Cluster<GmaItem>> clusters) {
        super.onClustersChanged(clusters);

        // update marker locations for all items
        final Map<GmaItem, LatLng> locations = new ArrayMap<>();
        for (final Cluster<GmaItem> cluster : clusters) {
            final boolean renderAsCluster = shouldRenderAsCluster(cluster);
            for (final GmaItem item : cluster.getItems()) {
                locations.put(item, renderAsCluster ? cluster.getPosition() : item.getPosition());
            }
        }
        mMarkerLocations = locations;

        // update parent polylines
        final Map<GmaItem, Polyline> parentLines = new ArrayMap<>();
        for (final GmaItem item : mMarkerLocations.keySet()) {
            final GmaItem parent = item.getParent();
            if (parent != null && mMarkerLocations.containsKey(parent)) {
                // update/create polyline
                Polyline line = mParentLines.remove(item);
                if (line == null) {
                    line = mMap.addPolyline(new PolylineOptions().width(5));
                }
                line.setPoints(ImmutableList.of(mMarkerLocations.get(parent), mMarkerLocations.get(item)));

                // store polyline
                parentLines.put(item, line);
            }
        }

        // remove old parent lines
        for (final Polyline line : mParentLines.values()) {
            line.remove();
        }
        mParentLines = parentLines;
    }

    @Override
    protected void onBeforeClusterItemRendered(final GmaItem item, final MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.anchor(0.5F, 0.5F);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getItemImage()));
        markerOptions.title(item.getName());
        markerOptions.snippet(item.getSnippet());
        markerOptions.draggable(item.isDraggable());
    }

    @Override
    protected void onBeforeClusterRendered(final Cluster<GmaItem> cluster, final MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        markerOptions.anchor(0.5F, 0.5F);
        int churches = 0;
        int trainings = 0;
        for (final GmaItem item : cluster.getItems()) {
            if (item instanceof ChurchItem) {
                churches++;
            } else if (item instanceof TrainingItem) {
                trainings++;
            }
        }

        // generate the title for this cluster
        final StringBuilder title = new StringBuilder();
        if(churches > 0) {
            title.append(churches).append(" Churches");
        }
        if(trainings > 0) {
            if(title.length() > 0) {
                title.append(" and ");
            }
            title.append(trainings).append(" training activities");
        }
        markerOptions.title(title.toString());
    }

    @Override
    public void onRemove() {
        super.onRemove();
        mClusterManager.getMarkerCollection().setOnMarkerDragListener(null);
    }

    /* END lifecycle */

    public interface OnMarkerDragListener<T extends ClusterItem> {
        void onMarkerDragStart(@NonNull T item, @NonNull Marker marker);

        void onMarkerDragEnd(@NonNull T item, @NonNull Marker marker);
    }
}
