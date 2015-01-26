package com.expidev.gcmapp.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class Training
{
    private int id;
    private UUID ministryId;
    private String name;
    private Date date;
    private String type;
    private String mcc;
    private double latitude;
    private double longitude;
    private List<GCMTrainingCompletions> completions;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public UUID getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(UUID ministryId)
    {
        this.ministryId = ministryId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getMcc()
    {
        return mcc;
    }

    public void setMcc(String mcc)
    {
        this.mcc = mcc;
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

    public List<GCMTrainingCompletions> getCompletions()
    {
        return completions;
    }

    public void setCompletions(List<GCMTrainingCompletions> completions)
    {
        this.completions = completions;
    }

    private class GCMTrainingCompletions
    {
        private int id;
        private String phase;
        private int numberCompleted;
        private Date date;
        private int trainingId;

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public String getPhase()
        {
            return phase;
        }

        public void setPhase(String phase)
        {
            this.phase = phase;
        }

        public int getNumberCompleted()
        {
            return numberCompleted;
        }

        public void setNumberCompleted(int numberCompleted)
        {
            this.numberCompleted = numberCompleted;
        }

        public Date getDate()
        {
            return date;
        }

        public void setDate(Date date)
        {
            this.date = date;
        }

        public int getTrainingId()
        {
            return trainingId;
        }

        public void setTrainingId(int trainingId)
        {
            this.trainingId = trainingId;
        }
    }
}
