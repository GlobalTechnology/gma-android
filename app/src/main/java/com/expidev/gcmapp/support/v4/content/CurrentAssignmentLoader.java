package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;
import static com.expidev.gcmapp.Constants.PREF_CURRENT_MINISTRY;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverSharedPreferencesChangeLoader;

import java.util.EnumSet;
import java.util.List;

public class CurrentAssignmentLoader extends AsyncTaskBroadcastReceiverSharedPreferencesChangeLoader<Assignment> {
    public static final String ARG_LOAD_MINISTRY = CurrentMinistryLoader.class.getSimpleName() + ".ARG_LOAD_MINISTRY";

    private final MinistriesDao mDao;

    //TODO: utilize guid when loading assignments
    @Nullable
    private final String mGuid;
    private final boolean mLoadMinistry;

    public CurrentAssignmentLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context, context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE));
        this.addIntentFilter(BroadcastUtils.updateAssignmentsFilter());
        this.addPreferenceKey(PREF_CURRENT_MINISTRY);
        mDao = MinistriesDao.getInstance(context);
        mGuid = args != null ? args.getString(ARG_GUID) : null;
        mLoadMinistry = args != null && args.getBoolean(ARG_LOAD_MINISTRY, false);
    }

    @Override
    public Assignment loadInBackground() {
        // load the current active assignment
        final String ministryId = mPrefs.getString(PREF_CURRENT_MINISTRY, Ministry.INVALID_ID);
        final List<Assignment> assignments =
                mDao.get(Assignment.class, Contract.Assignment.SQL_WHERE_MINISTRY, bindValues(ministryId));
        Assignment assignment = assignments.size() > 0 ? assignments.get(0) : null;

        // reset to default assignment if a current current assignment isn't found
        if (assignment == null) {
            assignment = initActiveAssignment();
        }

        // load the associated ministry if required
        if (assignment != null && mLoadMinistry) {
            loadMinistry(assignment);
        }

        //TODO: validate MCC when possible

        // return the assignment
        return assignment;
    }

    private Assignment initActiveAssignment() {
        final List<Assignment> assignments = mDao.get(Assignment.class);

        // short-circuit if there are no assignments for the current user
        if (assignments.size() == 0) {
            return null;
        }

        // find the default assignment based on role
        Assignment assignment = null;
        for (final Assignment current : assignments) {
            // XXX: this currently relies on Roles being ordered from most important to least important
            if (assignment == null || current.getRole().ordinal() < assignment.getRole().ordinal()) {
                assignment = current;
            }
        }
        assert assignment != null : "there is at least 1 assignment in assignments, so this should never be null";

        // set an MCC if one is not already selected
        if (assignment.getMcc() == Ministry.Mcc.UNKNOWN) {
            updateMcc(assignment);
        }

        mPrefs.edit().putString(PREF_CURRENT_MINISTRY, assignment.getMinistryId()).apply();

        // return the found assignment
        return assignment;
    }

    private void updateMcc(@NonNull final Assignment assignment) {
        loadMinistry(assignment);

        // set the MCC based off of what is available for the ministry
        final AssociatedMinistry ministry = assignment.getMinistry();
        if (ministry != null) {
            // pick a random MCC
            final EnumSet<Ministry.Mcc> mccs = ministry.getMccs();
            assignment.setMcc(mccs.size() > 0 ? mccs.iterator().next() : Ministry.Mcc.UNKNOWN);

            // update the assignment
            mDao.update(assignment, new String[] {Contract.Assignment.COLUMN_MCC});

            //TODO: should we broadcast this update?
        }
    }

    private void loadMinistry(@NonNull final Assignment assignment) {
        if (assignment.getMinistry() == null) {
            assignment.setMinistry(mDao.find(AssociatedMinistry.class, assignment.getMinistryId()));
        }
    }
}
