package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 1/21/2015.
 */
public class MinistriesDao
{
    private final String TAG = getClass().getSimpleName();

    private final SQLiteOpenHelper databaseHelper;

    private static final Object instanceLock = new Object();
    private static MinistriesDao instance;

    private MinistriesDao(final Context context)
    {
        this.databaseHelper = DatabaseOpenHelper.getInstance(context);
    }

    public static MinistriesDao getInstance(Context context)
    {
        synchronized(instanceLock)
        {
            if(instance == null)
            {
                instance = new MinistriesDao(context.getApplicationContext());
            }
        }

        return instance;
    }

    public Cursor retrieveAllMinistriesCursor()
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();

        try
        {
            return database.query(TableNames.ALL_MINISTRIES.getTableName(), null, null, null, null, null, null);
        }
        catch(Exception e)
        {
            Log.e(TAG, "Failed to retrieve all ministries: " + e.getMessage());
        }

        return null;
    }

    public List<Ministry> retrieveAllMinistries()
    {
        Cursor cursor = null;
        List<Ministry> allMinistries = new ArrayList<Ministry>();

        try
        {
            cursor = retrieveAllMinistriesCursor();

            if(cursor != null && cursor.getCount() > 0)
            {


                cursor.moveToFirst();

                for(int i = 0; i < cursor.getCount(); i++)
                {
                    Ministry ministry = new Ministry();
                    ministry.setMinistryId(cursor.getString(cursor.getColumnIndex("ministry_id")));
                    ministry.setName(cursor.getString(cursor.getColumnIndex("name")));

                    allMinistries.add(ministry);

                    cursor.moveToNext();
                }
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }

        return allMinistries;
    }

    public Cursor retrieveAssociatedMinistriesCursor()
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try
        {
            return database.query(TableNames.ASSOCIATED_MINISTRIES.getTableName(), null, null, null, null, null, null);
        }
        catch(Exception e)
        {
            Log.e(TAG, "Failed to retrieve associated ministries: " + e.getMessage());
        }

        return null;
    }

    public List<Ministry> retrieveAssociatedMinistriesList()
    {
        Log.i(TAG, "Retrieving associated ministries");
        
        Cursor cursor = null;
        List<Ministry> ministryList = new ArrayList<Ministry>();

        try
        {
            cursor = retrieveAssociatedMinistriesCursor();

            Log.i(TAG, "Associated Ministries found: " + cursor.getCount());
            
            if(cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    ministryList.add(buildMinistryFromCursor(cursor, null));
                    cursor.moveToNext();
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        finally
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }

        return ministryList;
    }
    
    public Assignment retrieveCurrentAssignment(Ministry ministry)
    {
        Cursor cursor = null;
        Log.i(TAG, "Looking for assignment with ministryId: " + ministry.getMinistryId());
        
        try
        {
            cursor = retrieveAssignmentsCursor();
            
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++)
                {
                    Log.i(TAG, "assignment ministry id: " + cursor.getString(cursor.getColumnIndex("ministry_id")));
                    if (cursor.getString(cursor.getColumnIndex("ministry_id")).equals(ministry.getMinistryId()))
                    {
                        return buildAssignmentFromCursor(cursor, ministry);
                    }
                }
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

    private Assignment buildAssignmentFromCursor(Cursor cursor, Ministry ministry)
    {
        Assignment assignment = new Assignment();
        assignment.setId(cursor.getString(cursor.getColumnIndex("ministry_id")));
        assignment.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
        assignment.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
        assignment.setMinistry(ministry);
        assignment.setLocationZoom(cursor.getInt(cursor.getColumnIndex("location_zoom")));
        assignment.setTeamRole(cursor.getString(cursor.getColumnIndex("team_role")));
        
        return assignment;
    }

    private Ministry buildMinistryFromCursor(Cursor cursor, String parentId)
    {
        Ministry ministry = new Ministry();
        ministry.setName(cursor.getString(cursor.getColumnIndex("name")));
        ministry.setMinistryId(cursor.getString(cursor.getColumnIndex("ministry_id")));
        ministry.setMinistryCode(cursor.getString(cursor.getColumnIndex("min_code")));
        ministry.setHasGcm(intToBoolean(cursor.getInt(cursor.getColumnIndex("has_gcm"))));
        ministry.setHasSlm(intToBoolean(cursor.getInt(cursor.getColumnIndex("has_slm"))));
        ministry.setHasDs(intToBoolean(cursor.getInt(cursor.getColumnIndex("has_ds"))));
        ministry.setHasLlm(intToBoolean(cursor.getInt(cursor.getColumnIndex("has_llm"))));
        ministry.setSubMinistries(retrieveMinistriesWithParent(ministry.getMinistryId()));
        
        if (parentId != null) ministry.setParentId(parentId);

        return ministry;
    }

    public List<Ministry> retrieveMinistriesWithParent(String parentMinistryId)
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor = null;
        List<Ministry> ministryList = null;

        try
        {
            cursor = database.query(
                TableNames.ASSOCIATED_MINISTRIES.getTableName(),
                null,
                "parent_ministry_id = ?",
                new String[] { parentMinistryId },
                null,
                null,
                null);

            if(cursor != null && cursor.getCount() > 0)
            {
                ministryList = new ArrayList<Ministry>();
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    ministryList.add(buildMinistryFromCursor(cursor, parentMinistryId));
                    cursor.moveToNext();
                }
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, "Failed to retrieve associated ministries: " + e.getMessage());
        }
        finally
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }

        return ministryList;
    }

    public List<String> retrieveAssociatedMinistries()
    {
        Cursor cursor = null;

        try
        {
            cursor = retrieveAssociatedMinistriesCursor();

            if(cursor != null && cursor.getCount() > 0)
            {
                List<String> associatedMinistries = new ArrayList<String>(cursor.getCount());

                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    associatedMinistries.add(cursor.getString(cursor.getColumnIndex("name")));
                    cursor.moveToNext();
                }

                return associatedMinistries;
            }
        }
        finally
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }

        return null;
    }

    public Cursor retrieveAssignmentsCursor()
    {
        final SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try
        {
            return database.query(TableNames.ASSIGNMENTS.getTableName(), null, null, null, null, null, null);
        }
        catch(Exception e)
        {
            Log.e(TAG, "Failed to retrieve associated ministries: " + e.getMessage());
        }

        return null;
    }

    public void saveAssociatedMinistries(List<Assignment> assignmentList)
    {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();

        try
        {
            database.beginTransaction();

            Cursor existingAssignments = retrieveAssignmentsCursor();
            Cursor existingAssociations = retrieveAssociatedMinistriesCursor();

            if(existingAssignments != null && existingAssignments.getCount() > 0)
            {
                // We need to insert associations that don't yet exist, and update those that do
                for(Assignment assignment : assignmentList)
                {
                    Ministry associatedMinistry = assignment.getMinistry();

                    ContentValues assignmentValues = buildAssignmentValues(assignment);

                    if(assignmentExistsInDatabase(assignment.getId(), existingAssignments))
                    {
                        insertOrUpdateMinistry(associatedMinistry, null, database, existingAssociations);

                        String[] whereArgs = { associatedMinistry.getMinistryId() };

                        database.update(
                            TableNames.ASSIGNMENTS.getTableName(),
                            assignmentValues,
                            "ministry_id = ?",
                            whereArgs);
                    }
                    else
                    {
                        insertOrUpdateMinistry(associatedMinistry, null, database, existingAssociations);
                        database.insert(TableNames.ASSIGNMENTS.getTableName(), null, assignmentValues);
                    }
                }
            }
            else
            {
                for(Assignment assignment : assignmentList)
                {
                    Ministry associatedMinistry = assignment.getMinistry();

                    ContentValues assignmentValues = buildAssignmentValues(assignment);

                    insertOrUpdateMinistry(associatedMinistry, null, database, existingAssociations);
                    database.insert(TableNames.ASSIGNMENTS.getTableName(), null, assignmentValues);
                }
            }

            database.setTransactionSuccessful();
        }
        catch(Exception e)
        {
            Log.e(TAG, "Failed to save assignments: " + e.getMessage());
        }
        finally
        {
            database.endTransaction();
            if (database.isDbLockedByCurrentThread()) Log.w(TAG, "Database Locked by thread (saveAssociatedMinistries)");
        }
    }

    void insertOrUpdateMinistry(
        Ministry ministry,
        String parentMinistryId,
        SQLiteDatabase database,
        Cursor existingAssociatedMinistries)
    {
        if(ministry.getSubMinistries() != null)
        {
            for(Ministry subMinistry : ministry.getSubMinistries())
            {
                insertOrUpdateMinistry(subMinistry, ministry.getMinistryId(), database, existingAssociatedMinistries);
            }
        }

        ContentValues subMinistryValues = buildAssociationValues(ministry);
        subMinistryValues.put("parent_ministry_id", parentMinistryId);

        // If the ministry already exists in the database, update it, otherwise insert it
        if(ministryExistsInDatabase(ministry.getMinistryId(), existingAssociatedMinistries))
        {
            String[] whereArgs = { ministry.getMinistryId() };
            database.update(
                TableNames.ASSOCIATED_MINISTRIES.getTableName(),
                subMinistryValues,
                "ministry_id = ?",
                whereArgs);
        }
        else
        {
            database.insert(
                TableNames.ASSOCIATED_MINISTRIES.getTableName(),
                null,
                subMinistryValues);
        }
    }

    private boolean ministryExistsInDatabase(String ministryId, Cursor existingAssociatedMinistries)
    {
        existingAssociatedMinistries.moveToFirst();
        for(int i = 0; i < existingAssociatedMinistries.getCount(); i++)
        {
            String associatedMinistryId = existingAssociatedMinistries.getString(
                existingAssociatedMinistries.getColumnIndex("ministry_id"));

            if(ministryId.equalsIgnoreCase(associatedMinistryId))
            {
                return true;
            }

            existingAssociatedMinistries.moveToNext();
        }
        return false;
    }

    private boolean assignmentExistsInDatabase(String assignmentId, Cursor existingAssignments)
    {
        existingAssignments.moveToFirst();
        for(int i = 0; i < existingAssignments.getCount(); i++)
        {
            String existingAssignmentId = existingAssignments.getString(existingAssignments.getColumnIndex("id"));
            if(assignmentId.equalsIgnoreCase(existingAssignmentId))
            {
                return true;
            }

            existingAssignments.moveToNext();
        }
        return false;
    }

    private int booleanToInt(boolean booleanValue)
    {
        return booleanValue ? 1 : 0;
    }

    private boolean intToBoolean(int intValue)
    {
        return intValue == 1;
    }

    private ContentValues buildAssociationValues(Ministry associatedMinistry)
    {
        ContentValues associationValues = new ContentValues();

        associationValues.put("ministry_id", associatedMinistry.getMinistryId());
        associationValues.put("name", associatedMinistry.getName());
        associationValues.put("min_code", associatedMinistry.getMinistryCode());
        associationValues.put("has_slm", booleanToInt(associatedMinistry.hasSlm()));
        associationValues.put("has_llm", booleanToInt(associatedMinistry.hasLlm()));
        associationValues.put("has_ds", booleanToInt(associatedMinistry.hasDs()));
        associationValues.put("has_gcm", booleanToInt(associatedMinistry.hasGcm()));

        return associationValues;
    }

    private ContentValues buildAssignmentValues(Assignment assignment)
    {
        ContentValues assignmentValues = new ContentValues();

        assignmentValues.put("id", assignment.getId());
        assignmentValues.put("team_role", assignment.getTeamRole());
        assignmentValues.put("ministry_id", assignment.getMinistry().getMinistryId());
        assignmentValues.put("latitude", assignment.getLatitude());
        assignmentValues.put("longitude", assignment.getLongitude());
        assignmentValues.put("location_zoom", assignment.getLocationZoom());

        return assignmentValues;
    }

    void deleteAllData()
    {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.beginTransaction();

        try
        {
            database.delete(TableNames.ASSIGNMENTS.getTableName(), null, null);
            database.delete(TableNames.ASSOCIATED_MINISTRIES.getTableName(), null, null);
            database.setTransactionSuccessful();
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            database.endTransaction();
            if (database.isDbLockedByCurrentThread()) Log.w(TAG, "Database Locked by thread (deleteAllData)");
        }
    }

    public void saveAllMinistries(List<Ministry> allMinistries)
    {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.beginTransaction();
        Cursor existingMinistries = retrieveAllMinistriesCursor();

        try
        {
            for(Ministry ministry : allMinistries)
            {
                ContentValues ministryValues = new ContentValues();
                ministryValues.put("ministry_id", ministry.getMinistryId());
                ministryValues.put("name", ministry.getName());

                if(ministryExistsInDatabase(ministry.getMinistryId(), existingMinistries))
                {
                    database.update(
                        TableNames.ALL_MINISTRIES.getTableName(),
                        ministryValues,
                        "ministry_id = ?",
                        new String[] { ministry.getMinistryId() });
                }
                else
                {
                    database.insert(
                        TableNames.ALL_MINISTRIES.getTableName(),
                        null,
                        ministryValues
                    );
                }
            }
            database.setTransactionSuccessful();
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
        finally
        {
            database.endTransaction();
            if (database.isDbLockedByCurrentThread()) Log.w(TAG, "Database Locked by thread (saveAllMinistries)");
        }
    }
    
    
}
