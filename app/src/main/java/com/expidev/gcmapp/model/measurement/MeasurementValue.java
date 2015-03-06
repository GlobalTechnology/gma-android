package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Base;
import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;

public abstract class MeasurementValue extends Base {
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
