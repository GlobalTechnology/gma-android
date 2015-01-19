package com.expidev.gcmapp.GcmTheKey;

import android.util.Log;

import com.expidev.gcmapp.http.GcmApiClient;
import com.expidev.gcmapp.http.TicketTask;
import com.expidev.gcmapp.http.TokenTask;
import com.expidev.gcmapp.model.User;

import org.json.JSONObject;

import me.thekey.android.TheKey;
import me.thekey.android.lib.content.TheKeyBroadcastReceiver;

/**
 * Created by matthewfrederick on 1/13/15.
 */
public class GcmBroadcastReceiver extends TheKeyBroadcastReceiver
{
    private final String TAG = this.getClass().getSimpleName();

    private TheKey theKey;
    
    public GcmBroadcastReceiver(TheKey theKey)
    {
        super();
        this.theKey = theKey;
    }

    @Override
    protected void onLogin(String guid)
    {
        Log.i(TAG, "On Login");
        
        GcmApiClient.getTicket(theKey, new TicketTask.TicketTaskHandler()
        {
            @Override
            public void taskComplete(String ticket)
            {
                GcmApiClient.getToken(ticket, new TokenTask.TokenTaskHandler()
                {
                    @Override
                    public void taskComplete(JSONObject object)
                    {
                        Log.i(TAG, "Task Complete");
                        User user = GcmTheKeyHelper.createUser(object);
                    }

                    @Override
                    public void taskFailed(String status)
                    {
                        Log.i(TAG, "Task Failed. Status: " + status);
                    }
                });
            }

            @Override
            public void taskFailed()
            {

            }
        });


    }

    @Override
    protected void onLogout(String guid, boolean changingUser)
    {
        Log.i(TAG, "On Logout");
        
        // if changing user onLogin will be called
        if (!changingUser)
        {
            
        }
    }

    @Override
    protected void onAttributesLoaded(String guid)
    {
        Log.i(TAG, "On Attributes Loaded");
    }
}
