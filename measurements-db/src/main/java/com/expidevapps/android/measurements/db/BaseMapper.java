package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Base.COLUMN_DELETED;
import static com.expidevapps.android.measurements.db.Contract.Base.COLUMN_DIRTY;
import static com.expidevapps.android.measurements.db.Contract.Base.COLUMN_LAST_SYNCED;
import static com.expidevapps.android.measurements.db.Contract.Base.COLUMN_NEW;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Base;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import java.io.File;

abstract class BaseMapper<T extends Base> extends AbstractMapper<T> {
    @Override
    protected void mapField(@NonNull ContentValues values, @NonNull String field, @NonNull T obj) {
        switch (field) {
            case COLUMN_NEW:
                values.put(field, obj.isNew());
                break;
            case COLUMN_DIRTY:
                values.put(field, obj.getDirty());
                break;
            case COLUMN_DELETED:
                values.put(field, obj.isDeleted());
                break;
            case COLUMN_LAST_SYNCED:
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

        obj.setNew(getBool(c, COLUMN_NEW, false));
        obj.setDirty(getString(c, COLUMN_DIRTY, null));
        obj.setDeleted(getBool(c, COLUMN_DELETED, false));
        obj.setLastSynced(getLong(c, COLUMN_LAST_SYNCED, 0L));

        return obj;
    }

    @Nullable
    protected final File getFile(@NonNull final Cursor c, @NonNull final String field, @Nullable final File defValue) {
        return CursorUtils.getFile(c, field, defValue);
    }

    @Nullable
    protected final YearMonth getYearMonth(@NonNull final Cursor c, @NonNull final String field,
                                           @Nullable final YearMonth defValue) {
        return CursorUtils.getYearMonth(c, field, defValue);
    }

    @NonNull
    protected final YearMonth getNonNullYearMonth(@NonNull final Cursor c, @NonNull final String field,
                                                  @NonNull final YearMonth defValue) {
        return CursorUtils.getNonNullYearMonth(c, field, defValue);
    }

    @Nullable
    protected final LocalDate getLocalDate(@NonNull final Cursor c, @NonNull final String field,
                                           @Nullable final LocalDate defValue) {
        return CursorUtils.getLocalDate(c, field, defValue);
    }

    @NonNull
    protected final LocalDate getNonNullLocalDate(@NonNull final Cursor c, @NonNull final String field,
                                                  @NonNull final LocalDate defValue) {
        return CursorUtils.getNonNullLocalDate(c, field, defValue);
    }
}
