package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.GmaDao;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.BroadcastUtils;

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
