package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.UserDao;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;

import static com.expidev.gcmapp.BuildConfig.THEKEY_CLIENTID;
import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public class AuthService extends IntentService
{
    private final String TAG = this.getClass().getSimpleName();
    private final String PREF_NAME = "gcm_prefs";

    private LocalBroadcastManager broadcastManager;
    private TheKey theKey;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;

    private static final String EXTRA_TYPE = AuthService.class.getName() + ".EXTRA_TYPE";

    private static final int TYPE_AUTHORIZE = 0;
    
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

        theKey = TheKeyImpl.getInstance(getApplicationContext(), THEKEY_CLIENTID);

        // set shared preferences that can be accessed throughout the application
        sharedPreferences = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefEditor = sharedPreferences.edit();

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
                case TYPE_AUTHORIZE:
                    authorizeUser();
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
    
    private void authorizeUser() throws JSONException
    {
        GmaApiClient gmaApi = new GmaApiClient(this);
        JSONObject jsonObject = gmaApi.authorizeUser();
        
        Log.i(TAG, "Session Ticket: " + jsonObject.getString("session_ticket"));
        
        prefEditor.putString("session_ticket", jsonObject.getString("session_ticket"));
        prefEditor.commit();

        UserDao userDao = UserDao.getInstance(this);
        userDao.saveUser(jsonObject.getJSONObject("user"));
        
        broadcastManager.sendBroadcast(stopBroadcast(BroadcastUtils.AUTH));
    }

    public static void authorizeUser(final Context context)
    {
        final Bundle extras = new Bundle(1);
        extras.putInt(EXTRA_TYPE, TYPE_AUTHORIZE);
        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }
}
