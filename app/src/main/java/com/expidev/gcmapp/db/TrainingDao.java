package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.expidev.gcmapp.model.Training;
import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.ccci.gto.android.common.db.AbstractDao;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class TrainingDao extends AbstractDao
{
    private final String TAG = getClass().getSimpleName();
    
    private final SQLiteOpenHelper databaseHelper;
    
    private static final Object instanceLock = new Object();
    private static TrainingDao instance;
    
    private TrainingDao(final Context context)
    {
        super(DatabaseOpenHelper.getInstance(context));
        this.databaseHelper = this.dbHelper;
    }
    
    public static TrainingDao getInstance(Context context)
    {
        if (instance == null)
        {
            synchronized (instanceLock)
            {
                instance = new TrainingDao(context.getApplicationContext());
            }
        }
        
        return instance;
    }
    
    public Cursor retrieveTrainingCursor(String tableName)
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        
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
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        
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
    
    public Cursor retrieveTrainingCursorById(int id)
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String whereCondition = "id = ?";
        String[] whereArgs = {String.valueOf(id)};

        try
        {
            return database.query(TableNames.TRAINING.getTableName(), null, whereCondition, whereArgs, null, null, null);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
    
    public Cursor retrieveTrainingCursorByMinistry(String ministryId)
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String where = "ministry_id = ?";
        String[] whereArgs = {ministryId};
        
        try
        {
            return database.query(TableNames.TRAINING.getTableName(), null, where, whereArgs, null, null, null);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
    
    public Cursor retrieveCompletedTrainingCursor(int trainingId)
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
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
        // first see if there is any completed training for this training
        List<Training.GCMTrainingCompletions> complete = getCompletedTrainingByTrainingId(id);
        
        Cursor cursor = null;
        
        try
        {
            cursor = retrieveTrainingCursorById(id);
            
            if (cursor != null && cursor.getCount() == 1)
            {   
                cursor.moveToFirst();
                return setTrainingFromCursor(cursor, complete);                
            }
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
    
    public List<Training> getAllMinistryTraining(String ministry_id)
    {
        Log.i(TAG, "Getting all training for ministry: " + ministry_id);
        
        Cursor cursor = null;
        List<Training> allTraining = new ArrayList<>();
        
        try
        {
            cursor = retrieveTrainingCursorByMinistry(ministry_id);
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++)
                {
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    List<Training.GCMTrainingCompletions> completed = getCompletedTrainingByTrainingId(id);
                    Training training = setTrainingFromCursor(cursor, completed);
                    
                    // if size is 0 go ahead an add
                    if (allTraining.size() > 0)
                    {
                        boolean exists = false;
                        for (Training trainingAlreadyAdded : allTraining)
                        {
                            if (Training.equals(trainingAlreadyAdded, training)) exists = true;
                        }
                        if (!exists) allTraining.add(training);
                    }
                    else
                    {
                        allTraining.add(training);
                    }
                    cursor.moveToNext();
                }
            }
            Log.i(TAG, "Trainings returned: " + allTraining.size());
            
            return allTraining;
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
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        try
        {
            database.beginTransaction();

            String trainingTable = TableNames.TRAINING.getTableName();
            String trainingCompleteTable = TableNames.TRAINING_COMPLETIONS.getTableName();
            
            Cursor existingTraining = retrieveTrainingCursor(trainingTable);
            Cursor existingCompletedTraining = retrieveTrainingCursor(trainingCompleteTable);

            Log.i(TAG, "API returned: " + jsonArray.length());
            
            for (int i = 0; i < jsonArray.length(); i++)
            {   
                JSONObject training = jsonArray.getJSONObject(i);
                int id = training.getInt("id");
                Log.i(TAG, "Saving id: " + id);
                
                ContentValues trainingToInsert = new ContentValues();
                trainingToInsert.put("id", id);
                trainingToInsert.put("ministry_id", training.getString("ministry_id"));
                trainingToInsert.put("name", training.getString("name"));
                trainingToInsert.put("date", training.getString("date"));
                trainingToInsert.put("type", training.getString("type"));
                trainingToInsert.put("mcc", training.getString("mcc"));
                trainingToInsert.put("latitude", training.getDouble("latitude"));
                trainingToInsert.put("longitude", training.getDouble("longitude"));
                trainingToInsert.put("synced", new Timestamp(new Date().getTime()).toString());
                
                JSONArray trainingCompletedArray = training.getJSONArray("gcm_training_completions");
                
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
                
                if (!trainingExistsInDatabase(id, existingTraining))
                {
                    database.insert(trainingTable, null, trainingToInsert);
                    Log.i(TAG, "Inserted training: " + trainingToInsert.getAsString("id"));
                }
                else
                {
                    String where = "id = ?";
                    String[] args = {trainingToInsert.getAsString("id")};
                    database.update(trainingTable, trainingToInsert, where, args);
                    Log.i(TAG, "Updated training: " + trainingToInsert.getAsString("id"));
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
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        try
        {
            database.beginTransaction();

            String trainingTable = TableNames.TRAINING.getTableName();
            String completedTrainingTable = TableNames.TRAINING_COMPLETIONS.getTableName();

            Cursor existingTraining = retrieveTrainingCursor(trainingTable);
            Cursor existingCompletedTraining = retrieveCompletedTrainingCursor(completedTrainingTable);
            
            ContentValues trainingToInsert = new ContentValues();
            trainingToInsert.put("id", training.getId());
            trainingToInsert.put("ministry_id", training.getMinistryId().toString());
            trainingToInsert.put("name", training.getName());
            trainingToInsert.put("date", dateToString(training.getDate()));
            trainingToInsert.put("type", training.getType());
            trainingToInsert.put("mcc", training.getMcc());
            trainingToInsert.put("latitude", training.getLatitude());
            trainingToInsert.put("longitude", training.getLongitude());
            trainingToInsert.put("synced", training.getSynced().toString());
            
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

            if (!trainingExistsInDatabase(training.getId(), existingTraining))
            {
                database.insert(trainingTable, null, trainingToInsert);
                Log.i(TAG, "Inserted training: " + training.getId());
            }
            else
            {
                database.update(trainingTable, trainingToInsert, null, null);
                Log.i(TAG, "Updated training: " + training.getId());
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
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();

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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.parse(string);
    }

    private String dateToString(Date date)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
    
    private Training setTrainingFromCursor(Cursor cursor, List<Training.GCMTrainingCompletions> completed) throws ParseException
    {
        Log.i(TAG, "Building training");
        Training training = new Training();
        training.setId(cursor.getInt(cursor.getColumnIndex("id")));
        training.setMinistryId(cursor.getString(cursor.getColumnIndex("ministry_id")));
        training.setName(cursor.getString(cursor.getColumnIndex("name")));
        training.setDate(stringToDate(cursor.getString(cursor.getColumnIndex("date"))));
        training.setType(cursor.getString(cursor.getColumnIndex("type")));
        training.setMcc(cursor.getString(cursor.getColumnIndex("mcc")));
        training.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
        training.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
        
        if (completed != null && completed.size() > 0)
        {
            training.setCompletions(completed);
        }
        
        if (!cursor.getString(cursor.getColumnIndex("synced")).isEmpty())
        {
            try
            {
                training.setSynced(Timestamp.valueOf(cursor.getString(cursor.getColumnIndex("synced"))));
            }
            catch (Exception e)
            {
                Log.i(TAG, "Could not parse Timestamp");
            }
        }
        Log.i(TAG, "Returning new training: " + training.getId());
        return training;
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
