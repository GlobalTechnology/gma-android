package com.expidev.gcmapp.utils;

import static org.ccci.gto.android.common.db.util.CursorUtils.getString;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

public class CursorUtils {
    @Nullable
    public static YearMonth getYearMonth(@NonNull final Cursor c, @NonNull final String field,
                                         @Nullable final YearMonth defValue) {
        final String raw = getString(c, field, null);
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

    @Nullable
    public static LocalDate getLocalDate(@NonNull final Cursor c, @NonNull final String field,
                                         @Nullable final LocalDate defValue) {
        final String raw = getString(c, field, null);
        if (raw != null) {
            try {
                return LocalDate.parse(raw);
            } catch (final Exception ignored) {

            }
        }
        return defValue;
    }

    @NonNull
    public static LocalDate getNonNullLocalDate(@NonNull final Cursor c, @NonNull final String field,
                                                @NonNull final LocalDate defValue) {
        final LocalDate val = getLocalDate(c, field, defValue);
        return val != null ? val : defValue;
    }
}
