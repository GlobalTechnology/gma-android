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
