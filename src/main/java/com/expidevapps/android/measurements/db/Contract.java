package com.expidevapps.android.measurements.db;

import android.provider.BaseColumns;
import android.text.TextUtils;

import org.ccci.gto.android.common.db.Join;

public class Contract {
    private Contract() {
    }

    public static abstract class Base implements BaseColumns {
        public static final String COLUMN_ROWID = _ID;
        public static final String COLUMN_DIRTY = "dirtyData";
        public static final String COLUMN_LAST_SYNCED = "lastSynced";

        static final String SQL_COLUMN_ROWID = COLUMN_ROWID + " INTEGER PRIMARY KEY";
        static final String SQL_COLUMN_DIRTY = COLUMN_DIRTY + " TEXT";
        static final String SQL_COLUMN_LAST_SYNCED = COLUMN_LAST_SYNCED + " INTEGER";

        public static final String SQL_WHERE_DIRTY = COLUMN_DIRTY + " != ''";
    }

    private interface Guid {
        String COLUMN_GUID = "guid";

        String SQL_COLUMN_GUID = COLUMN_GUID + " TEXT NOT NULL DEFAULT ''";

        String SQL_WHERE_GUID = COLUMN_GUID + " = ?";
    }

    private interface MinistryId {
        String COLUMN_MINISTRY_ID = "ministry_id";

        String SQL_COLUMN_MINISTRY_ID = COLUMN_MINISTRY_ID + " TEXT COLLATE NOCASE NOT NULL DEFAULT ''";

        String SQL_WHERE_MINISTRY = COLUMN_MINISTRY_ID + " = ?";
    }

    private interface Mcc {
        String COLUMN_MCC = "mcc";

        String SQL_COLUMN_MCC = COLUMN_MCC + " TEXT NOT NULL DEFAULT ''";

        String SQL_WHERE_MCC = COLUMN_MCC + " = ?";
    }

    public interface Location {
        String COLUMN_LATITUDE = "latitude";
        String COLUMN_LONGITUDE = "longitude";

        String SQL_COLUMN_LATITUDE = COLUMN_LATITUDE + " DECIMAL";
        String SQL_COLUMN_LONGITUDE = COLUMN_LONGITUDE + " DECIMAL";
    }

    static final class LastSync {
        static final String TABLE_NAME = "syncData";

        static final String COLUMN_KEY = "key";
        static final String COLUMN_LAST_SYNCED = "lastSynced";

        private static final String SQL_COLUMN_KEY = COLUMN_KEY + " TEXT PRIMARY KEY";
        private static final String SQL_COLUMN_LAST_SYNCED = COLUMN_LAST_SYNCED + " INTEGER";

        public static final String SQL_WHERE_KEY = COLUMN_KEY + " = ?";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_KEY, SQL_COLUMN_LAST_SYNCED}) + ")";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class Training extends Base implements MinistryId, Mcc, Location {
        public static final String TABLE_NAME = "training";

        static final String COLUMN_ID = _ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TYPE = "type";

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_DATE, COLUMN_TYPE, COLUMN_MCC, COLUMN_LATITUDE,
                        COLUMN_LONGITUDE, COLUMN_DIRTY, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DATE = COLUMN_DATE + " TEXT";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        public static final String SQL_WHERE_MINISTRY_MCC = SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_MCC;

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
    static final class LegacyTables {
        static final String SQL_DELETE_ALL_MINISTRIES_TABLE = "DROP TABLE IF EXISTS all_ministries";
        static final String SQL_DELETE_ASSOCIATED_MINISTRIES_TABLE = "DROP TABLE IF EXISTS associated_ministries";
        static final String SQL_DELETE_MEASUREMENTS_TABLE = "DROP TABLE IF EXISTS measurements";
        static final String SQL_DELETE_MEASUREMENTS_DETAILS_TABLE = "DROP TABLE IF EXISTS measurement_details";
        static final String SQL_DELETE_MEASUREMENTS_BREAKDOWN_TABLE = "DROP TABLE IF EXISTS breakdown_data";
        static final String SQL_DELETE_MEASUREMENTS_SIX_MONTHS_TABLE = "DROP TABLE IF EXISTS six_month_amounts";
        static final String SQL_DELETE_MEASUREMENTS_SUB_MINISTRIES_TABLE = "DROP TABLE IF EXISTS sub_ministry_details";
        static final String SQL_DELETE_MEASUREMENTS_TEAM_MEMBERS_TABLE = "DROP TABLE IF EXISTS team_member_details";
        static final String SQL_DELETE_MEASUREMENTS_TYPE_IDS_TABLE = "DROP TABLE IF EXISTS measurement_type_ids";
    }

    public static final class Ministry extends Base implements MinistryId, Location {
        public static final String TABLE_NAME = "ministries";

        public static final String COLUMN_MIN_CODE = "min_code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MCCS = "mccs";
        public static final String COLUMN_LOCATION_ZOOM = "location_zoom";
        public static final String COLUMN_PARENT_MINISTRY_ID = "parent_ministry_id";

        public static final String[] PROJECTION_ALL =
                {COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_MIN_CODE, COLUMN_MCCS, COLUMN_LATITUDE, COLUMN_LONGITUDE,
                        COLUMN_LOCATION_ZOOM, COLUMN_PARENT_MINISTRY_ID, COLUMN_LAST_SYNCED};

        static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_MIN_CODE = COLUMN_MIN_CODE + " TEXT";
        private static final String SQL_COLUMN_MCCS = COLUMN_MCCS + " TEXT NOT NULL DEFAULT ''";
        private static final String SQL_COLUMN_LOCATION_ZOOM = COLUMN_LOCATION_ZOOM + " INTEGER";
        private static final String SQL_COLUMN_PARENT_MINISTRY_ID = COLUMN_PARENT_MINISTRY_ID + " TEXT";
        static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_MINISTRY_ID + ")";

        private static final String SQL_PREFIX = TABLE_NAME + ".";

        static final String SQL_WHERE_PRIMARY_KEY = SQL_WHERE_MINISTRY;
        static final String SQL_WHERE_PARENT = COLUMN_PARENT_MINISTRY_ID + " = ?";

        private static final String SQL_JOIN_ON_ASSIGNMENT =
                SQL_PREFIX + COLUMN_MINISTRY_ID + " = " + Assignment.SQL_PREFIX + Assignment.COLUMN_MINISTRY_ID;
        public static final Join<com.expidevapps.android.measurements.model.Ministry, com.expidevapps.android.measurements.model.Assignment>
                JOIN_ASSIGNMENT = Join.create(com.expidevapps.android.measurements.model.Ministry.class,
                                              com.expidevapps.android.measurements.model.Assignment.class)
                .on(SQL_JOIN_ON_ASSIGNMENT);

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME, SQL_COLUMN_MIN_CODE,
                        SQL_COLUMN_MCCS, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE, SQL_COLUMN_LOCATION_ZOOM,
                        SQL_COLUMN_PARENT_MINISTRY_ID, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class Assignment extends Base implements Guid, MinistryId, Mcc {
        public static final String TABLE_NAME = "assignments";

        public static final String COLUMN_ROLE = "team_role";
        public static final String COLUMN_ID = "assignment_id";

        static final String[] PROJECTION_ALL =
                {COLUMN_GUID, COLUMN_ID, COLUMN_ROLE, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_LAST_SYNCED};
        public static final String[] PROJECTION_API_GET_ASSIGNMENT = {COLUMN_ID, COLUMN_ROLE, COLUMN_LAST_SYNCED};
        public static final String[] PROJECTION_API_CREATE_ASSIGNMENT = PROJECTION_API_GET_ASSIGNMENT;

        private static final String SQL_COLUMN_ROLE = COLUMN_ROLE + " TEXT";
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
    }

    public static final class Church extends Base implements MinistryId, Location {
        public static final String TABLE_NAME = "churches";

        static final String COLUMN_ID = _ID;
        public static final String COLUMN_PARENT = "parent";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CONTACT_NAME = "contact_name";
        public static final String COLUMN_CONTACT_EMAIL = "contact_email";
        public static final String COLUMN_DEVELOPMENT = "development";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_SECURITY = "security";
        static final String COLUMN_NEW = "new";

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_PARENT, COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_CONTACT_NAME, COLUMN_CONTACT_EMAIL,
                        COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_DEVELOPMENT, COLUMN_SIZE, COLUMN_SECURITY, COLUMN_NEW,
                        COLUMN_DIRTY, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
        private static final String SQL_COLUMN_PARENT = COLUMN_PARENT + " INTEGER";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_CONTACT_NAME = COLUMN_CONTACT_NAME + " TEXT";
        private static final String SQL_COLUMN_CONTACT_EMAIL = COLUMN_CONTACT_EMAIL + " TEXT";
        private static final String SQL_COLUMN_DEVELOPMENT = COLUMN_DEVELOPMENT + " INTEGER";
        private static final String SQL_COLUMN_SIZE = COLUMN_SIZE + " INTEGER";
        private static final String SQL_COLUMN_SECURITY = COLUMN_SECURITY + " INTEGER";
        private static final String SQL_COLUMN_NEW = COLUMN_NEW + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        public static final String SQL_WHERE_NEW_OR_DIRTY = COLUMN_NEW + " = 1 OR " + SQL_WHERE_DIRTY;

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_PARENT, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME,
                        SQL_COLUMN_CONTACT_NAME, SQL_COLUMN_CONTACT_EMAIL, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE,
                        SQL_COLUMN_DEVELOPMENT, SQL_COLUMN_SIZE, SQL_COLUMN_SECURITY, SQL_COLUMN_NEW, SQL_COLUMN_DIRTY,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        static final String SQL_v22_ALTER_NEW = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_NEW;
        @Deprecated
        static final String SQL_v33_ALTER_PARENT = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_PARENT;
    }

    ///////////////////////////////////////////////////////////////
    //              Measurement Contracts                       //
    //////////////////////////////////////////////////////////////

    private interface MeasurementPermLink {
        String COLUMN_PERM_LINK_STUB = "perm_link";

        String SQL_COLUMN_PERM_LINK_STUB = COLUMN_PERM_LINK_STUB + " TEXT NOT NULL DEFAULT ''";

        String SQL_WHERE_PERM_LINK_STUB = COLUMN_PERM_LINK_STUB + " = ?";

        @Deprecated
        String SQL_V27_UPDATE_PERMLINKSTUB_BASE =
                " SET " + COLUMN_PERM_LINK_STUB + " = substr(" + COLUMN_PERM_LINK_STUB + ", 8) WHERE substr(" +
                        COLUMN_PERM_LINK_STUB + ", 1,7) = 'custom_'";
    }

    private interface Period {
        String COLUMN_PERIOD = "period";
        String SQL_COLUMN_PERIOD = COLUMN_PERIOD + " TEXT";
        String SQL_WHERE_PERIOD = COLUMN_PERIOD + " = ?";
    }

    public static final class MeasurementType extends Base implements MeasurementPermLink {
        static final String TABLE_NAME = "measurementTypes";

        public static final String COLUMN_PERSONAL_ID = "personalId";
        public static final String COLUMN_LOCAL_ID = "localId";
        public static final String COLUMN_TOTAL_ID = "totalId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_SECTION = "section";
        public static final String COLUMN_COLUMN = "column";
        public static final String COLUMN_CUSTOM = "custom";
        public static final String COLUMN_SORT_ORDER = "sort_order";

        static final String[] PROJECTION_ALL =
                {COLUMN_PERM_LINK_STUB, COLUMN_PERSONAL_ID, COLUMN_LOCAL_ID, COLUMN_TOTAL_ID, COLUMN_NAME,
                        COLUMN_DESCRIPTION, COLUMN_SECTION, COLUMN_SECTION, COLUMN_COLUMN, COLUMN_CUSTOM,
                        COLUMN_SORT_ORDER};

        private static final String SQL_COLUMN_PERSONAL_ID = COLUMN_PERSONAL_ID + " TEXT";
        private static final String SQL_COLUMN_LOCAL_ID = COLUMN_LOCAL_ID + " TEXT";
        private static final String SQL_COLUMN_TOTAL_ID = COLUMN_TOTAL_ID + " TEXT";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DESCRIPTION = COLUMN_DESCRIPTION + " TEXT";
        private static final String SQL_COLUMN_SECTION = COLUMN_SECTION + " TEXT";
        private static final String SQL_COLUMN_COLUMN = COLUMN_COLUMN + " TEXT";
        private static final String SQL_COLUMN_CUSTOM = COLUMN_CUSTOM + " INTEGER NOT NULL DEFAULT 0";
        private static final String SQL_COLUMN_SORT_ORDER = COLUMN_SORT_ORDER + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_PERM_LINK_STUB + ")";

        private static final String SQL_PREFIX = TABLE_NAME + ".";

        static final String SQL_WHERE_PRIMARY_KEY = SQL_WHERE_PERM_LINK_STUB;
        public static final String SQL_WHERE_COLUMN = SQL_PREFIX + COLUMN_COLUMN + " = ?";
        public static final String SQL_WHERE_VISIBLE =
                "(" + MeasurementVisibility.SQL_PREFIX + MeasurementVisibility.COLUMN_VISIBLE + " = 1 OR (" +
                        SQL_PREFIX + COLUMN_CUSTOM + " = 0 AND " + MeasurementVisibility.SQL_PREFIX +
                        MeasurementVisibility.COLUMN_VISIBLE + " IS NULL))";

        public static final String SQL_JOIN_ON_MINISTRY_MEASUREMENT =
                SQL_PREFIX + COLUMN_PERM_LINK_STUB + " = " + MinistryMeasurement.SQL_PREFIX +
                        MinistryMeasurement.COLUMN_PERM_LINK_STUB;
        public static final String SQL_JOIN_ON_PERSONAL_MEASUREMENT =
                SQL_PREFIX + COLUMN_PERM_LINK_STUB + " = " + PersonalMeasurement.SQL_PREFIX +
                        PersonalMeasurement.COLUMN_PERM_LINK_STUB;
        public static final String SQL_JOIN_ON_MEASUREMENT_VISIBILITY =
                SQL_PREFIX + COLUMN_PERM_LINK_STUB + " = " + MeasurementVisibility.SQL_PREFIX +
                        MeasurementVisibility.COLUMN_PERM_LINK_STUB;
        public static final Join<com.expidevapps.android.measurements.model.MeasurementType, com.expidevapps.android.measurements.model.MinistryMeasurement>
                JOIN_MINISTRY_MEASUREMENT =
                Join.create(com.expidevapps.android.measurements.model.MeasurementType.class,
                            com.expidevapps.android.measurements.model.MinistryMeasurement.class).on(
                        SQL_JOIN_ON_MINISTRY_MEASUREMENT);
        public static final Join<com.expidevapps.android.measurements.model.MeasurementType, com.expidevapps.android.measurements.model.PersonalMeasurement>
                JOIN_PERSONAL_MEASUREMENT =
                Join.create(com.expidevapps.android.measurements.model.MeasurementType.class,
                            com.expidevapps.android.measurements.model.PersonalMeasurement.class).on(
                        SQL_JOIN_ON_PERSONAL_MEASUREMENT);
        public static final Join<com.expidevapps.android.measurements.model.MeasurementType, MeasurementVisibility>
                JOIN_MEASUREMENT_VISIBILITY =
                Join.create(com.expidevapps.android.measurements.model.MeasurementType.class,
                            MeasurementVisibility.class).on(SQL_JOIN_ON_MEASUREMENT_VISIBILITY);

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_PERSONAL_ID, SQL_COLUMN_LOCAL_ID,
                        SQL_COLUMN_TOTAL_ID, SQL_COLUMN_NAME, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_DESCRIPTION,
                        SQL_COLUMN_SECTION, SQL_COLUMN_COLUMN, SQL_COLUMN_CUSTOM, SQL_COLUMN_SORT_ORDER,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        static final String SQL_V20_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_PERSONAL_ID, SQL_COLUMN_LOCAL_ID,
                        SQL_COLUMN_TOTAL_ID, SQL_COLUMN_NAME, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_DESCRIPTION,
                        SQL_COLUMN_SECTION, SQL_COLUMN_COLUMN, SQL_COLUMN_SORT_ORDER, SQL_COLUMN_LAST_SYNCED,
                        SQL_PRIMARY_KEY}) + ");";
        @Deprecated
        public static final String SQL_V23_PERMLINKSTUB =
                "UPDATE " + TABLE_NAME + " SET " + COLUMN_PERM_LINK_STUB + " = replace(" + COLUMN_PERM_LINK_STUB +
                        ", 'lmi_total_', '')";
        @Deprecated
        public static final String SQL_V27_UPDATE_PERMLINKSTUB = "UPDATE " + TABLE_NAME +
                SQL_V27_UPDATE_PERMLINKSTUB_BASE;
        @Deprecated
        static final String SQL_V32_ALTER_CUSTOM = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_CUSTOM;
    }

    public static final class MeasurementVisibility extends Base implements MinistryId, MeasurementPermLink {
        static final String TABLE_NAME = "measurementVisibility";

        public static final String COLUMN_VISIBLE = "visible";

        static final String SQL_COLUMN_VISIBLE = COLUMN_VISIBLE + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + TextUtils
                .join(",", new Object[] {COLUMN_MINISTRY_ID, COLUMN_PERM_LINK_STUB}) + ")";

        public static final String SQL_PREFIX = TABLE_NAME + ".";

        public static final String SQL_WHERE_MINISTRY = SQL_PREFIX + MinistryId.SQL_WHERE_MINISTRY;

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils.join(",", new Object[] {
                SQL_COLUMN_ROWID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_VISIBLE,
                SQL_PRIMARY_KEY}) + ");";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class MeasurementValue extends Base implements MinistryId, Mcc, MeasurementPermLink, Period {
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_DELTA = "delta";

        static final String SQL_COLUMN_VALUE = COLUMN_VALUE + " INTEGER";
        static final String SQL_COLUMN_DELTA = COLUMN_DELTA + " INTEGER";

        public static final String SQL_WHERE_DIRTY = COLUMN_DELTA + " != 0";
    }

    public static final class MinistryMeasurement extends MeasurementValue {
        static final String TABLE_NAME = "localMeasurements";

        static final String[] PROJECTION_ALL =
                {COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB, COLUMN_PERIOD, COLUMN_VALUE, COLUMN_DELTA,
                        COLUMN_LAST_SYNCED};

        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + TextUtils
                .join(",", new Object[] {COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB, COLUMN_PERIOD}) + ")";

        public static final String SQL_PREFIX = TABLE_NAME + ".";

        private static final String SQL_WHERE_MINISTRY = SQL_PREFIX + MinistryId.SQL_WHERE_MINISTRY;
        private static final String SQL_WHERE_MCC = SQL_PREFIX + Mcc.SQL_WHERE_MCC;
        private static final String SQL_WHERE_PERIOD = SQL_PREFIX + MeasurementValue.SQL_WHERE_PERIOD;
        public static final String SQL_WHERE_MINISTRY_MCC_PERIOD =
                SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_MCC + " AND " + SQL_WHERE_PERIOD;
        static final String SQL_WHERE_PRIMARY_KEY =
                SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_MCC + " AND " + SQL_WHERE_PERM_LINK_STUB + " AND " +
                        SQL_WHERE_PERIOD;

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID,
                        SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_MCC, SQL_COLUMN_PERIOD,
                        SQL_COLUMN_VALUE, SQL_COLUMN_DELTA, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        public static final String SQL_V21_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_PERM_LINK_STUB,
                        SQL_COLUMN_MCC, SQL_COLUMN_PERIOD, SQL_COLUMN_VALUE, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) +
                ");";
        @Deprecated
        public static final String SQL_V23_PERMLINKSTUB =
                "UPDATE " + TABLE_NAME + " SET " + COLUMN_PERM_LINK_STUB + " = replace(" + COLUMN_PERM_LINK_STUB +
                        ", 'lmi_total_', '')";
        @Deprecated
        public static final String SQL_V25_ALTER_DELTA =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_DELTA;
        @Deprecated
        public static final String SQL_V26_UPDATE_DELTA = "UPDATE " + TABLE_NAME + " SET " + COLUMN_DELTA + " = 0";
        @Deprecated
        public static final String SQL_V27_UPDATE_PERMLINKSTUB = "UPDATE " + TABLE_NAME +
                SQL_V27_UPDATE_PERMLINKSTUB_BASE;
    }

    public static final class PersonalMeasurement extends MeasurementValue implements Guid {
        static final String TABLE_NAME = "personalMeasurements";

        static final String[] PROJECTION_ALL =
                {COLUMN_GUID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB, COLUMN_PERIOD, COLUMN_VALUE,
                        COLUMN_DELTA, COLUMN_LAST_SYNCED};

        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + TextUtils
                .join(",", new Object[] {COLUMN_GUID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB,
                        COLUMN_PERIOD}) + ")";

        public static final String SQL_PREFIX = TABLE_NAME + ".";

        private static final String SQL_WHERE_GUID = SQL_PREFIX + Guid.SQL_WHERE_GUID;
        private static final String SQL_WHERE_MINISTRY = SQL_PREFIX + MinistryId.SQL_WHERE_MINISTRY;
        private static final String SQL_WHERE_MCC = SQL_PREFIX + Mcc.SQL_WHERE_MCC;
        private static final String SQL_WHERE_PERIOD = SQL_PREFIX + MeasurementValue.SQL_WHERE_PERIOD;
        public static final String SQL_WHERE_GUID_MINISTRY_MCC_PERIOD =
                SQL_WHERE_GUID + " AND " + SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_MCC + " AND " + SQL_WHERE_PERIOD;
        static final String SQL_WHERE_PRIMARY_KEY =
                SQL_WHERE_GUID + " AND " + SQL_WHERE_MINISTRY + " AND " + COLUMN_MCC + " = ? AND " +
                        SQL_WHERE_PERM_LINK_STUB + " AND " + COLUMN_PERIOD + " = ?";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_MINISTRY_ID,
                        SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_MCC, SQL_COLUMN_PERIOD, SQL_COLUMN_VALUE,
                        SQL_COLUMN_DELTA, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        public static final String SQL_V21_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_MINISTRY_ID,
                        SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_MCC, SQL_COLUMN_PERIOD, SQL_COLUMN_VALUE,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ");";
        @Deprecated
        public static final String SQL_V23_PERMLINKSTUB =
                "UPDATE " + TABLE_NAME + " SET " + COLUMN_PERM_LINK_STUB + " = replace(" + COLUMN_PERM_LINK_STUB +
                        ", 'lmi_total_', '')";
        @Deprecated
        public static final String SQL_V25_ALTER_DELTA =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_DELTA;
        @Deprecated
        public static final String SQL_V26_UPDATE_DELTA = "UPDATE " + TABLE_NAME + " SET " + COLUMN_DELTA + " = 0";
        @Deprecated
        public static final String SQL_V27_UPDATE_PERMLINKSTUB = "UPDATE " + TABLE_NAME +
                SQL_V27_UPDATE_PERMLINKSTUB_BASE;
    }

    public static final class MeasurementDetails extends Base
            implements Guid, MinistryId, Mcc, MeasurementPermLink, Period {
        static final String TABLE_NAME = "measurementDetails";

        public static final String COLUMN_JSON = "json";
        static final String COLUMN_VERSION = "jsonVersion";

        static final String[] PROJECTION_ALL =
                {COLUMN_GUID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB, COLUMN_PERIOD, COLUMN_JSON,
                        COLUMN_VERSION, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_JSON = COLUMN_JSON + " TEXT";
        private static final String SQL_COLUMN_VERSION = COLUMN_VERSION + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + TextUtils.join(",", new Object[] {COLUMN_GUID,
                COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB, COLUMN_PERIOD}) + ")";

        private static final String SQL_PREFIX = TABLE_NAME + ".";

        static final String SQL_WHERE_PRIMARY_KEY =
                SQL_WHERE_GUID + " AND " + SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_MCC + " AND " +
                        SQL_WHERE_PERM_LINK_STUB + " AND " + SQL_WHERE_PERIOD;

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_MINISTRY_ID,
                        SQL_COLUMN_MCC, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_PERIOD, SQL_COLUMN_JSON,
                        SQL_COLUMN_VERSION, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ");";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
