package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Base;

import org.ccci.gto.android.common.db.AbstractMapper;

public abstract class BaseMapper<T extends Base> extends AbstractMapper<T> {
    @Override
    protected void mapField(@NonNull ContentValues values, @NonNull String field, @NonNull T obj) {
        switch (field) {
            case Contract.Base.COLUMN_LAST_SYNCED:
                values.put(field, obj.getLastSynced());
                break;
            default:
                super.mapField(values, field, obj);
                break;
        }
    }

    @NonNull
    @Override
    public T toObject(@NonNull Cursor c) {
        final T obj = super.toObject(c);
        obj.setLastSynced(this.getLong(c, Contract.Base.COLUMN_LAST_SYNCED, 0));
        return obj;
    }
}
