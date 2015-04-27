package com.expidevapps.android.measurements.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;

import org.ccci.gto.android.common.api.ApiException;

public class CreateAssignmentTask extends AsyncTask<Void, Void, Assignment> {
    private final GmaApiClient mApi;
    protected final GmaDao mDao;

    @NonNull
    private final String mMinistryId;
    @NonNull
    private final Assignment.Role mRole;
    @NonNull
    private final String mGuid;

    public CreateAssignmentTask(@NonNull final Context context, @NonNull final String ministryId,
                                @NonNull final Assignment.Role role, @NonNull final String guid) {
        mApi = GmaApiClient.getInstance(context);
        mDao = GmaDao.getInstance(context);
        mMinistryId = ministryId;
        mRole = role;
        mGuid = guid;
    }

    @Override
    protected Assignment doInBackground(final Void... params) {
        // try creating the assignment
        try {
            final Assignment assignment = mApi.createAssignment(mMinistryId, mRole, mGuid);
            if (assignment != null) {
                // TODO: I'm not happy with saving the assignment & ministry here
                // update attached ministry
                final Ministry ministry = assignment.getMinistry();
                if (ministry != null) {
                    mDao.updateOrInsert(ministry);
                }

                // save created assignment
                mDao.updateOrInsert(assignment, Contract.Assignment.PROJECTION_API_CREATE_ASSIGNMENT);

                // return assignment
                return assignment;
            }
        } catch (final ApiException ignored) {
        }

        // something failed, return null
        return null;
    }
}
