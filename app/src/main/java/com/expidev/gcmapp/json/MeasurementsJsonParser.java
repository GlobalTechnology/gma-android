package com.expidev.gcmapp.json;

import android.util.Log;

import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.model.measurement.MeasurementTypeIds;
import com.expidev.gcmapp.model.measurement.SubMinistryDetails;
import com.expidev.gcmapp.model.measurement.TeamMemberDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by William.Randall on 2/4/2015.
 */
public class MeasurementsJsonParser
{
    private static String TAG = MeasurementsJsonParser.class.getSimpleName();

    public static List<Measurement> parseMeasurements(JSONArray measurementsJson)
    {
        List<Measurement> measurementList = new ArrayList<>();

        try
        {
            for(int i = 0; i < measurementsJson.length(); i++)
            {
                JSONObject measurementJson = measurementsJson.getJSONObject(i);
                Measurement measurement = parseMeasurement(measurementJson);
                measurementList.add(measurement);
            }

            return measurementList;
        }
        catch(JSONException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    public static MeasurementDetails parseMeasurementDetails(JSONObject json)
    {
        MeasurementDetails measurementDetails = new MeasurementDetails();

        try
        {
            measurementDetails.setMeasurementTypeIds(parseMeasurementTypeIds(json.getJSONObject("measurement_type_ids")));
            measurementDetails.setSixMonthTotalAmounts(parseMeasurementMap(json.getJSONObject("total")));
            measurementDetails.setSixMonthLocalAmounts(parseMeasurementMap(json.getJSONObject("local")));
            measurementDetails.setSixMonthPersonalAmounts(parseMeasurementMap(json.getJSONObject("my_measurements")));
            measurementDetails.setLocalBreakdown(parseMeasurementMap(json.getJSONObject("local_breakdown")));
            measurementDetails.setSelfBreakdown(parseMeasurementMap(json.getJSONObject("self_breakdown")));
            measurementDetails.setSubMinistryDetails(parseSubMinistryDetails(json.getJSONArray("sub_ministries")));
            measurementDetails.setTeamMemberDetails(parseTeamDetails(json.getJSONArray("team")));
            measurementDetails.setSelfAssignedDetails(parseTeamDetails(json.getJSONArray("self_assigned")));

            return measurementDetails;
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    public static Measurement parseMeasurement(JSONObject measurementJson) throws JSONException
    {
        Measurement measurement = new Measurement();

        measurement.setName(measurementJson.getString("name"));
        measurement.setMeasurementId(measurementJson.getString("measurement_id"));
        measurement.setPermLink(measurementJson.getString("perm_link"));
        measurement.setCustom(measurementJson.getBoolean("is_custom"));
        measurement.setSection(measurementJson.getString("section"));
        measurement.setColumn(measurementJson.getString("column"));
        measurement.setTotal(measurementJson.optInt("total"));

        return measurement;
    }

    private static MeasurementTypeIds parseMeasurementTypeIds(JSONObject json) throws JSONException
    {
        MeasurementTypeIds measurementTypeIds = new MeasurementTypeIds();
        measurementTypeIds.setTotal(json.getString("total"));
        measurementTypeIds.setLocal(json.getString("local"));
        measurementTypeIds.setPerson(json.getString("person"));
        return measurementTypeIds;
    }

    private static Map<String, Integer> parseMeasurementMap(JSONObject json) throws JSONException
    {
        Map<String, Integer> measurementMap = new HashMap<String, Integer>();

        JSONArray names = json.names();
        for(int i = 0; i < names.length(); i++)
        {
            String key = names.getString(i);
            measurementMap.put(key, json.getInt(key));
        }

        return measurementMap;
    }

    private static List<SubMinistryDetails> parseSubMinistryDetails(JSONArray json) throws JSONException
    {
        List<SubMinistryDetails> subMinistryDetailsList = new ArrayList<SubMinistryDetails>();

        for(int i = 0; i < json.length(); i++)
        {
            JSONObject subMinistryJson = json.getJSONObject(i);

            SubMinistryDetails subMinistryDetails = new SubMinistryDetails();
            subMinistryDetails.setMinistryId(subMinistryJson.getString("ministry_id"));
            subMinistryDetails.setName(subMinistryJson.getString("name"));
            subMinistryDetails.setTotal(subMinistryJson.getInt("total"));

            subMinistryDetailsList.add(subMinistryDetails);
        }

        return subMinistryDetailsList;
    }

    private static List<TeamMemberDetails> parseTeamDetails(JSONArray json) throws JSONException
    {
        List<TeamMemberDetails> teamMemberDetailsList = new ArrayList<TeamMemberDetails>();

        for(int i = 0; i < json.length(); i++)
        {
            JSONObject teamMemberDetailsJson = json.getJSONObject(i);

            TeamMemberDetails teamMemberDetails = new TeamMemberDetails();
            teamMemberDetails.setAssignmentId(teamMemberDetailsJson.getString("assignment_id"));
            teamMemberDetails.setTeamRole(teamMemberDetailsJson.getString("team_role"));
            teamMemberDetails.setFirstName(teamMemberDetailsJson.getString("first_name"));
            teamMemberDetails.setLastName(teamMemberDetailsJson.getString("last_name"));
            teamMemberDetails.setPersonId(teamMemberDetailsJson.getString("person_id"));
            teamMemberDetails.setTotal(teamMemberDetailsJson.getInt("total"));

            teamMemberDetailsList.add(teamMemberDetails);
        }

        return teamMemberDetailsList;
    }
}
