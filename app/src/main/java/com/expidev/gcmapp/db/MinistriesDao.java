package com.expidev.gcmapp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 1/21/2015.
 */
public class MinistriesDao
{
    private final String TAG = getClass().getSimpleName();

    private final SQLiteOpenHelper databaseHelper;

    private static final Object instanceLock = new Object();
    private static MinistriesDao instance;

    private MinistriesDao(final Context context)
    {
        this.databaseHelper = new DatabaseOpenHelper(context);
    }

    public static MinistriesDao getInstance(Context context)
    {
        if(instance == null)
        {
            synchronized(instanceLock)
            {
                instance = new MinistriesDao(context.getApplicationContext());
            }
        }

        return instance;
    }

    public Cursor retrieveAssociatedMinistriesCursor()
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try
        {
            return database.query(TableNames.ASSOCIATED_MINISTRIES.getTableName(), null, null, null, null, null, null);
        }
        catch(Exception e)
        {
            Log.e(TAG, "Failed to retrieve associated ministries: " + e.getMessage());
        }

        return null;
    }

    public List<String> retrieveAssociatedMinistries()
    {
        Cursor cursor = null;

        try
        {
            cursor = retrieveAssociatedMinistriesCursor();

            if(cursor != null && cursor.getCount() > 0)
            {
                List<String> associatedMinistries = new ArrayList<String>(cursor.getCount());

                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    associatedMinistries.add(cursor.getString(cursor.getColumnIndex("name")));
                    cursor.moveToNext();
                }

                return associatedMinistries;
            }
        }
        finally
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }

        return null;
    }
}
