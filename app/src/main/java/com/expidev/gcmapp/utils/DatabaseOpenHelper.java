package com.expidev.gcmapp.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.expidev.gcmapp.db.Contract;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
    private final String TAG = getClass().getSimpleName();

    /*
     * Version history
     *
     * v0.8.1
     * 17: 2015-03-05
     * 18: 2015-03-06
     * 19: 2015-03-06
     * 20: 2015-03-06
     * 21: 2015-03-06
     * 22: 2015-03-10
     * v0.8.2 - v0.8.3
     * 23: 2015-03-24
     */
    private static final int DATABASE_VERSION = 23;
    private static final String DATABASE_NAME = "gcm_data.db";

    private static final Object LOCK_INSTANCE = new Object();
    private static DatabaseOpenHelper instance;

    private DatabaseOpenHelper(final Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static DatabaseOpenHelper getInstance(Context context)
    {
        synchronized (LOCK_INSTANCE) {
            if (instance == null) {
                instance = new DatabaseOpenHelper(context.getApplicationContext());
            }
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i(TAG, "Creating database...");
        createAssociatedMinistryTable(db);
        createAssignmentsTable(db);
        createTrainingTables(db);
        db.execSQL(Contract.Church.SQL_CREATE_TABLE);
        db.execSQL(Contract.MeasurementType.SQL_CREATE_TABLE);
        db.execSQL(Contract.MinistryMeasurement.SQL_CREATE_TABLE);
        db.execSQL(Contract.PersonalMeasurement.SQL_CREATE_TABLE);
        createMeasurementsTables(db);
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // perform upgrade in increments
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 17:
                    db.execSQL(Contract.MeasurementType.SQL_CREATE_TABLE);
                    break;
                case 18:
                    db.execSQL(Contract.MinistryMeasurement.SQL_CREATE_TABLE);
                    break;
                case 19:
                    db.execSQL(Contract.PersonalMeasurement.SQL_CREATE_TABLE);
                    break;
                case 20:
                    // XXX: let's just recreate the table instead of altering the existing table
                    db.execSQL(Contract.MeasurementType.SQL_DELETE_TABLE);
                    db.execSQL(Contract.MeasurementType.SQL_CREATE_TABLE);
                    break;
                case 21:
                    // XXX: let's just recreate the tables instead of altering the existing tables
                    db.execSQL(Contract.MinistryMeasurement.SQL_DELETE_TABLE);
                    db.execSQL(Contract.PersonalMeasurement.SQL_DELETE_TABLE);
                    db.execSQL(Contract.PersonalMeasurement.SQL_CREATE_TABLE);
                    db.execSQL(Contract.MinistryMeasurement.SQL_CREATE_TABLE);
                    break;
                case 22:
                    db.execSQL(Contract.Church.SQL_v22_ALTER_NEW);
                    break;
                case 23:
                    db.execSQL(Contract.MeasurementType.SQL_V23_PERMLINKSTUB);
                    db.execSQL(Contract.PersonalMeasurement.SQL_V23_PERMLINKSTUB);
                    db.execSQL(Contract.MinistryMeasurement.SQL_V23_PERMLINKSTUB);
                    break;
                default:
                    // unrecognized version, let's just reset the database and return
                    resetDatabase(db);
                    return;
            }

            // perform next upgrade increment
            upgradeTo++;
        }
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // reset the database, don't try and downgrade tables
        this.resetDatabase(db);
    }

    private void resetDatabase(final SQLiteDatabase db) {
        db.execSQL(Contract.Church.SQL_DELETE_TABLE);
        db.execSQL(Contract.MeasurementType.SQL_DELETE_TABLE);
        db.execSQL(Contract.MinistryMeasurement.SQL_DELETE_TABLE);
        db.execSQL(Contract.PersonalMeasurement.SQL_DELETE_TABLE);
        deleteAllTables(db);
        onCreate(db);
    }

    /**
     * This table holds information for ministries in GMA.
     */
    private void createAssociatedMinistryTable(SQLiteDatabase db)
    {
        db.execSQL(Contract.Ministry.SQL_CREATE_TABLE);
    }

    /**
     * This table holds information for assignments the current user
     * has to existing ministries/teams. This is closely related to the
     * Associated Ministries table, where every assignment will have an
     * associated ministry.
     */
    private void createAssignmentsTable(SQLiteDatabase db)
    {
        db.execSQL(Contract.Assignment.SQL_CREATE_TABLE);
    }

    private void createTrainingTables(SQLiteDatabase db)
    {
        db.execSQL(Contract.Training.SQL_CREATE_TABLE);
        db.execSQL(Contract.Training.Completion.SQL_CREATE_TABLE);
    }

    /**
     * These tables hold measurement and measurement details data
     */
    private void createMeasurementsTables(SQLiteDatabase db)
    {
        db.execSQL(Contract.Measurement.SQL_CREATE_TABLE);
        db.execSQL(Contract.Measurement.SQL_CREATE_INDEX);
        db.execSQL(Contract.MeasurementDetails.SQL_CREATE_TABLE);
        db.execSQL(Contract.MeasurementTypeIds.SQL_CREATE_TABLE);
        db.execSQL(Contract.SixMonthAmounts.SQL_CREATE_TABLE);
        db.execSQL(Contract.BreakdownData.SQL_CREATE_TABLE);
        db.execSQL(Contract.TeamMemberDetails.SQL_CREATE_TABLE);
        db.execSQL(Contract.SubMinistryDetails.SQL_CREATE_TABLE);
    }

    private void deleteAllTables(SQLiteDatabase db)
    {
        db.execSQL(Contract.Training.Completion.SQL_DELETE_TABLE);
        db.execSQL(Contract.Training.SQL_DELETE_TABLE);
        db.execSQL(Contract.Ministry.SQL_DELETE_TABLE);
        db.execSQL(Contract.Assignment.SQL_DELETE_TABLE);
        db.execSQL(Contract.Measurement.SQL_DELETE_TABLE);
        db.execSQL(Contract.MeasurementDetails.SQL_DELETE_TABLE);
        db.execSQL(Contract.MeasurementTypeIds.SQL_DELETE_TABLE);
        db.execSQL(Contract.SixMonthAmounts.SQL_DELETE_TABLE);
        db.execSQL(Contract.BreakdownData.SQL_DELETE_TABLE);
        db.execSQL(Contract.TeamMemberDetails.SQL_DELETE_TABLE);
        db.execSQL(Contract.SubMinistryDetails.SQL_DELETE_TABLE);

        // delete any orphaned legacy tables
        db.execSQL(Contract.LegacyTables.SQL_DELETE_ALL_MINISTRIES_TABLE);
        db.execSQL(Contract.LegacyTables.SQL_DELETE_ASSOCIATED_MINISTRIES_TABLE);
    }
}
