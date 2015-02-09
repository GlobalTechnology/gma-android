package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Ministry;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class MinistriesMapper extends BaseMapper<Ministry>
{
    @Override
    protected void mapField(
        @NonNull final ContentValues values,
        @NonNull final String field,
        @NonNull final Ministry ministry)
    {
        switch (field)
        {
            case Contract.Ministry.COLUMN_MINISTRY_ID:
                values.put(field, ministry.getMinistryId());
                break;
            case Contract.Ministry.COLUMN_NAME:
                values.put(field, ministry.getName());
                break;
            default:
                super.mapField(values, field, ministry);
                break;
        }
        super.mapField(values, field, ministry);
    }

    @NonNull
    @Override
    protected Ministry newObject(@NonNull final Cursor cursor)
    {
        return new Ministry();
    }

    @NonNull
    @Override
    public Ministry toObject(@NonNull final Cursor cursor)
    {
        final Ministry ministry = new Ministry();

        ministry.setMinistryId(this.getString(cursor, Contract.Ministry.COLUMN_MINISTRY_ID, "NO ID"));
        ministry.setName(this.getString(cursor, Contract.Ministry.COLUMN_NAME));

        return ministry;
    }
}
