package com.expidev.gcmapp.db;

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

    private static final Mapper<Assignment> ASSIGNMENT_MAPPER = new AssignmentMapper();
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
        } else if (Assignment.class.equals(clazz)) {
            return Contract.Assignment.TABLE_NAME;
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
        } else if (Assignment.class.equals(clazz)) {
            return Contract.Assignment.PROJECTION_ALL;
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
        } else if (Assignment.class.equals(clazz)) {
            return (Mapper<T>) ASSIGNMENT_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Class<?> clazz, @NonNull final Object... key)
    {
        final int keyLength;
        final String where;

        if(Ministry.class.equals(clazz))
        {
            keyLength = 1;
            where = Contract.Ministry.SQL_WHERE_PRIMARY_KEY;
        }
        else if(AssociatedMinistry.class.equals(clazz))
        {
            keyLength = 1;
            where = Contract.AssociatedMinistry.SQL_WHERE_PRIMARY_KEY;
        } else if (Assignment.class.equals(clazz)) {
            keyLength = 1;
            where = Contract.Assignment.SQL_WHERE_PRIMARY_KEY;
        }
        else
        {
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
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Object obj)
    {
        if(obj instanceof AssociatedMinistry)
        {
            return getPrimaryKeyWhere(AssociatedMinistry.class, ((AssociatedMinistry) obj).getMinistryId());
        }
        else if(obj instanceof Ministry)
        {
            return getPrimaryKeyWhere(Ministry.class, ((Ministry) obj).getMinistryId());
        } else if (obj instanceof Assignment) {
            return getPrimaryKeyWhere(Assignment.class, ((Assignment) obj).getId());
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

    @Nullable
    public Assignment retrieveCurrentAssignment(@NonNull final AssociatedMinistry ministry) {
        Log.i(TAG, "Looking for assignment with ministryId: " + ministry.getMinistryId());
        
        try
        {
            final List<Assignment> assignments = this.get(Assignment.class, Contract.Assignment.SQL_WHERE_MINISTRY,
                                                          new String[] {ministry.getMinistryId()});
            if (assignments.size() > 0) {
                final Assignment assignment = assignments.get(0);
                assignment.setMinistry(ministry);
                return assignment;
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
        }

        return null;
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

    public void saveAssociatedMinistries(List<Assignment> assignmentList)
    {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        try
        {
            database.beginTransaction();

            for (final Assignment assignment : assignmentList) {
                this.updateOrInsertAssignment(assignment);
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

    public void updateOrInsertAssignment(@NonNull final Assignment assignment) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            // first store the associated ministries
            final AssociatedMinistry associatedMinistry = assignment.getMinistry();
            associatedMinistry.setParentMinistryId(null);
            insertOrUpdateAssociatedMinistry(associatedMinistry);

            // then store the assignment
            this.updateOrInsert(assignment);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void insertOrUpdateAssociatedMinistry(@NonNull final AssociatedMinistry ministry) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            // insert this AssociatedMinistry
            this.updateOrInsert(ministry, Contract.AssociatedMinistry.PROJECTION_ALL);

            // process any sub ministries
            for (final AssociatedMinistry subMinistry : ministry.getSubMinistries()) {
                subMinistry.setParentMinistryId(ministry.getMinistryId());
                this.insertOrUpdateAssociatedMinistry(subMinistry);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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
