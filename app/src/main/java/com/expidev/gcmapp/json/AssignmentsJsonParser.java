package com.expidev.gcmapp.json;

import android.util.Log;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;

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
                
                JSONObject location = assignmentJson.getJSONObject("location");
                assignment.setLatitude(location.getDouble("latitude"));
                assignment.setLongitude(location.getDouble("longitude"));
                
                assignment.setLocationZoom(assignmentJson.getInt("location_zoom"));

                Ministry ministry = MinistryJsonParser.parseMinistry(assignmentJson);

                if(assignmentJson.has("sub_ministries"))
                {
                    JSONArray subMinistriesJson = assignmentJson.getJSONArray("sub_ministries");
                    List<Ministry> subMinistries = MinistryJsonParser.parseMinistriesJson(subMinistriesJson);
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
