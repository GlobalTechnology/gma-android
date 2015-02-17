package com.expidev.gcmapp.model.measurement;

import java.io.Serializable;

/**
 * Created by William.Randall on 1/29/2015.
 */
public class TeamMemberDetails extends MeasurementDetailsData implements Serializable
{
    private static final long serialVersionUID = 0L;

    private String assignmentId;
    private String teamRole;
    private String firstName;
    private String lastName;
    private String personId;
    private int total;
    private String type; // self vs team

    public String getAssignmentId()
    {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId)
    {
        this.assignmentId = assignmentId;
    }

    public String getTeamRole()
    {
        return teamRole;
    }

    public void setTeamRole(String teamRole)
    {
        this.teamRole = teamRole;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getPersonId()
    {
        return personId;
    }

    public void setPersonId(String personId)
    {
        this.personId = personId;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
