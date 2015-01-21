package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.UserDao;
import com.expidev.gcmapp.utils.GcmProperties;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import me.thekey.android.TheKey;
import me.thekey.android.TheKeySocketException;
import me.thekey.android.lib.TheKeyImpl;

import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.ticketReceivedBroadcast;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public class AuthService extends IntentService
{
    private final String TAG = this.getClass().getSimpleName();
    private final String PREF_NAME = "gcm_prefs";

    private LocalBroadcastManager broadcastManager;
    private TheKey theKey;
    private GcmProperties gcmProperties;
    private String ticket;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;

    private static final String EXTRA_TYPE = AuthService.class.getName() + ".EXTRA_TYPE";
    private static final String EXTRA_URL = AuthService.class.getName() + ".EXTRA_URL";

    private static final int TYPE_TICKET = 0;
    private static final int TYPE_TOKEN = 1;

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

        // set shared preferences that can be accessed throughout the application
        sharedPreferences = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefEditor = sharedPreferences.edit();

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
                case TYPE_TOKEN:
                    getToken(intent);
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
        final Bundle extras = new Bundle(2);
        extras.putInt(EXTRA_TYPE, TYPE_TICKET);
        extras.putString(EXTRA_URL, url);
        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }
    
    public static void getToken(final Context context, String url)
    {
        final Bundle extras = new Bundle(2);
        extras.putInt(EXTRA_TYPE, TYPE_TOKEN);
        extras.putString(EXTRA_URL, url);
        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }
    
    public void getTicket(Intent intent) throws TheKeySocketException
    {
        String url = intent.getStringExtra(EXTRA_URL);
        ticket = theKey.getTicket(url);

        prefEditor.putString("ticket", ticket);
        prefEditor.commit();
        
        Log.i(TAG, ticket);
        this.broadcastManager.sendBroadcast(ticketReceivedBroadcast(ticket));
    }
    
    public void getToken(Intent intent)
    {
        ticket = sharedPreferences.getString("ticket", null);
        
        String url = intent.getStringExtra(EXTRA_URL);
        url = url + "?st=" + ticket + "&refresh=true";
        Log.i(TAG, url);
        
        try
        {
            URL fullUrl = new URL(url);

            HttpsURLConnection urlConnection = (HttpsURLConnection) fullUrl.openConnection();

            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);

            urlConnection.connect();
            
            if (urlConnection.getResponseCode() == HttpStatus.SC_OK)
            {
                InputStream inputStream = urlConnection.getInputStream();
                
                if (inputStream != null)
                {
                    String jsonAsString = readFully(inputStream, "UTF-8");
                    Log.i(TAG, jsonAsString);

                    JSONObject jsonObject = new JSONObject(jsonAsString);

                    UserDao userDao = UserDao.getInstance(this);
                    userDao.saveUser(jsonObject.getJSONObject("user"));
                }
            }
            
            broadcastManager.sendBroadcast(stopBroadcast());
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private String readFully(InputStream inputStream, String encoding) throws IOException
    {
        return new String(readFully(inputStream), encoding);
    }

    private byte[] readFully(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1)
        {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
