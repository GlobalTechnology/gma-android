package com.expidev.gcmapp.map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MarkerRender extends DefaultClusterRenderer<GmaItem> {
    @NonNull
    private final ClusterManager<GmaItem> mClusterManager;

    @Nullable
    private OnMarkerDragListener<GmaItem> mMarkerDragListener;

    public MarkerRender(@NonNull final Context context, @NonNull final GoogleMap map,
                        @NonNull final ClusterManager<GmaItem> clusterManager) {
        super(context, map, clusterManager);
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
    protected void onBeforeClusterItemRendered(final GmaItem item, final MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getItemImage()));
        markerOptions.title(item.getName());
        markerOptions.snippet(item.getSnippet());
        markerOptions.draggable(item.isDraggable());
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<GmaItem> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
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
