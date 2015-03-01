package com.expidev.gcmapp.db;

import android.provider.BaseColumns;
import android.text.TextUtils;

import org.ccci.gto.android.common.db.Join;

public class Contract {
    private Contract() {
    }

    public static abstract class Base implements BaseColumns {
        public static final String COLUMN_ROWID = _ID;
        public static final String COLUMN_LAST_SYNCED = "lastSynced";

        static final String SQL_COLUMN_ROWID = COLUMN_ROWID + " INTEGER PRIMARY KEY";
        static final String SQL_COLUMN_LAST_SYNCED = COLUMN_LAST_SYNCED + " INTEGER";
    }

    private static interface MinistryId {
        public static final String COLUMN_MINISTRY_ID = "ministry_id";

        public static final String SQL_COLUMN_MINISTRY_ID =
                COLUMN_MINISTRY_ID + " TEXT COLLATE NOCASE NOT NULL DEFAULT ''";

        public static final String SQL_WHERE_MINISTRY = COLUMN_MINISTRY_ID + " = ?";
    }

    public static abstract class Location extends Base {
        static final String COLUMN_LATITUDE = "latitude";
        static final String COLUMN_LONGITUDE = "longitude";

        static final String SQL_COLUMN_LATITUDE = COLUMN_LATITUDE + " DECIMAL";
        static final String SQL_COLUMN_LONGITUDE = COLUMN_LONGITUDE + " DECIMAL";
    }

    public static final class Training extends Location implements MinistryId {
        public static final String TABLE_NAME = "training";

        static final String COLUMN_ID = _ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TYPE = "type";
        static final String COLUMN_MCC = "mcc";
        public static final String COLUMN_DIRTY = "dirtyData";

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_DATE, COLUMN_TYPE, COLUMN_MCC, COLUMN_LATITUDE,
                        COLUMN_LONGITUDE, COLUMN_DIRTY, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
        private static final String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DATE = COLUMN_DATE + " TEXT";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";
        private static final String SQL_COLUMN_DIRTY = COLUMN_DIRTY + " TEXT";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        public static final String SQL_WHERE_MINISTRY_ID_MCC = SQL_WHERE_MINISTRY + " AND " + COLUMN_MCC + " = ?";
        public static final String SQL_WHERE_DIRTY = COLUMN_DIRTY + " != ''";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME, SQL_COLUMN_DATE,
                        SQL_COLUMN_TYPE, SQL_COLUMN_MCC, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE, SQL_COLUMN_DIRTY,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final class Completion extends Base {
            public static final String TABLE_NAME = "training_completions";

            static final String COLUMN_ID = _ID;
            static final String COLUMN_TRAINING_ID = "training_id";
            static final String COLUMN_PHASE = "phase";
            static final String COLUMN_NUMBER_COMPLETED = "number_completed";
            static final String COLUMN_DATE = "date";

            static final String[] PROJECTION_ALL =
                    {COLUMN_ID, COLUMN_TRAINING_ID, COLUMN_PHASE, COLUMN_NUMBER_COMPLETED, COLUMN_DATE,
                            COLUMN_LAST_SYNCED};

            private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
            private static final String SQL_COLUMN_TRAINING_ID = COLUMN_TRAINING_ID + " INTEGER";
            private static final String SQL_COLUMN_PHASE = COLUMN_PHASE + " INTEGER";
            private static final String SQL_COLUMN_NUMBER_COMPLETED = COLUMN_NUMBER_COMPLETED + " INTEGER";
            private static final String SQL_COLUMN_DATE = COLUMN_DATE + " TEXT";

            private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

            static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
            static final String SQL_WHERE_TRAINING_ID = COLUMN_TRAINING_ID + " = ?";

            public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                    .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_TRAINING_ID, SQL_COLUMN_PHASE,
                            SQL_COLUMN_NUMBER_COMPLETED, SQL_COLUMN_DATE, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) +
                    ")";
            public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        }
    }

    @Deprecated
    public static final class LegacyTables {
        public static final String SQL_DELETE_ALL_MINISTRIES_TABLE = "DROP TABLE IF EXISTS all_ministries";
        public static final String SQL_DELETE_ASSOCIATED_MINISTRIES_TABLE =
                "DROP TABLE IF EXISTS associated_ministries";
    }

    public static final class Ministry extends Base implements MinistryId {
        public static final String TABLE_NAME = "ministries";

        static final String COLUMN_MIN_CODE = "min_code";
        public static final String COLUMN_NAME = "name";
        static final String COLUMN_MCCS = "mccs";
        static final String COLUMN_LATITUDE = "latitude";
        static final String COLUMN_LONGITUDE = "longitude";
        static final String COLUMN_LOCATION_ZOOM = "location_zoom";
        static final String COLUMN_PARENT_MINISTRY_ID = "parent_ministry_id";

        public static final String[] PROJECTION_ALL =
                {COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_MIN_CODE, COLUMN_MCCS, COLUMN_LATITUDE, COLUMN_LONGITUDE,
                        COLUMN_LOCATION_ZOOM, COLUMN_PARENT_MINISTRY_ID, COLUMN_LAST_SYNCED};

        static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_MIN_CODE = COLUMN_MIN_CODE + " TEXT";
        private static final String SQL_COLUMN_MCCS = COLUMN_MCCS + " TEXT NOT NULL DEFAULT ''";
        private static final String SQL_COLUMN_LATITUDE = COLUMN_LATITUDE + " DECIMAL";
        private static final String SQL_COLUMN_LONGITUDE = COLUMN_LONGITUDE + " DECIMAL";
        private static final String SQL_COLUMN_LOCATION_ZOOM = COLUMN_LOCATION_ZOOM + " INTEGER";
        private static final String SQL_COLUMN_PARENT_MINISTRY_ID = COLUMN_PARENT_MINISTRY_ID + " TEXT";
        static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_MINISTRY_ID + ")";

        private static final String SQL_PREFIX = TABLE_NAME + ".";

        static final String SQL_WHERE_PRIMARY_KEY = SQL_WHERE_MINISTRY;
        static final String SQL_WHERE_PARENT = COLUMN_PARENT_MINISTRY_ID + " = ?";

        private static final String SQL_JOIN_ON_ASSIGNMENT =
                SQL_PREFIX + COLUMN_MINISTRY_ID + " = " + Assignment.SQL_PREFIX + Assignment.COLUMN_MINISTRY_ID;
        public static final Join<com.expidev.gcmapp.model.Ministry, com.expidev.gcmapp.model.Assignment>
                JOIN_ASSIGNMENT = Join.create(com.expidev.gcmapp.model.Ministry.class,
                                              com.expidev.gcmapp.model.Assignment.class).on(SQL_JOIN_ON_ASSIGNMENT);

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME, SQL_COLUMN_MIN_CODE,
                        SQL_COLUMN_MCCS, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE, SQL_COLUMN_LOCATION_ZOOM,
                        SQL_COLUMN_PARENT_MINISTRY_ID, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        public static final String SQL_V15_RENAME_TABLE = "ALTER TABLE associated_ministries RENAME TO " + TABLE_NAME;
        @Deprecated
        public static final String SQL_V16_MCCS = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_MCCS;
    }

    public static final class Assignment extends Base implements MinistryId {
        public static final String TABLE_NAME = "assignments";

        public static final String COLUMN_GUID = "guid";
        public static final String COLUMN_ROLE = "team_role";
        public static final String COLUMN_MCC = "mcc";
        public static final String COLUMN_ID = "assignment_id";

        static final String[] PROJECTION_ALL =
                {COLUMN_GUID, COLUMN_ID, COLUMN_ROLE, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_LAST_SYNCED};
        public static final String[] PROJECTION_API_GET_ASSIGNMENT = {COLUMN_ID, COLUMN_ROLE, COLUMN_LAST_SYNCED};
        public static final String[] PROJECTION_API_CREATE_ASSIGNMENT = PROJECTION_API_GET_ASSIGNMENT;

        private static final String SQL_COLUMN_GUID = COLUMN_GUID + " TEXT NOT NULL DEFAULT ''";
        private static final String SQL_COLUMN_ROLE = COLUMN_ROLE + " TEXT";
        private static final String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT NOT NULL DEFAULT ''";
        private static final String SQL_COLUMN_ID = COLUMN_ID + " TEXT";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_GUID + "," + COLUMN_MINISTRY_ID + ")";
        private static final String SQL_FOREIGN_KEY_MINISTRIES =
                "FOREIGN KEY(" + COLUMN_MINISTRY_ID + ") REFERENCES " + Ministry.TABLE_NAME + "(" +
                        Ministry.COLUMN_MINISTRY_ID + ")";

        private static final String SQL_PREFIX = TABLE_NAME + ".";

        public static final String SQL_WHERE_GUID = SQL_PREFIX + COLUMN_GUID + " = ?";
        static final String SQL_WHERE_PRIMARY_KEY = SQL_WHERE_GUID + " AND " + SQL_WHERE_MINISTRY;

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_ID,
                        SQL_COLUMN_ROLE, SQL_COLUMN_MCC, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY,
                        SQL_FOREIGN_KEY_MINISTRIES}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        public static final String SQL_V11_MCC = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_MCC;
    }

    public static final class Church extends Location implements MinistryId {
        public static final String TABLE_NAME = "churches";

        static final String COLUMN_ID = _ID;
        static final String COLUMN_NAME = "name";
        public static final String COLUMN_CONTACT_NAME = "contact_name";
        public static final String COLUMN_CONTACT_EMAIL = "contact_email";
        static final String COLUMN_DEVELOPMENT = "development";
        public static final String COLUMN_SIZE = "size";
        static final String COLUMN_SECURITY = "security";
        public static final String COLUMN_DIRTY = "dirtyData";

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_CONTACT_NAME, COLUMN_CONTACT_EMAIL, COLUMN_LATITUDE,
                        COLUMN_LONGITUDE, COLUMN_DEVELOPMENT, COLUMN_SIZE, COLUMN_SECURITY, COLUMN_DIRTY,
                        COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_CONTACT_NAME = COLUMN_CONTACT_NAME + " TEXT";
        private static final String SQL_COLUMN_CONTACT_EMAIL = COLUMN_CONTACT_EMAIL + " TEXT";
        private static final String SQL_COLUMN_DEVELOPMENT = COLUMN_DEVELOPMENT + " INTEGER";
        private static final String SQL_COLUMN_SIZE = COLUMN_SIZE + " INTEGER";
        private static final String SQL_COLUMN_SECURITY = COLUMN_SECURITY + " INTEGER";
        private static final String SQL_COLUMN_DIRTY = COLUMN_DIRTY + " TEXT";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        public static final String SQL_WHERE_DIRTY = COLUMN_DIRTY + " != ''";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME,
                        SQL_COLUMN_CONTACT_NAME, SQL_COLUMN_CONTACT_EMAIL, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE,
                        SQL_COLUMN_DEVELOPMENT, SQL_COLUMN_SIZE, SQL_COLUMN_SECURITY, SQL_COLUMN_DIRTY,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        // DB migration queries
        @Deprecated
        public static final String SQL_V8_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME,
                        SQL_COLUMN_CONTACT_NAME, SQL_COLUMN_CONTACT_EMAIL, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE,
                        SQL_COLUMN_DEVELOPMENT, SQL_COLUMN_SIZE, SQL_COLUMN_SECURITY, SQL_COLUMN_LAST_SYNCED,
                        SQL_PRIMARY_KEY}) + ")";
        @Deprecated
        public static final String SQL_V9_ALTER_DIRTY = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_DIRTY;
    }

    ///////////////////////////////////////////////////////////////
    //              Measurement Contracts                       //
    //////////////////////////////////////////////////////////////

    public static final class Measurement extends Base implements MinistryId {
        public static final String TABLE_NAME = "measurements";
        private static final String INDEX_NAME = "measurements_unique_index";

        static final String COLUMN_MEASUREMENT_ID = "measurement_id";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_PERM_LINK = "perm_link";
        static final String COLUMN_CUSTOM = "custom";
        static final String COLUMN_SECTION = "section";
        static final String COLUMN_COLUMN = "measurement_column";
        static final String COLUMN_TOTAL = "total";
        static final String COLUMN_MCC = "mcc";
        static final String COLUMN_PERIOD = "period";
        static final String COLUMN_SORT_ORDER = "sort_order";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_NAME, COLUMN_PERM_LINK, COLUMN_CUSTOM, COLUMN_SECTION, COLUMN_COLUMN,
            COLUMN_TOTAL, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERIOD, COLUMN_SORT_ORDER, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_MEASUREMENT_ID = COLUMN_MEASUREMENT_ID + " TEXT";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_PERM_LINK = COLUMN_PERM_LINK + " TEXT";
        private static final String SQL_COLUMN_CUSTOM = COLUMN_CUSTOM + " INTEGER";
        private static final String SQL_COLUMN_SECTION= COLUMN_SECTION + " TEXT";
        private static final String SQL_COLUMN_COLUMN = COLUMN_COLUMN + " TEXT";
        private static final String SQL_COLUMN_TOTAL = COLUMN_TOTAL + " INTEGER";
        private static final String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT";
        private static final String SQL_COLUMN_PERIOD = COLUMN_PERIOD + " TEXT";
        private static final String SQL_COLUMN_SORT_ORDER = COLUMN_SORT_ORDER + " INTEGER";

        public static final String SQL_WHERE_UNIQUE = COLUMN_MEASUREMENT_ID + " = ? AND " +
            COLUMN_MINISTRY_ID + " = ? AND " + COLUMN_MCC + " = ? AND " + COLUMN_PERIOD + " = ?";
        public static final String SQL_WHERE_MINISTRY_MCC_PERIOD = SQL_WHERE_MINISTRY + " AND " +
            COLUMN_MCC + " = ? AND " + COLUMN_PERIOD + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[]{ SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_NAME, SQL_COLUMN_PERM_LINK,
                SQL_COLUMN_CUSTOM, SQL_COLUMN_SECTION, SQL_COLUMN_COLUMN, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_MCC,
                SQL_COLUMN_PERIOD, SQL_COLUMN_TOTAL, SQL_COLUMN_SORT_ORDER, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String SQL_CREATE_INDEX = "CREATE UNIQUE INDEX " + INDEX_NAME + " ON " + TABLE_NAME + "(" +
            TextUtils.join(",", new Object[] { COLUMN_MEASUREMENT_ID, COLUMN_MINISTRY_ID, COLUMN_MCC,
                COLUMN_PERIOD }) + ");";

        // DB migration queries
        @Deprecated
        public static final String SQL_V13_ALTER_SORT = "ALTER TABLE " + TABLE_NAME +
            " ADD COLUMN " + SQL_COLUMN_SORT_ORDER + ";";
    }

    public static final class MeasurementDetails extends Base implements MinistryId {
        public static final String TABLE_NAME = "measurement_details";

        static final String COLUMN_MEASUREMENT_ID = "measurement_id"; //Foreign key for Measurement
        static final String COLUMN_MCC = "mcc";
        static final String COLUMN_PERIOD = "period";
        static final String COLUMN_LOCAL_AMOUNT = "local_amount";
        static final String COLUMN_PERSONAL_AMOUNT = "personal_amount";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_MCC, COLUMN_MINISTRY_ID,
            COLUMN_PERIOD, COLUMN_LOCAL_AMOUNT, COLUMN_PERSONAL_AMOUNT, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_MEASUREMENT_ID = COLUMN_MEASUREMENT_ID + " TEXT";
        private static final String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT";
        private static final String SQL_COLUMN_PERIOD = COLUMN_PERIOD + " TEXT";
        private static final String SQL_COLUMN_LOCAL_AMOUNT = COLUMN_LOCAL_AMOUNT + " INTEGER";
        private static final String SQL_COLUMN_PERSONAL_AMOUNT = COLUMN_PERSONAL_AMOUNT + " INTEGER";

        public static final String SQL_WHERE_MEASUREMENT = COLUMN_MEASUREMENT_ID + " = ? AND " +
            COLUMN_MINISTRY_ID + " = ? AND " + COLUMN_MCC + " = ? AND " + COLUMN_PERIOD + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_MCC,
                SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_PERIOD, SQL_COLUMN_LOCAL_AMOUNT,
                SQL_COLUMN_PERSONAL_AMOUNT, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        // DB migration queries
        @Deprecated
        public static final String SQL_V10_ALTER_LOCAL = "ALTER TABLE " + TABLE_NAME +
            " ADD COLUMN " + SQL_COLUMN_LOCAL_AMOUNT + ";";
        @Deprecated
        public static final String SQL_V10_ALTER_PERSONAL = "ALTER TABLE " + TABLE_NAME +
            " ADD COLUMN " + SQL_COLUMN_PERSONAL_AMOUNT + ";";
    }

    public static abstract class MeasurementDetailsData extends Base implements MinistryId {
        static final String COLUMN_MEASUREMENT_ID = "measurement_id"; //Link to which Measurement this is
        static final String COLUMN_MCC = "mcc";
        static final String COLUMN_PERIOD = "period";

        static final String SQL_COLUMN_MEASUREMENT_ID = COLUMN_MEASUREMENT_ID + " TEXT";
        static final String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT";
        static final String SQL_COLUMN_PERIOD = COLUMN_PERIOD + " TEXT";

        public static final String SQL_WHERE_MEASUREMENT = COLUMN_MEASUREMENT_ID + " = ? AND " +
            COLUMN_MINISTRY_ID + " = ? AND " + COLUMN_MCC + " = ? AND " + COLUMN_PERIOD + " = ?";
    }

    public static final class MeasurementTypeIds extends MeasurementDetailsData
    {
        public static final String TABLE_NAME = "measurement_type_ids";

        static final String COLUMN_TOTAL_ID = "total_id";
        static final String COLUMN_LOCAL_ID = "local_id";
        static final String COLUMN_PERSON_ID = "person_id";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERIOD, COLUMN_TOTAL_ID,
            COLUMN_LOCAL_ID, COLUMN_PERSON_ID, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_TOTAL_ID = COLUMN_TOTAL_ID + " TEXT";
        private static final String SQL_COLUMN_LOCAL_ID = COLUMN_LOCAL_ID + " TEXT";
        private static final String SQL_COLUMN_PERSON_ID = COLUMN_PERSON_ID + " TEXT";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_MINISTRY_ID,
                SQL_COLUMN_MCC, SQL_COLUMN_PERIOD, SQL_COLUMN_TOTAL_ID, SQL_COLUMN_LOCAL_ID,
                SQL_COLUMN_PERSON_ID, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class SixMonthAmounts extends MeasurementDetailsData
    {
        public static final String TABLE_NAME = "six_month_amounts";

        static final String COLUMN_MONTH = "month";
        static final String COLUMN_AMOUNT = "amount";
        static final String COLUMN_AMOUNT_TYPE = "amount_type";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERIOD, COLUMN_MONTH,
            COLUMN_AMOUNT, COLUMN_AMOUNT_TYPE, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_MONTH = COLUMN_MONTH + " TEXT";
        private static final String SQL_COLUMN_AMOUNT = COLUMN_AMOUNT + " INTEGER";
        private static final String SQL_COLUMN_AMOUNT_TYPE = COLUMN_AMOUNT_TYPE + " TEXT";  // local, personal, total

        public static final String SQL_WHERE_SEARCH = SQL_WHERE_MEASUREMENT + " AND " + COLUMN_AMOUNT_TYPE + " = ?";
        public static final String SQL_WHERE_UNIQUE = SQL_WHERE_MEASUREMENT + " AND " + COLUMN_MONTH + " = ? AND " +
            COLUMN_AMOUNT_TYPE + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_MINISTRY_ID,
                SQL_COLUMN_MCC, SQL_COLUMN_PERIOD, SQL_COLUMN_MONTH, SQL_COLUMN_AMOUNT,
                SQL_COLUMN_AMOUNT_TYPE, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class BreakdownData extends MeasurementDetailsData
    {
        public static final String TABLE_NAME = "breakdown_data";

        static final String COLUMN_SOURCE = "source";
        static final String COLUMN_AMOUNT = "amount";
        static final String COLUMN_TYPE = "type";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERIOD,
            COLUMN_SOURCE, COLUMN_AMOUNT, COLUMN_TYPE, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_SOURCE = COLUMN_SOURCE + " TEXT";
        private static final String SQL_COLUMN_AMOUNT = COLUMN_AMOUNT + " INTEGER";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";

        public static final String SQL_WHERE_UNIQUE = SQL_WHERE_MEASUREMENT + " AND " + COLUMN_SOURCE + " = ? AND " +
            COLUMN_TYPE + " = ?";
        public static final String SQL_WHERE_SEARCH = SQL_WHERE_MEASUREMENT + " AND " + COLUMN_TYPE + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_MINISTRY_ID,
                SQL_COLUMN_MCC, SQL_COLUMN_PERIOD, SQL_COLUMN_SOURCE, SQL_COLUMN_AMOUNT, SQL_COLUMN_TYPE,
                SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TeamMemberDetails extends MeasurementDetailsData
    {
        public static final String TABLE_NAME = "team_member_details";

        static final String COLUMN_ASSIGNMENT_ID = "assignment_id";
        static final String COLUMN_TEAM_ROLE = "team_role";
        static final String COLUMN_FIRST_NAME = "first_name";
        static final String COLUMN_LAST_NAME = "last_name";
        static final String COLUMN_PERSON_ID = "person_id";
        static final String COLUMN_TOTAL = "total";
        static final String COLUMN_TYPE = "type";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERIOD,
            COLUMN_ASSIGNMENT_ID, COLUMN_TEAM_ROLE, COLUMN_FIRST_NAME, COLUMN_LAST_NAME,
            COLUMN_PERSON_ID, COLUMN_TOTAL, COLUMN_TYPE, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_ASSIGNMENT_ID = COLUMN_ASSIGNMENT_ID + " TEXT";
        private static final String SQL_COLUMN_TEAM_ROLE = COLUMN_TEAM_ROLE + " TEXT";
        private static final String SQL_COLUMN_FIRST_NAME = COLUMN_FIRST_NAME + " TEXT";
        private static final String SQL_COLUMN_LAST_NAME = COLUMN_LAST_NAME + " TEXT";
        private static final String SQL_COLUMN_PERSON_ID = COLUMN_PERSON_ID + " TEXT";
        private static final String SQL_COLUMN_TOTAL = COLUMN_TOTAL + " INTEGER";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";

        public static final String SQL_WHERE_SEARCH = SQL_WHERE_MEASUREMENT + " AND " + COLUMN_TYPE + " = ?";
        public static final String SQL_WHERE_UNIQUE = SQL_WHERE_MEASUREMENT + " AND " + COLUMN_TYPE + " = ? AND " +
            COLUMN_PERSON_ID + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_MINISTRY_ID,
                SQL_COLUMN_MCC, SQL_COLUMN_PERIOD, SQL_COLUMN_ASSIGNMENT_ID, SQL_COLUMN_TEAM_ROLE,
                SQL_COLUMN_FIRST_NAME, SQL_COLUMN_LAST_NAME, SQL_COLUMN_PERSON_ID,
                SQL_COLUMN_TOTAL, SQL_COLUMN_TYPE, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class SubMinistryDetails extends MeasurementDetailsData
    {
        public static final String TABLE_NAME = "sub_ministry_details";

        static final String COLUMN_NAME = "name";
        static final String COLUMN_TOTAL = "total";
        static final String COLUMN_SUB_MINISTRY_ID = "sub_ministry_id";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERIOD,
            COLUMN_NAME, COLUMN_SUB_MINISTRY_ID, COLUMN_TOTAL, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_TOTAL = COLUMN_TOTAL + " INTEGER";
        private static final String SQL_COLUMN_SUB_MINISTRY_ID = COLUMN_SUB_MINISTRY_ID + " TEXT";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_MINISTRY_ID,
                SQL_COLUMN_MCC, SQL_COLUMN_PERIOD, SQL_COLUMN_NAME, SQL_COLUMN_TOTAL,
                SQL_COLUMN_SUB_MINISTRY_ID, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
