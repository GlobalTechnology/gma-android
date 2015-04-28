package com.expidevapps.android.measurements.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Base;
import com.expidevapps.android.measurements.util.CursorUtils;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

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
