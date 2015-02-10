package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.model.Base;

import org.ccci.gto.android.common.db.AbstractMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private final static SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    protected Date stringToDate(@Nullable final String string) {
        if (string != null) {
            try {
                return FORMAT_DATE.parse(string);
            } catch (final ParseException ignored) {
            }
        }
        return null;
    }

    @Nullable
    protected String dateToString(@Nullable final Date date) {
        return date != null ? FORMAT_DATE.format(date) : null;
    }
}
