package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;
import static com.expidev.gcmapp.Constants.PREF_CURRENT_MINISTRY;

import android.content.Context;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverSharedPreferencesChangeLoader;

public class CurrentMinistryLoader extends AsyncTaskBroadcastReceiverSharedPreferencesChangeLoader<AssociatedMinistry> {
    private final MinistriesDao mDao;

    public CurrentMinistryLoader(@NonNull final Context context) {
        super(context, context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE));
        this.addIntentFilter(BroadcastUtils.updateAssignmentsFilter());
        this.addPreferenceKey(PREF_CURRENT_MINISTRY);
        mDao = MinistriesDao.getInstance(context);
    }

    @Override
    public AssociatedMinistry loadInBackground() {
        final String ministryId = mPrefs.getString(PREF_CURRENT_MINISTRY, null);
        return ministryId != null ? mDao.find(AssociatedMinistry.class, ministryId) : null;
    }
}
