package com.expidev.gcmapp.db;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.expidev.gcmapp.model.Training;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by matthewfrederick on 1/27/15.
 */
public class TrainingDaoTest extends InstrumentationTestCase
{
    private final String TAG = getClass().getSimpleName();
    
    private TrainingDao trainingDao;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        Context context = new RenamingDelegatingContext(getInstrumentation().getTargetContext().getApplicationContext(), "test_");
        trainingDao = TrainingDao.getInstance(context);
    }

    private void cleanupDatabase()
    {
        Log.i(TAG, "Cleaning up database");
        trainingDao.deleteAllData();
    }
    
    public void testSaveTrainingFromAPI() throws JSONException
    {
        cleanupDatabase();
        trainingDao.saveTrainingFromAPI(createTestData());

        Training training = trainingDao.retrieveTrainingById(13);
        assertNotNull(training);
        
        assertEquals(training.getId(), 13);
        assertEquals(training.getMcc(), "slm");
        assertEquals(training.getName(), "Test Training Map12");
        assertEquals(training.getMinistryId(), "770ffd2c-d6ac-11e3-9e38-12725f8f377c");
        assertEquals(training.getLongitude(), -90.3214498270645);
        assertEquals(training.getLatitude(), 15.533247294294055);
        assertEquals(training.getType(), "MC2");
        
        Calendar calendar = new GregorianCalendar(2014, Calendar.NOVEMBER, 13);
        assertEquals(training.getDate(), calendar.getTime());

        List<Training> allTraining = trainingDao.getAllMinistryTraining("770ffd2c-d6ac-11e3-9e38-12725f8f377c");
        assertNotNull(allTraining);
        assertEquals(allTraining.size(), 3);
    }
    
    public void testSaveTraining()
    {       
        cleanupDatabase();
        
        Training training = new Training();
        training.setId(1);
        training.setMinistryId(UUID.randomUUID().toString());
        training.setLongitude(1.12345);
        training.setLatitude(3.14159);
        training.setMcc("slm");
        training.setDate(new GregorianCalendar(2014, Calendar.NOVEMBER, 13).getTime());
        training.setName("Test training");
        training.setSynced(new Timestamp(456789L));
        
        trainingDao.saveTraining(training);
        
        Training returnedTraining = trainingDao.retrieveTrainingById(1);
        assertNotNull(returnedTraining);
        
        assertEquals(returnedTraining.getId(), training.getId());
        assertEquals(returnedTraining.getMinistryId(), training.getMinistryId());
        assertEquals(returnedTraining.getLongitude(), training.getLongitude());
        assertEquals(returnedTraining.getLatitude(), training.getLatitude());
        assertEquals(returnedTraining.getMcc(), training.getMcc());
        assertEquals(returnedTraining.getDate(), training.getDate());
        assertEquals(returnedTraining.getName(), training.getName());
        assertEquals(returnedTraining.getSynced(), training.getSynced());
    }
    
    private JSONArray createTestData() throws JSONException
    {
        return new JSONArray("[{\"id\":13,\"mcc\":\"slm\",\"gcm_training_completions\":" +
                "[{\"id\":12,\"number_completed\":45,\"training_id\":13,\"date\":\"2014-11-13\",\"phase\":1}" +
                ",{\"id\":14,\"number_completed\":27,\"training_id\":13,\"date\":\"2014-12-12\",\"phase\":2}]," +
                "\"name\":\"Test Training Map12\",\"ministry_id\":\"770ffd2c-d6ac-11e3-9e38-12725f8f377c\",\"longitude\"" +
                ":-90.3214498270645,\"latitude\":15.533247294294055,\"type\":\"MC2\",\"date\":\"2014-11-13\"}," +
                "{\"id\":14,\"mcc\":\"slm\",\"gcm_training_completions\":[{\"id\":13,\"number_completed\":32," +
                "\"training_id\":14,\"date\":\"2014-11-05\",\"phase\":1}],\"name\":\"Test T4T\"," +
                "\"ministry_id\":\"770ffd2c-d6ac-11e3-9e38-12725f8f377c\",\"longitude\":-90.414833616127," +
                "\"latitude\":15.199550142700533,\"type\":\"T4T\",\"date\":\"2014-11-05\"},{\"id\":15," +
                "\"mcc\":\"slm\",\"gcm_training_completions\":[{\"id\":15,\"number_completed\":15," +
                "\"training_id\":15,\"date\":\"2014-12-11\",\"phase\":1}],\"name\":\"Keith's\"," +
                "\"ministry_id\":\"770ffd2c-d6ac-11e3-9e38-12725f8f377c\",\"longitude\":-90.162148069252," +
                "\"latitude\":15.207501500625604,\"type\":\"MC2\",\"date\":\"2014-12-11\"}]");
    }
}
