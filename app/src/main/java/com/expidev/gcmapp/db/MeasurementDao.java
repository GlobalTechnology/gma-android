package com.expidev.gcmapp.db;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;

/**
 * Created by William.Randall on 2/17/2015.
 */
public class MeasurementDao extends AbstractDao
{
    private static final Object instanceLock = new Object();
    private static MeasurementDao instance;

    private static final Mapper<Measurement> MEASUREMENT_MAPPER = new MeasurementMapper();

    private MeasurementDao(final Context context)
    {
        super(DatabaseOpenHelper.getInstance(context));
    }

    public static MeasurementDao getInstance(Context context)
    {
        synchronized(instanceLock)
        {
            if(instance == null)
            {
                instance = new MeasurementDao(context.getApplicationContext());
            }
        }

        return instance;
    }

    @NonNull
    @Override
    protected String getTable(@NonNull final Class<?> clazz)
    {
        if(Measurement.class.equals(clazz))
        {
            return Contract.Measurement.TABLE_NAME;
        }

        return super.getTable(clazz);
    }

    @NonNull
    @Override
    protected String[] getFullProjection(@NonNull final Class<?> clazz)
    {
        if(Measurement.class.equals(clazz))
        {
            return Contract.Measurement.PROJECTION_ALL;
        }

        return super.getFullProjection(clazz);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mapper<T> getMapper(@NonNull final Class<T> clazz)
    {
        if(Measurement.class.equals(clazz))
        {
            return (Mapper<T>) MEASUREMENT_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Class<?> clazz, @NonNull final Object... key)
    {
        final String where;

        if(Measurement.class.equals(clazz))
        {
            if(key.length != 1)
            {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.Measurement.SQL_WHERE_PRIMARY_KEY;
        }
        else
        {
            return super.getPrimaryKeyWhere(clazz, key);
        }

        return Pair.create(where, this.getBindValues(key));
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Object obj)
    {
        if(obj instanceof Measurement)
        {
            return getPrimaryKeyWhere(Measurement.class, ((Measurement) obj).getMeasurementId());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public void saveMeasurement(Measurement measurement)
    {
        this.updateOrInsert(measurement, Contract.Measurement.PROJECTION_ALL);
    }
}
