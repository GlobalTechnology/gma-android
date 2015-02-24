package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class Training extends Location implements Cloneable 
{
    public static final long INVALID_ID = -1;
    
    private long id;
    private String ministryId;
    private String name;
    private Date date;
    private String type;
    private String mcc;
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

    public long getId()
    {
        return id;
    }

    public void setId(long id)
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

    public void addCompletion(@NonNull final GCMTrainingCompletions completion) {
        this.completions.add(completion);
    }

    public static class GCMTrainingCompletions extends Base
    {
        private long id;
        private int phase;
        private int numberCompleted;
        private Date date;
        private int trainingId;

        public static boolean equals(GCMTrainingCompletions first, GCMTrainingCompletions second)
        {
            if (first.getId() != second.getId()) return false;
            if (first.getPhase() != second.getPhase()) return false;
            if (first.getTrainingId() != second.getTrainingId()) return false;

            return true;
        }

        public long getId()
        {
            return id;
        }

        public void setId(long id)
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
    }
}
