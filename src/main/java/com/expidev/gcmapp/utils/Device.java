package com.expidev.gcmapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by matthewfrederick on 1/9/15.
 */
public class Device
{
    public static boolean isConnected(Context context) 
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] allNetworks = cm.getAllNetworkInfo();

        for (NetworkInfo networkInfo : allNetworks) 
        {
            if (networkInfo.isAvailable() && networkInfo.isConnected()) return true;
        }
        return false;
    }
}
