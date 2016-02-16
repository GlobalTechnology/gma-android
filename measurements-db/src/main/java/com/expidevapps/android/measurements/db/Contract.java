package com.expidevapps.android.measurements.db;

import static org.ccci.gto.android.common.db.Expression.bind;
import static org.ccci.gto.android.common.db.Expression.constant;
import static org.ccci.gto.android.common.db.Expression.field;

import android.provider.BaseColumns;
import android.text.TextUtils;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Expression.Field;
import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.db.Table;

public class Contract {
    private Contract() {
    }

    public static abstract class Base implements BaseColumns {
        public static final String COLUMN_ROWID = _ID;
        public static final String COLUMN_NEW = "new";
        public static final String COLUMN_DIRTY = "dirtyData";
        public static final String COLUMN_DELETED = "deleted";
        public static final String COLUMN_LAST_SYNCED = "lastSynced";

        static final String SQL_COLUMN_ROWID = COLUMN_ROWID + " INTEGER PRIMARY KEY";
        static final String SQL_COLUMN_NEW = COLUMN_NEW + " INTEGER";
        static final String SQL_COLUMN_DIRTY = COLUMN_DIRTY + " TEXT";
        static final String SQL_COLUMN_DELETED = COLUMN_DELETED + " INTEGER";
        static final String SQL_COLUMN_LAST_SYNCED = COLUMN_LAST_SYNCED + " INTEGER";

        static final String SQL_WHERE_NEW = COLUMN_NEW + " = 1";
        static final String SQL_WHERE_DIRTY = COLUMN_DIRTY + " != ''";
        static final String SQL_WHERE_DELETED = COLUMN_DELETED + " = 1";
        static final String SQL_WHERE_NOT_DELETED = "(" + COLUMN_DELETED + " IS NULL OR " + COLUMN_DELETED + " != 1)";
    }

    interface Guid {
        String COLUMN_GUID = "guid";

        String SQL_COLUMN_GUID = COLUMN_GUID + " TEXT NOT NULL DEFAULT ''";

        String SQL_WHERE_GUID = COLUMN_GUID + " = ?";
    }

    public interface MinistryId {
        String COLUMN_MINISTRY_ID = "ministry_id";

        String SQL_COLUMN_MINISTRY_ID = COLUMN_MINISTRY_ID + " TEXT COLLATE NOCASE NOT NULL DEFAULT ''";

        String SQL_WHERE_MINISTRY = COLUMN_MINISTRY_ID + " = ?";
    }

    interface Mcc {
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
        public static final String COLUMN_CREATED_BY = "created_by";
        public static final String COLUMN_PARTICIPANTS = "participants";

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_DATE, COLUMN_TYPE, COLUMN_MCC, COLUMN_LATITUDE,
                        COLUMN_LONGITUDE, COLUMN_DIRTY, COLUMN_LAST_SYNCED, COLUMN_NEW, COLUMN_DELETED, COLUMN_CREATED_BY, COLUMN_PARTICIPANTS};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DATE = COLUMN_DATE + " TEXT";
        private static final String SQL_COLUMN_TYPE = COLUMN_TYPE + " TEXT";
        private static final String SQL_COLUMN_CREATED_BY = COLUMN_CREATED_BY + " TEXT";
        private static final String SQL_COLUMN_PARTICIPANTS = COLUMN_PARTICIPANTS + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        public static final String SQL_WHERE_NEW_DELETED_OR_DIRTY =
                SQL_WHERE_NEW + " OR " + SQL_WHERE_DELETED + " OR " + SQL_WHERE_DIRTY;

        public static final String SQL_WHERE_MINISTRY_MCC = SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_MCC;
        public static final String SQL_WHERE_MINISTRY_MCC_NOT_DELETED =
                SQL_WHERE_MINISTRY_MCC + " AND " + SQL_WHERE_NOT_DELETED;

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME, SQL_COLUMN_DATE,
                        SQL_COLUMN_TYPE, SQL_COLUMN_MCC, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE, SQL_COLUMN_CREATED_BY, SQL_COLUMN_PARTICIPANTS,
                        SQL_COLUMN_DIRTY, SQL_COLUMN_NEW, SQL_COLUMN_DELETED, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        static final String SQL_v35_ALTER_NEW = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_NEW;
        @Deprecated
        static final String SQL_v37_ALTER_DELETED = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_DELETED;
        @Deprecated
        static final String SQL_v39_ALTER_CREATED_BY = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_CREATED_BY;
        @Deprecated
        static final String SQL_v44_ALTER_PARTICIPANTS = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_PARTICIPANTS;

        public static final class Completion extends Base {
            public static final String TABLE_NAME = "training_completions";

            static final String COLUMN_ID = _ID;
            public static final String COLUMN_TRAINING_ID = "training_id";
            public static final String COLUMN_PHASE = "phase";
            public static final String COLUMN_NUMBER_COMPLETED = "number_completed";
            public static final String COLUMN_DATE = "date";
            public static final String COLUMN_DELETED = "deleted";

            static final String[] PROJECTION_ALL =
                    {COLUMN_ID, COLUMN_TRAINING_ID, COLUMN_PHASE, COLUMN_NUMBER_COMPLETED, COLUMN_DATE,
                            COLUMN_LAST_SYNCED, COLUMN_DIRTY, COLUMN_NEW, COLUMN_DELETED};

            private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
            private static final String SQL_COLUMN_TRAINING_ID = COLUMN_TRAINING_ID + " INTEGER";
            private static final String SQL_COLUMN_PHASE = COLUMN_PHASE + " INTEGER";
            private static final String SQL_COLUMN_NUMBER_COMPLETED = COLUMN_NUMBER_COMPLETED + " INTEGER";
            private static final String SQL_COLUMN_DATE = COLUMN_DATE + " TEXT";
            private static final String SQL_COLUMN_DELETED = COLUMN_DELETED + " INTEGER";

            private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

            static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
            public static final String SQL_WHERE_TRAINING_ID = COLUMN_TRAINING_ID + " = ?";
            public static final String SQL_WHERE_NEW_DELETED_OR_DIRTY =
                    SQL_WHERE_NEW + " OR " + SQL_WHERE_DELETED + " OR " + SQL_WHERE_DIRTY;

            public static final String SQL_WHERE_NOT_DELETED_AND_TRAINING_ID =
                    "((" + SQL_WHERE_NOT_DELETED + ") AND " + COLUMN_TRAINING_ID + "= ?)";

            public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                    .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_TRAINING_ID, SQL_COLUMN_PHASE, SQL_COLUMN_DIRTY, SQL_COLUMN_NEW,
                            SQL_COLUMN_DELETED, SQL_COLUMN_NUMBER_COMPLETED, SQL_COLUMN_DATE, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) +
                    ")";
            public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

            @Deprecated
            static final String SQL_v40_ALTER_DIRTY = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_DIRTY;

            @Deprecated
            static final String SQL_v41_ALTER_NEW = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_NEW;

            @Deprecated
            static final String SQL_v42_ALTER_DELETED = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_DELETED;
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
        private static final Table<com.expidevapps.android.measurements.model.Ministry> TABLE =
                Table.forClass(com.expidevapps.android.measurements.model.Ministry.class);

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

        public static final Join<com.expidevapps.android.measurements.model.Ministry, com.expidevapps.android.measurements.model.Assignment>
                JOIN_ASSIGNMENT = Join.create(TABLE, Assignment.TABLE)
                .on(field(TABLE, COLUMN_MINISTRY_ID).eq(field(Assignment.TABLE, Assignment.COLUMN_MINISTRY_ID)));

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME, SQL_COLUMN_MIN_CODE,
                        SQL_COLUMN_MCCS, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE, SQL_COLUMN_LOCATION_ZOOM,
                        SQL_COLUMN_PARENT_MINISTRY_ID, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class Assignment extends Base implements Guid, MinistryId, Mcc {
        public static final String TABLE_NAME = "assignments";
        private static final Table<com.expidevapps.android.measurements.model.Assignment> TABLE =
                Table.forClass(com.expidevapps.android.measurements.model.Assignment.class);

        public static final String COLUMN_ROLE = "team_role";
        public static final String COLUMN_ID = "assignment_id";
        public static final String COLUMN_PERSON_ID = "person_id";

        static final String[] PROJECTION_ALL =
                {COLUMN_GUID, COLUMN_PERSON_ID, COLUMN_ID, COLUMN_ROLE, COLUMN_MINISTRY_ID, COLUMN_MCC,
                        COLUMN_LAST_SYNCED};
        public static final String[] PROJECTION_API_GET_ASSIGNMENT =
                {COLUMN_ID, COLUMN_ROLE, COLUMN_PERSON_ID, COLUMN_LAST_SYNCED};
        public static final String[] PROJECTION_API_CREATE_ASSIGNMENT = PROJECTION_API_GET_ASSIGNMENT;

        private static final String SQL_COLUMN_ROLE = COLUMN_ROLE + " TEXT";
        private static final String SQL_COLUMN_ID = COLUMN_ID + " TEXT";
        private static final String SQL_COLUMN_PERSON_ID = COLUMN_PERSON_ID + " TEXT";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_GUID + "," + COLUMN_MINISTRY_ID + ")";
        private static final String SQL_FOREIGN_KEY_MINISTRIES =
                "FOREIGN KEY(" + COLUMN_MINISTRY_ID + ") REFERENCES " + Ministry.TABLE_NAME + "(" +
                        Ministry.COLUMN_MINISTRY_ID + ")";

        private static final String SQL_PREFIX = TABLE_NAME + ".";

        public static final String SQL_WHERE_GUID = SQL_PREFIX + COLUMN_GUID + " = ?";
        static final String SQL_WHERE_PRIMARY_KEY = SQL_WHERE_GUID + " AND " + SQL_WHERE_MINISTRY;

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new String[] {SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_ID,
                        SQL_COLUMN_ROLE, SQL_COLUMN_MCC, SQL_COLUMN_PERSON_ID, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY,
                        SQL_FOREIGN_KEY_MINISTRIES}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        static final String SQL_V46_ALTER_PERSON_ID =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_PERSON_ID;
    }

    public static final class Church extends Base implements MinistryId, Location {
        public static final String TABLE_NAME = "churches";

        static final String COLUMN_ID = _ID;
        public static final String COLUMN_PARENT = "parent";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CONTACT_NAME = "contact_name";
        public static final String COLUMN_CONTACT_EMAIL = "contact_email";
        public static final String COLUMN_CONTACT_MOBILE = "contact_mobile";
        public static final String COLUMN_JESUS_FILM_ACTIVITY = "jf_contrib";
        public static final String COLUMN_DEVELOPMENT = "development";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_SECURITY = "security";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_CREATED_BY = "created_by";

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_PARENT, COLUMN_MINISTRY_ID, COLUMN_NAME, COLUMN_CONTACT_NAME, COLUMN_CONTACT_EMAIL,
                        COLUMN_CONTACT_MOBILE, COLUMN_JESUS_FILM_ACTIVITY, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_DEVELOPMENT,
                        COLUMN_SIZE, COLUMN_SECURITY, COLUMN_END_DATE, COLUMN_NEW, COLUMN_DIRTY, COLUMN_CREATED_BY, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
        private static final String SQL_COLUMN_PARENT = COLUMN_PARENT + " INTEGER";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_CONTACT_NAME = COLUMN_CONTACT_NAME + " TEXT";
        private static final String SQL_COLUMN_CONTACT_EMAIL = COLUMN_CONTACT_EMAIL + " TEXT";
        private static final String SQL_COLUMN_CONTACT_MOBILE = COLUMN_CONTACT_MOBILE + " TEXT";
        private static final String SQL_COLUMN_JESUS_FILM_ACTIVITY = COLUMN_JESUS_FILM_ACTIVITY + " INTEGER";
        private static final String SQL_COLUMN_DEVELOPMENT = COLUMN_DEVELOPMENT + " INTEGER";
        private static final String SQL_COLUMN_SIZE = COLUMN_SIZE + " INTEGER";
        private static final String SQL_COLUMN_SECURITY = COLUMN_SECURITY + " INTEGER";
        private static final String SQL_COLUMN_END_DATE = COLUMN_END_DATE + " TEXT";
        private static final String SQL_COLUMN_CREATED_BY = COLUMN_CREATED_BY + " TEXT";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        public static final String SQL_WHERE_NOT_ENDED =
                "(" + COLUMN_END_DATE + " IS NULL OR " + COLUMN_END_DATE + " >= ?)";
        public static final String SQL_WHERE_MINISTRY_AND_NOT_ENDED =
                SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_NOT_ENDED;
        public static final String SQL_WHERE_NEW_OR_DIRTY = SQL_WHERE_NEW + " OR " + SQL_WHERE_DIRTY;

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_PARENT, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_NAME,
                        SQL_COLUMN_CONTACT_NAME, SQL_COLUMN_CONTACT_EMAIL, SQL_COLUMN_CONTACT_MOBILE,SQL_COLUMN_JESUS_FILM_ACTIVITY,
                        SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE, SQL_COLUMN_DEVELOPMENT, SQL_COLUMN_SIZE,
                        SQL_COLUMN_SECURITY, SQL_COLUMN_END_DATE, SQL_COLUMN_CREATED_BY, SQL_COLUMN_NEW, SQL_COLUMN_DIRTY,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        static final String SQL_v22_ALTER_NEW = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_NEW;
        @Deprecated
        static final String SQL_v33_ALTER_PARENT = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_PARENT;
        @Deprecated
        static final String SQL_v35_ALTER_MOBILE = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_CONTACT_MOBILE;


        @Deprecated
        static final String SQL_v36_ALTER_END_DATE = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_END_DATE;

        @Deprecated
        static final String SQL_v38_ALTER_CREATED_BY = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN" +
                " " + SQL_COLUMN_CREATED_BY;

        @Deprecated
        static final String SQL_v43_ALTER_JESUS_FILM_ACTIVITY = "ALTER TABLE " + TABLE_NAME + " " +
                "ADD COLUMN" +
                " " + SQL_COLUMN_JESUS_FILM_ACTIVITY;
    }

    public static final class Story extends Base implements MinistryId, Mcc, Location {
        private static final Table<com.expidevapps.android.measurements.model.Story> TABLE =
                Table.forClass(com.expidevapps.android.measurements.model.Story.class);
        static final String TABLE_NAME = "stories";

        public static final String COLUMN_ID = _ID;
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_IMAGE = "imageUrl";
        public static final String COLUMN_PENDING_IMAGE = "pendingImage";
        public static final String COLUMN_PRIVACY = "privacy";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_CREATED_BY = "createdBy";

        public static final Field FIELD_MINISTRY_ID = field(TABLE, COLUMN_MINISTRY_ID);
        public static final Field FIELD_PRIVACY = field(TABLE, COLUMN_PRIVACY);
        public static final Field FIELD_STATE = field(TABLE, COLUMN_STATE);
        public static final Field FIELD_PENDING_IMAGE = field(TABLE, COLUMN_PENDING_IMAGE);
        public static final Field FIELD_NEW = field(TABLE, COLUMN_NEW);
        public static final Field FIELD_DIRTY = field(TABLE, COLUMN_DIRTY);

        static final String[] PROJECTION_ALL =
                {COLUMN_ID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_IMAGE, COLUMN_LATITUDE,
                        COLUMN_LONGITUDE, COLUMN_PRIVACY, COLUMN_STATE, COLUMN_CREATED, COLUMN_CREATED_BY,
                        COLUMN_PENDING_IMAGE, COLUMN_NEW, COLUMN_DIRTY};

        private static final String SQL_COLUMN_ID = COLUMN_ID + " INTEGER";
        private static final String SQL_COLUMN_TITLE = COLUMN_TITLE + " TEXT NOT NULL";
        private static final String SQL_COLUMN_CONTENT = COLUMN_CONTENT + " TEXT NOT NULL";
        private static final String SQL_COLUMN_IMAGE = COLUMN_IMAGE + " TEXT";
        private static final String SQL_COLUMN_PENDING_IMAGE = COLUMN_PENDING_IMAGE + " TEXT";
        private static final String SQL_COLUMN_PRIVACY = COLUMN_PRIVACY + " TEXT NOT NULL";
        private static final String SQL_COLUMN_STATE = COLUMN_STATE + " TEXT NOT NULL";
        private static final String SQL_COLUMN_CREATED = COLUMN_CREATED + " TEXT";
        private static final String SQL_COLUMN_CREATED_BY = COLUMN_CREATED_BY + " TEXT";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_ID + ")";

        static final String SQL_WHERE_PRIMARY_KEY = COLUMN_ID + " = ?";
        public static final Expression SQL_WHERE_HAS_PENDING_IMAGE = FIELD_PENDING_IMAGE.isNot(Expression.NULL);
        public static final Expression SQL_WHERE_NEW = FIELD_NEW.eq(1);
        public static final Expression SQL_WHERE_DIRTY = FIELD_DIRTY.ne(constant(""));

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_MCC, SQL_COLUMN_TITLE,
                        SQL_COLUMN_CONTENT, SQL_COLUMN_IMAGE, SQL_COLUMN_PENDING_IMAGE, SQL_COLUMN_LATITUDE,
                        SQL_COLUMN_LONGITUDE, SQL_COLUMN_PRIVACY, SQL_COLUMN_STATE, SQL_COLUMN_CREATED,
                        SQL_COLUMN_CREATED_BY, SQL_COLUMN_NEW, SQL_COLUMN_DIRTY, SQL_COLUMN_LAST_SYNCED,
                        SQL_PRIMARY_KEY}) + ")";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        static final String SQL_V54_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_MCC, SQL_COLUMN_TITLE,
                        SQL_COLUMN_CONTENT, SQL_COLUMN_LATITUDE, SQL_COLUMN_LONGITUDE, SQL_COLUMN_PRIVACY,
                        SQL_COLUMN_STATE, SQL_COLUMN_CREATED, SQL_COLUMN_CREATED_BY, SQL_COLUMN_NEW, SQL_COLUMN_DIRTY,
                        SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        @Deprecated
        static final String SQL_V55_ALTER_IMAGE = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_IMAGE;
        @Deprecated
        static final String SQL_V56_ALTER_PENDING_IMAGE =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_PENDING_IMAGE;
    }

    public static final class UserPreference extends Base implements Guid {
        public static final String TABLE_NAME = "userPreferences";

        static final String COLUMN_NAME = "name";
        public static final String COLUMN_VALUE = "value";

        static final String[] PROJECTION_ALL =
                {COLUMN_ROWID, COLUMN_GUID, COLUMN_NAME, COLUMN_VALUE, COLUMN_NEW, COLUMN_DIRTY};

        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_VALUE = COLUMN_VALUE + " TEXT";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_GUID + "," + COLUMN_NAME + ")";

        private static final String SQL_WHERE_NAME = COLUMN_NAME + " = ?";
        static final String SQL_WHERE_PRIMARY_KEY = SQL_WHERE_GUID + " AND " + SQL_WHERE_NAME;
        public static final String SQL_WHERE_GUID_AND_NEW_OR_DIRTY =
                SQL_WHERE_GUID + " AND (" + SQL_WHERE_NEW + " OR " + SQL_WHERE_DIRTY + ")";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils
                .join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_NAME, SQL_COLUMN_VALUE,
                        COLUMN_NEW, COLUMN_DIRTY, SQL_PRIMARY_KEY}) + ")";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    ///////////////////////////////////////////////////////////////
    //              Measurement Contracts                       //
    //////////////////////////////////////////////////////////////

    public interface MeasurementPermLink {
        String COLUMN_PERM_LINK_STUB = "perm_link";

        String SQL_COLUMN_PERM_LINK_STUB = COLUMN_PERM_LINK_STUB + " TEXT NOT NULL DEFAULT ''";

        String SQL_WHERE_PERM_LINK_STUB = COLUMN_PERM_LINK_STUB + " = ?";

        @Deprecated
        String SQL_V27_UPDATE_PERMLINKSTUB_BASE =
                " SET " + COLUMN_PERM_LINK_STUB + " = substr(" + COLUMN_PERM_LINK_STUB + ", 8) WHERE substr(" +
                        COLUMN_PERM_LINK_STUB + ", 1,7) = 'custom_'";
    }

    interface Period {
        String COLUMN_PERIOD = "period";
        String SQL_COLUMN_PERIOD = COLUMN_PERIOD + " TEXT";
        String SQL_WHERE_PERIOD = COLUMN_PERIOD + " = ?";
    }

    public static final class MeasurementType extends Base implements MeasurementPermLink {
        static final String TABLE_NAME = "measurementTypes";
        private static final Table<com.expidevapps.android.measurements.model.MeasurementType> TABLE =
                Table.forClass(com.expidevapps.android.measurements.model.MeasurementType.class);

        public static final String COLUMN_PERSONAL_ID = "personalId";
        public static final String COLUMN_LOCAL_ID = "localId";
        public static final String COLUMN_TOTAL_ID = "totalId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_SECTION = "section";
        public static final String COLUMN_COLUMN = "column";
        public static final String COLUMN_CUSTOM = "custom";
        public static final String COLUMN_SORT_ORDER = "sort_order";
        public static final String COLUMN_SUPPORTED_STAFF_ONLY = "supported_staff_only";
        public static final String COLUMN_LEADER_ONLY = "leader_only";

        private static final Field FIELD_PERM_LINK_STUB = field(TABLE, COLUMN_PERM_LINK_STUB);

        static final String[] PROJECTION_ALL =
                {COLUMN_PERM_LINK_STUB, COLUMN_PERSONAL_ID, COLUMN_LOCAL_ID, COLUMN_TOTAL_ID, COLUMN_NAME,
                        COLUMN_DESCRIPTION, COLUMN_SECTION, COLUMN_SECTION, COLUMN_COLUMN, COLUMN_CUSTOM,
                        COLUMN_SORT_ORDER, COLUMN_SUPPORTED_STAFF_ONLY, COLUMN_LEADER_ONLY};

        private static final String SQL_COLUMN_PERSONAL_ID = COLUMN_PERSONAL_ID + " TEXT";
        private static final String SQL_COLUMN_LOCAL_ID = COLUMN_LOCAL_ID + " TEXT";
        private static final String SQL_COLUMN_TOTAL_ID = COLUMN_TOTAL_ID + " TEXT";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DESCRIPTION = COLUMN_DESCRIPTION + " TEXT";
        private static final String SQL_COLUMN_SECTION = COLUMN_SECTION + " TEXT";
        private static final String SQL_COLUMN_COLUMN = COLUMN_COLUMN + " TEXT";
        private static final String SQL_COLUMN_CUSTOM = COLUMN_CUSTOM + " INTEGER NOT NULL DEFAULT 0";
        private static final String SQL_COLUMN_SORT_ORDER = COLUMN_SORT_ORDER + " INTEGER";
        private static final String SQL_COLUMN_SUPPORTED_STAFF_ONLY = COLUMN_SUPPORTED_STAFF_ONLY + " INTEGER";
        private static final String SQL_COLUMN_LEADER_ONLY = COLUMN_LEADER_ONLY + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + COLUMN_PERM_LINK_STUB + ")";

        public static final String SQL_PREFIX = TABLE_NAME + ".";

        public static final String SQL_WHERE_PRIMARY_KEY = SQL_WHERE_PERM_LINK_STUB;
        public static final String SQL_WHERE_COLUMN = SQL_PREFIX + COLUMN_COLUMN + " = ?";
        public static final String SQL_WHERE_NOT_LEADER_ONLY = SQL_PREFIX + COLUMN_LEADER_ONLY + " != 1";
        public static final Expression SQL_WHERE_NOT_SUPPORTED_STAFF = field(TABLE, COLUMN_SUPPORTED_STAFF_ONLY).ne(1);

        public static final Expression SQL_WHERE_VISIBLE = MeasurementVisibility.FIELD_VISIBLE.eq(1)
                .or(MeasurementVisibility.FIELD_VISIBLE.is(Expression.NULL).and(field(TABLE, COLUMN_CUSTOM).eq(0)));

        public static final Join<com.expidevapps.android.measurements.model.MeasurementType, com.expidevapps.android.measurements.model.MinistryMeasurement>
                JOIN_MINISTRY_MEASUREMENT = Join.create(TABLE, MinistryMeasurement.TABLE)
                .on(FIELD_PERM_LINK_STUB.eq(MinistryMeasurement.FIELD_PERM_LINK_STUB));
        public static final Join<com.expidevapps.android.measurements.model.MeasurementType, com.expidevapps.android.measurements.model.PersonalMeasurement>
                JOIN_PERSONAL_MEASUREMENT = Join.create(MeasurementType.TABLE, PersonalMeasurement.TABLE)
                .on(FIELD_PERM_LINK_STUB.eq(PersonalMeasurement.FIELD_PERM_LINK_STUB));
        public static final Join<com.expidevapps.android.measurements.model.MeasurementType, MeasurementVisibility>
                JOIN_MEASUREMENT_VISIBILITY = Join.create(TABLE, MeasurementVisibility.TABLE)
                .on(FIELD_PERM_LINK_STUB.eq(MeasurementVisibility.FIELD_PERM_LINK_STUB));
        public static final Join<com.expidevapps.android.measurements.model.MeasurementType, FavoriteMeasurement>
                JOIN_FAVORITE_MEASUREMENT = Join.create(TABLE, FavoriteMeasurement.TABLE)
                .on(FIELD_PERM_LINK_STUB.eq(FavoriteMeasurement.FIELD_PERM_LINK_STUB));

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_PERSONAL_ID, SQL_COLUMN_LOCAL_ID,
                        SQL_COLUMN_TOTAL_ID, SQL_COLUMN_NAME, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_DESCRIPTION,
                        SQL_COLUMN_SECTION, SQL_COLUMN_COLUMN, SQL_COLUMN_CUSTOM, SQL_COLUMN_SORT_ORDER, SQL_COLUMN_SUPPORTED_STAFF_ONLY,
                        SQL_COLUMN_LEADER_ONLY, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ");";
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
        @Deprecated
        static final String SQL_V47_ALTER_SUPPORTED_STAFF_ONLY = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_SUPPORTED_STAFF_ONLY;
        @Deprecated
        static final String SQL_V48_ALTER_LEADER_ONLY = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_LEADER_ONLY;
    }

    public static final class MeasurementTypeLocalization extends Base implements MinistryId, MeasurementPermLink {
        static final String TABLE_NAME = "measurementTypeLocalizations";

        public static final String COLUMN_LOCALE = "locale";
        public static final String COLUMN_NAME = "name";
        static final String COLUMN_DESCRIPTION = "description";

        static final String[] PROJECTION_ALL =
                {COLUMN_PERM_LINK_STUB, COLUMN_MINISTRY_ID, COLUMN_LOCALE, COLUMN_NAME, COLUMN_DESCRIPTION};

        private static final String SQL_COLUMN_LOCALE = COLUMN_LOCALE + " TEXT";
        private static final String SQL_COLUMN_NAME = COLUMN_NAME + " TEXT";
        private static final String SQL_COLUMN_DESCRIPTION = COLUMN_DESCRIPTION + " TEXT";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" +
                TextUtils.join(",", new Object[] {COLUMN_PERM_LINK_STUB, COLUMN_MINISTRY_ID, COLUMN_LOCALE}) + ")";

        private static final String SQL_PREFIX = TABLE_NAME + ".";

        private static final String SQL_WHERE_LOCALE = COLUMN_LOCALE + " = ?";
        static final String SQL_WHERE_PRIMARY_KEY =
                SQL_WHERE_PERM_LINK_STUB + " AND " + SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_LOCALE;

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_MINISTRY_ID,
                        SQL_COLUMN_LOCALE, SQL_COLUMN_NAME, SQL_COLUMN_DESCRIPTION, SQL_PRIMARY_KEY}) + ");";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        static final String SQL_V45_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_MINISTRY_ID,
                        SQL_COLUMN_LOCALE, SQL_COLUMN_NAME, SQL_COLUMN_DESCRIPTION, SQL_PRIMARY_KEY}) + ");";
    }

    public static final class FavoriteMeasurement extends Base implements Guid, MinistryId, Mcc, MeasurementPermLink {
        static final String TABLE_NAME = "favoriteMeasurements";
        static final Table<FavoriteMeasurement> TABLE = Table.forClass(FavoriteMeasurement.class);

        public static final String COLUMN_FAVORITE = "favorite";

        public static final Field FIELD_GUID = field(TABLE, COLUMN_GUID);
        public static final Field FIELD_MINISTRY_ID = field(TABLE, COLUMN_MINISTRY_ID);
        public static final Field FIELD_MCC = field(TABLE, COLUMN_MCC);
        static final Field FIELD_PERM_LINK_STUB = field(TABLE, COLUMN_PERM_LINK_STUB);
        public static final Field FIELD_FAVORITE = field(TABLE, COLUMN_FAVORITE);

        private static final String SQL_COLUMN_FAVORITE = COLUMN_FAVORITE + " INTEGER";

        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + TextUtils.join(",", new Object[] {COLUMN_GUID,
                COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB}) + ")";

        public static final String SQL_PREFIX = TABLE_NAME + ".";

        public static final Expression SQL_WHERE_GUID_MINISTRY_MCC =
                FIELD_GUID.eq(bind()).and(FIELD_MINISTRY_ID.eq(bind())).and(FIELD_MCC.eq(bind()));
        static final Expression SQL_WHERE_PRIMARY_KEY = FIELD_GUID.eq(bind()).and(FIELD_MINISTRY_ID.eq(bind()))
                .and(FIELD_MCC.eq(bind())).and(FIELD_PERM_LINK_STUB.eq(bind()));

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + TextUtils.join(",", new Object[] {
                SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_MCC, SQL_COLUMN_PERM_LINK_STUB,
                SQL_COLUMN_FAVORITE, SQL_PRIMARY_KEY}) + ");";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class MeasurementVisibility extends Base implements MinistryId, MeasurementPermLink {
        static final String TABLE_NAME = "measurementVisibility";
        static final Table<MeasurementVisibility> TABLE = Table.forClass(MeasurementVisibility.class);

        public static final String COLUMN_VISIBLE = "visible";

        private static final Field FIELD_MINISTRY_ID = field(TABLE, COLUMN_MINISTRY_ID);
        private static final Field FIELD_PERM_LINK_STUB = field(TABLE, COLUMN_PERM_LINK_STUB);
        private static final Field FIELD_VISIBLE = field(TABLE, COLUMN_VISIBLE);

        static final String SQL_COLUMN_VISIBLE = COLUMN_VISIBLE + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + TextUtils
                .join(",", new Object[] {COLUMN_MINISTRY_ID, COLUMN_PERM_LINK_STUB}) + ")";

        public static final String SQL_PREFIX = TABLE_NAME + ".";

        public static final Expression SQL_WHERE_MINISTRY = FIELD_MINISTRY_ID.eq(bind());

        static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_MINISTRY_ID, SQL_COLUMN_PERM_LINK_STUB,
                        SQL_COLUMN_VISIBLE, SQL_PRIMARY_KEY}) + ");";
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
        private static final Table<com.expidevapps.android.measurements.model.MinistryMeasurement> TABLE =
                Table.forClass(com.expidevapps.android.measurements.model.MinistryMeasurement.class);

        private static final Field FIELD_PERM_LINK_STUB = field(TABLE, COLUMN_PERM_LINK_STUB);

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
        private static final Table<com.expidevapps.android.measurements.model.PersonalMeasurement> TABLE =
                Table.forClass(com.expidevapps.android.measurements.model.PersonalMeasurement.class);

        private static final Field FIELD_PERM_LINK_STUB = field(TABLE, COLUMN_PERM_LINK_STUB);

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
        static final String COLUMN_SOURCE = "source";
        static final String COLUMN_VERSION = "jsonVersion";

        static final String[] PROJECTION_ALL =
                {COLUMN_GUID, COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB, COLUMN_PERIOD, COLUMN_SOURCE,
                        COLUMN_JSON, COLUMN_VERSION, COLUMN_LAST_SYNCED};

        private static final String SQL_COLUMN_SOURCE = COLUMN_SOURCE + " TEXT";
        private static final String SQL_COLUMN_JSON = COLUMN_JSON + " TEXT";
        private static final String SQL_COLUMN_VERSION = COLUMN_VERSION + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "UNIQUE(" + TextUtils.join(",", new Object[] {COLUMN_GUID,
                COLUMN_MINISTRY_ID, COLUMN_MCC, COLUMN_PERM_LINK_STUB, COLUMN_PERIOD}) + ")";

        static final String SQL_WHERE_PRIMARY_KEY =
                SQL_WHERE_GUID + " AND " + SQL_WHERE_MINISTRY + " AND " + SQL_WHERE_MCC + " AND " +
                        SQL_WHERE_PERM_LINK_STUB + " AND " + SQL_WHERE_PERIOD;

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_MINISTRY_ID,
                        SQL_COLUMN_MCC, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_PERIOD, SQL_COLUMN_SOURCE,
                        SQL_COLUMN_JSON, SQL_COLUMN_VERSION, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
         static final String SQL_V28_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                TextUtils.join(",", new Object[] {SQL_COLUMN_ROWID, SQL_COLUMN_GUID, SQL_COLUMN_MINISTRY_ID,
                        SQL_COLUMN_MCC, SQL_COLUMN_PERM_LINK_STUB, SQL_COLUMN_PERIOD, SQL_COLUMN_JSON,
                        SQL_COLUMN_VERSION, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY}) + ")";
        @Deprecated
         static final String SQL_V53_ALTER_SOURCE =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_SOURCE;
        @Deprecated
         static final String SQL_V53_UPDATE_SOURCE =
                "UPDATE " + TABLE_NAME + " SET " + COLUMN_SOURCE + " = 'gma-app'";
    }
}
