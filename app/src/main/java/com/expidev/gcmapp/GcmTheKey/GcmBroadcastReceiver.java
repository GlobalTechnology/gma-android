package com.expidev.gcmapp.GcmTheKey;

import android.content.Context;
import android.util.Log;

import com.expidev.gcmapp.service.AuthService;
import com.expidev.gcmapp.service.MinistriesService;

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

        AuthService.authorizeUser(context);
        MinistriesService.syncAllMinistries(context);
        MinistriesService.syncAssignments(context);
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
