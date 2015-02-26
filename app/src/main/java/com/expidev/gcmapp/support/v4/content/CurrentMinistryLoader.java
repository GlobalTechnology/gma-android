package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.PREFS_SETTINGS;
import static com.expidev.gcmapp.Constants.PREF_CURRENT_MINISTRY;

import android.content.Context;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverSharedPreferencesChangeLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // load the set current ministry
        final String ministryId = mPrefs.getString(PREF_CURRENT_MINISTRY, null);
        final AssociatedMinistry ministry = ministryId != null ? mDao.find(AssociatedMinistry.class, ministryId) : null;

        // reset to default ministry if current ministry isn't found
        if (ministry == null) {
            return initCurrentMinistry();
        }

        return ministry;
    }

    private AssociatedMinistry initCurrentMinistry() {
        final List<AssociatedMinistry> ministries = mDao.get(AssociatedMinistry.class);

        // set the default ministry based on how many AssociatedMinistries exist
        AssociatedMinistry ministry;
        switch (ministries.size()) {
            case 0:
                return null;
            case 1:
                ministry = ministries.get(0);
                assert ministry != null;
                break;
            default:
                // convert to a Map for quick lookups
                final Map<String, AssociatedMinistry> ministriesMap = new HashMap<>();
                for (final AssociatedMinistry ministry2 : ministries) {
                    ministriesMap.put(ministry2.getMinistryId(), ministry2);
                }

                // search for root parent for first ministry in list
                ministry = ministries.get(0);
                while (ministriesMap.containsKey(ministry.getParentMinistryId())) {
                    ministry = ministriesMap.get(ministry.getParentMinistryId());
                }
        }

        // save found ministry as the currentMinistry
        mPrefs.edit().putString(PREF_CURRENT_MINISTRY, ministry.getMinistryId()).apply();

        // return the found ministry
        return ministry;
    }
}
