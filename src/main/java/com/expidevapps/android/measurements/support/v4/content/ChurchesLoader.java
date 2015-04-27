package com.expidevapps.android.measurements.support.v4.content;

import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.service.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

import java.util.List;

public class ChurchesLoader extends AsyncTaskBroadcastReceiverLoader<List<Church>> {
    private final GmaDao mDao;
    @NonNull
    private final String mMinistryId;

    public ChurchesLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);
        mDao = GmaDao.getInstance(context);
        final String ministryId = args != null ? args.getString(ARG_MINISTRY_ID) : null;
        mMinistryId = ministryId != null ? ministryId : Ministry.INVALID_ID;

        // configure Loader if we have a valid ministryId
        if (!mMinistryId.equals(Ministry.INVALID_ID)) {
            addIntentFilter(BroadcastUtils.updateChurchesFilter(mMinistryId));
        }
    }

    @Override
    public List<Church> loadInBackground() {
        if (!Ministry.INVALID_ID.equals(mMinistryId)) {
            return mDao.get(Church.class, Contract.Church.SQL_WHERE_MINISTRY, new String[] {mMinistryId});
        }
        return null;
    }
}
