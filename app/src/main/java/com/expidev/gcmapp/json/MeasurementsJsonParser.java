package com.expidev.gcmapp.json;

import android.util.Log;

import com.expidev.gcmapp.model.measurement.BreakdownData;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.model.measurement.MeasurementTypeIds;
import com.expidev.gcmapp.model.measurement.SixMonthAmounts;
import com.expidev.gcmapp.model.measurement.SubMinistryDetails;
import com.expidev.gcmapp.model.measurement.TeamMemberDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 2/4/2015.
 */
public class MeasurementsJsonParser
{
    private static String TAG = MeasurementsJsonParser.class.getSimpleName();

    public static List<Measurement> parseMeasurements(
        JSONArray measurementsJson,
        String ministryId,
        String mcc,
        String period)
    {
        List<Measurement> measurementList = new ArrayList<>();

        try
        {
            for(int i = 0; i < measurementsJson.length(); i++)
            {
                JSONObject measurementJson = measurementsJson.getJSONObject(i);
                Measurement measurement = parseMeasurement(measurementJson);
                measurement.setMinistryId(ministryId);
                measurement.setMcc(mcc);
                measurement.setPeriod(period);
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
            measurementDetails.setSixMonthTotalAmounts(parseSixMonthsAmounts(json.getJSONObject("total"), "total"));
            measurementDetails.setSixMonthLocalAmounts(parseSixMonthsAmounts(json.getJSONObject("local"), "local"));
            measurementDetails.setSixMonthPersonalAmounts(parseSixMonthsAmounts(json.getJSONObject("my_measurements"), "personal"));
            measurementDetails.setLocalBreakdown(parseBreakdownData(json.getJSONObject("local_breakdown"), "local"));
            measurementDetails.setSelfBreakdown(parseBreakdownData(json.getJSONObject("self_breakdown"), "self"));
            measurementDetails.setSubMinistryDetails(parseSubMinistryDetails(json.getJSONArray("sub_ministries")));
            measurementDetails.setTeamMemberDetails(parseTeamDetails(json.getJSONArray("team"), "team"));
            measurementDetails.setSelfAssignedDetails(parseTeamDetails(json.getJSONArray("self_assigned"), "self"));

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

    private static List<SixMonthAmounts> parseSixMonthsAmounts(JSONObject json, String type) throws JSONException
    {
        List<SixMonthAmounts> sixMonthAmountsList = new ArrayList<>();

        JSONArray names = json.names();
        for(int i = 0; i < names.length(); i++)
        {
            SixMonthAmounts row = new SixMonthAmounts();
            row.setMonth(names.getString(i));
            row.setAmount(json.getInt(row.getMonth()));
            row.setAmountType(type);
            sixMonthAmountsList.add(row);
        }

        return sixMonthAmountsList;
    }

    private static List<BreakdownData> parseBreakdownData(JSONObject json, String type) throws JSONException
    {
        List<BreakdownData> breakdownDataList = new ArrayList<>();

        JSONArray names = json.names();
        for(int i = 0; i < names.length(); i++)
        {
            BreakdownData row = new BreakdownData();
            row.setSource(names.getString(i));
            row.setAmount(json.getInt(row.getSource()));
            row.setType(type);
            breakdownDataList.add(row);
        }

        return breakdownDataList;
    }

    private static List<SubMinistryDetails> parseSubMinistryDetails(JSONArray json) throws JSONException
    {
        List<SubMinistryDetails> subMinistryDetailsList = new ArrayList<SubMinistryDetails>();

        for(int i = 0; i < json.length(); i++)
        {
            JSONObject subMinistryJson = json.getJSONObject(i);

            SubMinistryDetails subMinistryDetails = new SubMinistryDetails();
            subMinistryDetails.setSubMinistryId(subMinistryJson.getString("ministry_id"));
            subMinistryDetails.setName(subMinistryJson.getString("name"));
            subMinistryDetails.setTotal(subMinistryJson.getInt("total"));

            subMinistryDetailsList.add(subMinistryDetails);
        }

        return subMinistryDetailsList;
    }

    private static List<TeamMemberDetails> parseTeamDetails(JSONArray json, String type) throws JSONException
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
            teamMemberDetails.setType(type);

            teamMemberDetailsList.add(teamMemberDetails);
        }

        return teamMemberDetailsList;
    }

    /**
     * Returns a single {@link org.json.JSONObject} to be posted to the server, this will be part
     * of a {@link org.json.JSONArray} created using
     * {@link #createPostJsonForMeasurementDetails(org.json.JSONObject...)}
     *
     * @param type either local or personal depending on which value is being updated
     */
    public static JSONObject createJsonForMeasurementDetails(
        MeasurementDetails measurementDetails,
        String type) throws JSONException
    {
        JSONObject jsonObject = new JSONObject();

        switch(type)
        {
            case "local":
                jsonObject.put("measurement_type_id", measurementDetails.getMeasurementTypeIds().getLocal());
                jsonObject.put("value", measurementDetails.getLocalValue());
                break;
            case "personal":
                jsonObject.put("measurement_type_id", measurementDetails.getMeasurementTypeIds().getPerson());
                jsonObject.put("value", measurementDetails.getPersonalValue());
                break;
            default:
                break;
        }

        jsonObject.put("related_entity_id", measurementDetails.getMinistryId());
        jsonObject.put("period", measurementDetails.getPeriod());
        jsonObject.put("mcc", measurementDetails.getMcc().toLowerCase() + "_gma-app");

        return jsonObject;
    }

    /**
     * Returns a {@link org.json.JSONArray} that can be posted to the server in order to
     * add or update measurements
     *
     * @param objectsToPost individual {@link org.json.JSONObject}s that will make up the POST request
     */
    public static JSONArray createPostJsonForMeasurementDetails(List<JSONObject> objectsToPost)
    {
        JSONArray data = new JSONArray();

        for(JSONObject object : objectsToPost)
        {
            data.put(object);
        }

        return data;
    }
}
