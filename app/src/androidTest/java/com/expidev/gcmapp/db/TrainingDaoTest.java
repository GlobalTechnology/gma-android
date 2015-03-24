package com.expidev.gcmapp.db;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.expidev.gcmapp.model.Training;

import java.util.Calendar;
import java.util.GregorianCalendar;
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
        training.setLastSynced(456789L);

        trainingDao.saveTraining(training);
    }
}
