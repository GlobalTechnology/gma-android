package com.expidev.gcmapp.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by matthewfrederick on 2/3/15.
 */
public class MarkerRender extends DefaultClusterRenderer<Marker> {
    public MarkerRender(Context context, GoogleMap map, ClusterManager<Marker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(final Marker item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getItemImage()))
                .title(item.getName());
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

        // TODO: for now disable custom icons for clusters until we figured out nuances of clustering
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.training));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<Marker> cluster) {
        return cluster.getSize() > 3;
    }
    
    
}
