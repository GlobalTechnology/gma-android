package com.expidev.gcmapp.support.v4.content;

import android.content.Context;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.Church;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;

public class ChurchLoader extends AsyncTaskBroadcastReceiverLoader<Church> {
    private final MinistriesDao mDao;

    private final long mId;

    public ChurchLoader(@NonNull final Context context, final long churchId) {
        super(context);
        setBroadcastReceiver(new ChurchLoaderBroadcastReceiver(this, churchId));
        mDao = MinistriesDao.getInstance(context);
        mId = churchId;
    }

    @Override
    public Church loadInBackground() {
        if (mId != Church.INVALID_ID) {
            return mDao.find(Church.class, mId);
        }
        return null;
    }
}
