package com.expidev.gcmapp.GcmTheKey;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.expidev.gcmapp.http.GcmApiClient;
import com.expidev.gcmapp.service.SessionService;

import org.json.JSONException;
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
    private Context context;
    
    public GcmBroadcastReceiver(TheKey theKey, Context context)
    {
        super();
        this.theKey = theKey;
        this.context = context;
    }

    @Override
    protected void onLogin(String guid)
    {
        Log.i(TAG, "On Login");

        GcmApiClient.getTicket(context);
    }

    private void writeSessionTokenToDatabase(String sessionToken)
    {
        Intent saveSessionToken = new Intent(context, SessionService.class);
        saveSessionToken.putExtra("sessionToken", sessionToken);
        context.startService(saveSessionToken);
    }

    private String getTokenFromJson(JSONObject json)
    {
        try
        {
            return json.getString("session_ticket");
        }
        catch(JSONException e)
        {
            Log.e(TAG, "Failed to get session token from json: " + e.getMessage());
            return null;
        }
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
