package com.expidev.gcmapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.MeasurementDetails;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.MeasurementType;
import com.expidev.gcmapp.model.measurement.MeasurementValue;
import com.expidev.gcmapp.model.measurement.MinistryMeasurement;
import com.expidev.gcmapp.model.measurement.PersonalMeasurement;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;
import org.ccci.gto.android.common.util.ArrayUtils;

public class GmaDao extends AbstractDao
{
    private static final Object instanceLock = new Object();
    private static GmaDao instance;

    private static final Mapper<Assignment> ASSIGNMENT_MAPPER = new AssignmentMapper();
    private static final Mapper<Ministry> MINISTRY_MAPPER = new MinistryMapper();
    private static final Mapper<MeasurementType> MEASUREMENT_TYPE_MAPPER = new MeasurementTypeMapper();
    private static final Mapper<MinistryMeasurement> MINISTRY_MEASUREMENT_MAPPER = new MinistryMeasurementMapper();
    private static final Mapper<PersonalMeasurement> PERSONAL_MEASUREMENT_MAPPER = new PersonalMeasurementMapper();
    private static final Mapper<MeasurementDetails> MEASUREMENT_DETAILS_MAPPER = new MeasurementDetailsMapper();
    private static final Mapper<Church> CHURCH_MAPPER = new ChurchMapper();

    private GmaDao(final Context context)
    {
        super(DatabaseOpenHelper.getInstance(context));
    }

    public static GmaDao getInstance(Context context)
    {
        synchronized(instanceLock)
        {
            if(instance == null)
            {
                instance = new GmaDao(context.getApplicationContext());
            }
        }

        return instance;
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
        } else if (MinistryMeasurement.class.equals(clazz)) {
            return Contract.MinistryMeasurement.TABLE_NAME;
        } else if (PersonalMeasurement.class.equals(clazz)) {
            return Contract.PersonalMeasurement.TABLE_NAME;
        } else if (MeasurementDetails.class.equals(clazz)) {
            return Contract.MeasurementDetails.TABLE_NAME;
        } else if(Church.class.equals(clazz)) {
            return Contract.Church.TABLE_NAME;
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
        } else if (MinistryMeasurement.class.equals(clazz)) {
            return Contract.MinistryMeasurement.PROJECTION_ALL;
        } else if (PersonalMeasurement.class.equals(clazz)) {
            return Contract.PersonalMeasurement.PROJECTION_ALL;
        } else if (MeasurementDetails.class.equals(clazz)) {
            return Contract.MeasurementDetails.PROJECTION_ALL;
        } else if (Church.class.equals(clazz)) {
            return Contract.Church.PROJECTION_ALL;
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
        } else if (MinistryMeasurement.class.equals(clazz)) {
            return (Mapper<T>) MINISTRY_MEASUREMENT_MAPPER;
        } else if (PersonalMeasurement.class.equals(clazz)) {
            return (Mapper<T>) PERSONAL_MEASUREMENT_MAPPER;
        } else if (MeasurementDetails.class.equals(clazz)) {
            return (Mapper<T>) MEASUREMENT_DETAILS_MAPPER;
        } else if(Church.class.equals(clazz)) {
            return (Mapper<T>) CHURCH_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Class<?> clazz, @NonNull final Object... key)
    {
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
        } else if (MeasurementType.class.equals(clazz)) {
            keyLength = 1;
            where = Contract.MeasurementType.SQL_WHERE_PRIMARY_KEY;
        } else if (MinistryMeasurement.class.equals(clazz)) {
            keyLength = 4;
            where = Contract.MinistryMeasurement.SQL_WHERE_PRIMARY_KEY;
        } else if (PersonalMeasurement.class.equals(clazz)) {
            keyLength = 5;
            where = Contract.PersonalMeasurement.SQL_WHERE_PRIMARY_KEY;
        } else if (MeasurementDetails.class.equals(clazz)) {
            keyLength = 5;
            where = Contract.MeasurementDetails.SQL_WHERE_PRIMARY_KEY;
        }
        else
        {
            return super.getPrimaryKeyWhere(clazz, key);
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
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Object obj)
    {
        if(obj instanceof Ministry)
        {
            return getPrimaryKeyWhere(Ministry.class, ((Ministry) obj).getMinistryId());
        } else if (obj instanceof Assignment) {
            return getPrimaryKeyWhere(Assignment.class, ((Assignment) obj).getGuid(),
                                      ((Assignment) obj).getMinistryId());
        } else if (obj instanceof MeasurementType) {
            return getPrimaryKeyWhere(MeasurementType.class, ((MeasurementType) obj).getPermLinkStub());
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
        }

        return super.getPrimaryKeyWhere(obj);
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
        final Pair<String, String[]> where = getPrimaryKeyWhere(value);
        sql.append(" WHERE ").append(where.first);
        final String[] args = ArrayUtils.merge(String.class, bindValues(change), where.second);

        // perform update and sanitize PersonalMeasurements
        final SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL(sql.toString(), args);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
