package com.expidev.gcmapp.json;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.expidev.gcmapp.model.Training;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrainingJsonParser {
    @NonNull
    public static List<Training> parseTrainings(@NonNull final JSONArray json) throws JSONException {
        final List<Training> trainings = new ArrayList<>();

        // parse all trainings in the provided array
        for (int i = 0; i < json.length(); i++) {
            trainings.add(parseTraining(json.getJSONObject(i)));
        }

        return trainings;
    }

    @NonNull
    public static Training parseTraining(@NonNull final JSONObject json) throws JSONException {
        final Training training = new Training();
        training.setId(json.optLong("Id"));
        training.setMinistryId(json.getString("ministry_id"));
        training.setName(json.getString("name"));
        training.setDate(stringToDate(json.getString("date")));
        training.setType(json.getString("type"));
        training.setMcc(json.getString("mcc"));
        training.setLastSynced(new Date());
        
        try
        {
            training.setLatitude(json.getDouble("latitude"));
            training.setLongitude(json.getDouble("longitude"));
        }
        catch (Exception e)
        {
            Log.w("TrainingJsonParser", "Location is null");
        }

        // parse completions
        if (json.has("gcm_training_completions")) {
            training.setCompletions(Training.Completion.listFromJson(json.getJSONArray("gcm_training_completions")));
        }

        return training;
    }

    private static final SimpleDateFormat PARSER_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    private static Date stringToDate(@NonNull final String string) {
        try {
            return PARSER_DATE.parse(string);
        } catch (final ParseException e) {
            return null;
        }
    }
}
