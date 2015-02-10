package com.expidev.gcmapp.model;

import java.io.Serializable;

/**
 * Created by William.Randall on 1/23/2015.
 */
public class Assignment implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String id;
    private String teamRole;
    private AssociatedMinistry ministry;
    private double latitude;
    private double longitude;
    private int locationZoom;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTeamRole()
    {
        return teamRole;
    }

    public void setTeamRole(String teamRole)
    {
        this.teamRole = teamRole;
    }

    public AssociatedMinistry getMinistry()
    {
        return ministry;
    }

    public void setMinistry(AssociatedMinistry ministry)
    {
        this.ministry = ministry;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public int getLocationZoom()
    {
        return locationZoom;
    }

    public void setLocationZoom(int locationZoom)
    {
        this.locationZoom = locationZoom;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + id + ", ");
        sb.append("latitude: " + latitude + ", ");
        sb.append("longitude: " + longitude);
        
        return sb.toString();
    }
}
