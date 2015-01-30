package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.expidev.gcmapp.model.User;
import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.json.JSONObject;

/**
 * Created by matthewfrederick on 1/23/15.
 */
public class UserDao
{
    private final String TAG = getClass().getSimpleName();
    
    private final SQLiteOpenHelper databaseHelper;
    
    private static final Object instanceLock = new Object();
    private static UserDao instance;
    
    private UserDao(final Context context)
    {
        this.databaseHelper = DatabaseOpenHelper.getInstance(context);
    }
    
    public static UserDao getInstance(Context context)
    {
        if (instance == null)
        {
            synchronized (instanceLock)
            {
                instance = new UserDao(context.getApplicationContext());
            }
        }
        
        return instance;
    }
    
    public Cursor retrieveUserCursor()
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        
        try
        {
            return database.query(TableNames.USER.getTableName(), null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to retrieve user: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public User retrieveUser()
    {
        Cursor cursor = null;
        User user = new User();
        try
        {
            cursor = retrieveUserCursor();
            
            if (cursor != null && cursor.getCount() == 1)
            {
                cursor.moveToFirst();
                user.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
                user.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
                user.setCasUsername(cursor.getString(cursor.getColumnIndex("cas_username")));
                user.setPersonId(cursor.getString(cursor.getColumnIndex("person_id")));
                
                return user;
            }
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        
        return null;
    }
    
    public void saveUser(JSONObject jsonObject)
    {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try
        {
            String userTable = TableNames.USER.getTableName();

            ContentValues userToInsert = new ContentValues();
            userToInsert.put("first_name", jsonObject.getString("first_name"));
            userToInsert.put("last_name", jsonObject.getString("last_name"));
            userToInsert.put("cas_username", jsonObject.getString("cas_username"));
            userToInsert.put("person_id", jsonObject.getString("person_id"));
            
            database.beginTransaction();
            
            User existingUser = retrieveUser();
            
            if (existingUser == null)
            {
                database.insert(userTable, null, userToInsert);
            }
            else
            {
                database.update(userTable, userToInsert, null, null);
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
            
            if (database.isDbLockedByCurrentThread()) Log.w(TAG, "Database Locked by thread (saveUser)");
        }
    }

    public void deleteAllData()
    {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.beginTransaction();

        try
        {
            database.delete(TableNames.USER.getTableName(), null, null);
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
