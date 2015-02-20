package com.expidev.gcmapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class TrainingDao extends AbstractDao
{
    private final String TAG = getClass().getSimpleName();
    
    private static final Mapper<Training> TRAINING_MAPPER = new TrainingMapper();
    private static final Mapper<Training.GCMTrainingCompletions> COMPLETION_MAPPER = new TrainingCompletionMapper();

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
        } else if (Training.GCMTrainingCompletions.class.equals(clazz)) {
            return Contract.Training.Completion.TABLE_NAME;
        }

        return super.getTable(clazz);
    }

    @NonNull
    @Override
    protected String[] getFullProjection(@NonNull final Class<?> clazz) {
        if (Training.class.equals(clazz)) {
            return Contract.Training.PROJECTION_ALL;
        } else if (Training.GCMTrainingCompletions.class.equals(clazz)) {
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
        } else if (Training.GCMTrainingCompletions.class.equals(clazz)) {
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
        } else if (Training.GCMTrainingCompletions.class.equals(clazz)) {
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
        return Pair.create(where, this.getBindValues(key));
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Object obj) {
        if (obj instanceof Training) {
            return this.getPrimaryKeyWhere(Training.class, ((Training) obj).getId());
        } else if (obj instanceof Training.GCMTrainingCompletions) {
            return this.getPrimaryKeyWhere(Training.GCMTrainingCompletions.class,
                                           ((Training.GCMTrainingCompletions) obj).getId());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public Training retrieveTrainingById(int id)
    {
        try
        {
            final Training training = this.find(Training.class, id);
            if (training != null) {
                training.setCompletions(getCompletedTrainingByTrainingId(training.getId()));
            }
            return training;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
    
    @Nullable
    public List<Training> getAllMinistryTraining(String ministry_id)
    {
        Log.i(TAG, "Getting all training for ministry: " + ministry_id);
        
        try
        {
            final List<Training> trainings =
                    this.get(Training.class, Contract.Training.SQL_WHERE_MINISTRY_ID, this.getBindValues(ministry_id));
            for (final Training training : trainings) {
                training.setCompletions(getCompletedTrainingByTrainingId(training.getId()));
            }
            Log.i(TAG, "Trainings returned: " + trainings.size());

            return trainings;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    @Nullable
    public List<Training.GCMTrainingCompletions> getCompletedTrainingByTrainingId(int id)
    {
        try
        {
            return this.get(Training.GCMTrainingCompletions.class, Contract.Training.Completion.SQL_WHERE_TRAINING_ID,
                            this.getBindValues(id));
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    // TODO: this should be handled at either the API or Service layer
    public void saveTrainingFromAPI(JSONArray jsonArray)
    {
        try
        {
            Log.i(TAG, "API returned: " + jsonArray.length());
            
            for (int i = 0; i < jsonArray.length(); i++)
            {
                final JSONObject json = jsonArray.getJSONObject(i);

                // build training object
                final Training training = new Training();
                training.setId(json.getInt("Id"));
                training.setMinistryId(json.getString("ministry_id"));
                training.setName(json.getString("name"));
                training.setDate(stringToDate(json.getString("date")));
                training.setType(json.getString("type"));
                training.setMcc(json.getString("mcc"));
                training.setLatitude(json.getDouble("latitude"));
                training.setLongitude(json.getDouble("longitude"));
                training.setLastSynced(new Date());

                // build completion objects for all completed trainings
                JSONArray trainingCompletedArray = json.getJSONArray("gcm_training_completions");
                for (int j = 0; j < trainingCompletedArray.length(); j++)
                {
                    final JSONObject completionJson = trainingCompletedArray.getJSONObject(j);

                    final Training.GCMTrainingCompletions completion = new Training.GCMTrainingCompletions();
                    completion.setId(completionJson.getInt("Id"));
                    completion.setTrainingId(completionJson.getInt("training_id"));
                    completion.setPhase(completionJson.getInt("phase"));
                    completion.setNumberCompleted(completionJson.getInt("number_completed"));
                    completion.setDate(stringToDate(completionJson.getString("date")));

                    training.addCompletion(completion);
                }

                // update or insert training object
                this.saveTraining(training);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    
    public void saveTraining(Training training)
    {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        try
        {
            database.beginTransaction();

            this.updateOrInsert(training, Contract.Training.PROJECTION_ALL);
            for (final Training.GCMTrainingCompletions completion : training.getCompletions()) {
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

    void deleteAllData()
    {
        final SQLiteDatabase database = this.dbHelper.getWritableDatabase();

        database.beginTransaction();

        try
        {
            database.delete(TableNames.TRAINING.getTableName(), null, null);
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

    private Date stringToDate(String string) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return format.parse(string);
    }
}
