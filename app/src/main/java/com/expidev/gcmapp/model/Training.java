package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class Training extends Location implements Cloneable 
{
    public static final long INVALID_ID = -1;
    
    private long id;
    @NonNull
    private String ministryId = Ministry.INVALID_ID;
    private String name;
    private Date date;
    private String type;
    @NonNull
    private Ministry.Mcc mcc = Ministry.Mcc.UNKNOWN;
    @NonNull
    private final List<GCMTrainingCompletions> completions = new ArrayList<>();
    private boolean mTrackingChanges = false;
    @NonNull
    private final Set<String> mDirty = new HashSet<>();

    public static final String JSON_ID = "id";
    public static final String JSON_NAME = "name";
    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_TYPE = "type";
    public static final String JSON_DATE = "date";
    public static final String JSON_MCC = "mcc";
    
    public Training()
    {       
    }
    
    private Training(@NonNull final Training training)
    {
        super(training);
        this.id = training.getId();
        this.ministryId = training.getMinistryId();
        this.name = training.getName();
        this.date = training.getDate();
        this.type = training.getType();
        this.mcc = training.mcc;
        this.setCompletions(training.getCompletions());
        mDirty.clear();
        mDirty.addAll(training.mDirty);
        mTrackingChanges = training.mTrackingChanges;
    }

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

    @NonNull
    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(@NonNull final String ministryId) {
        this.ministryId = ministryId;
    }

    @Nullable
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        if (mTrackingChanges)
        {
            mDirty.add(JSON_NAME);
        }
    }

    @Nullable
    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
        if (mTrackingChanges)
        {
            mDirty.add(JSON_DATE);
        }
    }
    
    public void setDate(String date) throws ParseException
    {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        this.date = format.parse(date);
        if (mTrackingChanges)
        {
            mDirty.add(JSON_DATE);
        }
    }

    @Nullable
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
        if (mTrackingChanges)
        {
            mDirty.add(JSON_TYPE);
        }
    }

    public Ministry.Mcc getMcc() {
        return mcc;
    }

    public void setMcc(@Nullable final String mcc) {
        setMcc(Ministry.Mcc.fromRaw(mcc));
    }

    public void setMcc(@NonNull final Ministry.Mcc mcc) {
        this.mcc = mcc;
    }

    public void trackingChanges(final boolean state)
    {
        mTrackingChanges = state;
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

    @Override
    public Training clone()
    {
        return new Training(this);
    }
    
    public boolean isDirty()
    {
        return !mDirty.isEmpty();
    }
    
    public void setDirty(@Nullable final String dirty)
    {
        mDirty.clear();
        if (dirty != null) Collections.addAll(mDirty, TextUtils.split(dirty, ","));
    }
    
    public JSONObject dirtyToJson() throws JSONException
    {
        final JSONObject json = this.toJson();
        final Iterator<String> keys = json.keys();
        while (keys.hasNext())
        {
            if (!this.mDirty.contains(keys.next())) keys.remove();
        }
        return json;
    }
    
    public JSONObject toJson() throws JSONException
    {
        final JSONObject json = new JSONObject();
        json.put(JSON_ID, this.getId());
        json.put(JSON_NAME, this.getName());
        json.put(JSON_MINISTRY_ID, this.getMinistryId());
        json.put(JSON_DATE, this.getDate());
        json.put(JSON_TYPE, this.getType());
        json.put(JSON_MCC, this.getMcc().raw);
        return json;
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
