package com.expidev.gcmapp.json;

import com.expidev.gcmapp.model.Training;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by matthewfrederick on 2/23/15.
 */
public class TrainingJsonParserTest extends TestCase
{
    public void testParseTrainingDetails() throws Exception
    {
        List<Training> trainings = TrainingJsonParser.parseTrainings(testTrainingDetails());
        Assert.assertNotNull(trainings);
        Assert.assertEquals(trainings.size(), 2);
    }
    
    private JSONArray testTrainingDetails() throws JSONException
    {
        return new JSONArray("[" +
            "{" +
                "\"Id\":16," +
                "\"ministry_id\":\"770ffd2c-d6ac-11e3-9e38-12725f8f377c\"," +
                "\"name\":\"Harare Ops\"," +
                "\"date\":\"2015-01-21\"," +
                "\"type\":\"MC2\"," +
                "\"mcc\":\"gcm\"," +
                "\"latitude\":null," +
                "\"longitude\":null," +
                "\"last_updated\":null," +
                "\"gcm_training_completions\":[" +
                "{" +
                    "\"Id\":16," +
                    "\"phase\":1," +
                    "\"number_completed\":11," +
                    "\"date\":\"2015-01-21\"," +
                    "\"training_id\":16," +
                    "\"last_updated\":null" +
                "}]" +
            "}," +
            "{" +
                "\"Id\":28," +
                "\"ministry_id\":\"770ffd2c-d6ac-11e3-9e38-12725f8f377c\"," +
                "\"name\":\"test\"," +
                "\"date\":\"2015-02-27\"," +
                "\"type\":\"T4T\"," +
                "\"mcc\":\"gcm\"," +
                "\"latitude\":17.167196888127869," +
                "\"longitude\":-88.787483762611373," +
                "\"last_updated\":null," +
                "\"gcm_training_completions\":[]" +
            "}]");        
    }
}
