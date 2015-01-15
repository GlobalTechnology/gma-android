package com.expidev.gcmapp.ministries;

import android.util.Log;

import com.expidev.gcmapp.model.Ministry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by William.Randall on 1/15/2015.
 */
public class MinistryJsonParser
{
    private static final String TAG = MinistryJsonParser.class.getSimpleName();

    public static List<Ministry> parseMinistriesJson(JSONArray jsonResults)
    {
        List<Ministry> ministryList = new ArrayList<Ministry>();
        try
        {
            for(int i = 0; i < jsonResults.length(); i++)
            {
                JSONObject jsonObject = jsonResults.getJSONObject(i);
                Ministry ministry = new Ministry();
                ministry.setMinistryId(jsonObject.getString("ministry_id"));
                ministry.setName(jsonObject.getString("name"));
                ministryList.add(ministry);
            }
        }
        catch(JSONException e)
        {
            //Do stuff
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
        }

        return ministryList;
    }

    public static Map<String, String> parseMinistriesAsMap(JSONArray jsonResults)
    {
        Map<String, String> ministryMap = new HashMap<String, String>();

        try
        {
            for(int i = 0; i < jsonResults.length(); i++)
            {
                JSONObject jsonObject = jsonResults.getJSONObject(i);
                ministryMap.put(jsonObject.getString("ministry_id"), jsonObject.getString("name"));
            }
        }
        catch(JSONException e)
        {
            //Do stuff
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
        }

        return ministryMap;
    }
}
