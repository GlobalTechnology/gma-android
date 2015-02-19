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
            keyLength = 4;
            where = Contract.Measurement.SQL_WHERE_UNIQUE;
        }
        else if(MeasurementDetails.class.equals(clazz))
        {
            keyLength = 4;
            where = Contract.MeasurementDetails.SQL_WHERE_MEASUREMENT;
        }
        else if(SixMonthAmounts.class.equals(clazz))
        {
            keyLength = 6;
            where = Contract.SixMonthAmounts.SQL_WHERE_UNIQUE;
        }
        else if(BreakdownData.class.equals(clazz))
        {
            keyLength = 6;
            where = Contract.BreakdownData.SQL_WHERE_UNIQUE;
        }
        else if(TeamMemberDetails.class.equals(clazz))
        {
            keyLength = 6;
            where = Contract.TeamMemberDetails.SQL_WHERE_UNIQUE;
        }
        else if(MeasurementDetailsData.class.equals(clazz) || MeasurementTypeIds.class.equals(clazz) ||
            SubMinistryDetails.class.equals(clazz))
        {
            keyLength = 4;
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
            String mcc = ((Measurement) obj).getMcc();
            return getPrimaryKeyWhere(
                Measurement.class,
                ((Measurement) obj).getMeasurementId(),
                ((Measurement) obj).getMinistryId(),
                mcc != null ? mcc : "SLM",
                ((Measurement) obj).getPeriod());
        }
        else if(obj instanceof MeasurementDetails)
        {
            String mcc = ((MeasurementDetails) obj).getMcc();
            return getPrimaryKeyWhere(
                MeasurementDetails.class,
                ((MeasurementDetails) obj).getMeasurementId(),
                ((MeasurementDetails) obj).getMinistryId(),
                mcc != null ? mcc : "SLM",
                ((MeasurementDetails) obj).getPeriod());
        }
        else if(obj instanceof SixMonthAmounts)
        {
            String mcc = ((SixMonthAmounts) obj).getMcc();
            return getPrimaryKeyWhere(
                SixMonthAmounts.class,
                ((SixMonthAmounts) obj).getMeasurementId(),
                ((SixMonthAmounts) obj).getMinistryId(),
                mcc != null ? mcc : "SLM",
                ((SixMonthAmounts) obj).getPeriod(),
                ((SixMonthAmounts) obj).getMonth(),
                ((SixMonthAmounts) obj).getAmountType());
        }
        else if(obj instanceof BreakdownData)
        {
            String mcc = ((BreakdownData) obj).getMcc();
            return getPrimaryKeyWhere(
                BreakdownData.class,
                ((BreakdownData) obj).getMeasurementId(),
                ((BreakdownData) obj).getMinistryId(),
                mcc != null ? mcc : "SLM",
                ((BreakdownData) obj).getPeriod(),
                ((BreakdownData) obj).getSource(),
                ((BreakdownData) obj).getType());
        }
        else if(obj instanceof TeamMemberDetails)
        {
            String mcc = ((TeamMemberDetails) obj).getMcc();
            return getPrimaryKeyWhere(
                TeamMemberDetails.class,
                ((TeamMemberDetails) obj).getMeasurementId(),
                ((TeamMemberDetails) obj).getMinistryId(),
                mcc != null ? mcc : "SLM",
                ((TeamMemberDetails) obj).getPeriod(),
                ((TeamMemberDetails) obj).getType(),
                ((TeamMemberDetails) obj).getPersonId());
        }
        else if(obj instanceof MeasurementDetailsData)
        {
            String mcc = ((MeasurementDetailsData) obj).getMcc();
            return getPrimaryKeyWhere(
                MeasurementDetailsData.class,
                ((MeasurementDetailsData) obj).getMeasurementId(),
                ((MeasurementDetailsData) obj).getMinistryId(),
                mcc != null ? mcc : "SLM",
                ((MeasurementDetailsData) obj).getPeriod());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public void saveMeasurement(Measurement measurement)
    {
        this.updateOrInsert(measurement, Contract.Measurement.PROJECTION_ALL);
    }

    public void saveMeasurementDetails(MeasurementDetails measurementDetails)
    {
        String measurementId = measurementDetails.getMeasurementId();
        String ministryId = measurementDetails.getMinistryId();
        String mcc = measurementDetails.getMcc();
        String period = measurementDetails.getPeriod();

        this.updateOrInsert(measurementDetails, Contract.MeasurementDetails.PROJECTION_ALL);

        saveMeasurementTypeIds(measurementDetails.getMeasurementTypeIds(), measurementId, ministryId, mcc, period);

        saveSixMonthAmounts(measurementDetails.getSixMonthTotalAmounts(), measurementId, ministryId, mcc, period);
        saveSixMonthAmounts(measurementDetails.getSixMonthLocalAmounts(), measurementId, ministryId, mcc, period);
        saveSixMonthAmounts(measurementDetails.getSixMonthPersonalAmounts(), measurementId, ministryId, mcc, period);

        saveBreakdownData(measurementDetails.getLocalBreakdown(), measurementId, ministryId, mcc, period);
        saveBreakdownData(measurementDetails.getSelfBreakdown(), measurementId, ministryId, mcc, period);

        saveTeamMemberDetails(measurementDetails.getTeamMemberDetails(), measurementId, ministryId, mcc, period);
        saveTeamMemberDetails(measurementDetails.getSelfAssignedDetails(), measurementId, ministryId, mcc, period);

        saveSubMinistryDetails(measurementDetails.getSubMinistryDetails(), measurementId, ministryId, mcc, period);
    }

    private void saveMeasurementTypeIds(
        MeasurementTypeIds ids,
        String measurementId,
        String ministryId,
        String mcc,
        String period)
    {
        ids.setMeasurementId(measurementId);
        ids.setMinistryId(ministryId);
        ids.setMcc(mcc);
        ids.setPeriod(period);
        this.updateOrInsert(ids, Contract.MeasurementTypeIds.PROJECTION_ALL);
    }

    private void saveSixMonthAmounts(
        List<SixMonthAmounts> amounts,
        String measurementId,
        String ministryId,
        String mcc,
        String period)
    {
        for(SixMonthAmounts row : amounts)
        {
            row.setMeasurementId(measurementId);
            row.setMinistryId(ministryId);
            row.setMcc(mcc);
            row.setPeriod(period);
            this.updateOrInsert(row, Contract.SixMonthAmounts.PROJECTION_ALL);
        }
    }

    private void saveBreakdownData(
        List<BreakdownData> breakdownDataList,
        String measurementId,
        String ministryId,
        String mcc,
        String period)
    {
        for(BreakdownData row : breakdownDataList)
        {
            row.setMeasurementId(measurementId);
            row.setMinistryId(ministryId);
            row.setMcc(mcc);
            row.setPeriod(period);
            this.updateOrInsert(row, Contract.BreakdownData.PROJECTION_ALL);
        }
    }

    private void saveTeamMemberDetails(
        List<TeamMemberDetails> teamMemberDetailsList,
        String measurementId,
        String ministryId,
        String mcc,
        String period)
    {
        for(TeamMemberDetails row : teamMemberDetailsList)
        {
            row.setMeasurementId(measurementId);
            row.setMinistryId(ministryId);
            row.setMcc(mcc);
            row.setPeriod(period);
            this.updateOrInsert(row, Contract.TeamMemberDetails.PROJECTION_ALL);
        }
    }

    private void saveSubMinistryDetails(
        List<SubMinistryDetails> subMinistryDetailsList,
        String measurementId,
        String ministryId,
        String mcc,
        String period)
    {
        for(SubMinistryDetails row : subMinistryDetailsList)
        {
            row.setMeasurementId(measurementId);
            row.setMinistryId(ministryId);
            row.setMcc(mcc);
            row.setPeriod(period);
            this.updateOrInsert(row, Contract.SubMinistryDetails.PROJECTION_ALL);
        }
    }
}
