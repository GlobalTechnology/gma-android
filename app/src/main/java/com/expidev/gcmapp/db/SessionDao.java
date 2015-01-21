package com.expidev.gcmapp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

/**
 * Created by William.Randall on 1/21/2015.
 */
public class SessionDao
{
    private final String TAG = getClass().getSimpleName();

    private final SQLiteOpenHelper databaseHelper;

    private static final Object instanceLock = new Object();
    private static SessionDao instance;

    private SessionDao(final Context context)
    {
        this.databaseHelper = new DatabaseOpenHelper(context);
    }

    public static SessionDao getInstance(Context context)
    {
        if(instance == null)
        {
            synchronized(instanceLock)
            {
                instance = new SessionDao(context.getApplicationContext());
            }
        }

        return instance;
    }

    public Cursor retrieveSessionToken()
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();

        try
        {
            return database.query(TableNames.SESSION.getTableName(), null, null, null, null, null, null);
        }
        catch(Exception e)
        {
            Log.e(TAG, "Failed to retrieve session token: " + e.getMessage());
        }

        return null;
    }

    public void saveSessionToken(String sessionToken)
    {

    }
}
