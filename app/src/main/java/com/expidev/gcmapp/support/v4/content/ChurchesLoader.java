package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

import java.util.List;

public class ChurchesLoader extends AsyncTaskBroadcastReceiverLoader<List<Church>> {
    private final MinistriesDao mDao;
    @NonNull
    private final String mMinistryId;

    public ChurchesLoader(@NonNull final Context context, @Nullable final Bundle args,
                          @NonNull final IntentFilter... filters) {
        this(context, args != null ? args.getString(ARG_MINISTRY_ID) : null, filters);
    }

    public ChurchesLoader(@NonNull final Context context, @Nullable final String ministryId,
                          @NonNull final IntentFilter... filters) {
        super(context, filters);
        if(ministryId != null) {
            setBroadcastReceiver(new ChurchLoaderBroadcastReceiver(this, ministryId));
            addIntentFilter(BroadcastUtils.updateChurchesFilter());
        }
        mDao = MinistriesDao.getInstance(context);
        mMinistryId = ministryId != null ? ministryId : Ministry.INVALID_ID;
    }

    @Override
    public List<Church> loadInBackground() {
        if (!Ministry.INVALID_ID.equals(mMinistryId)) {
            return mDao.get(Church.class, Contract.Church.SQL_WHERE_MINISTRY, new String[] {mMinistryId});
        }
        return null;
    }
}
