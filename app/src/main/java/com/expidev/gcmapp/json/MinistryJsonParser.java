package com.expidev.gcmapp.json;

import android.util.Log;

import com.expidev.gcmapp.model.Ministry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
                Ministry ministry = parseMinistry(jsonObject);
                ministryList.add(ministry);
            }
        }
        catch(JSONException e)
        {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage(), e);
        }

        return ministryList;
    }

    public static Ministry parseMinistry(JSONObject jsonObject) throws JSONException
    {
        Ministry ministry = new Ministry();

        ministry.setMinistryId(jsonObject.getString("ministry_id"));
        ministry.setName(jsonObject.getString("name"));

        return ministry;
    }

}
