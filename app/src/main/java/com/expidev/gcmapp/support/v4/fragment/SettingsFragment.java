package com.expidev.gcmapp.support.v4.fragment;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;
import static com.expidev.gcmapp.Constants.PREF_CURRENT_MINISTRY;
import static com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader.ARG_LOAD_MINISTRY;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;

import com.expidev.gcmapp.BuildConfig;
import com.expidev.gcmapp.R;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader;
import com.expidev.gcmapp.support.v4.content.MinistriesCursorLoader;
import com.expidev.gcmapp.utils.BroadcastUtils;
import com.github.machinarius.preferencefragment.PreferenceFragment;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.CursorUtils;

import java.util.Collections;
import java.util.Set;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;

public class SettingsFragment extends PreferenceFragment {
    private static final int LOADER_MINISTRIES = 1;
    private static final int LOADER_CURRENT_ASSIGNMENT = 2;

    private TheKey mTheKey;

    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();
    private final CursorLoaderCallbacks mLoaderCallbacksCursor = new CursorLoaderCallbacks();

    @Nullable
    private ListPreference mPrefMinistry;
    @Nullable
    private ListPreference mPrefMcc;

    @Nullable
    private Assignment mCurrentAssignment = null;
    @Nullable
    private Cursor mMinistries = null;

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        mTheKey = TheKeyImpl.getInstance(getActivity(), BuildConfig.THEKEY_CLIENTID);

        getPreferenceManager().setSharedPreferencesName(PREFS_SETTINGS);
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();
        setupPreferences();
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
        args.putString(ARG_GUID, mTheKey.getGuid());
        args.putBoolean(ARG_LOAD_MINISTRY, true);

        manager.initLoader(LOADER_MINISTRIES, args, mLoaderCallbacksCursor);
        manager.initLoader(LOADER_CURRENT_ASSIGNMENT, args, mLoaderCallbacksAssignment);
    }

    private void setupPreferences() {
        mPrefMinistry = (ListPreference) findPreference(PREF_CURRENT_MINISTRY);
        mPrefMcc = (ListPreference) findPreference("mcc_list");
        if (mPrefMcc != null) {
            mPrefMcc.setOnPreferenceChangeListener(new MccChangeListener());
        }

        // init preferences data
        updateMinistriesPreference();
        updateMccsPreference();
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
                    names[i] = CursorUtils.getString(mMinistries, Contract.AssociatedMinistry.COLUMN_NAME, null);
                    ids[i] = CursorUtils.getString(mMinistries, Contract.AssociatedMinistry.COLUMN_MINISTRY_ID, null);
                    i++;
                }
            }

            mPrefMinistry.setEntries(names);
            mPrefMinistry.setEntryValues(ids);
        }
    }

    private void updateMccsPreference() {
        if (mPrefMcc != null) {
            final AssociatedMinistry ministry = mCurrentAssignment != null ? mCurrentAssignment.getMinistry() : null;
            final Set<Ministry.Mcc> mccs = ministry != null ? ministry.getMccs() : Collections.<Ministry.Mcc>emptySet();

            final int count = mccs.size();
            final CharSequence[] names = new CharSequence[count];
            int i = 0;
            for (final Ministry.Mcc mcc : mccs) {
                names[i++] = mcc.name();
            }

            mPrefMcc.setEntries(names);
            mPrefMcc.setEntryValues(names);

            if (mCurrentAssignment != null) {
                mPrefMcc.setValue(mCurrentAssignment.getMcc().toString());
            }
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
                final MinistriesDao dao = MinistriesDao.getInstance(context);
                dao.async(new Runnable() {
                    @Override
                    public void run() {
                        // store changes
                        dao.updateOrInsert(assignment, new String[] {Contract.Assignment.COLUMN_MCC});

                        // broadcast the update
                        broadcastManager.sendBroadcast(BroadcastUtils.updateAssignmentsBroadcast());
                    }
                });
            }

            return true;
        }
    }

    private class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment> {
        @Nullable
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle bundle) {
            switch (id) {
                case LOADER_CURRENT_ASSIGNMENT:
                    return new CurrentAssignmentLoader(getActivity(), bundle);
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
                    return new MinistriesCursorLoader(getActivity());
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
