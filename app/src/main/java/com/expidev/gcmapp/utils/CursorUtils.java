package com.expidev.gcmapp.utils;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.YearMonth;

public class CursorUtils {
    @Nullable
    public static YearMonth getYearMonth(@NonNull final Cursor c, @NonNull final String field,
                                         @Nullable final YearMonth defValue) {
        final String raw = org.ccci.gto.android.common.util.CursorUtils.getString(c, field, null);
        if (raw != null) {
            try {
                return YearMonth.parse(raw);
            } catch (final Exception ignored) {

            }
        }
        return defValue;
    }

    @NonNull
    public static YearMonth getNonNullYearMonth(@NonNull final Cursor c, @NonNull final String field,
                                                @NonNull final YearMonth defValue) {
        final YearMonth val = getYearMonth(c, field, defValue);
        return val != null ? val : defValue;
    }
}
