package com.expidev.gcmapp.map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MarkerRender extends DefaultClusterRenderer<Marker> {
    @NonNull
    private final ClusterManager<Marker> mClusterManager;

    @Nullable
    private OnMarkerDragListener<Marker> mMarkerDragListener;

    public MarkerRender(@NonNull final Context context, @NonNull final GoogleMap map,
                        @NonNull final ClusterManager<Marker> clusterManager) {
        super(context, map, clusterManager);
        mClusterManager = clusterManager;
    }

    public void setMarkerDragListener(@Nullable final OnMarkerDragListener<Marker> listener) {
        mMarkerDragListener = listener;
    }

    /* BEGIN lifecycle */

    @Override
    public void onAdd() {
        super.onAdd();
        mClusterManager.getMarkerCollection().setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(com.google.android.gms.maps.model.Marker marker) {
                // do nothing
            }

            @Override
            public void onMarkerDrag(com.google.android.gms.maps.model.Marker marker) {
                // do nothing
            }

            @Override
            public void onMarkerDragEnd(com.google.android.gms.maps.model.Marker marker) {
                if (mMarkerDragListener != null) {
                    final Marker item = getClusterItem(marker);
                    if (item != null) {
                        mMarkerDragListener.onMarkerDragEnd(item, marker.getPosition());
                    }
                }
            }
        });
    }

    @Override
    protected void onBeforeClusterItemRendered(final Marker item, final MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getItemImage()));
        markerOptions.title(item.getName());
        markerOptions.snippet(item.getSnippet());
        markerOptions.draggable(item.isDraggable());
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<Marker> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        int churches = 0;
        int trainings = 0;
        for (final Marker marker : cluster.getItems()) {
            if(marker instanceof ChurchMarker) {
                churches++;
            } else if(marker instanceof TrainingMarker) {
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

    public interface OnMarkerDragListener<T> {
        void onMarkerDragEnd(@NonNull T marker, @NonNull LatLng position);
    }
}
