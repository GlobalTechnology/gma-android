package com.expidev.gcmapp.json;

import android.util.Log;

import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.model.measurement.MeasurementTypeIds;
import com.expidev.gcmapp.model.measurement.SubMinistryDetails;
import com.expidev.gcmapp.model.measurement.TeamMemberDetails;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by William.Randall on 1/30/2015.
 */
public class MeasurementsJsonParserTest extends TestCase
{
    private final String TAG = getClass().getSimpleName();

    public void testParseMeasurementDetails() throws Exception
    {
        MeasurementDetails measurementDetails = MeasurementsJsonParser.parseMeasurementDetails(dummyMeasurementDetails());
        MeasurementTypeIds measurementTypeIds = measurementDetails.getMeasurementTypeIds();
        Map<String, Integer> sixMonthTotals = measurementDetails.getSixMonthTotalAmounts();
        Map<String, Integer> sixMonthLocalData = measurementDetails.getSixMonthLocalAmounts();
        Map<String, Integer> sixMonthPersonalData = measurementDetails.getSixMonthPersonalAmounts();
        Map<String, Integer> localBreakdown = measurementDetails.getLocalBreakdown();
        Map<String, Integer> selfBreakdown = measurementDetails.getSelfBreakdown();
        List<SubMinistryDetails> subMinistryDetailsList = measurementDetails.getSubMinistryDetails();
        List<TeamMemberDetails> teamMemberDetailsList = measurementDetails.getTeamMemberDetails();
        List<TeamMemberDetails> selfAssignedDetails = measurementDetails.getSelfAssignedDetails();

        // Measurement Type Ids
        assertEquals("451f4386-6112-11e4-8bc7-12c37bb2d521", measurementTypeIds.getTotal());
        assertEquals("2e33574c-64fb-11e4-855c-12c37bb2d521", measurementTypeIds.getLocal());
        assertEquals("9d2bf37e-64fc-11e4-8569-12c37bb2d521", measurementTypeIds.getPerson());
        Log.i(TAG, "Measurement Type Ids OK");

        // Six month total data
        assertTrue(sixMonthTotals.containsKey("2014-05"));
        assertTrue(sixMonthTotals.containsKey("2014-06"));
        assertTrue(sixMonthTotals.containsKey("2014-07"));
        assertTrue(sixMonthTotals.containsKey("2014-08"));
        assertTrue(sixMonthTotals.containsKey("2014-09"));
        assertTrue(sixMonthTotals.containsKey("2014-10"));

        assertEquals(sixMonthTotals.get("2014-05"), Integer.valueOf(0));
        assertEquals(sixMonthTotals.get("2014-06"), Integer.valueOf(0));
        assertEquals(sixMonthTotals.get("2014-07"), Integer.valueOf(0));
        assertEquals(sixMonthTotals.get("2014-08"), Integer.valueOf(0));
        assertEquals(sixMonthTotals.get("2014-09"), Integer.valueOf(0));
        assertEquals(sixMonthTotals.get("2014-10"), Integer.valueOf(115));
        Log.i(TAG, "Six month totals OK");

        // Six month local data
        assertTrue(sixMonthLocalData.containsKey("2014-05"));
        assertTrue(sixMonthLocalData.containsKey("2014-06"));
        assertTrue(sixMonthLocalData.containsKey("2014-07"));
        assertTrue(sixMonthLocalData.containsKey("2014-08"));
        assertTrue(sixMonthLocalData.containsKey("2014-09"));
        assertTrue(sixMonthLocalData.containsKey("2014-10"));

        assertEquals(sixMonthLocalData.get("2014-05"), Integer.valueOf(0));
        assertEquals(sixMonthLocalData.get("2014-06"), Integer.valueOf(0));
        assertEquals(sixMonthLocalData.get("2014-07"), Integer.valueOf(0));
        assertEquals(sixMonthLocalData.get("2014-08"), Integer.valueOf(0));
        assertEquals(sixMonthLocalData.get("2014-09"), Integer.valueOf(0));
        assertEquals(sixMonthLocalData.get("2014-10"), Integer.valueOf(115));
        Log.i(TAG, "Six month local data OK");

        // Six month personal data
        assertTrue(sixMonthPersonalData.containsKey("2014-05"));
        assertTrue(sixMonthPersonalData.containsKey("2014-06"));
        assertTrue(sixMonthPersonalData.containsKey("2014-07"));
        assertTrue(sixMonthPersonalData.containsKey("2014-08"));
        assertTrue(sixMonthPersonalData.containsKey("2014-09"));
        assertTrue(sixMonthPersonalData.containsKey("2014-10"));

        assertEquals(sixMonthPersonalData.get("2014-05"), Integer.valueOf(0));
        assertEquals(sixMonthPersonalData.get("2014-06"), Integer.valueOf(0));
        assertEquals(sixMonthPersonalData.get("2014-07"), Integer.valueOf(0));
        assertEquals(sixMonthPersonalData.get("2014-08"), Integer.valueOf(0));
        assertEquals(sixMonthPersonalData.get("2014-09"), Integer.valueOf(0));
        assertEquals(sixMonthPersonalData.get("2014-10"), Integer.valueOf(6));
        Log.i(TAG, "Six month personal data OK");

        // Local breakdown
        assertTrue(localBreakdown.containsKey("gcmapp"));
        assertTrue(localBreakdown.containsKey("training"));
        assertTrue(localBreakdown.containsKey("total"));

        assertEquals(localBreakdown.get("gcmapp"), Integer.valueOf(0));
        assertEquals(localBreakdown.get("training"), Integer.valueOf(115));
        assertEquals(localBreakdown.get("total"), Integer.valueOf(115));
        Log.i(TAG, "Local breakdown OK");

        // Personal breakdown
        assertTrue(selfBreakdown.containsKey("gcmapp"));
        assertTrue(selfBreakdown.containsKey("total"));

        assertEquals(selfBreakdown.get("gcmapp"), Integer.valueOf(6));
        assertEquals(selfBreakdown.get("total"), Integer.valueOf(6));
        Log.i(TAG, "Personal breakdown OK");

        // Sub ministries
        assertEquals(1, subMinistryDetailsList.size());

        SubMinistryDetails subMinistryDetails = subMinistryDetailsList.get(0);
        assertEquals("Guatemala Sub 1", subMinistryDetails.getName());
        assertEquals("d5d465f6-60df-11e4-9758-12c37bb2d521", subMinistryDetails.getMinistryId());
        assertEquals(0, subMinistryDetails.getTotal());
        Log.i(TAG, "Sub ministries OK");

        // Team details
        assertEquals(7, teamMemberDetailsList.size());

        TeamMemberDetails ryan = teamMemberDetailsList.get(0);
        assertEquals("1d02bd44-74b6-11e4-bc3b-12c37bb2d521", ryan.getAssignmentId());
        assertEquals("leader", ryan.getTeamRole());
        assertEquals("Ryan", ryan.getFirstName());
        assertEquals("Carlson", ryan.getLastName());
        assertEquals("1ce34112-74b6-11e4-bc32-12c37bb2d521", ryan.getPersonId());
        assertEquals(0, ryan.getTotal());
        Log.i(TAG, "Team details OK");

        // Self assigned details
        assertEquals(1, selfAssignedDetails.size());

        TeamMemberDetails selfAssigned = selfAssignedDetails.get(0);
        assertEquals("bce234b6-64d0-11e4-96ff-12c37bb2d521", selfAssigned.getAssignmentId());
        assertEquals("", selfAssigned.getTeamRole());
        assertEquals("Staff", selfAssigned.getFirstName());
        assertEquals("Member", selfAssigned.getLastName());
        assertEquals("13c71d52-5057-11e4-9d23-12c37bb2d521", selfAssigned.getPersonId());
        assertEquals(0, selfAssigned.getTotal());
        Log.i(TAG, "Self assigned details OK");
    }

    private JSONObject dummyMeasurementDetails() throws JSONException
    {
        return new JSONObject("{" +
            "    \"measurement_type_ids\": {" +
            "        \"total\": \"451f4386-6112-11e4-8bc7-12c37bb2d521\"," +
            "        \"local\": \"2e33574c-64fb-11e4-855c-12c37bb2d521\"," +
            "        \"person\": \"9d2bf37e-64fc-11e4-8569-12c37bb2d521\"" +
            "    }," +
            "    \"total\": {" +
            "        \"2014-05\": 0," +
            "        \"2014-06\": 0," +
            "        \"2014-07\": 0," +
            "        \"2014-08\": 0," +
            "        \"2014-09\": 0," +
            "        \"2014-10\": 115" +
            "    }," +
            "    \"local_breakdown\": {" +
            "        \"gcmapp\": 0," +
            "        \"training\": 115," +
            "        \"total\": 115" +
            "    }," +
            "    \"local\": {" +
            "        \"2014-05\": 0," +
            "        \"2014-06\": 0," +
            "        \"2014-07\": 0," +
            "        \"2014-08\": 0," +
            "        \"2014-09\": 0," +
            "        \"2014-10\": 115" +
            "    }," +
            "    \"self_breakdown\": {" +
            "        \"gcmapp\": 6," +
            "        \"total\": 6" +
            "    }," +
            "    \"my_measurements\": {" +
            "        \"2014-05\": 0," +
            "        \"2014-06\": 0," +
            "        \"2014-07\": 0," +
            "        \"2014-08\": 0," +
            "        \"2014-09\": 0," +
            "        \"2014-10\": 6" +
            "    }," +
            "    \"sub_ministries\": [" +
            "        {" +
            "            \"name\": \"Guatemala Sub 1\"," +
            "            \"ministry_id\": \"d5d465f6-60df-11e4-9758-12c37bb2d521\"," +
            "            \"total\": 0" +
            "        }" +
            "    ]," +
            "    \"team\": [" +
            "        {" +
            "            \"assignment_id\": \"1d02bd44-74b6-11e4-bc3b-12c37bb2d521\"," +
            "            \"team_role\": \"leader\"," +
            "            \"first_name\": \"Ryan\"," +
            "            \"last_name\": \"Carlson\"," +
            "            \"person_id\": \"1ce34112-74b6-11e4-bc32-12c37bb2d521\"," +
            "            \"total\": 0" +
            "        }," +
            "        {" +
            "            \"assignment_id\": \"56e813fc-6ab0-11e4-94ac-12c37bb2d521\"," +
            "            \"team_role\": \"leader\"," +
            "            \"first_name\": \"Matthew\"," +
            "            \"last_name\": \"Ritsema\"," +
            "            \"person_id\": \"56c03d6e-6ab0-11e4-94a3-12c37bb2d521\"," +
            "            \"total\": 0" +
            "        }," +
            "        {" +
            "            \"assignment_id\": \"fdc0c006-64d0-11e4-970b-12c37bb2d521\"," +
            "            \"team_role\": \"leader\"," +
            "            \"first_name\": \"Team\"," +
            "            \"last_name\": \"Leader\"," +
            "            \"person_id\": \"14c3e55a-5057-11e4-9d2c-12c37bb2d521\"," +
            "            \"total\": 0" +
            "        }," +
            "        {" +
            "            \"assignment_id\": \"2d80886c-64d1-11e4-9717-12c37bb2d521\"," +
            "            \"team_role\": \"leader\"," +
            "            \"first_name\": \"Accounts\"," +
            "            \"last_name\": \"Team\"," +
            "            \"person_id\": \"16bef14c-5057-11e4-9d35-12c37bb2d521\"," +
            "            \"total\": 0" +
            "        }," +
            "        {" +
            "            \"assignment_id\": \"19945ebe-6e95-11e4-ba23-12c37bb2d521\"," +
            "            \"team_role\": \"leader\"," +
            "            \"first_name\": \"Keith\"," +
            "            \"last_name\": \"Seabourn\"," +
            "            \"person_id\": \"d460d176-edaf-11e3-b702-12725f8f377c\"," +
            "            \"total\": 0" +
            "        }," +
            "        {" +
            "            \"assignment_id\": \"34f38cd8-6ad1-11e4-94b8-12c37bb2d521\"," +
            "            \"team_role\": \"leader\"," +
            "            \"first_name\": \"Mike\"," +
            "            \"last_name\": \"Thacker\"," +
            "            \"person_id\": \"35716bec-5057-11e4-9e4d-12c37bb2d521\"," +
            "            \"total\": 0" +
            "        }," +
            "        {" +
            "            \"assignment_id\": \"0b1d97c4-6e95-11e4-ba17-12c37bb2d521\"," +
            "            \"team_role\": \"leader\"," +
            "            \"first_name\": \"Virgil\"," +
            "            \"last_name\": \"Anderson\"," +
            "            \"person_id\": \"a61eae26-edb6-11e3-9daf-12725f8f377c\"," +
            "            \"total\": 0" +
            "        }" +
            "    ]," +
            "    \"self_assigned\": [" +
            "        {" +
            "            \"assignment_id\": \"bce234b6-64d0-11e4-96ff-12c37bb2d521\"," +
            "            \"team_role\": \"\"," +
            "            \"first_name\": \"Staff\"," +
            "            \"last_name\": \"Member\"," +
            "            \"person_id\": \"13c71d52-5057-11e4-9d23-12c37bb2d521\"," +
            "            \"total\": 0" +
            "        }" +
            "    ]" +
            "}");
    }
}
