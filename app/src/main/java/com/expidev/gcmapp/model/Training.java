package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class Training extends Base
{
    private int id;
    private String ministryId;
    private String name;
    private Date date;
    private String type;
    private String mcc;
    private double latitude;
    private double longitude;
    @NonNull
    private final List<GCMTrainingCompletions> completions = new ArrayList<>();

    public static boolean equals(Training first, Training second)
    {
        // does everything in the object need to be compared?
        
        if (first.getId() != second.getId()) return false;
        if (!first.getName().equals(second.getName())) return false;
        if (!first.getMinistryId().equals(second.getMinistryId())) return false;
        
        return true;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(String ministryId)
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

    @NonNull
    public List<GCMTrainingCompletions> getCompletions()
    {
        return Collections.unmodifiableList(completions);
    }

    public void setCompletions(@Nullable final List<GCMTrainingCompletions> completions) {
        this.completions.clear();
        if (completions != null) {
            this.completions.addAll(completions);
        }
    }

    public static class GCMTrainingCompletions
    {
        private int id;
        private int phase;
        private int numberCompleted;
        private Date date;
        private int trainingId;
        private Timestamp synced;
        
        public static boolean equals(GCMTrainingCompletions first, GCMTrainingCompletions second)
        {
            if (first.getId() != second.getId()) return false;
            if (first.getPhase() != second.getPhase()) return false;
            if (first.getTrainingId() != second.getTrainingId()) return false;

            return true;
        }

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public int getPhase()
        {
            return phase;
        }

        public void setPhase(int phase)
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

        public Timestamp getSynced()
        {
            return synced;
        }

        public void setSynced(Timestamp synced)
        {
            this.synced = synced;
        }
    }
}
