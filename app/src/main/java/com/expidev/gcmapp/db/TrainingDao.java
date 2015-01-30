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

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by matthewfrederick on 1/26/15.
 */
public class TrainingDao
{
    private final String TAG = getClass().getSimpleName();
    
    private final SQLiteOpenHelper databaseHelper;
    
    private static final Object instanceLock = new Object();
    private static TrainingDao instance;
    
    private TrainingDao(final Context context)
    {
        this.databaseHelper = DatabaseOpenHelper.getInstance(context);
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
    
    public Cursor retrieveTrainingCursor()
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        
        try
        {
            return database.query(TableNames.TRAINING.getTableName(), null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to retrieve training: " + e.getMessage(), e);
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
    
    public Training retrieveTrainingById(int id)
    {
        Cursor cursor = null;
        try
        {
            cursor = retrieveTrainingCursorById(id);
            if (cursor != null && cursor.getCount() == 1)
            {
                Log.i(TAG, "Count: " + cursor.getCount());
                
                cursor.moveToFirst();
                return setTrainingFromCursor(cursor);                
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
    
    public void saveTrainingFromAPI(JSONArray jsonArray)
    {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        
        try
        {
            database.beginTransaction();
            
            Cursor existingTraining = retrieveTrainingCursor();
            
            String trainingTable = TableNames.TRAINING.getTableName();
            
            for (int i = 0; i < jsonArray.length(); i++)
            {   
                JSONObject training = jsonArray.getJSONObject(i);
                int id = training.getInt("id");
                
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
                
                if (!trainingExistsInDatabase(id, existingTraining))
                {
                    database.insert(trainingTable, null, trainingToInsert);
                }
                else
                {
                    database.update(trainingTable, trainingToInsert, null, null);
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

            Cursor existingTraining = retrieveTrainingCursor();
            
            String trainingTable = TableNames.TRAINING.getTableName();
            
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

            if (trainingExistsInDatabase(training.getId(), existingTraining))
            {
                database.insert(trainingTable, null, trainingToInsert);
            }
            else
            {
                database.update(trainingTable, trainingToInsert, null, null);
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
    
    private Training setTrainingFromCursor(Cursor cursor) throws ParseException
    {
        Training training = new Training();
        training.setId(cursor.getInt(cursor.getColumnIndex("id")));
        training.setMinistryId(UUID.fromString(cursor.getString(cursor.getColumnIndex("ministry_id"))));
        training.setName(cursor.getString(cursor.getColumnIndex("name")));
        training.setDate(stringToDate(cursor.getString(cursor.getColumnIndex("date"))));
        training.setType(cursor.getString(cursor.getColumnIndex("type")));
        training.setMcc(cursor.getString(cursor.getColumnIndex("mcc")));
        training.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
        training.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
        
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
        
        return training;
    }
}
