package com.expidevapps.android.measurements.support.v4.content;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateAssignmentsFilter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;
import org.ccci.gto.android.common.util.BundleCompat;

public class AssignmentLoader extends AsyncTaskBroadcastReceiverLoader<Assignment> {
    @NonNull
    private final GmaDao mDao;

    @Nullable
    private final String mGuid;
    @NonNull
    private final String mMinistryId;

    public AssignmentLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);
        mDao = GmaDao.getInstance(context);

        if (args != null) {
            mGuid = args.getString(ARG_GUID);
            mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, Ministry.INVALID_ID);
        } else {
            mGuid = null;
            mMinistryId = Ministry.INVALID_ID;
        }

        if (mGuid != null) {
            // listen for updates to this assignment
            // XXX: make this more fine grain when possible
            addIntentFilter(updateAssignmentsFilter(mGuid));
        }
    }

    @Nullable
    @Override
    public Assignment loadInBackground() {
        if (mGuid != null && !Ministry.INVALID_ID.equals(mMinistryId)) {
            return mDao.find(Assignment.class, mGuid, mMinistryId);
        }
        return null;
    }
}
