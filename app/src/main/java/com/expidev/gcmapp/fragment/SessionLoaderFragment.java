package com.expidev.gcmapp.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.expidev.gcmapp.loader.SessionCursorLoader;

/**
 * Created by William.Randall on 1/21/2015.
 */
public class SessionLoaderFragment extends Fragment
{
    private static final int SESSION_LOADER = 1;

    private OnSessionTokenReadyListener callbackListener;
    private String sessionToken;

    public interface OnSessionTokenReadyListener
    {
        public void onSessionTokenReturned(String sessionToken);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            callbackListener = (OnSessionTokenReadyListener) activity;
        }
        catch(ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement SessionLoaderListener");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        getLoaderManager().initLoader(SESSION_LOADER, null, new CursorLoaderCallbacks());
    }

    private class CursorLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>
    {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args)
        {
            switch(id)
            {
                case SESSION_LOADER:
                    return new SessionCursorLoader(getActivity());
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
            if(data.getCount() != 1)
            {
                throw new IllegalStateException("There should only be one session token!");
            }
            else
            {
                data.moveToFirst();
                sessionToken = data.getString(0);
                Log.i(getClass().getSimpleName(), "Session Token: " + sessionToken);
                callbackListener.onSessionTokenReturned(sessionToken);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {

        }
    }

    public String getSessionToken()
    {
        return sessionToken;
    }
}
