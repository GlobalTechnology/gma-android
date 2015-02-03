package com.expidev.gcmapp.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by matthewfrederick on 2/3/15.
 */
public class GcmMarker implements ClusterItem
{
    public final String name;
    private final LatLng position;
    
    public GcmMarker(String name, double lat, double lng)
    {
        this.name = name;
        position = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition()
    {
        return position;
    }
}
