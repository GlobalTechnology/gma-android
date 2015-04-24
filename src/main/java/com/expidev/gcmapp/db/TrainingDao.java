package com.expidev.gcmapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class TrainingDao extends AbstractDao
{
    private final String TAG = getClass().getSimpleName();
    
    private static final Mapper<Training> TRAINING_MAPPER = new TrainingMapper();
    private static final Mapper<Training.Completion> COMPLETION_MAPPER = new TrainingCompletionMapper();

    private static final Object instanceLock = new Object();
    private static TrainingDao instance;
    
    private TrainingDao(final Context context)
    {
        super(DatabaseOpenHelper.getInstance(context));
    }
    
    public static TrainingDao getInstance(Context context)
    {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new TrainingDao(context.getApplicationContext());
            }
        }
        
        return instance;
    }

    @NonNull
    @Override
    protected String getTable(@NonNull final Class<?> clazz) {
        if (Training.class.equals(clazz)) {
            return Contract.Training.TABLE_NAME;
        } else if (Training.Completion.class.equals(clazz)) {
            return Contract.Training.Completion.TABLE_NAME;
        }

        return super.getTable(clazz);
    }

    @NonNull
    @Override
    public String[] getFullProjection(@NonNull final Class<?> clazz) {
        if (Training.class.equals(clazz)) {
            return Contract.Training.PROJECTION_ALL;
        } else if (Training.Completion.class.equals(clazz)) {
            return Contract.Training.Completion.PROJECTION_ALL;
        }

        return super.getFullProjection(clazz);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mapper<T> getMapper(@NonNull final Class<T> clazz) {
        if (Training.class.equals(clazz)) {
            return (Mapper<T>) TRAINING_MAPPER;
        } else if (Training.Completion.class.equals(clazz)) {
            return (Mapper<T>) COMPLETION_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Class<?> clazz, @NonNull final Object... key) {
        final int keyLength;
        final String where;
        if (Training.class.equals(clazz)) {
            where = Contract.Training.SQL_WHERE_PRIMARY_KEY;
            keyLength = 1;
        } else if (Training.Completion.class.equals(clazz)) {
            where = Contract.Training.Completion.SQL_WHERE_PRIMARY_KEY;
            keyLength = 1;
        } else {
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
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Object obj) {
        if (obj instanceof Training) {
            return this.getPrimaryKeyWhere(Training.class, ((Training) obj).getId());
        } else if (obj instanceof Training.Completion) {
            return this.getPrimaryKeyWhere(Training.Completion.class,
                                           ((Training.Completion) obj).getId());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public void saveTraining(@NonNull final Training training) {
        final SQLiteDatabase database = getWritableDatabase();
        try
        {
            database.beginTransaction();

            this.updateOrInsert(training, Contract.Training.PROJECTION_ALL);
            for (final Training.Completion completion : training.getCompletions()) {
                this.updateOrInsert(completion, Contract.Training.Completion.PROJECTION_ALL);
            }

            database.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        finally
        {
            database.endTransaction();

            if (database.isDbLockedByCurrentThread()) Log.w(TAG, "Database Locked by thread (saveTraining)");
        }
    }

    public void deleteAllData()
    {
        final SQLiteDatabase database = getWritableDatabase();

        database.beginTransaction();

        try
        {
            database.delete(getTable(Training.class), null, null);
            database.setTransactionSuccessful();
        } catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        } finally
        {
            database.endTransaction();

            if (database.isDbLockedByCurrentThread()) Log.w(TAG, "Database Locked by thread (deleteAllData)");
        }
    }
}
