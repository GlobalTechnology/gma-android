package com.expidev.gcmapp.GcmTheKey;

import android.util.Log;

import com.expidev.gcmapp.User;
import com.expidev.gcmapp.http.GcmApiClient;
import com.expidev.gcmapp.http.TokenTask;

import org.json.JSONObject;

import me.thekey.android.lib.content.TheKeyBroadcastReceiver;

/**
 * Created by matthewfrederick on 1/13/15.
 */
public class GcmBroadcastReceiver extends TheKeyBroadcastReceiver
{
    private final String TAG = this.getClass().getSimpleName();
    
    public GcmBroadcastReceiver()
    {
        super();
    }

    @Override
    protected void onLogin(String guid)
    {
        super.onLogin(guid);
        Log.i(TAG, "On Login");

        GcmApiClient.getToken(guid, new TokenTask.TokenTaskHandler()
        {
            @Override
            public void taskComplete(JSONObject object)
            {
                Log.i(TAG, "Task Complete");
                User user = GcmTheKeyHelper.createUser(object);
            }

            @Override
            public void taskFailed()
            {
                Log.i(TAG, "Task Failed");
            }
        });
    }

    @Override
    protected void onLogout(String guid, boolean changingUser)
    {
        super.onLogout(guid, changingUser);
        Log.i(TAG, "On Logout");
    }

    @Override
    protected void onAttributesLoaded(String guid)
    {
        super.onAttributesLoaded(guid);
        Log.i(TAG, "On Attributes Loaded");
    }
}
