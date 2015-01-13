package com.expidev.gcmapp.GcmTheKey;

import android.util.Log;

import com.expidev.gcmapp.User;

import org.json.JSONObject;

/**
 * Created by matthewfrederick on 1/13/15.
 */
public final class GcmTheKeyHelper
{
    private static final String TAG = "GcmTheKeyHelper";
    
    public static User createUser(JSONObject object)
    {
        User user = new User();
        
        try
        {
            user.setFirstName(object.getString("first_name"));
            user.setLastName(object.getString("last_name"));
            user.setCasUsername(object.getString("cas_username"));
            user.setPersonId(object.getString("person_id"));
        } catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }  
        
        return user;
    }
}
