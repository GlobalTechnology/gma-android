package com.expidevapps.android.measurements.support.v4.fragment;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.PREFS_SETTINGS;
import static com.expidevapps.android.measurements.Constants.PREF_ACTIVE_MCC;
import static com.expidevapps.android.measurements.Constants.PREF_ACTIVE_MINISTRY;
import static com.expidevapps.android.measurements.support.v4.content.CurrentAssignmentLoader.ARG_LOAD_MINISTRY;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.support.v4.content.CurrentAssignmentLoader;
import com.expidevapps.android.measurements.support.v4.content.MinistriesCursorLoader;
import com.expidevapps.android.measurements.sync.BroadcastUtils;
import com.github.machinarius.preferencefragment.PreferenceFragment;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.AsyncTaskCompat;

import java.util.Collections;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment {
    private static final int LOADER_MINISTRIES = 1;
    private static final int LOADER_CURRENT_ASSIGNMENT = 2;

    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();
    private final CursorLoaderCallbacks mLoaderCallbacksCursor = new CursorLoaderCallbacks();

    @Nullable
    private ListPreference mPrefMinistry;
    @Nullable
    private ListPreference mPrefMcc;

    @NonNull
    private /* final */ String mGuid;
    @Nullable
    private Assignment mCurrentAssignment = null;
    @Nullable
    private Cursor mMinistries = null;

    public static SettingsFragment newInstance(@NonNull final String guid) {
        final SettingsFragment fragment = new SettingsFragment();

        final Bundle args = new Bundle(1);
        args.putString(ARG_GUID, guid);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        final Bundle args = this.getArguments();
        mGuid = args.getString(ARG_GUID);

        setupPreferences();
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();

        // init preferences data
        updateMinistriesPreference();
        updateMccsPreference();
    }

    public void onLoadCurrentAssignment(@Nullable final Assignment assignment) {
        mCurrentAssignment = assignment;
        updateMccsPreference();
    }

    public void onLoadMinistries(@Nullable final Cursor c) {
        mMinistries = c;
        updateMinistriesPreference();
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();

        // build the args used for various loaders
        final Bundle args = new Bundle(2);
        args.putString(ARG_GUID, mGuid);
        args.putBoolean(ARG_LOAD_MINISTRY, true);

        manager.initLoader(LOADER_MINISTRIES, args, mLoaderCallbacksCursor);
        manager.initLoader(LOADER_CURRENT_ASSIGNMENT, args, mLoaderCallbacksAssignment);
    }

    private void setupPreferences() {
        // load base preferences
        final PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(PREFS_SETTINGS(mGuid));
        addPreferencesFromResource(R.xml.pref_general);

        // get preferences to modify them
        mPrefMinistry = (ListPreference) findPreference(PREF_ACTIVE_MINISTRY);
        if (mPrefMinistry != null) {
            mPrefMinistry.setOnPreferenceChangeListener(new MinistryChangeListener());
        }
        mPrefMcc = (ListPreference) findPreference(PREF_ACTIVE_MCC);
        if (mPrefMcc != null) {
            mPrefMcc.setOnPreferenceChangeListener(new MccChangeListener());
        }
    }

    private void updateMinistriesPreference() {
        if (mPrefMinistry != null) {
            final int count = mMinistries != null ? mMinistries.getCount() : 0;
            final CharSequence[] names = new CharSequence[count];
            final CharSequence[] ids = new CharSequence[count];

            // extract data from Cursor
            if (mMinistries != null) {
                mMinistries.moveToPosition(-1);
                int i = 0;
                while (mMinistries.moveToNext()) {
                    names[i] = CursorUtils.getString(mMinistries, Contract.Ministry.COLUMN_NAME, null);
                    ids[i] = CursorUtils.getNonNullString(mMinistries, Contract.Ministry.COLUMN_MINISTRY_ID,
                                                          Ministry.INVALID_ID);
                    i++;
                }
            }

            mPrefMinistry.setEntries(names);
            mPrefMinistry.setEntryValues(ids);

            // update summary as necessary
            updateMinistryPrefSummary();
        }
    }

    private void updateMccsPreference() {
        if (mPrefMcc != null) {
            final Ministry ministry = mCurrentAssignment != null ? mCurrentAssignment.getMinistry() : null;
            final Set<Ministry.Mcc> mccs = ministry != null ? ministry.getMccs() : Collections.<Ministry.Mcc>emptySet();

            final int count = mccs.size();
            final CharSequence[] names = new CharSequence[count];
            int i = 0;
            for (final Ministry.Mcc mcc : mccs) {
                names[i++] = mcc.name();
            }

            mPrefMcc.setEnabled(count != 0);
            mPrefMcc.setEntries(names);
            mPrefMcc.setEntryValues(names);

            if (mCurrentAssignment != null) {
                mPrefMcc.setValue(mCurrentAssignment.getMcc().toString());
            }

            // update summary as necessary
            updateMccPrefSummary();
        }
    }

    private void updateMinistryPrefSummary() {
        if (mPrefMinistry != null) {
            if (mPrefMinistry.getEntries().length == 0) {
                mPrefMinistry.setSummary(R.string.pref_summary_ministry_none_available);
            } else if (mPrefMinistry.getEntry() == null) {
                mPrefMinistry.setSummary(R.string.pref_summary_ministry_none_set);
            } else {
                mPrefMinistry.setSummary(R.string.pref_summary_ministry);
            }
        }
    }

    private void updateMccPrefSummary() {
        if (mPrefMcc != null) {
            if (mPrefMcc.getEntries().length == 0) {
                mPrefMcc.setSummary(R.string.pref_summary_mcc_none_available);
            } else if (mPrefMcc.getEntry() == null) {
                mPrefMcc.setSummary(R.string.pref_summary_mcc_none_set);
            } else {
                mPrefMcc.setSummary(R.string.pref_summary_mcc);
            }
        }
    }

    private class MinistryChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(final Preference preference, final Object newValue) {
            // only fire if this update is for mPrefMinistry (this is probably over-paranoid)
            if(preference == mPrefMinistry && mPrefMinistry != null) {
                // we need to manually set the preference before updating summary to allow summary to be updated based on correct state
                mPrefMinistry.setValue(newValue != null ? newValue.toString() : null);

                // update summary as necessary
                updateMinistryPrefSummary();

                // we already updated the value, no need to update it again
                return false;
            }

            // this should probably never happen, but if it does we want to let the Preference update normally
            return true;
        }
    }

    private class MccChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (mCurrentAssignment != null) {
                // update the mcc for this assignment
                final Assignment assignment = mCurrentAssignment.clone();
                assignment.setMcc(Ministry.Mcc.fromRaw(newValue.toString()));

                // update assignment on a background thread
                final Context context = getActivity();
                final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                final GmaDao dao = GmaDao.getInstance(context);
                AsyncTaskCompat.execute(new Runnable() {
                    @Override
                    public void run() {
                        // store changes
                        dao.update(assignment, new String[] {Contract.Assignment.COLUMN_MCC});

                        // broadcast the update
                        broadcastManager.sendBroadcast(BroadcastUtils.updateAssignmentsBroadcast(assignment.getGuid()));
                    }
                });
            }

            // update summary as necessary
            updateMccPrefSummary();

            return true;
        }
    }

    private class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment> {
        @Nullable
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle bundle) {
            switch (id) {
                case LOADER_CURRENT_ASSIGNMENT:
                    return new CurrentAssignmentLoader(getActivity(), mGuid, bundle);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Assignment> loader, @Nullable final Assignment assignment) {
            switch (loader.getId()) {
                case LOADER_CURRENT_ASSIGNMENT:
                    onLoadCurrentAssignment(assignment);
                    break;
            }
        }
    }

    private class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MINISTRIES:
                    return new MinistriesCursorLoader(getActivity(), args);
                default:
                    return null;
            }
        }

        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_MINISTRIES:
                    onLoadMinistries(cursor);
                    break;
            }
        }
    }
}
