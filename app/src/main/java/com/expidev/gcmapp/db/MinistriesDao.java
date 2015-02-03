package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William.Randall on 1/21/2015.
 */
public class MinistriesDao extends AbstractDao
{
    private final String TAG = getClass().getSimpleName();

    private static final Object instanceLock = new Object();
    private static MinistriesDao instance;

    private static final Mapper<Ministry> ALL_MINISTRIES_MAPPER = new MinistriesMapper();
    private static final Mapper<AssociatedMinistry> ASSOCIATED_MINISTRIES_MAPPER = new AssociatedMinistriesMapper();

    private MinistriesDao(final Context context)
    {
        super(DatabaseOpenHelper.getInstance(context));
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

    @NonNull
    @Override
    protected String getTable(@NonNull final Class<?> clazz)
    {
        if(Ministry.class.equals(clazz))
        {
            return Contract.Ministry.TABLE_NAME;
        }
        else if(AssociatedMinistry.class.equals(clazz))
        {
            return Contract.AssociatedMinistry.TABLE_NAME;
        }

        return super.getTable(clazz);
    }

    @NonNull
    @Override
    protected String[] getFullProjection(@NonNull final Class<?> clazz)
    {
        if(Ministry.class.equals(clazz))
        {
            return Contract.Ministry.PROJECTION_ALL;
        }
        else if(AssociatedMinistry.class.equals(clazz))
        {
            return Contract.AssociatedMinistry.PROJECTION_ALL;
        }

        return super.getFullProjection(clazz);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mapper<T> getMapper(@NonNull final Class<T> clazz)
    {
        if(Ministry.class.equals(clazz))
        {
            return (Mapper<T>) ALL_MINISTRIES_MAPPER;
        }
        else if(AssociatedMinistry.class.equals(clazz))
        {
            return (Mapper<T>) ASSOCIATED_MINISTRIES_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Class<?> clazz, @NonNull final Object... key)
    {
        final String where;

        if(Ministry.class.equals(clazz))
        {
            if (key.length != 1)
            {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.Ministry.SQL_WHERE_PRIMARY_KEY;
        }
        else if(AssociatedMinistry.class.equals(clazz))
        {
            if (key.length != 1)
            {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.AssociatedMinistry.SQL_WHERE_PRIMARY_KEY;
        }
        else
        {
            return super.getPrimaryKeyWhere(clazz, key);
        }

        // return where clause pair
        return Pair.create(where, this.getBindValues(key));
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Object obj)
    {
        if(obj instanceof AssociatedMinistry)
        {
            return getPrimaryKeyWhere(AssociatedMinistry.class, ((AssociatedMinistry) obj).getMinistryId());
        }
        else if(obj instanceof Ministry)
        {
            return getPrimaryKeyWhere(Ministry.class, ((Ministry) obj).getMinistryId());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public List<AssociatedMinistry> retrieveAssociatedMinistriesList()
    {
        Log.i(TAG, "Retrieving associated ministries");
        
        Cursor cursor = null;
        List<AssociatedMinistry> ministryList = new ArrayList<AssociatedMinistry>();

        try
        {
            cursor = getCursor(AssociatedMinistry.class);

            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    ministryList.add(buildAssociatedMinistryFromCursor(cursor));
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
    
    public Assignment retrieveCurrentAssignment(AssociatedMinistry ministry)
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

    private AssociatedMinistry buildAssociatedMinistryFromCursor(Cursor cursor)
    {
        AssociatedMinistry associatedMinistry = new AssociatedMinistry();
        associatedMinistry.setName(cursor.getString(cursor.getColumnIndex("name")));
        associatedMinistry.setMinistryId(cursor.getString(cursor.getColumnIndex("ministry_id")));
        associatedMinistry.setMinistryCode(cursor.getString(cursor.getColumnIndex("min_code")));
        associatedMinistry.setHasGcm(intToBoolean(cursor.getInt(cursor.getColumnIndex("has_gcm"))));
        associatedMinistry.setHasSlm(intToBoolean(cursor.getInt(cursor.getColumnIndex("has_slm"))));
        associatedMinistry.setHasDs(intToBoolean(cursor.getInt(cursor.getColumnIndex("has_ds"))));
        associatedMinistry.setHasLlm(intToBoolean(cursor.getInt(cursor.getColumnIndex("has_llm"))));
        associatedMinistry.setSubMinistries(retrieveMinistriesWithParent(associatedMinistry.getMinistryId()));

        return associatedMinistry;
    }

    private Assignment buildAssignmentFromCursor(Cursor cursor, AssociatedMinistry ministry)
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

        if (parentId != null) ministry.setParentId(parentId);

        return ministry;
    }

    public List<AssociatedMinistry> retrieveMinistriesWithParent(String parentMinistryId)
    {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        List<AssociatedMinistry> ministryList = null;

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
                ministryList = new ArrayList<AssociatedMinistry>();
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    ministryList.add(buildAssociatedMinistryFromCursor(cursor));
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
            cursor = getCursor(AssociatedMinistry.class);

            if(cursor.getCount() > 0)
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
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
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
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        try
        {
            database.beginTransaction();

            Cursor existingAssignments = retrieveAssignmentsCursor();
            Cursor existingAssociations = getCursor(AssociatedMinistry.class);

            if(existingAssignments != null && existingAssignments.getCount() > 0)
            {
                // We need to insert associations that don't yet exist, and update those that do
                for(Assignment assignment : assignmentList)
                {
                    AssociatedMinistry associatedMinistry = assignment.getMinistry();

                    ContentValues assignmentValues = buildAssignmentValues(assignment);

                    if(assignmentExistsInDatabase(assignment.getId(), existingAssignments))
                    {
                        insertOrUpdateAssociatedMinistry(associatedMinistry, null, database, existingAssociations);

                        String[] whereArgs = { associatedMinistry.getMinistryId() };

                        database.update(
                            TableNames.ASSIGNMENTS.getTableName(),
                            assignmentValues,
                            "ministry_id = ?",
                            whereArgs);
                    }
                    else
                    {
                        insertOrUpdateAssociatedMinistry(associatedMinistry, null, database, existingAssociations);
                        database.insert(TableNames.ASSIGNMENTS.getTableName(), null, assignmentValues);
                    }
                }
            }
            else
            {
                for(Assignment assignment : assignmentList)
                {
                    AssociatedMinistry associatedMinistry = assignment.getMinistry();

                    ContentValues assignmentValues = buildAssignmentValues(assignment);

                    insertOrUpdateAssociatedMinistry(associatedMinistry, null, database, existingAssociations);
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

    void insertOrUpdateAssociatedMinistry(
        AssociatedMinistry associatedMinistry,
        String parentMinistryId,
        SQLiteDatabase database,
        Cursor existingAssociatedMinistries)
    {
        if(associatedMinistry.getSubMinistries() != null)
        {
            for(AssociatedMinistry subMinistry : associatedMinistry.getSubMinistries())
            {
                insertOrUpdateAssociatedMinistry(
                    subMinistry,
                    associatedMinistry.getMinistryId(),
                    database,
                    existingAssociatedMinistries);
            }
        }

        ContentValues subMinistryValues = buildAssociationValues(associatedMinistry);
        subMinistryValues.put("parent_ministry_id", parentMinistryId);

        // If the ministry already exists in the database, update it, otherwise insert it
        if(ministryExistsInDatabase(associatedMinistry.getMinistryId(), existingAssociatedMinistries))
        {
            String[] whereArgs = { associatedMinistry.getMinistryId() };
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

    private ContentValues buildAssociationValues(AssociatedMinistry associatedMinistry)
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
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

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
        for(Ministry ministry : allMinistries)
        {
            updateOrInsert(ministry, Contract.Ministry.PROJECTION_ALL);
        }
    }
    
    
}
