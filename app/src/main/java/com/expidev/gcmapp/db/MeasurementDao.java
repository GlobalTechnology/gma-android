package com.expidev.gcmapp.db;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.expidev.gcmapp.model.measurement.BreakdownData;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.model.measurement.MeasurementDetailsData;
import com.expidev.gcmapp.model.measurement.MeasurementTypeIds;
import com.expidev.gcmapp.model.measurement.SixMonthAmounts;
import com.expidev.gcmapp.model.measurement.SubMinistryDetails;
import com.expidev.gcmapp.model.measurement.TeamMemberDetails;
import com.expidev.gcmapp.utils.DatabaseOpenHelper;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;

import java.util.List;

/**
 * Created by William.Randall on 2/17/2015.
 */
public class MeasurementDao extends AbstractDao
{
    private static final Object instanceLock = new Object();
    private static MeasurementDao instance;

    private static final Mapper<Measurement> MEASUREMENT_MAPPER = new MeasurementMapper();
    private static final Mapper<MeasurementDetails> MEASUREMENT_DETAILS_MAPPER = new MeasurementDetailsMapper();
    private static final Mapper<SixMonthAmounts> SIX_MONTH_AMOUNTS_MAPPER = new SixMonthAmountsMapper();
    private static final Mapper<MeasurementTypeIds> MEASUREMENT_TYPE_IDS_MAPPER = new MeasurementTypeIdsMapper();
    private static final Mapper<BreakdownData> BREAKDOWN_DATA_MAPPER = new BreakdownDataMapper();
    private static final Mapper<TeamMemberDetails> TEAM_MEMBER_DETAILS_MAPPER = new TeamMemberDetailsMapper();
    private static final Mapper<SubMinistryDetails> SUB_MINISTRY_DETAILS_MAPPER = new SubMinistryDetailsMapper();

    private MeasurementDao(final Context context)
    {
        super(DatabaseOpenHelper.getInstance(context));
    }

    public static MeasurementDao getInstance(Context context)
    {
        synchronized(instanceLock)
        {
            if(instance == null)
            {
                instance = new MeasurementDao(context.getApplicationContext());
            }
        }

        return instance;
    }

    @NonNull
    @Override
    protected String getTable(@NonNull final Class<?> clazz)
    {
        if(Measurement.class.equals(clazz))
        {
            return Contract.Measurement.TABLE_NAME;
        }
        else if(MeasurementDetails.class.equals(clazz))
        {
            return Contract.MeasurementDetails.TABLE_NAME;
        }
        else if(SixMonthAmounts.class.equals(clazz))
        {
            return Contract.SixMonthAmounts.TABLE_NAME;
        }
        else if(MeasurementTypeIds.class.equals(clazz))
        {
            return Contract.MeasurementTypeIds.TABLE_NAME;
        }
        else if(BreakdownData.class.equals(clazz))
        {
            return Contract.BreakdownData.TABLE_NAME;
        }
        else if(TeamMemberDetails.class.equals(clazz))
        {
            return Contract.TeamMemberDetails.TABLE_NAME;
        }
        else if(SubMinistryDetails.class.equals(clazz))
        {
            return Contract.SubMinistryDetails.TABLE_NAME;
        }

        return super.getTable(clazz);
    }

    @NonNull
    @Override
    protected String[] getFullProjection(@NonNull final Class<?> clazz)
    {
        if(Measurement.class.equals(clazz))
        {
            return Contract.Measurement.PROJECTION_ALL;
        }
        else if(MeasurementDetails.class.equals(clazz))
        {
            return Contract.MeasurementDetails.PROJECTION_ALL;
        }
        else if(SixMonthAmounts.class.equals(clazz))
        {
            return Contract.SixMonthAmounts.PROJECTION_ALL;
        }
        else if(MeasurementTypeIds.class.equals(clazz))
        {
            return Contract.MeasurementTypeIds.PROJECTION_ALL;
        }
        else if(BreakdownData.class.equals(clazz))
        {
            return Contract.BreakdownData.PROJECTION_ALL;
        }
        else if(TeamMemberDetails.class.equals(clazz))
        {
            return Contract.TeamMemberDetails.PROJECTION_ALL;
        }
        else if(SubMinistryDetails.class.equals(clazz))
        {
            return Contract.SubMinistryDetails.PROJECTION_ALL;
        }

        return super.getFullProjection(clazz);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mapper<T> getMapper(@NonNull final Class<T> clazz)
    {
        if(Measurement.class.equals(clazz))
        {
            return (Mapper<T>) MEASUREMENT_MAPPER;
        }
        else if(MeasurementDetails.class.equals(clazz))
        {
            return (Mapper<T>) MEASUREMENT_DETAILS_MAPPER;
        }
        else if(SixMonthAmounts.class.equals(clazz))
        {
            return (Mapper<T>) SIX_MONTH_AMOUNTS_MAPPER;
        }
        else if(MeasurementTypeIds.class.equals(clazz))
        {
            return (Mapper<T>) MEASUREMENT_TYPE_IDS_MAPPER;
        }
        else if(BreakdownData.class.equals(clazz))
        {
            return (Mapper<T>) BREAKDOWN_DATA_MAPPER;
        }
        else if(TeamMemberDetails.class.equals(clazz))
        {
            return (Mapper<T>) TEAM_MEMBER_DETAILS_MAPPER;
        }
        else if(SubMinistryDetails.class.equals(clazz))
        {
            return (Mapper<T>) SUB_MINISTRY_DETAILS_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Class<?> clazz, @NonNull final Object... key)
    {
        final int keyLength;
        final String where;

        if(Measurement.class.equals(clazz))
        {
            keyLength = 1;
            where = Contract.Measurement.SQL_WHERE_PRIMARY_KEY;
        }
        else if(MeasurementDetails.class.equals(clazz))
        {
            keyLength = 4;
            where = Contract.MeasurementDetails.SQL_WHERE_MEASUREMENT;
        }
        else if(MeasurementDetailsData.class.equals(clazz) || SixMonthAmounts.class.equals(clazz) ||
            MeasurementTypeIds.class.equals(clazz) || BreakdownData.class.equals(clazz) ||
            TeamMemberDetails.class.equals(clazz) || SubMinistryDetails.class.equals(clazz))
        {
            keyLength = 1;
            where = Contract.MeasurementDetailsData.SQL_WHERE_MEASUREMENT;
        }
        else
        {
            return super.getPrimaryKeyWhere(clazz, key);
        }

        // throw an error if the provided key is the wrong size
        if (key.length != keyLength)
        {
            throw new IllegalArgumentException("invalid key for " + clazz);
        }

        return Pair.create(where, this.getBindValues(key));
    }

    @NonNull
    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(@NonNull final Object obj)
    {
        if(obj instanceof Measurement)
        {
            return getPrimaryKeyWhere(Measurement.class, ((Measurement) obj).getMeasurementId());
        }
        else if(obj instanceof MeasurementDetails)
        {
            return getPrimaryKeyWhere(
                MeasurementDetails.class,
                ((MeasurementDetails) obj).getMeasurementId(),
                ((MeasurementDetails) obj).getMinistryId(),
                ((MeasurementDetails) obj).getMcc(),
                ((MeasurementDetails) obj).getPeriod());
        }
        else if(obj instanceof MeasurementDetailsData)
        {
            return getPrimaryKeyWhere(MeasurementDetailsData.class, ((MeasurementDetailsData) obj).getMeasurementId());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public void saveMeasurement(Measurement measurement)
    {
        this.updateOrInsert(measurement, Contract.Measurement.PROJECTION_ALL);
    }

    public void saveMeasurementDetails(MeasurementDetails measurementDetails)
    {
        this.updateOrInsert(measurementDetails, Contract.MeasurementDetails.PROJECTION_ALL);
        String measurementId = measurementDetails.getMeasurementId();

        saveSixMonthAmounts(measurementDetails.getSixMonthTotalAmounts(), measurementId);
        saveSixMonthAmounts(measurementDetails.getSixMonthLocalAmounts(), measurementId);
        saveSixMonthAmounts(measurementDetails.getSixMonthPersonalAmounts(), measurementId);

        saveBreakdownData(measurementDetails.getLocalBreakdown(), measurementId);
        saveBreakdownData(measurementDetails.getSelfBreakdown(), measurementId);

        saveTeamMemberDetails(measurementDetails.getTeamMemberDetails(), measurementId);
        saveTeamMemberDetails(measurementDetails.getSelfAssignedDetails(), measurementId);

        saveSubMinistryDetails(measurementDetails.getSubMinistryDetails(), measurementId);
    }

    private void saveSixMonthAmounts(List<SixMonthAmounts> amounts, String measurementId)
    {
        for(SixMonthAmounts row : amounts)
        {
            row.setMeasurementId(measurementId);
            this.updateOrInsert(row, Contract.SixMonthAmounts.PROJECTION_ALL);
        }
    }

    private void saveBreakdownData(List<BreakdownData> breakdownDataList, String measurementId)
    {
        for(BreakdownData row : breakdownDataList)
        {
            row.setMeasurementId(measurementId);
            this.updateOrInsert(row, Contract.BreakdownData.PROJECTION_ALL);
        }
    }

    private void saveTeamMemberDetails(List<TeamMemberDetails> teamMemberDetailsList, String measurementId)
    {
        for(TeamMemberDetails row : teamMemberDetailsList)
        {
            row.setMeasurementId(measurementId);
            this.updateOrInsert(row, Contract.TeamMemberDetails.PROJECTION_ALL);
        }
    }

    private void saveSubMinistryDetails(List<SubMinistryDetails> subMinistryDetailsList, String measurementId)
    {
        for(SubMinistryDetails row : subMinistryDetailsList)
        {
            row.setMeasurementId(measurementId);
            this.updateOrInsert(row, Contract.SubMinistryDetails.PROJECTION_ALL);
        }
    }
}
