package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.sql.TableNames;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;
import org.ccci.gto.android.common.util.CursorUtils;

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

    @NonNull
    public List<AssociatedMinistry> retrieveAssociatedMinistriesList()
    {
        final List<AssociatedMinistry> ministries = this.get(AssociatedMinistry.class);

        // populate sub-ministries list
        for (final AssociatedMinistry ministry : ministries) {
            ministry.setSubMinistries(this.retrieveMinistriesWithParent(ministry.getMinistryId()));
        }

        return ministries;
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

    private Assignment buildAssignmentFromCursor(Cursor cursor, AssociatedMinistry ministry)
    {
        Assignment assignment = new Assignment();
        assignment.setId(cursor.getString(cursor.getColumnIndex("ministry_id")));
        assignment.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
        assignment.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
        assignment.setMinistry(ministry);
        assignment.setLocationZoom(cursor.getInt(cursor.getColumnIndex("location_zoom")));
        assignment.setRole(cursor.getString(cursor.getColumnIndex("team_role")));
        
        return assignment;
    }

    @Nullable
    public List<AssociatedMinistry> retrieveMinistriesWithParent(final String parentMinistryId) {
        try
        {
            final List<AssociatedMinistry> ministries =
                    this.get(AssociatedMinistry.class, Contract.AssociatedMinistry.SQL_WHERE_PARENT,
                             new String[] {parentMinistryId});

            // post process Ministries found to populate sub-ministries
            for (final AssociatedMinistry ministry : ministries) {
                ministry.setSubMinistries(this.retrieveMinistriesWithParent(ministry.getMinistryId()));
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, "Failed to retrieve associated ministries: " + e.getMessage());
        }

        return null;
    }

    @NonNull
    public List<String> retrieveAssociatedMinistries()
    {
        // fetch the names of all AssociatedMinistries
        final Cursor c = this.getCursor(AssociatedMinistry.class,
                                        new String[] {Contract.AssociatedMinistry.COLUMN_NAME}, null, null, null);

        // process names into a list
        final List<String> ministries = new ArrayList<>(c.getCount());
        while (c.moveToNext()) {
            // XXX: this will currently include null names
            ministries.add(CursorUtils.getString(c, Contract.AssociatedMinistry.COLUMN_NAME, null));
        }
        c.close();

        return ministries;
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

            if(existingAssignments != null && existingAssignments.getCount() > 0)
            {
                // We need to insert associations that don't yet exist, and update those that do
                for(Assignment assignment : assignmentList)
                {
                    AssociatedMinistry associatedMinistry = assignment.getMinistry();

                    ContentValues assignmentValues = buildAssignmentValues(assignment);

                    if(assignmentExistsInDatabase(assignment.getId(), existingAssignments))
                    {
                        associatedMinistry.setParentMinistryId(null);
                        insertOrUpdateAssociatedMinistry(associatedMinistry);

                        String[] whereArgs = { associatedMinistry.getMinistryId() };

                        database.update(
                            TableNames.ASSIGNMENTS.getTableName(),
                            assignmentValues,
                            "ministry_id = ?",
                            whereArgs);
                    }
                    else
                    {
                        associatedMinistry.setParentMinistryId(null);
                        insertOrUpdateAssociatedMinistry(associatedMinistry);
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

                    associatedMinistry.setParentMinistryId(null);
                    insertOrUpdateAssociatedMinistry(associatedMinistry);
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

    void insertOrUpdateAssociatedMinistry(final AssociatedMinistry ministry) {
        // insert this AssociatedMinistry
        this.updateOrInsert(ministry, Contract.AssociatedMinistry.PROJECTION_ALL);

        // process any sub ministries
        for (final AssociatedMinistry subMinistry : ministry.getSubMinistries()) {
            subMinistry.setParentMinistryId(ministry.getMinistryId());
            this.insertOrUpdateAssociatedMinistry(subMinistry);
        }
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

    private ContentValues buildAssignmentValues(Assignment assignment)
    {
        ContentValues assignmentValues = new ContentValues();

        assignmentValues.put("id", assignment.getId());
        assignmentValues.put("team_role", assignment.getRole().raw);
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
