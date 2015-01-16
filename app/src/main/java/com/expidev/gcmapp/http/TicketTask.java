package com.expidev.gcmapp.http;

import android.os.AsyncTask;
import android.util.Log;

import me.thekey.android.TheKey;

/**
 * Created by matthewfrederick on 1/15/15.
 */
public class TicketTask extends AsyncTask<Object, Void, String>
{
    private final String TAG = this.getClass().getSimpleName();
    
    private TicketTaskHandler taskHandler;
    String ticket;
    
    public static interface TicketTaskHandler
    {
        void taskComplete(String ticket);
        void taskFailed();
    }
    
    public TicketTask(TicketTaskHandler listener)
    {
        taskHandler = listener;        
    }
    
    @Override
    protected String doInBackground(Object... params)
    {
        String urlString = params[0].toString();
        TheKey theKey = (TheKey) params[1];
        
        Log.i(TAG, "url: " + urlString);
        
        try
        {
            ticket = theKey.getTicket(urlString);
            Log.i(TAG, ticket);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        
        
        return null;
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);
        
        if (ticket != null) taskHandler.taskComplete(ticket);
        else taskHandler.taskFailed();
    }
}
