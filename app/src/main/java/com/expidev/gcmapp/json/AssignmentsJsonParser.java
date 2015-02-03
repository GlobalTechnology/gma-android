package com.expidev.gcmapp.json;

import android.util.Log;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 1/23/2015.
 */
public class AssignmentsJsonParser
{
    private static String TAG = AssignmentsJsonParser.class.getSimpleName();

    public static List<Assignment> parseAssignments(JSONArray jsonArray)
    {
        List<Assignment> assignments = new ArrayList<Assignment>();

        try
        {
            for(int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject assignmentJson = jsonArray.getJSONObject(i);
                Assignment assignment = new Assignment();

                assignment.setId(assignmentJson.getString("id"));
                assignment.setTeamRole(assignmentJson.getString("team_role"));
                
                JSONObject location = assignmentJson.optJSONObject("location");
                if(location != null)
                {
                    assignment.setLatitude(location.getDouble("latitude"));
                    assignment.setLongitude(location.getDouble("longitude"));
                    assignment.setLocationZoom(assignmentJson.optInt("location_zoom"));
                }

                AssociatedMinistry ministry = MinistryJsonParser.parseAssociatedMinistry(assignmentJson);

                if(assignmentJson.has("sub_ministries"))
                {
                    JSONArray subMinistriesJson = assignmentJson.getJSONArray("sub_ministries");
                    List<AssociatedMinistry> subMinistries = MinistryJsonParser.parseAssociatedMinistriesJson(subMinistriesJson);
                    ministry.setSubMinistries(subMinistries);
                }

                assignment.setMinistry(ministry);
                assignments.add(assignment);
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

        return assignments;
    }
}
