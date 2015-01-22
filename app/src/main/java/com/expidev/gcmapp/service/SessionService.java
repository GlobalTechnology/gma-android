package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.expidev.gcmapp.db.SessionDao;

/**
 * Created by William.Randall on 1/21/2015.
 */
public class SessionService extends IntentService
{
    private final String TAG = getClass().getSimpleName();

    public SessionService()
    {
        super("SessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        SessionDao sessionDao = SessionDao.getInstance(getBaseContext());
        sessionDao.saveSessionToken(intent.getStringExtra("sessionToken"));

        Log.i(TAG, "Successfully saved session token to database");
    }
}
