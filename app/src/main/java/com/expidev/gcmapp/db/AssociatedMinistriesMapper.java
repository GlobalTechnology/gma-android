package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.AssociatedMinistry;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class AssociatedMinistriesMapper extends BaseMapper<AssociatedMinistry>
{
    @Override
    protected void mapField(
        @NonNull final ContentValues values,
        @NonNull final String field,
        @NonNull final AssociatedMinistry ministry)
    {
        switch (field)
        {
            case Contract.AssociatedMinistry.COLUMN_MINISTRY_ID:
                values.put(field, ministry.getMinistryId());
                break;
            case Contract.AssociatedMinistry.COLUMN_NAME:
                values.put(field, ministry.getName());
                break;
            case Contract.AssociatedMinistry.COLUMN_MIN_CODE:
                values.put(field, ministry.getMinistryCode());
                break;
            case Contract.AssociatedMinistry.COLUMN_HAS_SLM:
                values.put(field, ministry.hasSlm() ? 1 : 0);
                break;
            case Contract.AssociatedMinistry.COLUMN_HAS_LLM:
                values.put(field, ministry.hasLlm() ? 1 : 0);
                break;
            case Contract.AssociatedMinistry.COLUMN_HAS_DS:
                values.put(field, ministry.hasDs() ? 1 : 0);
                break;
            case Contract.AssociatedMinistry.COLUMN_HAS_GCM:
                values.put(field, ministry.hasGcm() ? 1 : 0);
                break;
            case Contract.AssociatedMinistry.COLUMN_PARENT_MINISTRY_ID:
                values.put(field, ministry.getParentMinistryId());
                break;
            default:
                super.mapField(values, field, ministry);
                break;
        }
    }

    @NonNull
    @Override
    protected AssociatedMinistry newObject(@NonNull final Cursor cursor)
    {
        return new AssociatedMinistry();
    }

    @NonNull
    @Override
    public AssociatedMinistry toObject(@NonNull final Cursor cursor)
    {
        final AssociatedMinistry ministry = super.toObject(cursor);

        ministry.setMinistryId(this.getString(cursor, Contract.AssociatedMinistry.COLUMN_MINISTRY_ID, "NO ID"));
        ministry.setParentMinistryId(this.getString(cursor, Contract.AssociatedMinistry.COLUMN_PARENT_MINISTRY_ID, null));
        ministry.setName(this.getString(cursor, Contract.AssociatedMinistry.COLUMN_NAME, null));
        ministry.setMinistryCode(this.getString(cursor, Contract.AssociatedMinistry.COLUMN_MIN_CODE, null));
        ministry.setHasGcm(this.getBool(cursor, Contract.AssociatedMinistry.COLUMN_HAS_GCM, false));
        ministry.setHasSlm(this.getBool(cursor, Contract.AssociatedMinistry.COLUMN_HAS_SLM, false));
        ministry.setHasDs(this.getBool(cursor, Contract.AssociatedMinistry.COLUMN_HAS_DS, false));
        ministry.setHasLlm(this.getBool(cursor, Contract.AssociatedMinistry.COLUMN_HAS_LLM, false));

        return ministry;
    }
}
