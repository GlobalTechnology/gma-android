package com.expidev.gcmapp.model.measurement;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by William.Randall on 1/29/2015.
 */
public class MeasurementDetails implements Serializable
{
    private static final long serialVersionUID = 0L;

    private MeasurementTypeIds measurementTypeIds;

    private Map<String, Integer> sixMonthTotalAmounts;
    private Map<String, Integer> sixMonthLocalAmounts;
    private Map<String, Integer> sixMonthPersonalAmounts;

    private Map<String, Integer> localBreakdown;
    private Map<String, Integer> selfBreakdown;

    private List<TeamMemberDetails> teamMemberDetails;
    private List<SubMinistryDetails> subMinistryDetails;
    private List<TeamMemberDetails> selfAssignedDetails;

    public MeasurementTypeIds getMeasurementTypeIds()
    {
        return measurementTypeIds;
    }

    public void setMeasurementTypeIds(MeasurementTypeIds measurementTypeIds)
    {
        this.measurementTypeIds = measurementTypeIds;
    }

    public Map<String, Integer> getSixMonthTotalAmounts()
    {
        return sixMonthTotalAmounts;
    }

    public void setSixMonthTotalAmounts(Map<String, Integer> sixMonthTotalAmounts)
    {
        this.sixMonthTotalAmounts = sixMonthTotalAmounts;
    }

    public Map<String, Integer> getSixMonthLocalAmounts()
    {
        return sixMonthLocalAmounts;
    }

    public void setSixMonthLocalAmounts(Map<String, Integer> sixMonthLocalAmounts)
    {
        this.sixMonthLocalAmounts = sixMonthLocalAmounts;
    }

    public Map<String, Integer> getSixMonthPersonalAmounts()
    {
        return sixMonthPersonalAmounts;
    }

    public void setSixMonthPersonalAmounts(Map<String, Integer> sixMonthPersonalAmounts)
    {
        this.sixMonthPersonalAmounts = sixMonthPersonalAmounts;
    }

    public Map<String, Integer> getLocalBreakdown()
    {
        return localBreakdown;
    }

    public void setLocalBreakdown(Map<String, Integer> localBreakdown)
    {
        this.localBreakdown = localBreakdown;
    }

    public Map<String, Integer> getSelfBreakdown()
    {
        return selfBreakdown;
    }

    public void setSelfBreakdown(Map<String, Integer> selfBreakdown)
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
}
