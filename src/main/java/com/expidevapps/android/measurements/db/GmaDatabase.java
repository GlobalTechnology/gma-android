package com.expidevapps.android.measurements.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.expidevapps.android.measurements.BuildConfig;
import com.google.common.base.Throwables;

import org.ccci.gto.android.common.db.WalSQLiteOpenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GmaDatabase extends WalSQLiteOpenHelper {
    private static final Logger LOG = LoggerFactory.getLogger(GmaDatabase.class);

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
     * v0.8.4
     * 27: 2015-04-15
     * 28: 2015-04-16
     * 29: 2015-04-21
     * v0.8.5
     * 30: 2015-04-27
     * v0.8.6
     * v0.8.7
     * 31: 2015-05-14
     * 32: 2015-05-15
     */
    private static final String DATABASE_NAME = "gcm_data.db";
    private static final int DATABASE_VERSION = 32;

    private static final Object LOCK_INSTANCE = new Object();
    private static GmaDatabase INSTANCE;

    private GmaDatabase(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @NonNull
    public static GmaDatabase getInstance(@NonNull final Context context) {
        synchronized (LOCK_INSTANCE) {
            if (INSTANCE == null) {
                INSTANCE = new GmaDatabase(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(Contract.Ministry.SQL_CREATE_TABLE);
            db.execSQL(Contract.Assignment.SQL_CREATE_TABLE);
            createTrainingTables(db);
            db.execSQL(Contract.Church.SQL_CREATE_TABLE);
            db.execSQL(Contract.MeasurementType.SQL_CREATE_TABLE);
            db.execSQL(Contract.MinistryMeasurement.SQL_CREATE_TABLE);
            db.execSQL(Contract.PersonalMeasurement.SQL_CREATE_TABLE);
            db.execSQL(Contract.MeasurementDetails.SQL_CREATE_TABLE);
            db.execSQL(Contract.LastSync.SQL_CREATE_TABLE);

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
                        db.execSQL(Contract.MeasurementType.SQL_V20_CREATE_TABLE);
                        break;
                    case 18:
                        db.execSQL(Contract.MinistryMeasurement.SQL_V21_CREATE_TABLE);
                        break;
                    case 19:
                        db.execSQL(Contract.PersonalMeasurement.SQL_V21_CREATE_TABLE);
                        break;
                    case 20:
                        // XXX: let's just recreate the table instead of altering the existing table
                        db.execSQL(Contract.MeasurementType.SQL_DELETE_TABLE);
                        db.execSQL(Contract.MeasurementType.SQL_V20_CREATE_TABLE);
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
                    case 27:
                        db.execSQL(Contract.MeasurementType.SQL_V27_UPDATE_PERMLINKSTUB);
                        db.execSQL(Contract.PersonalMeasurement.SQL_V27_UPDATE_PERMLINKSTUB);
                        db.execSQL(Contract.MinistryMeasurement.SQL_V27_UPDATE_PERMLINKSTUB);
                        break;
                    case 28:
                        db.execSQL(Contract.MeasurementDetails.SQL_CREATE_TABLE);
                        break;
                    case 29:
                        db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_TABLE);
                        db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_DETAILS_TABLE);
                        db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_BREAKDOWN_TABLE);
                        db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_SIX_MONTHS_TABLE);
                        db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_SUB_MINISTRIES_TABLE);
                        db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_TEAM_MEMBERS_TABLE);
                        db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_TYPE_IDS_TABLE);
                        break;
                    case 30:
                        db.execSQL(Contract.LastSync.SQL_CREATE_TABLE);
                        break;
                    case 31:
                        db.execSQL(Contract.MeasurementVisibility.SQL_CREATE_TABLE);
                        break;
                    case 32:
                        db.execSQL(Contract.MeasurementType.SQL_V32_ALTER_CUSTOM);
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
                Crashlytics.logException(e);
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

            db.execSQL(Contract.LastSync.SQL_DELETE_TABLE);
            db.execSQL(Contract.Church.SQL_DELETE_TABLE);
            db.execSQL(Contract.MeasurementType.SQL_DELETE_TABLE);
            db.execSQL(Contract.MeasurementVisibility.SQL_DELETE_TABLE);
            db.execSQL(Contract.MinistryMeasurement.SQL_DELETE_TABLE);
            db.execSQL(Contract.PersonalMeasurement.SQL_DELETE_TABLE);
            db.execSQL(Contract.MeasurementDetails.SQL_DELETE_TABLE);
            deleteAllTables(db);

            // delete any orphaned legacy tables
            db.execSQL(Contract.LegacyTables.SQL_DELETE_ALL_MINISTRIES_TABLE);
            db.execSQL(Contract.LegacyTables.SQL_DELETE_ASSOCIATED_MINISTRIES_TABLE);
            db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_TABLE);
            db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_DETAILS_TABLE);
            db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_BREAKDOWN_TABLE);
            db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_SIX_MONTHS_TABLE);
            db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_SUB_MINISTRIES_TABLE);
            db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_TEAM_MEMBERS_TABLE);
            db.execSQL(Contract.LegacyTables.SQL_DELETE_MEASUREMENTS_TYPE_IDS_TABLE);

            onCreate(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void createTrainingTables(SQLiteDatabase db)
    {
        db.execSQL(Contract.Training.SQL_CREATE_TABLE);
        db.execSQL(Contract.Training.Completion.SQL_CREATE_TABLE);
    }

    private void deleteAllTables(SQLiteDatabase db)
    {
        db.execSQL(Contract.Training.Completion.SQL_DELETE_TABLE);
        db.execSQL(Contract.Training.SQL_DELETE_TABLE);
        db.execSQL(Contract.Ministry.SQL_DELETE_TABLE);
        db.execSQL(Contract.Assignment.SQL_DELETE_TABLE);
    }
}
