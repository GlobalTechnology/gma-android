package com.expidev.gcmapp.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by matthewfrederick on 1/12/15.
 */
public class GcmProperties
{
    private String TAG = this.getClass().getSimpleName();
    
    private Context context;
    private Properties properties;
    
    public GcmProperties(Context context)
    {
        this.context = context;
        properties = new Properties();
    }
    
    public Properties getProperties(String fileName)
    {
        try
        {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            properties.load(inputStream);
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        
        return properties;
    }
}
