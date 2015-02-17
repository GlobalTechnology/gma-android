package com.expidev.gcmapp.service;

import static com.expidev.gcmapp.service.Type.AUTH;
import static com.expidev.gcmapp.utils.BroadcastUtils.runningBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.startBroadcast;
import static com.expidev.gcmapp.utils.BroadcastUtils.stopBroadcast;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.expidev.gcmapp.db.UserDao;
import com.expidev.gcmapp.http.GmaApiClient;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public class AuthService extends IntentService
{
    private final String TAG = this.getClass().getSimpleName();

    @NonNull
    private GmaApiClient mApi;
    private LocalBroadcastManager broadcastManager;

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
        mApi = GmaApiClient.getInstance(this);

        Log.i(TAG, "on Create");
        this.broadcastManager = LocalBroadcastManager.getInstance(this);

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
        JSONObject jsonObject = mApi.authorizeUser();
        if (jsonObject != null) {
            UserDao userDao = UserDao.getInstance(this);
            userDao.saveUser(jsonObject.getJSONObject("user"));

            MinistriesService.saveAssociatedMinistriesFromServer(
                    getApplicationContext(), jsonObject.optJSONArray("assignments"));
        }

        broadcastManager.sendBroadcast(stopBroadcast(AUTH));
    }

    public static void authorizeUser(final Context context)
    {
        final Bundle extras = new Bundle(1);
        extras.putInt(EXTRA_TYPE, TYPE_AUTHORIZE);
        final Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }
}
