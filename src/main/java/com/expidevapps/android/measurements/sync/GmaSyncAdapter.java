package com.expidevapps.android.measurements.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;

import java.util.List;

import me.thekey.android.lib.accounts.AccountUtils;

import static com.expidevapps.android.measurements.sync.BaseSyncTasks.baseExtras;
import static com.expidevapps.android.measurements.sync.BaseSyncTasks.ministryExtras;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

public class GmaSyncAdapter extends AbstractThreadedSyncAdapter {
    static final String EXTRA_SYNCTYPE = GmaSyncAdapter.class.getName() + ".EXTRA_SYNCTYPE";

    // supported sync types
    static final int SYNCTYPE_NONE = 0;
    static final int SYNCTYPE_ALL = 1;
    static final int SYNCTYPE_MINISTRIES = 2;
    static final int SYNCTYPE_ASSIGNMENTS = 3;
    static final int SYNCTYPE_SAVE_ASSIGNMENTS = 4;
    static final int SYNCTYPE_CHURCHES = 5;
    static final int SYNCTYPE_DIRTY_CHURCHES = 6;
    static final int SYNCTYPE_MEASUREMENT_TYPES = 7;
    static final int SYNCTYPE_MEASUREMENTS = 8;
    static final int SYNCTYPE_DIRTY_MEASUREMENTS = 9;
    static final int SYNCTYPE_MEASUREMENT_DETAILS = 10;
    static final int SYNCTYPE_TRAININGS = 11;
    static final int SYNCTYPE_DIRTY_TRAININGS = 12;
    static final int SYNCTYPE_DIRTY_TRAINING_COMPLETIONS = 13;

    private static final Object INSTANCE_LOCK = new Object();
    private static GmaSyncAdapter INSTANCE = null;

    @NonNull
    private final Context mContext;

    private GmaSyncAdapter(@NonNull final Context context, final boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @NonNull
    public static GmaSyncAdapter getInstance(@NonNull final Context context) {
        synchronized (INSTANCE_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = new GmaSyncAdapter(context.getApplicationContext(), true);
            }

            return INSTANCE;
        }
    }

    @Override
    public void onPerformSync(@NonNull final Account account, @Nullable final Bundle extras,
                              @NonNull final String authority, final ContentProviderClient provider,
                              @NonNull final SyncResult result) {
        final String guid = AccountUtils.getGuid(mContext, account);

        if (guid != null) {
            if (extras != null) {
                dispatchSync(guid, extras.getInt(EXTRA_SYNCTYPE, SYNCTYPE_ALL), extras, result);
            } else {
                dispatchSync(guid, SYNCTYPE_ALL, Bundle.EMPTY, result);
            }
        }
    }

    void dispatchSync(@NonNull final String guid, final int type, @NonNull final Bundle extras,
                      @NonNull final SyncResult result) {
        try {
            switch (type) {
                case SYNCTYPE_MINISTRIES:
                    MinistrySyncTasks.syncMinistries(mContext, guid, extras);
                    break;
                case SYNCTYPE_ASSIGNMENTS:
                    AssignmentSyncTasks.syncAssignments(mContext, guid, extras);
                    break;
                case SYNCTYPE_SAVE_ASSIGNMENTS:
                    AssignmentSyncTasks.saveAssignments(mContext, guid, extras, result);
                    break;
                case SYNCTYPE_CHURCHES:
                    ChurchSyncTasks.syncChurches(mContext, guid, extras);
                    break;
                case SYNCTYPE_DIRTY_CHURCHES:
                    ChurchSyncTasks.syncDirtyChurches(mContext, guid, extras, result);
                    break;
                case SYNCTYPE_TRAININGS:
                    TrainingSyncTasks.syncTrainings(mContext, guid, extras);
                    break;
                case SYNCTYPE_DIRTY_TRAININGS:
                    TrainingSyncTasks.syncDirtyTrainings(mContext, guid, extras, result);
                    break;
                case SYNCTYPE_DIRTY_TRAINING_COMPLETIONS:
                    TrainingSyncTasks.syncDirtyTrainingCompletions(mContext, guid, extras, result);
                    break;
                case SYNCTYPE_MEASUREMENT_TYPES:
                    MeasurementSyncTasks.syncMeasurementTypes(mContext, guid, extras);
                    break;
                case SYNCTYPE_MEASUREMENTS:
                    MeasurementSyncTasks.syncMeasurements(mContext, guid, extras);
                    break;
                case SYNCTYPE_MEASUREMENT_DETAILS:
                    MeasurementSyncTasks.syncMeasurementDetails(mContext, guid, extras);
                    break;
                case SYNCTYPE_DIRTY_MEASUREMENTS:
                    MeasurementSyncTasks.syncDirtyMeasurements(mContext, guid, extras, result);
                    break;
                case SYNCTYPE_ALL:
                    syncAll(guid, extras, result);
                    break;
                case SYNCTYPE_NONE:
                default:
                    break;
            }
        } catch (final InvalidSessionApiException e) {
            result.stats.numAuthExceptions++;
        } catch (final ApiException e) {
            result.stats.numIoExceptions++;
        }
    }

    private void syncAll(@NonNull final String guid, @NonNull final Bundle extras, @NonNull final SyncResult result) {
        final boolean force = BaseSyncTasks.isForced(extras);

        // sync assignments for this user
        dispatchSync(guid, SYNCTYPE_ASSIGNMENTS, baseExtras(guid, force), result);

        // sync measurement types
        dispatchSync(guid, SYNCTYPE_MEASUREMENT_TYPES, baseExtras(guid, force), result);

        // process all assignments for this user
        final GmaDao dao = GmaDao.getInstance(mContext);
        final List<Assignment> assignments =
                dao.get(Assignment.class, Contract.Assignment.SQL_WHERE_GUID, bindValues(guid));
        for (final Assignment assignment : assignments) {
            // sync churches for this assignment
            dispatchSync(guid, SYNCTYPE_CHURCHES, ministryExtras(guid, assignment.getMinistryId(), force), result);

            // retrieve the actual ministry object
            final Ministry ministry = dao.find(Ministry.class, assignment.getMinistryId());
            if (ministry == null) {
                continue;
            }

            // sync mcc specific data for this ministry
            for (final Mcc mcc : ministry.getMccs()) {
                // sync trainings for this mcc
                dispatchSync(guid, SYNCTYPE_TRAININGS, ministryExtras(guid, ministry.getMinistryId(), mcc, force),
                             result);

                // sync measurements for the current period
                dispatchSync(guid, SYNCTYPE_MEASUREMENTS, ministryExtras(guid, ministry.getMinistryId(), mcc, force),
                             result);
            }
        }
    }
}
