package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        }

        return super.getTable(clazz);
    }

    @NonNull
    @Override
    protected String[] getFullProjection(@NonNull final Class<?> clazz) {
        if (Training.class.equals(clazz)) {
            return Contract.Training.PROJECTION_ALL;
        }

        return super.getFullProjection(clazz);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mapper<T> getMapper(@NonNull final Class<T> clazz) {
        if (Training.class.equals(clazz)) {
            return (Mapper<T>) TRAINING_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Class<?> clazz, @NonNull final Object... key) {
        final String where;
        if (Training.class.equals(clazz)) {
            if (key.length != 1) {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.Training.SQL_WHERE_PRIMARY_KEY;
        } else {
            return super.getPrimaryKeyWhere(clazz, key);
        }

        // return where clause pair
        return Pair.create(where, this.getBindValues(key));
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Object obj) {
        if (obj instanceof Training) {
            return this.getPrimaryKeyWhere(Training.class, ((Training) obj).getId());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public Cursor retrieveTrainingCursor(String tableName)
    {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        
        try
        {
            return database.query(tableName, null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to retrieve training: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public Cursor retrieveCompletedTrainingCursor(String tableName)
    {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        
        try
        {
            return database.query(tableName, null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        
        return null;
    }

    public Cursor retrieveCompletedTrainingCursor(int trainingId)
    {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        String whereCondition = "training_id = ?";
        String[] whereArgs = {String.valueOf(trainingId)};
        
        try
        {
            return database.query(TableNames.TRAINING_COMPLETIONS.getTableName(), null, whereCondition, whereArgs, null, null, null);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
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
    
    public List<Training.GCMTrainingCompletions> getCompletedTrainingByTrainingId(int id)
    {
        Cursor cursor = null;
        List<Training.GCMTrainingCompletions> completed =  new ArrayList<>();
        
        try
        {
            cursor = retrieveCompletedTrainingCursor(id);
            
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++)
                {
                    Training.GCMTrainingCompletions completedTraining = setCompletedTrainingFromCursor(cursor);
                    
                    // if size is 0, go ahead and add
                    if (completed.size() > 0)
                    {
                        boolean exists = false;
                        for (Training.GCMTrainingCompletions trainingAlreadyAdded : completed)
                        {
                            if (Training.GCMTrainingCompletions.equals(trainingAlreadyAdded, completedTraining)) exists = true;
                        }
                        if (!exists) completed.add(completedTraining);
                    }
                    else
                    {
                        completed.add(completedTraining);
                    }
                    cursor.moveToNext();
                }
            }
            
            return completed;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        finally
        {
            if (cursor != null) cursor.close();
        }
        return null;
    }
    
    public void saveTrainingFromAPI(JSONArray jsonArray)
    {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        
        try
        {
            database.beginTransaction();

            String trainingCompleteTable = TableNames.TRAINING_COMPLETIONS.getTableName();
            
            Cursor existingCompletedTraining = retrieveTrainingCursor(trainingCompleteTable);

            Log.i(TAG, "API returned: " + jsonArray.length());
            
            for (int i = 0; i < jsonArray.length(); i++)
            {
                final JSONObject json = jsonArray.getJSONObject(i);

                // build training object
                final Training training = new Training();
                training.setId(json.getInt("id"));
                training.setMinistryId(json.getString("ministry_id"));
                training.setName(json.getString("name"));
                training.setDate(stringToDate(json.getString("date")));
                training.setType(json.getString("type"));
                training.setMcc(json.getString("mcc"));
                training.setLatitude(json.getDouble("latitude"));
                training.setLongitude(json.getDouble("longitude"));
                training.setLastSynced(new Date());

                // update or insert training object
                this.updateOrInsert(training, Contract.Training.PROJECTION_ALL);

                JSONArray trainingCompletedArray = json.getJSONArray("gcm_training_completions");
                
                for (int j = 0; j < trainingCompletedArray.length(); j++)
                {
                    JSONObject completedTraining = trainingCompletedArray.getJSONObject(j);
                    int completedId = completedTraining.getInt("id");
                    
                    ContentValues completedTrainingToInsert = new ContentValues();
                    completedTrainingToInsert.put("id", completedId);
                    completedTrainingToInsert.put("phase", completedTraining.getInt("phase"));
                    completedTrainingToInsert.put("number_completed", completedTraining.getInt("number_completed"));
                    completedTrainingToInsert.put("date", completedTraining.getString("date"));
                    completedTrainingToInsert.put("training_id", completedTraining.getInt("training_id"));
                    completedTrainingToInsert.put("synced", new Timestamp(new Date().getTime()).toString());
                    
                    if (!trainingExistsInDatabase(completedId, existingCompletedTraining))
                    {
                        database.insert(trainingCompleteTable, null, completedTrainingToInsert);
                    }
                    else
                    {
                        database.update(trainingCompleteTable, completedTrainingToInsert, null, null);
                    }

                    Log.i(TAG, "Inserted/Updated completed training: " + completedId);
                    
                }
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

            if (database.isDbLockedByCurrentThread()) Log.w(TAG, "Database Locked by thread (saveTrainingFromAPI)");
        }
    }
    
    public void saveTraining(Training training)
    {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        
        try
        {
            database.beginTransaction();

            this.updateOrInsert(training, Contract.Training.PROJECTION_ALL);

            String completedTrainingTable = TableNames.TRAINING_COMPLETIONS.getTableName();

            Cursor existingCompletedTraining = retrieveCompletedTrainingCursor(completedTrainingTable);
            
            if (training.getCompletions() != null && training.getCompletions().size() > 0)
            {

                for (Training.GCMTrainingCompletions completion : training.getCompletions())
                {
                    ContentValues completedTrainingToInsert = new ContentValues();
                    completedTrainingToInsert.put("id", completion.getId());
                    completedTrainingToInsert.put("phase", completion.getPhase());
                    completedTrainingToInsert.put("number_completed", completion.getNumberCompleted());
                    completedTrainingToInsert.put("training_id", completion.getTrainingId());
                    completedTrainingToInsert.put("synced", completion.getSynced().toString());

                    if (!trainingExistsInDatabase(completion.getId(), existingCompletedTraining))
                    {
                        database.insert(completedTrainingTable, null, completedTrainingToInsert);
                    }
                    else
                    {
                        database.update(completedTrainingTable, completedTrainingToInsert, null, null);
                    }

                    Log.i(TAG, "Inserted/Updated completed training: " + completion.getId());
                }
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

    private boolean trainingExistsInDatabase(int id, Cursor existingTraining)
    {
        existingTraining.moveToFirst();
        
        for (int i = 0; i < existingTraining.getCount(); i++)
        {
            int existingId = existingTraining.getInt(existingTraining.getColumnIndex("id"));
            if (existingId == id) return true;
            existingTraining.moveToNext();
        }
        
        return false;
    }

    private Date stringToDate(String string) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return format.parse(string);
    }

    private Training.GCMTrainingCompletions setCompletedTrainingFromCursor(Cursor cursor) throws ParseException
    {
        Training.GCMTrainingCompletions trainingCompletions = new Training.GCMTrainingCompletions();
        trainingCompletions.setId(cursor.getInt(cursor.getColumnIndex("id")));
        trainingCompletions.setPhase(cursor.getInt(cursor.getColumnIndex("phase")));
        trainingCompletions.setNumberCompleted(cursor.getInt(cursor.getColumnIndex("number_completed")));
        trainingCompletions.setTrainingId(cursor.getInt(cursor.getColumnIndex("training_id")));
        trainingCompletions.setDate(stringToDate(cursor.getString(cursor.getColumnIndex("date"))));
        
        if (!cursor.getString(cursor.getColumnIndex("synced")).isEmpty())
        {
            try
            {
                trainingCompletions.setSynced(Timestamp.valueOf(cursor.getString(cursor.getColumnIndex("synced"))));
            }
            catch (Exception e)
            {
                Log.i(TAG, "Could not parse Timestamp");
            }
        }
        
        return trainingCompletions;
    }
}
