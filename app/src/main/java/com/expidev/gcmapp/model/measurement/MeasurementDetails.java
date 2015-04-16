package com.expidev.gcmapp.model.measurement;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.model.Base;
import com.expidev.gcmapp.model.Ministry;

import java.io.Serializable;
import java.util.List;

@Deprecated
public class MeasurementDetails extends Base implements Serializable {
    private static final long serialVersionUID = 0L;

    private String measurementId;
    @NonNull
    private String ministryId = Ministry.INVALID_ID;
    @NonNull
    private Ministry.Mcc mcc = Ministry.Mcc.UNKNOWN;
    private String period;
    private MeasurementTypeIds measurementTypeIds;

    private List<SixMonthAmounts> sixMonthTotalAmounts;
    private List<SixMonthAmounts> sixMonthLocalAmounts;
    private List<SixMonthAmounts> sixMonthPersonalAmounts;

    private List<BreakdownData> localBreakdown;
    private List<BreakdownData> selfBreakdown;

    private List<TeamMemberDetails> teamMemberDetails;
    private List<SubMinistryDetails> subMinistryDetails;
    private List<TeamMemberDetails> selfAssignedDetails;

    // The editable text boxes
    private int localValue;
    private int personalValue;


    public String getMeasurementId()
    {
        return measurementId;
    }

    public void setMeasurementId(String measurementId)
    {
        this.measurementId = measurementId;
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
        return mcc;
    }

    public void setMcc(@Nullable final String mcc) {
        setMcc(Ministry.Mcc.fromRaw(mcc));
    }

    public void setMcc(@NonNull final Ministry.Mcc mcc) {
        this.mcc = mcc;
    }

    public String getPeriod()
    {
        return period;
    }

    public void setPeriod(String period)
    {
        this.period = period;
    }

    public MeasurementTypeIds getMeasurementTypeIds()
    {
        return measurementTypeIds;
    }

    public void setMeasurementTypeIds(MeasurementTypeIds measurementTypeIds)
    {
        this.measurementTypeIds = measurementTypeIds;
    }

    public List<SixMonthAmounts> getSixMonthTotalAmounts()
    {
        return sixMonthTotalAmounts;
    }

    public void setSixMonthTotalAmounts(List<SixMonthAmounts> sixMonthTotalAmounts)
    {
        this.sixMonthTotalAmounts = sixMonthTotalAmounts;
    }

    public List<SixMonthAmounts> getSixMonthLocalAmounts()
    {
        return sixMonthLocalAmounts;
    }

    public void setSixMonthLocalAmounts(List<SixMonthAmounts> sixMonthLocalAmounts)
    {
        this.sixMonthLocalAmounts = sixMonthLocalAmounts;
    }

    public List<SixMonthAmounts> getSixMonthPersonalAmounts()
    {
        return sixMonthPersonalAmounts;
    }

    public void setSixMonthPersonalAmounts(List<SixMonthAmounts> sixMonthPersonalAmounts)
    {
        this.sixMonthPersonalAmounts = sixMonthPersonalAmounts;
    }

    public List<BreakdownData> getLocalBreakdown()
    {
        return localBreakdown;
    }

    public void setLocalBreakdown(List<BreakdownData> localBreakdown)
    {
        this.localBreakdown = localBreakdown;
    }

    public List<BreakdownData> getSelfBreakdown()
    {
        return selfBreakdown;
    }

    public void setSelfBreakdown(List<BreakdownData> selfBreakdown)
    {
        this.selfBreakdown = selfBreakdown;
    }

    public List<TeamMemberDetails> getTeamMemberDetails()
    {
        return teamMemberDetails;
    }

    public void setTeamMemberDetails(List<TeamMemberDetails> teamMemberDetails)
    {
        this.teamMemberDetails = teamMemberDetails;
    }

    public List<SubMinistryDetails> getSubMinistryDetails()
    {
        return subMinistryDetails;
    }

    public void setSubMinistryDetails(List<SubMinistryDetails> subMinistryDetails)
    {
        this.subMinistryDetails = subMinistryDetails;
    }

    public List<TeamMemberDetails> getSelfAssignedDetails()
    {
        return selfAssignedDetails;
    }

    public void setSelfAssignedDetails(List<TeamMemberDetails> selfAssignedDetails)
    {
        this.selfAssignedDetails = selfAssignedDetails;
    }

    public int getLocalValue()
    {
        return localValue;
    }

    public void setLocalValue(int localValue)
    {
        this.localValue = localValue;
    }

    public int getPersonalValue()
    {
        return personalValue;
    }

    public void setPersonalValue(int personalValue)
    {
        this.personalValue = personalValue;
    }

    public BreakdownData getTotalLocalBreakdown()
    {
        for(final BreakdownData breakdownData : getLocalBreakdown())
        {
            if("total".equals(breakdownData.getSource()))
            {
                return breakdownData;
            }
        }

        return null;
    }
}
