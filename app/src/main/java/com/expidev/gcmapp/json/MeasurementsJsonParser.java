package com.expidev.gcmapp.json;

import android.util.Log;

import com.expidev.gcmapp.model.Measurement;

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
}
