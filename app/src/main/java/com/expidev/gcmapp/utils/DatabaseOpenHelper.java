package com.expidev.gcmapp.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.expidev.gcmapp.sql.TableNames;

/**
 * Created by William.Randall on 1/15/2015.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper
{
    private final String TAG = getClass().getSimpleName();

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "gcm_data";

    public DatabaseOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating database...");
        createAssociatedMinistryTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        deleteAllTables(db);
        onCreate(db);
    }

    /**
     * This table holds information for ministries the current user
     * has already joined or requested to join.
     */
    private void createAssociatedMinistryTable(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TableNames.ASSOCIATED_MINISTRIES.getTableName() + "(" +
            "ministry_id TEXT, " +
            "name TEXT, " +
            "team_role TEXT, " +                // Team Role of the current user for this ministry/team
            "last_synced TEXT);");              // Last time this information was synced with the web
    }

    private void deleteAllTables(SQLiteDatabase db)
    {
        db.delete(TableNames.ASSOCIATED_MINISTRIES.getTableName(), null, null);
    }
}
