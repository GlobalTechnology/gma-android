package com.expidevapps.android.measurements.db;

import static org.ccci.gto.android.common.db.util.CursorUtils.getLong;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.MeasurementDetails;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.MeasurementTypeLocalization;
import com.expidevapps.android.measurements.model.MeasurementValue;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.MinistryMeasurement;
import com.expidevapps.android.measurements.model.PersonalMeasurement;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.model.UserPreference;
import com.google.common.base.Joiner;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Mapper;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.util.ArrayUtils;

import java.util.Collection;

public class GmaDao extends AbstractDao {
    private static final Mapper<Assignment> ASSIGNMENT_MAPPER = new AssignmentMapper();
    private static final Mapper<Ministry> MINISTRY_MAPPER = new MinistryMapper();
    private static final Mapper<MeasurementType> MEASUREMENT_TYPE_MAPPER = new MeasurementTypeMapper();
    private static final Mapper<MeasurementTypeLocalization> MEASUREMENT_TYPE_LOCALIZATION_MAPPER =
            new MeasurementTypeLocalizationMapper();
    private static final Mapper<MinistryMeasurement> MINISTRY_MEASUREMENT_MAPPER = new MinistryMeasurementMapper();
    private static final Mapper<PersonalMeasurement> PERSONAL_MEASUREMENT_MAPPER = new PersonalMeasurementMapper();
    private static final Mapper<MeasurementDetails> MEASUREMENT_DETAILS_MAPPER = new MeasurementDetailsMapper();
    private static final Mapper<Church> CHURCH_MAPPER = new ChurchMapper();
    private static final Mapper<Training> TRAINING_MAPPER = new TrainingMapper();
    private static final Mapper<Training.Completion> TRAINING_COMPLETION_MAPPER = new TrainingCompletionMapper();
    private static final Mapper<Story> STORY_MAPPER = new StoryMapper();
    private static final Mapper<UserPreference> PREFERENCE_MAPPER = new UserPreferenceMapper();

    private static final Object LOCK_INSTANCE = new Object();
    private static GmaDao INSTANCE;

    private GmaDao(@NonNull final Context context) {
        super(GmaDatabase.getInstance(context));
    }

    @NonNull
    public static GmaDao getInstance(@NonNull final Context context) {
        synchronized (LOCK_INSTANCE) {
            if (INSTANCE == null) {
                INSTANCE = new GmaDao(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    @NonNull
    @Override
    protected String getTable(@NonNull final Class<?> clazz)
    {
        if (Ministry.class.equals(clazz)) {
            return Contract.Ministry.TABLE_NAME;
        } else if (Assignment.class.equals(clazz)) {
            return Contract.Assignment.TABLE_NAME;
        } else if (MeasurementType.class.equals(clazz)) {
            return Contract.MeasurementType.TABLE_NAME;
        } else if (MeasurementTypeLocalization.class.equals(clazz)) {
            return Contract.MeasurementTypeLocalization.TABLE_NAME;
        } else if (Contract.MeasurementVisibility.class.equals(clazz)) {
            return Contract.MeasurementVisibility.TABLE_NAME;
        } else if (Contract.FavoriteMeasurement.class.equals(clazz)) {
            return Contract.FavoriteMeasurement.TABLE_NAME;
        } else if (MinistryMeasurement.class.equals(clazz)) {
            return Contract.MinistryMeasurement.TABLE_NAME;
        } else if (PersonalMeasurement.class.equals(clazz)) {
            return Contract.PersonalMeasurement.TABLE_NAME;
        } else if (MeasurementDetails.class.equals(clazz)) {
            return Contract.MeasurementDetails.TABLE_NAME;
        } else if(Church.class.equals(clazz)) {
            return Contract.Church.TABLE_NAME;
        } else if (Training.class.equals(clazz)) {
            return Contract.Training.TABLE_NAME;
        } else if (Training.Completion.class.equals(clazz)) {
            return Contract.Training.Completion.TABLE_NAME;
        } else if (Story.class.equals(clazz)) {
            return Contract.Story.TABLE_NAME;
        } else if (Contract.LastSync.class.equals(clazz)) {
            return Contract.LastSync.TABLE_NAME;
        } else if (UserPreference.class.equals(clazz)) {
            return Contract.UserPreference.TABLE_NAME;
        }

        return super.getTable(clazz);
    }

    @NonNull
    @Override
    public String[] getFullProjection(@NonNull final Class<?> clazz) {
        if (Ministry.class.equals(clazz)) {
            return Contract.Ministry.PROJECTION_ALL;
        } else if (Assignment.class.equals(clazz)) {
            return Contract.Assignment.PROJECTION_ALL;
        } else if (MeasurementType.class.equals(clazz)) {
            return Contract.MeasurementType.PROJECTION_ALL;
        } else if (MeasurementTypeLocalization.class.equals(clazz)) {
            return Contract.MeasurementTypeLocalization.PROJECTION_ALL;
        } else if (MinistryMeasurement.class.equals(clazz)) {
            return Contract.MinistryMeasurement.PROJECTION_ALL;
        } else if (PersonalMeasurement.class.equals(clazz)) {
            return Contract.PersonalMeasurement.PROJECTION_ALL;
        } else if (MeasurementDetails.class.equals(clazz)) {
            return Contract.MeasurementDetails.PROJECTION_ALL;
        } else if (Church.class.equals(clazz)) {
            return Contract.Church.PROJECTION_ALL;
        } else if (Training.class.equals(clazz)) {
            return Contract.Training.PROJECTION_ALL;
        } else if (Training.Completion.class.equals(clazz)) {
            return Contract.Training.Completion.PROJECTION_ALL;
        } else if (Story.class.equals(clazz)) {
            return Contract.Story.PROJECTION_ALL;
        } else if (UserPreference.class.equals(clazz)) {
            return Contract.UserPreference.PROJECTION_ALL;
        }

        return super.getFullProjection(clazz);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mapper<T> getMapper(@NonNull final Class<T> clazz)
    {
        if (Ministry.class.equals(clazz)) {
            return (Mapper<T>) MINISTRY_MAPPER;
        } else if (Assignment.class.equals(clazz)) {
            return (Mapper<T>) ASSIGNMENT_MAPPER;
        } else if (MeasurementType.class.equals(clazz)) {
            return (Mapper<T>) MEASUREMENT_TYPE_MAPPER;
        } else if (MeasurementTypeLocalization.class.equals(clazz)) {
            return (Mapper<T>) MEASUREMENT_TYPE_LOCALIZATION_MAPPER;
        } else if (MinistryMeasurement.class.equals(clazz)) {
            return (Mapper<T>) MINISTRY_MEASUREMENT_MAPPER;
        } else if (PersonalMeasurement.class.equals(clazz)) {
            return (Mapper<T>) PERSONAL_MEASUREMENT_MAPPER;
        } else if (MeasurementDetails.class.equals(clazz)) {
            return (Mapper<T>) MEASUREMENT_DETAILS_MAPPER;
        } else if(Church.class.equals(clazz)) {
            return (Mapper<T>) CHURCH_MAPPER;
        } else if (Training.class.equals(clazz)) {
            return (Mapper<T>) TRAINING_MAPPER;
        } else if (Training.Completion.class.equals(clazz)) {
            return (Mapper<T>) TRAINING_COMPLETION_MAPPER;
        } else if (Story.class.equals(clazz)) {
            return (Mapper<T>) STORY_MAPPER;
        } else if (UserPreference.class.equals(clazz)) {
            return (Mapper<T>) PREFERENCE_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhereRaw(@NonNull final Class<?> clazz,
                                                           @NonNull final Object... key) {
        final int keyLength;
        final String where;

        if (Ministry.class.equals(clazz)) {
            keyLength = 1;
            where = Contract.Ministry.SQL_WHERE_PRIMARY_KEY;
        } else if (Assignment.class.equals(clazz)) {
            keyLength = 2;
            where = Contract.Assignment.SQL_WHERE_PRIMARY_KEY;
        } else if(Church.class.equals(clazz)) {
            keyLength = 1;
            where = Contract.Church.SQL_WHERE_PRIMARY_KEY;
        } else if (Training.class.equals(clazz)) {
            keyLength = 1;
            where = Contract.Training.SQL_WHERE_PRIMARY_KEY;
        } else if (Training.Completion.class.equals(clazz)) {
            keyLength = 1;
            where = Contract.Training.Completion.SQL_WHERE_PRIMARY_KEY;
        } else if (MeasurementType.class.equals(clazz)) {
            keyLength = 1;
            where = Contract.MeasurementType.SQL_WHERE_PRIMARY_KEY;
        } else if (MeasurementTypeLocalization.class.equals(clazz)) {
            keyLength = 3;
            where = Contract.MeasurementTypeLocalization.SQL_WHERE_PRIMARY_KEY;
        } else if (MinistryMeasurement.class.equals(clazz)) {
            keyLength = 4;
            where = Contract.MinistryMeasurement.SQL_WHERE_PRIMARY_KEY;
        } else if (PersonalMeasurement.class.equals(clazz)) {
            keyLength = 5;
            where = Contract.PersonalMeasurement.SQL_WHERE_PRIMARY_KEY;
        } else if (MeasurementDetails.class.equals(clazz)) {
            keyLength = 5;
            where = Contract.MeasurementDetails.SQL_WHERE_PRIMARY_KEY;
        } else if (Story.class.equals(clazz)) {
            keyLength = 1;
            where = Contract.Story.SQL_WHERE_PRIMARY_KEY;
        } else if (UserPreference.class.equals(clazz)) {
            keyLength = 2;
            where = Contract.UserPreference.SQL_WHERE_PRIMARY_KEY;
        }
        else
        {
            return super.getPrimaryKeyWhereRaw(clazz, key);
        }

        // throw an error if the provided key is the wrong size
        if (key.length != keyLength) {
            throw new IllegalArgumentException("invalid key for " + clazz);
        }

        // return where clause pair
        return Pair.create(where, bindValues(key));
    }

    @NonNull
    @Override
    protected Expression getPrimaryKeyWhere(@NonNull final Object obj) {
        if(obj instanceof Ministry)
        {
            return getPrimaryKeyWhere(Ministry.class, ((Ministry) obj).getMinistryId());
        } else if (obj instanceof Assignment) {
            return getPrimaryKeyWhere(Assignment.class, ((Assignment) obj).getGuid(),
                                      ((Assignment) obj).getMinistryId());
        } else if (obj instanceof MeasurementType) {
            return getPrimaryKeyWhere(MeasurementType.class, ((MeasurementType) obj).getPermLinkStub());
        } else if (obj instanceof MeasurementTypeLocalization) {
            final MeasurementTypeLocalization localization = (MeasurementTypeLocalization) obj;
            return getPrimaryKeyWhere(MeasurementTypeLocalization.class, localization.getPermLinkStub(),
                                      localization.getMinistryId(), localization.getLocale());
        } else if (obj instanceof MinistryMeasurement) {
            final MinistryMeasurement measurement = (MinistryMeasurement) obj;
            return getPrimaryKeyWhere(MinistryMeasurement.class, measurement.getMinistryId(), measurement.getMcc(),
                                      measurement.getPermLinkStub(), measurement.getPeriod());
        } else if (obj instanceof PersonalMeasurement) {
            final PersonalMeasurement measurement = (PersonalMeasurement) obj;
            return getPrimaryKeyWhere(PersonalMeasurement.class, measurement.getGuid(), measurement.getMinistryId(),
                                      measurement.getMcc(), measurement.getPermLinkStub(), measurement.getPeriod());
        } else if (obj instanceof MeasurementDetails) {
            final MeasurementDetails details = (MeasurementDetails) obj;
            return getPrimaryKeyWhere(MeasurementDetails.class, details.getGuid(), details.getMinistryId(),
                                      details.getMcc(), details.getPermLink(), details.getPeriod());
        } else if (obj instanceof Church) {
            return getPrimaryKeyWhere(Church.class, ((Church) obj).getId());
        } else if (obj instanceof Training) {
            return getPrimaryKeyWhere(Training.class, ((Training) obj).getId());
        } else if (obj instanceof Training.Completion) {
            return getPrimaryKeyWhere(Training.Completion.class, ((Training.Completion) obj).getId());
        } else if (obj instanceof Story) {
            return getPrimaryKeyWhere(Story.class, ((Story) obj).getId());
        } else if (obj instanceof UserPreference) {
            final UserPreference pref = (UserPreference) obj;
            return getPrimaryKeyWhere(UserPreference.class, pref.getGuid(), pref.getName());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    private static final Joiner JOINER_KEY = Joiner.on(':');

    public long getLastSyncTime(@NonNull final Object... key) {
        final Cursor c =
                getCursor(Query.select(Contract.LastSync.class).projection(Contract.LastSync.COLUMN_LAST_SYNCED).where(
                        Contract.LastSync.SQL_WHERE_KEY, JOINER_KEY.join(key)));
        if (c.moveToFirst()) {
            return getLong(c, Contract.LastSync.COLUMN_LAST_SYNCED, 0);
        }
        return 0;
    }

    public void updateLastSyncTime(@NonNull final Object... key) {
        // update the last sync time, we can replace since this is just a keyed timestamp
        final ContentValues values = new ContentValues();
        values.put(Contract.LastSync.COLUMN_KEY, JOINER_KEY.join(key));
        values.put(Contract.LastSync.COLUMN_LAST_SYNCED, System.currentTimeMillis());
        getWritableDatabase().replace(getTable(Contract.LastSync.class), null, values);
    }

    public void updateMeasurementValueDelta(@NonNull final MeasurementValue value, final int change) {
        // short-circuit if the delta isn't actually changing
        if (change == 0) {
            return;
        }

        // create MeasurementValue if it doesn't exist
        insert(value, SQLiteDatabase.CONFLICT_IGNORE);

        // build update query
        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTable(value.getClass()))
                .append(" SET " + Contract.MeasurementValue.COLUMN_DELTA + " = ");
        if (value instanceof PersonalMeasurement) {
            sql.append("max(");
        }
        sql.append(Contract.MeasurementValue.COLUMN_DELTA + " + ?");
        if (value instanceof PersonalMeasurement) {
            sql.append(", 0 - " + Contract.MeasurementValue.COLUMN_VALUE + ")");
        }
        final Pair<String, String[]> where = compileExpression(getPrimaryKeyWhere(value));
        sql.append(" WHERE ").append(where.first);
        final String[] args = ArrayUtils.merge(String.class, bindValues(change), where.second);

        // perform update and sanitize PersonalMeasurements
        final SQLiteDatabase db = getWritableDatabase();
        final Transaction tx = new Transaction(db);
        try {
            tx.beginTransactionNonExclusive();
            db.execSQL(sql.toString(), args);
            tx.setTransactionSuccessful();
        } finally {
            tx.endTransaction();
        }
    }

    public void setMeasurementVisibility(@NonNull final String ministryId, @NonNull final Collection<String> show,
                                         @NonNull final Collection<String> hide) {
        final String table = getTable(Contract.MeasurementVisibility.class);
        final SQLiteDatabase db = getWritableDatabase();
        final Transaction tx = new Transaction(db);
        try {
            tx.beginTransactionNonExclusive();

            // clear out pre-existing visibility
            delete(Contract.MeasurementVisibility.class,
                   Contract.MeasurementVisibility.SQL_WHERE_MINISTRY.args(ministryId));

            // create base ContentValues
            final ContentValues values = new ContentValues();
            values.put(Contract.MeasurementVisibility.COLUMN_MINISTRY_ID, ministryId);

            // add all explicitly shown measurements
            values.put(Contract.MeasurementVisibility.COLUMN_VISIBLE, 1);
            for (final String permLink : show) {
                values.put(Contract.MeasurementVisibility.COLUMN_PERM_LINK_STUB, permLink);
                db.replaceOrThrow(table, null, values);
            }

            // add all explicitly hidden measurements
            values.put(Contract.MeasurementVisibility.COLUMN_VISIBLE, 0);
            for (final String permLink : hide) {
                values.put(Contract.MeasurementVisibility.COLUMN_PERM_LINK_STUB, permLink);
                db.replaceOrThrow(table, null, values);
            }

            tx.setSuccessful();
        } finally {
            tx.end();
        }
    }

    public void setFavoriteMeasurement(@NonNull final String guid, @NonNull final String ministryId,
                                       @NonNull final Mcc mcc, @NonNull final String permLink, final boolean favorite) {
        if (favorite) {
            final String table = getTable(Contract.FavoriteMeasurement.class);
            final ContentValues values = new ContentValues(5);
            values.put(Contract.FavoriteMeasurement.COLUMN_GUID, guid);
            values.put(Contract.FavoriteMeasurement.COLUMN_MINISTRY_ID, ministryId);
            values.put(Contract.FavoriteMeasurement.COLUMN_MCC, mcc.toString());
            values.put(Contract.FavoriteMeasurement.COLUMN_PERM_LINK_STUB, permLink);
            values.put(Contract.FavoriteMeasurement.COLUMN_FAVORITE, true);

            final SQLiteDatabase db = getWritableDatabase();
            final Transaction tx = new Transaction(db);
            try {
                tx.beginTransactionNonExclusive();
                db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                tx.setSuccessful();
            } finally {
                tx.end();
            }
        } else {
            delete(Contract.FavoriteMeasurement.class,
                   Contract.FavoriteMeasurement.SQL_WHERE_PRIMARY_KEY.args(guid, ministryId, mcc, permLink));
        }
    }
}
