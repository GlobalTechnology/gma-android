package com.expidev.gcmapp.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Training extends Location implements Cloneable {
    public static final long INVALID_ID = -1;

    public static final String JSON_ID = "Id";
    public static final String JSON_NAME = "name";
    public static final String JSON_MINISTRY_ID = "ministry_id";
    public static final String JSON_TYPE = "type";
    public static final String JSON_DATE = "date";
    public static final String JSON_MCC = "mcc";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LONGITUDE = "longitude";
    public static final String JSON_COMPLETIONS = "gcm_training_completions";

    private long id;
    @NonNull
    private String ministryId = Ministry.INVALID_ID;
    private String name;
    @Nullable
    private LocalDate date;
    private String type;
    @NonNull
    private Ministry.Mcc mcc = Ministry.Mcc.UNKNOWN;
    @NonNull
    private final List<Completion> completions = new ArrayList<>();
    private boolean mTrackingChanges = false;
    @NonNull
    private final Set<String> mDirty = new HashSet<>();

    public Training()
    {       
    }
    
    private Training(@NonNull final Training training)
    {
        super(training);
        this.id = training.id;
        this.ministryId = training.ministryId;
        this.name = training.name;
        this.date = training.date;
        this.type = training.type;
        this.mcc = training.mcc;
        this.setCompletions(training.completions);
        mDirty.clear();
        mDirty.addAll(training.mDirty);
        mTrackingChanges = training.mTrackingChanges;
    }

    @NonNull
    public static List<Training> listFromJson(@NonNull final JSONArray json) throws JSONException {
        final List<Training> trainings = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            trainings.add(fromJson(json.getJSONObject(i)));
        }
        return trainings;
    }

    @NonNull
    public static Training fromJson(@NonNull final JSONObject json) throws JSONException {
        final Training training = new Training();

        training.id = json.getLong(JSON_ID);
        training.ministryId = json.getString(JSON_MINISTRY_ID);
        training.name = json.getString(JSON_NAME);
        training.type = json.getString(JSON_TYPE);
        training.mcc = Ministry.Mcc.fromRaw(json.getString(JSON_MCC));
        training.date = LocalDate.parse(json.getString(JSON_DATE));
        training.setLatitude(json.optDouble(JSON_LATITUDE, Double.NaN));
        training.setLongitude(json.optDouble(JSON_LONGITUDE, Double.NaN));

        final JSONArray completions = json.optJSONArray(JSON_COMPLETIONS);
        if (completions != null) {
            training.setCompletions(Completion.listFromJson(completions));
        }

        return training;
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
    public LocalDate getDate() {
        return date;
    }

    public void setDate(@Nullable final LocalDate date) {
        this.date = date;
        if (mTrackingChanges)
        {
            mDirty.add(JSON_DATE);
        }
    }

    @Deprecated
    public void setDate(String date) throws ParseException
    {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        setDate(new LocalDate(format.parse(date)));
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
    public List<Completion> getCompletions() {
        return Collections.unmodifiableList(completions);
    }

    public void setCompletions(@Nullable final List<Completion> completions) {
        this.completions.clear();
        if (completions != null) {
            this.completions.addAll(completions);
        }
    }

    public void addCompletion(@NonNull final Completion completion) {
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
        json.put(JSON_NAME, this.getName());
        json.put(JSON_MINISTRY_ID, this.getMinistryId());
        json.put(JSON_DATE, this.getDate());
        json.put(JSON_TYPE, this.getType());
        json.put(JSON_MCC, this.getMcc().raw);
        return json;
    }

    public static class Completion extends Base {
        public static final long INVALID_ID = -1;

        private static final String JSON_ID = "Id";
        private static final String JSON_TRAINING_ID = "training_id";
        private static final String JSON_PHASE = "phase";
        private static final String JSON_NUMBER_COMPLETED = "number_completed";
        private static final String JSON_DATE = "date";

        private long id;
        private long trainingId;
        private int phase;
        private int numberCompleted;
        @Nullable
        private LocalDate date = LocalDate.now();

        @NonNull
        public static List<Completion> listFromJson(@NonNull final JSONArray json) throws JSONException {
            final List<Completion> completions = new ArrayList<>();
            for (int i = 0; i < json.length(); i++) {
                completions.add(fromJson(json.getJSONObject(i)));
            }
            return completions;
        }

        @NonNull
        public static Completion fromJson(@NonNull final JSONObject json) throws JSONException {
            final Completion completion = new Completion();
            completion.id = json.getLong(JSON_ID);
            completion.trainingId = json.getLong(JSON_TRAINING_ID);
            completion.phase = json.getInt(JSON_PHASE);
            completion.numberCompleted = json.getInt(JSON_NUMBER_COMPLETED);
            completion.date = LocalDate.parse(json.getString(JSON_DATE));
            return completion;
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

        @Nullable
        public LocalDate getDate() {
            return date;
        }

        public void setDate(@Nullable final LocalDate date) {
            this.date = date;
        }

        public long getTrainingId() {
            return trainingId;
        }

        public void setTrainingId(final long trainingId) {
            this.trainingId = trainingId;
        }
    }
}
