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
    private Ministry ministry;

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

    public Ministry getMinistry()
    {
        return ministry;
    }

    public void setMinistry(Ministry ministry)
    {
        this.ministry = ministry;
    }
}
