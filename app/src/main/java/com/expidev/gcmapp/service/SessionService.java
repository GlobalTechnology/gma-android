package com.expidev.gcmapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.expidev.gcmapp.db.SessionDao;

/**
 * Created by William.Randall on 1/21/2015.
 */
public class SessionService extends IntentService
{
    public SessionService()
    {
        super("SessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        SessionDao sessionDao = SessionDao.getInstance(getBaseContext());
        sessionDao.saveSessionToken(intent.getStringExtra("sessionToken"));

        Log.i(getClass().getSimpleName(), "Successfully saved session token to database");
    }
}
