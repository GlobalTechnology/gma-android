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

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gcm_data.db";

    public DatabaseOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating database...");
        createAssociatedMinistryTable(db);
        createUserTable(db);
        createAssignmentsTable(db);
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
            "ministry_id TEXT PRIMARY KEY, " +
            "name TEXT, " +
            "min_code TEXT, " +
            "has_slm INTEGER, " +               // This is really a boolean (0 = false, 1 = true)
            "has_llm INTEGER, " +
            "has_ds INTEGER, " +
            "has_gcm INTEGER, " +
            "parent_ministry_id TEXT, " +       // This will be populated if this ministry is a sub ministry
            "last_synced TEXT);");              // Last time this information was synced with the web
    }

    /**
     * This table holds information for assignments the current user
     * has to existing ministries/teams. This is closely related to the
     * Associated Ministries table, where every assignment will have an
     * associated ministry.
     */
    private void createAssignmentsTable(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TableNames.ASSIGNMENTS.getTableName() + "( " +
            "id TEXT PRIMARY KEY, " +
            "team_role TEXT, " +               // Team Role of the current user for this ministry/team
            "ministry_id TEXT, " +
            "last_synced TEXT, " +             // Last time this information was synced with the web
            "FOREIGN KEY(ministry_id) REFERENCES " + TableNames.ASSOCIATED_MINISTRIES.getTableName() + "(ministry_id));");
    }

    /**
     * This table holds the user information.
     */
    private void createUserTable(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TableNames.USER.getTableName() +
            "(first_name TEXT, last_name TEXT, cas_username TEXT, person_id TEXT);");        
    }

    private void deleteAllTables(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TableNames.ASSOCIATED_MINISTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TableNames.ASSIGNMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TableNames.USER);
    }
}
