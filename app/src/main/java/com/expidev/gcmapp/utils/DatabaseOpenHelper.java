package com.expidev.gcmapp.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.expidev.gcmapp.db.Contract;

/**
 * Created by William.Randall on 1/15/2015.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper
{
    private final String TAG = getClass().getSimpleName();

    /*
     * Version history
     *
     * 8: 2015-02-19
     * 9: 2015-02-23
     * 10: 2015-02-24
     * 11: 2015-02-23
     * 13: 2015-02-26
     * v0.8.0
     * 14: 2015-03-01
     * 15: 2015-03-01
     * 16: 2015-03-01
     * v0.8.1
     * 17: 2015-03-05
     */
    private static final int DATABASE_VERSION = 17;
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
        createMeasurementsTables(db);
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (oldVersion < 7) {
            // version is too old, reset database
            resetDatabase(db);
            return;
        }

        // perform upgrade in increments
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 8:
                    db.execSQL(Contract.Church.SQL_V8_CREATE_TABLE);
                    break;
                case 9:
                    db.execSQL(Contract.Church.SQL_V9_ALTER_DIRTY);
                    break;
                case 10:
                    db.execSQL(Contract.MeasurementDetails.SQL_V10_ALTER_LOCAL);
                    db.execSQL(Contract.MeasurementDetails.SQL_V10_ALTER_PERSONAL);
                    break;
                case 11:
                    db.execSQL(Contract.Assignment.SQL_V11_MCC);
                    break;
                case 12:
                    db.execSQL(Contract.Assignment.SQL_DELETE_TABLE);
                    db.execSQL(Contract.Assignment.SQL_CREATE_TABLE);
                    break;
                case 13:
                    db.execSQL(Contract.Measurement.SQL_V13_ALTER_SORT);
                    break;
                case 14:
                    db.execSQL(Contract.LegacyTables.SQL_DELETE_ALL_MINISTRIES_TABLE);
                    break;
                case 15:
                    db.execSQL(Contract.Ministry.SQL_V15_RENAME_TABLE);
                    break;
                case 16:
                    db.execSQL(Contract.Ministry.SQL_V16_MCCS);
                    // XXX: we should have converted legacy data, but because we have no real users yet I'm skipping it -DF
                    break;
                case 17:
                    db.execSQL(Contract.MeasurementType.SQL_CREATE_TABLE);
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
