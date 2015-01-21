package com.expidev.gcmapp.loader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.expidev.gcmapp.db.SessionDao;

/**
 * Cursor Loader to retrieve the session token
 *
 * Created by William.Randall on 1/21/2015.
 */
public class SessionCursorLoader extends CursorLoader
{
    private SessionDao sessionDao;

    public SessionCursorLoader(Context context)
    {
        super(context);
        sessionDao = SessionDao.getInstance(context);
    }

    @Override
    public final Cursor loadInBackground()
    {
        return sessionDao.retrieveSessionToken();
    }
}
