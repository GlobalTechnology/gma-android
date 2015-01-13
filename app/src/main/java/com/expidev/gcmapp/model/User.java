package com.expidev.gcmapp.model;

/**
 * Created by matthewfrederick on 1/13/15.
 */
public class User
{
    private String firstName;
    private String lastName;
    private String casUsername;
    private String personId;

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

    public String getCasUsername()
    {
        return casUsername;
    }

    public void setCasUsername(String casUsername)
    {
        this.casUsername = casUsername;
    }

    public String getPersonId()
    {
        return personId;
    }

    public void setPersonId(String personId)
    {
        this.personId = personId;
    }
}
