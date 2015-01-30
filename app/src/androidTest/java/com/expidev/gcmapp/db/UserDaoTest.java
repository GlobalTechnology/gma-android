package com.expidev.gcmapp.db;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.expidev.gcmapp.model.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matthewfrederick on 1/30/15.
 */
public class UserDaoTest extends InstrumentationTestCase
{
    private final String TAG = getClass().getSimpleName();
    
    private UserDao userDao;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        Context context = new RenamingDelegatingContext(getInstrumentation().getTargetContext().getApplicationContext(), "test_");
        userDao = UserDao.getInstance(context);
    }
    
    private void cleanupDatabase()
    {
        Log.i(TAG, "Cleaning up database");
        userDao.deleteAllData();
    }
    
    public void testRetrieveUser() throws JSONException
    {
        cleanupDatabase();
        
        userDao.saveUser(createTestData());

        User user = userDao.retrieveUser();
        assertNotNull(user);
        
        assertEquals(user.getFirstName(), "Bob");
        assertEquals(user.getLastName(), "Smith");
        assertEquals(user.getCasUsername(), "bob.smith@gmail.com");
        assertEquals(user.getPersonId(), "123456");

    }

    private JSONObject createTestData() throws JSONException
    {
        return new JSONObject("{\"first_name\":\"Bob\", \"last_name\":\"Smith\", " +
                "\"cas_username\":\"bob.smith@gmail.com\", \"person_id\":\"123456\"}");        
    }
}
