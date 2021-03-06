package com.expidevapps.android.measurements.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Throwables;

import org.ccci.gto.android.common.app.ApplicationUtils;
import org.ccci.gto.android.common.db.WalSQLiteOpenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric.sdk.android.Fabric;

class GmaDatabase extends WalSQLiteOpenHelper {
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
     * v0.8.8
     * 33: 2015-05-19
     * v0.8.9
     * 34: 2015-07-13
     * v0.8.10
     * 35: 2015-09-03
     * 36: 2015-09-08
     * 37: 2015-09-08
     * v0.8.11
     * 38: 2015-09-09
     * 39: 2015-09-09
     * 40: 2015-09-14
     * 41: 2015-09-14
     * 42: 2015-09-14
     * 43: 2015-09-13
     * 44: 2015-09-15
     * 45: 2015-09-17
     * 46: 2015-09-22
     * 47: 2015-09-24
     * 48: 2015-09-24
     * 49: 2015-09-25
     * 50: 2015-09-28
     * v0.8.12
     * 51: 2015-09-30
     * 52: 2015-10-06
     * 53: 2015-10-14
     * v1.0.0
     * 54: 2015-10-20
     * 55: 2015-10-27
     * 56: 2015-11-02
     */
    private static final String DATABASE_NAME = "gcm_data.db";
    private static final int DATABASE_VERSION = 56;

    private static final Object LOCK_INSTANCE = new Object();
    private static GmaDatabase INSTANCE;

    @NonNull
    private final Context mContext;

    private GmaDatabase(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
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
            db.execSQL(Contract.Training.SQL_CREATE_TABLE);
            db.execSQL(Contract.Training.Completion.SQL_CREATE_TABLE);
            db.execSQL(Contract.Church.SQL_CREATE_TABLE);
            db.execSQL(Contract.MeasurementType.SQL_CREATE_TABLE);
            db.execSQL(Contract.MeasurementTypeLocalization.SQL_CREATE_TABLE);
            db.execSQL(Contract.MinistryMeasurement.SQL_CREATE_TABLE);
            db.execSQL(Contract.PersonalMeasurement.SQL_CREATE_TABLE);
            db.execSQL(Contract.MeasurementDetails.SQL_CREATE_TABLE);
            db.execSQL(Contract.MeasurementVisibility.SQL_CREATE_TABLE);
            db.execSQL(Contract.FavoriteMeasurement.SQL_CREATE_TABLE);
            db.execSQL(Contract.Story.SQL_CREATE_TABLE);
            db.execSQL(Contract.UserPreference.SQL_CREATE_TABLE);
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
                        db.execSQL(Contract.MeasurementDetails.SQL_V28_CREATE_TABLE);
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
                        break;
                    case 32:
                        db.execSQL(Contract.MeasurementType.SQL_V32_ALTER_CUSTOM);
                        break;
                    case 33:
                        db.execSQL(Contract.Church.SQL_v33_ALTER_PARENT);
                        break;
                    case 34:
                        db.execSQL(Contract.MeasurementVisibility.SQL_CREATE_TABLE);
                        break;
                    case 35:
                        db.execSQL(Contract.Church.SQL_v35_ALTER_MOBILE);
                        db.execSQL(Contract.Training.SQL_v35_ALTER_NEW);
                        break;
                    case 36:
                        db.execSQL(Contract.Church.SQL_v36_ALTER_END_DATE);
                        break;
                    case 37:
                        db.execSQL(Contract.Training.SQL_v37_ALTER_DELETED);
                        break;
                    case 38:
                        db.execSQL(Contract.Church.SQL_v38_ALTER_CREATED_BY);
                        break;
                    case 39:
                        db.execSQL(Contract.Training.SQL_v39_ALTER_CREATED_BY);
                        break;
                    case 40:
                        db.execSQL(Contract.Training.Completion.SQL_v40_ALTER_DIRTY);
                        break;
                    case 41:
                        db.execSQL(Contract.Training.Completion.SQL_v41_ALTER_NEW);
                        break;
                    case 42:
                        db.execSQL(Contract.Training.Completion.SQL_v42_ALTER_DELETED);
                        break;
                    case 43:
                        db.execSQL(Contract.Church.SQL_v43_ALTER_JESUS_FILM_ACTIVITY);
                        break;
                    case 44:
                        db.execSQL(Contract.Training.SQL_v44_ALTER_PARTICIPANTS);
                        break;
                    case 45:
                        db.execSQL(Contract.MeasurementTypeLocalization.SQL_V45_CREATE_TABLE);
                        break;
                    case 46:
                        db.execSQL(Contract.Assignment.SQL_V46_ALTER_PERSON_ID);
                        break;
                    case 47:
                        db.execSQL(Contract.MeasurementType.SQL_V47_ALTER_SUPPORTED_STAFF_ONLY);
                        break;
                    case 48:
                        db.execSQL(Contract.MeasurementType.SQL_V48_ALTER_LEADER_ONLY);
                        break;
                    case 49:
                        break;
                    case 50:
                        db.execSQL(Contract.UserPreference.SQL_CREATE_TABLE);
                        break;
                    case 51:
                        break;
                    case 52:
                        db.execSQL(Contract.FavoriteMeasurement.SQL_CREATE_TABLE);
                        break;
                    case 53:
                        db.execSQL(Contract.MeasurementDetails.SQL_V53_ALTER_SOURCE);
                        db.execSQL(Contract.MeasurementDetails.SQL_V53_UPDATE_SOURCE);
                        break;
                    case 54:
                        db.execSQL(Contract.Story.SQL_V54_CREATE_TABLE);
                        break;
                    case 55:
                        db.execSQL(Contract.Story.SQL_V55_ALTER_IMAGE);
                        break;
                    case 56:
                        db.execSQL(Contract.Story.SQL_V56_ALTER_PENDING_IMAGE);
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
            if (ApplicationUtils.isDebuggable(mContext)) {
                throw Throwables.propagate(e);
            } else if (Fabric.isInitialized() && Crashlytics.getInstance() != null) {
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
            db.execSQL(Contract.UserPreference.SQL_DELETE_TABLE);
            db.execSQL(Contract.Church.SQL_DELETE_TABLE);
            db.execSQL(Contract.MeasurementTypeLocalization.SQL_DELETE_TABLE);
            db.execSQL(Contract.MeasurementType.SQL_DELETE_TABLE);
            db.execSQL(Contract.MeasurementVisibility.SQL_DELETE_TABLE);
            db.execSQL(Contract.FavoriteMeasurement.SQL_DELETE_TABLE);
            db.execSQL(Contract.MinistryMeasurement.SQL_DELETE_TABLE);
            db.execSQL(Contract.PersonalMeasurement.SQL_DELETE_TABLE);
            db.execSQL(Contract.MeasurementDetails.SQL_DELETE_TABLE);
            db.execSQL(Contract.Story.SQL_DELETE_TABLE);
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

    private void deleteAllTables(SQLiteDatabase db)
    {
        db.execSQL(Contract.Training.Completion.SQL_DELETE_TABLE);
        db.execSQL(Contract.Training.SQL_DELETE_TABLE);
        db.execSQL(Contract.Ministry.SQL_DELETE_TABLE);
        db.execSQL(Contract.Assignment.SQL_DELETE_TABLE);
    }
}
