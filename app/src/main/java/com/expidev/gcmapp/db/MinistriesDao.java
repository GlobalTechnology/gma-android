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
        this.databaseHelper = new DatabaseOpenHelper(context);
    }

    public static MinistriesDao getInstance(Context context)
    {
        if(instance == null)
        {
            synchronized(instanceLock)
            {
                instance = new MinistriesDao(context.getApplicationContext());
            }
        }

        return instance;
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

            existingAssignments.moveToNext();
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

            existingAssociatedMinistries.moveToNext();
        }
        return false;
    }

    private int booleanToInt(boolean booleanValue)
    {
        return booleanValue ? 1 : 0;
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
        }
    }
}
