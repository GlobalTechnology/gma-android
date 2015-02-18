package com.expidev.gcmapp.db;

import android.provider.BaseColumns;
import android.text.TextUtils;

public class Contract {
    private Contract() {
    }

    public static abstract class Base implements BaseColumns {
        public static final String COLUMN_ROWID = _ID;
        public static final String COLUMN_LAST_SYNCED = "lastSynced";

        static final String SQL_COLUMN_ROWID = COLUMN_ROWID + " INTEGER PRIMARY KEY";
        static final String SQL_COLUMN_LAST_SYNCED = COLUMN_LAST_SYNCED + " INTEGER";
    }

    public static final class Training extends Base {
        public static final String TABLE_NAME = "training";

        static final String COLUMN_ID = _ID;
        static final String COLUMN_MINISTRY_ID = "ministry_id";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_DATE = "date";
        static final String COLUMN_TYPE = "type";
        static final String COLUMN_MCC = "mcc";
        static final String COLUMN_LATITUDE = "latitude";
        static final String COLUMN_LONGITUDE = "longitude";

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_DATE, COLUMN_TYPE, COLUMN_MCC, COLUMN_LATITUDE,
                        COLUMN_LONGITUDE, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
        private static final String SQL_COLUMN_MINISTRY_ID = COLUMN_MINISTRY_ID + " TEXT";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DATE = COLUMN_DATE + " TEXT";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";
        private static final String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT";
        private static final String SQL_COLUMN_LATITUDE = COLUMN_LATITUDE + " DECIMAL";
        private static final String SQL_COLUMN_LONGITUDE = COLUMN_LONGITUDE + " DECIMAL";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        static final String SQL_WHERE_MINISTRY_ID = COLUMN_MINISTRY_ID + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME, SQL_COLUMN_DATE,
                        SQL_COLUMN_TYPE, SQL_COLUMN_MCC, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE,
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

    public static abstract class MinistryBase extends Base
    {
        static final String COLUMN_MINISTRY_ID = "ministry_id";
        public static final String COLUMN_NAME = "name";

        static final String SQL_COLUMN_MINISTRY_ID = COLUMN_MINISTRY_ID + " TEXT";
        static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_MINISTRY_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_MINISTRY_ID + " = ?";
    }

    public static final class Ministry extends MinistryBase
    {
        public static final String TABLE_NAME = "all_ministries";

        static final String[] PROJECTION_ALL = { COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_LAST_SYNCED };

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class AssociatedMinistry extends MinistryBase
    {
        public static final String TABLE_NAME = "associated_ministries";

        static final String COLUMN_MIN_CODE = "min_code";
        static final String COLUMN_HAS_SLM = "has_slm";
        static final String COLUMN_HAS_LLM = "has_llm";
        static final String COLUMN_HAS_DS = "has_ds";
        static final String COLUMN_HAS_GCM = "has_gcm";
        static final String COLUMN_LATITUDE = "latitude";
        static final String COLUMN_LONGITUDE = "longitude";
        static final String COLUMN_LOCATION_ZOOM = "location_zoom";
        static final String COLUMN_PARENT_MINISTRY_ID = "parent_ministry_id";

        static final String[] PROJECTION_ALL =
                {COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_MIN_CODE, COLUMN_HAS_SLM, COLUMN_HAS_LLM, COLUMN_HAS_DS,
                        COLUMN_HAS_GCM, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_LOCATION_ZOOM,
                        COLUMN_PARENT_MINISTRY_ID, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_MIN_CODE = COLUMN_MIN_CODE + " TEXT";
        private static final String SQL_COLUMN_HAS_SLM = COLUMN_HAS_SLM + " INTEGER";
        private static final String SQL_COLUMN_HAS_LLM = COLUMN_HAS_LLM + " INTEGER";
        private static final String SQL_COLUMN_HAS_DS = COLUMN_HAS_DS + " INTEGER";
        private static final String SQL_COLUMN_HAS_GCM = COLUMN_HAS_GCM + " INTEGER";
        private static final String SQL_COLUMN_LATITUDE = COLUMN_LATITUDE + " DECIMAL";
        private static final String SQL_COLUMN_LONGITUDE = COLUMN_LONGITUDE + " DECIMAL";
        private static final String SQL_COLUMN_LOCATION_ZOOM = COLUMN_LOCATION_ZOOM + " INTEGER";
        private static final String SQL_COLUMN_PARENT_MINISTRY_ID = COLUMN_PARENT_MINISTRY_ID + " TEXT";

        static final String SQL_WHERE_PARENT = COLUMN_PARENT_MINISTRY_ID + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME, SQL_COLUMN_MIN_CODE,
                        SQL_COLUMN_HAS_SLM, SQL_COLUMN_HAS_LLM, SQL_COLUMN_HAS_DS, SQL_COLUMN_HAS_GCM,
                        SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE, SQL_COLUMN_LOCATION_ZOOM,
                        SQL_COLUMN_PARENT_MINISTRY_ID, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class Assignment extends Base {
        public static final String TABLE_NAME = "assignments";

        static final String COLUMN_ID = "assignment_id";
        public static final String COLUMN_ROLE = "team_role";
        public static final String COLUMN_MINISTRY_ID = "ministry_id";

        static final String[] PROJECTION_ALL = {COLUMN_ID, COLUMN_ROLE, COLUMN_MINISTRY_ID, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " TEXT";
        private static final String SQL_COLUMN_ROLE = COLUMN_ROLE + " TEXT";
        private static final String SQL_COLUMN_MINISTRY_ID = COLUMN_MINISTRY_ID + " TEXT";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_ID + ")";
        private static final String SQL_FOREIGN_KEY_MINISTRIES =
                "FOREIGN KEY(" + COLUMN_MINISTRY_ID + ") REFERENCES " + AssociatedMinistry.TABLE_NAME + "(" +
                        AssociatedMinistry.COLUMN_MINISTRY_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        static final String SQL_WHERE_MINISTRY = COLUMN_MINISTRY_ID + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_ID, SQL_COLUMN_ROLE, SQL_COLUMN_MINISTRY_ID,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY, SQL_FOREIGN_KEY_MINISTRIES}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    ///////////////////////////////////////////////////////////////
    //              Measurement Contracts                       //
    //////////////////////////////////////////////////////////////

    public static final class Measurement extends Base
    {
        public static final String TABLE_NAME = "measurements";

        static final String COLUMN_MEASUREMENT_ID = "measurement_id";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_PERM_LINK = "perm_link";
        static final String COLUMN_CUSTOM = "custom";
        static final String COLUMN_SECTION = "section";
        static final String COLUMN_COLUMN = "measurement_column";
        static final String COLUMN_TOTAL = "total";
        static final String COLUMN_MINISTRY_ID = "ministry_id";
        static final String COLUMN_MCC = "mcc";
        static final String COLUMN_PERIOD = "period";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_NAME, COLUMN_PERM_LINK, COLUMN_CUSTOM, COLUMN_SECTION, COLUMN_COLUMN,
            COLUMN_TOTAL, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERIOD, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_MEASUREMENT_ID = COLUMN_MEASUREMENT_ID + " TEXT";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_PERM_LINK = COLUMN_PERM_LINK + " TEXT";
        private static final String SQL_COLUMN_CUSTOM = COLUMN_CUSTOM + " INTEGER";
        private static final String SQL_COLUMN_SECTION= COLUMN_SECTION + " TEXT";
        private static final String SQL_COLUMN_COLUMN = COLUMN_COLUMN + " TEXT";
        private static final String SQL_COLUMN_TOTAL = COLUMN_TOTAL + " INTEGER";
        private static final String SQL_COLUMN_MINISTRY_ID = COLUMN_MINISTRY_ID + " TEXT";
        private static final String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT";
        private static final String SQL_COLUMN_PERIOD = COLUMN_PERIOD + " TEXT";

        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_MEASUREMENT_ID + ")";
        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_MEASUREMENT_ID + " = ?";
        public static final String SQL_WHERE_MINISTRY_MCC_PERIOD = COLUMN_MINISTRY_ID + " = ? AND " +
            COLUMN_MCC + " = ? AND " + COLUMN_PERIOD + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[]{ SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_NAME, SQL_COLUMN_PERM_LINK,
                SQL_COLUMN_CUSTOM, SQL_COLUMN_SECTION, SQL_COLUMN_COLUMN, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_MCC,
                SQL_COLUMN_PERIOD, SQL_COLUMN_TOTAL, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class MeasurementDetails extends Base
    {
        public static final String TABLE_NAME = "measurement_details";

        static final String COLUMN_MEASUREMENT_ID = "measurement_id"; //Foreign key for Measurement
        static final String COLUMN_MCC = "mcc";
        static final String COLUMN_MINISTRY_ID = "ministry_id";
        static final String COLUMN_PERIOD = "period";

        static final String[] PROJECTION_ALL = { COLUMN_MEASUREMENT_ID, COLUMN_LAST_SYNCED };

        private static final String SQL_COLUMN_MEASUREMENT_ID = COLUMN_MEASUREMENT_ID + " TEXT";
        private static final String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT";
        private static final String SQL_COLUMN_MINISTRY_ID = COLUMN_MINISTRY_ID + " TEXT";
        private static final String SQL_COLUMN_PERIOD = COLUMN_PERIOD + " TEXT";

        static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ROWID + ")";
        public static final String SQL_WHERE_MEASUREMENT = COLUMN_MEASUREMENT_ID + " = ? AND " +
            COLUMN_MINISTRY_ID + " = ? AND " + COLUMN_MCC + " = ? AND " + COLUMN_PERIOD + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_MCC,
                SQL_COLUMN_LAST_SYNCED, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_PERIOD }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class MeasurementDetailsData extends Base
    {
        static final String COLUMN_MEASUREMENT_ID = "measurement_id"; //Link to which Measurement this is

        static final String SQL_COLUMN_MEASUREMENT_ID = COLUMN_MEASUREMENT_ID + " INTEGER";

        static final String SQL_WHERE_MEASUREMENT = COLUMN_MEASUREMENT_ID + " = ?";
    }

    public static final class MeasurementTypeIds extends MeasurementDetailsData
    {
        public static final String TABLE_NAME = "measurement_type_ids";

        static final String COLUMN_TOTAL_ID = "total_id";
        static final String COLUMN_LOCAL_ID = "local_id";
        static final String COLUMN_PERSON_ID = "person_id";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_TOTAL_ID,
            COLUMN_LOCAL_ID, COLUMN_PERSON_ID, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_TOTAL_ID = COLUMN_TOTAL_ID + " TEXT";
        private static final String SQL_COLUMN_LOCAL_ID = COLUMN_LOCAL_ID + " TEXT";
        private static final String SQL_COLUMN_PERSON_ID = COLUMN_PERSON_ID + " TEXT";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_TOTAL_ID,
                SQL_COLUMN_LOCAL_ID, SQL_COLUMN_PERSON_ID, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class SixMonthAmounts extends MeasurementDetailsData
    {
        public static final String TABLE_NAME = "six_month_amounts";

        static final String COLUMN_PERIOD = "period";
        static final String COLUMN_AMOUNT = "amount";
        static final String COLUMN_AMOUNT_TYPE = "amount_type";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_PERIOD,
            COLUMN_AMOUNT, COLUMN_AMOUNT_TYPE, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_PERIOD = COLUMN_PERIOD + " TEXT";
        private static final String SQL_COLUMN_AMOUNT = COLUMN_AMOUNT + " INTEGER";
        private static final String SQL_COLUMN_AMOUNT_TYPE = COLUMN_AMOUNT_TYPE + " TEXT";  // local, personal, total

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_PERIOD, SQL_COLUMN_AMOUNT,
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
            COLUMN_MEASUREMENT_ID, COLUMN_SOURCE,
            COLUMN_AMOUNT, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_SOURCE = COLUMN_SOURCE + " TEXT";
        private static final String SQL_COLUMN_AMOUNT = COLUMN_AMOUNT + " INTEGER";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_SOURCE,
                SQL_COLUMN_AMOUNT, SQL_COLUMN_TYPE, SQL_COLUMN_LAST_SYNCED }) + ");";
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
            COLUMN_MEASUREMENT_ID, COLUMN_ASSIGNMENT_ID,
            COLUMN_TEAM_ROLE, COLUMN_FIRST_NAME, COLUMN_LAST_NAME, COLUMN_PERSON_ID,
            COLUMN_TOTAL, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_ASSIGNMENT_ID = COLUMN_ASSIGNMENT_ID + " TEXT";
        private static final String SQL_COLUMN_TEAM_ROLE = COLUMN_TEAM_ROLE + " TEXT";
        private static final String SQL_COLUMN_FIRST_NAME = COLUMN_FIRST_NAME + " TEXT";
        private static final String SQL_COLUMN_LAST_NAME = COLUMN_LAST_NAME + " TEXT";
        private static final String SQL_COLUMN_PERSON_ID = COLUMN_PERSON_ID + " TEXT";
        private static final String SQL_COLUMN_TOTAL = COLUMN_TOTAL + " INTEGER";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_ASSIGNMENT_ID,
                SQL_COLUMN_TEAM_ROLE, SQL_COLUMN_FIRST_NAME, SQL_COLUMN_LAST_NAME, SQL_COLUMN_PERSON_ID,
                SQL_COLUMN_TOTAL, SQL_COLUMN_TYPE, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class SubMinistryDetails extends MeasurementDetailsData
    {
        public static final String TABLE_NAME = "sub_ministry_details";

        static final String COLUMN_NAME = "name";
        static final String COLUMN_MINISTRY_ID = "ministry_id";
        static final String COLUMN_TOTAL = "total";

        static final String[] PROJECTION_ALL = {
            COLUMN_MEASUREMENT_ID, COLUMN_NAME,
            COLUMN_MINISTRY_ID, COLUMN_TOTAL, COLUMN_LAST_SYNCED
        };

        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_MINISTRY_ID = COLUMN_MINISTRY_ID + " TEXT";
        private static final String SQL_COLUMN_TOTAL = COLUMN_TOTAL + " INTEGER";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
            .join(",", new Object[] { SQL_COLUMN_ROWID, SQL_COLUMN_MEASUREMENT_ID, SQL_COLUMN_NAME,
                SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_TOTAL, SQL_COLUMN_LAST_SYNCED }) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
