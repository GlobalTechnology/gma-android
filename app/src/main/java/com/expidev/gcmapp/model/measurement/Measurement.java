package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.model.Base;
import com.expidev.gcmapp.model.Ministry;

import org.joda.time.YearMonth;

import java.io.Serializable;

/**
 * Created by William.Randall on 2/4/2015.
 */
public class Measurement extends Base implements Serializable
{
    private static final long serialVersionUID = 0L;

    @Nullable
    private MeasurementType type;
    private String name;
    private String measurementId;
    private String permLink;
    private boolean custom;
    private String section;
    private String column;
    private int total;
    private MeasurementDetails measurementDetails;
    @NonNull
    private YearMonth period = YearMonth.now();
    @NonNull
    private String ministryId = Ministry.INVALID_ID;
    @NonNull
    private Ministry.Mcc mcc = Ministry.Mcc.UNKNOWN;
    private int sortOrder;

    @Nullable
    public MeasurementType getType() {
        return type;
    }

    public void setType(@Nullable final MeasurementType type) {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getMeasurementId()
    {
        return measurementId;
    }

    public void setMeasurementId(String measurementId)
    {
        this.measurementId = measurementId;
    }

    public String getPermLink()
    {
        return permLink;
    }

    public void setPermLink(String permLink)
    {
        this.permLink = permLink;
    }

    public boolean isCustom()
    {
        return custom;
    }

    public void setCustom(boolean custom)
    {
        this.custom = custom;
    }

    public String getSection()
    {
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    public String getColumn()
    {
        return column;
    }

    public void setColumn(String column)
    {
        this.column = column;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public MeasurementDetails getMeasurementDetails()
    {
        return measurementDetails;
    }

    public void setMeasurementDetails(MeasurementDetails measurementDetails)
    {
        this.measurementDetails = measurementDetails;
    }

    @NonNull
    public YearMonth getPeriod() {
        return period;
    }

    public void setPeriod(@NonNull final YearMonth period) {
        this.period = period;
    }

    @NonNull
    public String getMinistryId()
    {
        return ministryId;
    }

    public void setMinistryId(@NonNull final String ministryId) {
        this.ministryId = ministryId;
    }

    @NonNull
    public Ministry.Mcc getMcc() {
        return this.mcc;
    }

    public void setMcc(@Nullable final String mcc) {
        setMcc(Ministry.Mcc.fromRaw(mcc));
    }

    public void setMcc(@NonNull final Ministry.Mcc mcc) {
        this.mcc = mcc;
    }

    public int getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder)
    {
        this.sortOrder = sortOrder;
    }
}
