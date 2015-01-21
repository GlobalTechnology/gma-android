package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.utils.GcmProperties;

import java.util.Properties;

import me.thekey.android.TheKey;
import me.thekey.android.TheKeySocketException;
import me.thekey.android.lib.TheKeyImpl;

import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.ticketReceivedBroadcast;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public class AuthService extends IntentService
{
    private final String TAG = this.getClass().getSimpleName();

    private LocalBroadcastManager broadcastManager;
    private TheKey theKey;
    private GcmProperties gcmProperties;

    private static final String EXTRA_TYPE = AuthService.class.getName() + ".EXTRA_TYPE";
    private static final String EXTRA_URL = AuthService.class.getName() + ".EXTRA_URL";

    private static final int TYPE_TICKET = 0;

    private static boolean running = false;
    
    public AuthService()
    {
        super("AuthService");        
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i(TAG, "on Create");
        this.broadcastManager = LocalBroadcastManager.getInstance(this);

        GcmProperties gcmProperties = new GcmProperties(this);
        Properties properties = gcmProperties.getProperties("gcm_properties.properties");
        long keyClientId = Long.parseLong(properties.getProperty("TheKeyClientId", ""));

        theKey = TheKeyImpl.getInstance(getApplicationContext(), keyClientId);

        running = true;
        this.broadcastManager.sendBroadcast(startBroadcast());
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.i(TAG, "Handle Intent");
        broadcastManager.sendBroadcast(runningBroadcast());
        
        final int type = intent.getIntExtra(EXTRA_TYPE, -1);
        Log.i(TAG, "Type: " + type);
        try
        {
            switch (type)
            {
                case TYPE_TICKET:
                    getTicket(intent);
                    break;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static Intent baseIntent(final Context context, final Bundle extras)
    {
        final Intent intent = new Intent(context, AuthService.class);
        if (extras != null)
        {
            intent.putExtras(extras);
        }
        return intent;
    }

    public static void getTicket(final Context context, String url)
    {
        final Bundle extras = new Bundle(1);
        extras.putInt(EXTRA_TYPE, TYPE_TICKET);
        extras.putString(EXTRA_URL, url);
        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }
    
    public void getTicket(Intent intent) throws TheKeySocketException
    {
        String url = intent.getStringExtra(EXTRA_URL);
        String ticket = theKey.getTicket(url);
        Log.i(TAG, ticket);
        this.broadcastManager.sendBroadcast(ticketReceivedBroadcast(ticket));
    }
}
