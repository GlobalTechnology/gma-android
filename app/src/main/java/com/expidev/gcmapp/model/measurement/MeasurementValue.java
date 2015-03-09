package com.expidev.gcmapp.model.measurement;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Base;
import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class MeasurementValue extends Base {
    public static final int TYPE_PERSONAL = 1;
    public static final int TYPE_LOCAL = 2;

    @IntDef({TYPE_PERSONAL, TYPE_LOCAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ValueType {}

    @NonNull
    private final String ministryId;
    @NonNull
    private final String permLink;
    @NonNull
    private final Ministry.Mcc mcc;
    @NonNull
    private final YearMonth period;
    private int value = 0;

    protected MeasurementValue(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                               @NonNull final String permLink, @NonNull final YearMonth period) {
        this.ministryId = ministryId;
        this.mcc = mcc;
        this.permLink = permLink;
        this.period = period;
    }

    @NonNull
    public String getMinistryId() {
        return ministryId;
    }

    @NonNull
    public Ministry.Mcc getMcc() {
        return mcc;
    }

    @NonNull
    public String getPermLink() {
        return permLink;
    }

    @NonNull
    public YearMonth getPeriod() {
        return period;
    }

    public int getValue() {
        return value;
    }

    public void setValue(final int value) {
        this.value = value;
    }
}
