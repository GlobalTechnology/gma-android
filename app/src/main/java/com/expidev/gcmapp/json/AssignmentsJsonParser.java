package com.expidev.gcmapp.json;

import android.support.annotation.NonNull;
import android.util.Log;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 1/23/2015.
 */
public class AssignmentsJsonParser
{
    private static String TAG = AssignmentsJsonParser.class.getSimpleName();

    @NonNull
    public static Assignment parseAssignment(@NonNull final JSONObject json) throws JSONException {
        // load base assignment
        final Assignment assignment = new Assignment();
        assignment.setId(json.getString("id"));
        assignment.setRole(json.getString("team_role"));
        assignment.setMinistryId(json.getString("ministry_id"));

        // load location data
        final JSONObject location = json.optJSONObject("location");
        if (location != null) {
            assignment.setLatitude(location.getDouble("latitude"));
            assignment.setLongitude(location.getDouble("longitude"));
            assignment.setLocationZoom(json.optInt("location_zoom"));
        }

        // parse the ministry
        final AssociatedMinistry ministry = MinistryJsonParser.parseAssociatedMinistry(json);
        assignment.setMinistry(ministry);

        // parse any sub ministries
        final JSONArray subMinistries = json.optJSONArray("sub_ministries");
        if (subMinistries != null) {
            ministry.setSubMinistries(MinistryJsonParser.parseAssociatedMinistriesJson(subMinistries));
        }

        // return the assignment
        return assignment;
    }

    @NonNull
    public static List<Assignment> parseAssignments(@NonNull final JSONArray jsonArray) {
        List<Assignment> assignments = new ArrayList<Assignment>();

        try
        {
            for(int i = 0; i < jsonArray.length(); i++)
            {
                assignments.add(parseAssignment(jsonArray.getJSONObject(i)));
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

        return assignments;
    }
}
