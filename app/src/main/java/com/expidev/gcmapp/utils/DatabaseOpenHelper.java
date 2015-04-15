package com.expidev.gcmapp.utils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.BuildConfig;
import com.expidev.gcmapp.db.Contract;
import com.google.common.base.Throwables;

import org.ccci.gto.android.common.newrelic.CrashReporterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseOpenHelper.class);

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
     * 24: 2015-04-06
     * 25: 2015-04-08
     * 26: 2015-04-09
     */
    private static final String DATABASE_NAME = "gcm_data.db";
    private static final int DATABASE_VERSION = 26;

    private static final Object LOCK_INSTANCE = new Object();
    private static DatabaseOpenHelper INSTANCE;

    private final Context mContext;

    private DatabaseOpenHelper(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @NonNull
    public static DatabaseOpenHelper getInstance(@NonNull final Context context) {
        synchronized (LOCK_INSTANCE) {
            if (INSTANCE == null) {
                INSTANCE = new DatabaseOpenHelper(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        try {
            db.beginTransaction();

            createAssociatedMinistryTable(db);
            createAssignmentsTable(db);
            createTrainingTables(db);
            db.execSQL(Contract.Church.SQL_CREATE_TABLE);
            db.execSQL(Contract.MeasurementType.SQL_CREATE_TABLE);
            db.execSQL(Contract.MinistryMeasurement.SQL_CREATE_TABLE);
            db.execSQL(Contract.PersonalMeasurement.SQL_CREATE_TABLE);
            createMeasurementsTables(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        try {
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
                        db.execSQL(Contract.PersonalMeasurement.SQL_V21_CREATE_TABLE);
                        db.execSQL(Contract.MinistryMeasurement.SQL_V21_CREATE_TABLE);
                        break;
                    case 22:
                        db.execSQL(Contract.Church.SQL_v22_ALTER_NEW);
                        break;
                    case 23:
                        db.execSQL(Contract.MeasurementType.SQL_V23_PERMLINKSTUB);
                        db.execSQL(Contract.PersonalMeasurement.SQL_V23_PERMLINKSTUB);
                        db.execSQL(Contract.MinistryMeasurement.SQL_V23_PERMLINKSTUB);
                        break;
                    case 24:
                        break;
                    case 25:
                        db.execSQL(Contract.PersonalMeasurement.SQL_V25_ALTER_DELTA);
                        db.execSQL(Contract.MinistryMeasurement.SQL_V25_ALTER_DELTA);
                        break;
                    case 26:
                        db.execSQL(Contract.PersonalMeasurement.SQL_V26_UPDATE_DELTA);
                        db.execSQL(Contract.MinistryMeasurement.SQL_V26_UPDATE_DELTA);
                        break;
                    default:
                        // unrecognized version
                        throw new SQLiteException("Unrecognized database version");
                }

                // perform next upgrade increment
                upgradeTo++;
            }
        } catch (final SQLException e) {
            LOG.error("error upgrading database", e);

            // report (or rethrow) exception
            if (BuildConfig.DEBUG) {
                throw Throwables.propagate(e);
            } else {
                CrashReporterUtils.reportException(mContext, e);
            }

            // let's try resetting the database instead
            resetDatabase(db);
        }
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // reset the database, don't try and downgrade tables
        resetDatabase(db);
    }

    private void resetDatabase(final SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(Contract.Church.SQL_DELETE_TABLE);
            db.execSQL(Contract.MeasurementType.SQL_DELETE_TABLE);
            db.execSQL(Contract.MinistryMeasurement.SQL_DELETE_TABLE);
            db.execSQL(Contract.PersonalMeasurement.SQL_DELETE_TABLE);
            deleteAllTables(db);

            onCreate(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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
