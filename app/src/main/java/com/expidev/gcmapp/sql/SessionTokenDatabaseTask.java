package com.expidev.gcmapp.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.expidev.gcmapp.utils.DatabaseOpenHelper;

/**
 * Created by William.Randall on 1/20/2015.
 */
public class SessionTokenDatabaseTask extends AsyncTask<Object, Void, String>
{
    private final String TAG = getClass().getSimpleName();

    private SessionTokenDatabaseTaskHandler taskHandler;
    private String reason;

    public static interface SessionTokenDatabaseTaskHandler
    {
        void taskComplete();
        void taskFailed(String reason);
    }

    public SessionTokenDatabaseTask(SessionTokenDatabaseTaskHandler taskHandler)
    {
        this.taskHandler = taskHandler;
    }

    @Override
    protected String doInBackground(Object... params)
    {
        try
        {
            Context context = (Context) params[0];
            String sessionToken = (String) params[1];

            DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(context);
            SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();
            String sessionTable = TableNames.SESSION.getTableName();

            ContentValues sessionTokenToInsert = new ContentValues();
            sessionTokenToInsert.put("session_token", sessionToken);


            database.beginTransaction();

            Cursor cursor = database.query(sessionTable, null, null, null, null, null, null);

            if(cursor.getCount() == 0)
            {
                database.insert(sessionTable, null, sessionTokenToInsert);
            }
            else
            {
                database.update(sessionTable, sessionTokenToInsert, null, null);
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();
            cursor.close();
        }
        catch(Exception e)
        {
            reason = e.getMessage();
            Log.e(TAG, "Failed to save session token: " + reason);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s)
    {
        if(reason == null || reason.length() == 0)
        {
            taskHandler.taskComplete();
        }
        else
        {
            taskHandler.taskFailed(reason);
        }
    }
}
