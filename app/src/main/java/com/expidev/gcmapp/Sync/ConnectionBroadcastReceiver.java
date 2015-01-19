package com.expidev.gcmapp.Sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.expidev.gcmapp.utils.Device;

/**
 * Created by matthewfrederick on 1/16/15.
 */
public class ConnectionBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Device.isConnected(context))
        {
            Log.i("Connectivity", "Internet Connection is available");
        }
    }
}
