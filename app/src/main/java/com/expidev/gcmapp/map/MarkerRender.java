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
    private final Context context;

    public MarkerRender(Context context, GoogleMap map, ClusterManager<Marker> clusterManager) {
        super(context.getApplicationContext(), map, clusterManager);

        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(final Marker item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getItemImage()))
                .title(item.getName());
    }

    // TODO: for now disable custom icons for clusters until we figured out nuances of clustering
//    @Override
//    protected void onBeforeClusterRendered(Cluster<Marker> cluster, MarkerOptions markerOptions) {
//        super.onBeforeClusterRendered(cluster, markerOptions);
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.training))
//            .title("" + cluster.getSize() + " training activities");
//    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<Marker> cluster) {
        return cluster.getSize() > 3;
    }
    
    
}
