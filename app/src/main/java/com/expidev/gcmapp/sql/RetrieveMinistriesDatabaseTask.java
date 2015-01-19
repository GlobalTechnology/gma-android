package com.expidev.gcmapp.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.expidev.gcmapp.utils.DatabaseOpenHelper;

/**
 * Created by William.Randall on 1/19/2015.
 */
public class RetrieveMinistriesDatabaseTask extends AsyncTask<Object, Void, String>
{
    private final String TAG = this.getClass().getSimpleName();

    private RetrieveMinistriesDatabaseTaskHandler taskHandler;
    private Cursor databaseResults;
    private String reason;

    public static interface RetrieveMinistriesDatabaseTaskHandler
    {
        void taskComplete(Cursor databaseResults);
        void taskFailed(String reason);
    }

    public RetrieveMinistriesDatabaseTask(RetrieveMinistriesDatabaseTaskHandler taskHandler)
    {
        this.taskHandler = taskHandler;
    }

    @Override
    protected String doInBackground(Object... params)
    {
        Context context = (Context)params[0];
        String tableName = (String)params[1];

        try
        {
            DatabaseOpenHelper helper = new DatabaseOpenHelper(context);
            SQLiteDatabase database = helper.getReadableDatabase();
            databaseResults = database.query(tableName, null, null, null, null, null, null);
        }
        catch(Exception e)
        {
            reason = e.getMessage();
            Log.e(TAG, reason);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);
        if(databaseResults != null && databaseResults.getCount() > 0)
        {
            taskHandler.taskComplete(databaseResults);
        }
        else if(reason != null && reason.length() > 0)
        {
            taskHandler.taskFailed(reason);
        }
        else
        {
            Log.i(TAG, "No results");
        }
    }
}
