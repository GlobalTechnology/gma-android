package com.expidev.gcmapp.model.measurement;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    @Nullable
    private MeasurementType type;

    @NonNull
    private final String ministryId;
    @NonNull
    private final String permLinkStub;
    @NonNull
    private final Ministry.Mcc mcc;
    @NonNull
    private final YearMonth period;
    private int value = 0;

    protected MeasurementValue(@NonNull final String ministryId, @NonNull final Ministry.Mcc mcc,
                               @NonNull final String permLinkStub, @NonNull final YearMonth period) {
        this.ministryId = ministryId;
        this.mcc = mcc;
        this.permLinkStub = permLinkStub;
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
    public String getPermLinkStub() {
        return permLinkStub;
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

    @Nullable
    public MeasurementType getType() {
        return type;
    }

    public void setType(@Nullable final MeasurementType type) {
        this.type = type;
    }
}
