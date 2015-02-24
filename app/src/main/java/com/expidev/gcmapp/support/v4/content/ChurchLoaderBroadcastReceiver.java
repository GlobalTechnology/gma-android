package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.EXTRA_CHURCH_IDS;
import static com.expidev.gcmapp.Constants.EXTRA_MINISTRY_ID;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import org.ccci.gto.android.common.support.v4.content.LoaderBroadcastReceiver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChurchLoaderBroadcastReceiver extends LoaderBroadcastReceiver {
    @Nullable
    private final String mMinistryId;
    @NonNull
    private final Set<Long> mIds;

    public ChurchLoaderBroadcastReceiver(@NonNull final Loader loader, @NonNull final long... ids) {
        this(loader, null, ids);
    }

    public ChurchLoaderBroadcastReceiver(@NonNull final Loader loader, @Nullable final String ministryId,
                                         @NonNull final long... ids) {
        super(loader);
        mMinistryId = ministryId;
        if (ids.length == 0) {
            mIds = Collections.emptySet();
        } else {
            mIds = new HashSet<>(ids.length);
            for (final long id : ids) {
                mIds.add(id);
            }
        }
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        final boolean ministryMatch =
                mMinistryId == null || mMinistryId.equals(intent.getStringExtra(EXTRA_MINISTRY_ID));
        boolean idsMatch = mIds.isEmpty();
        if (!idsMatch) {
            final long[] ids = intent.getLongArrayExtra(EXTRA_CHURCH_IDS);
            for (final long id : ids) {
                idsMatch = mIds.contains(id);
                if (idsMatch) {
                    break;
                }
            }
        }

        // only process intent if ministry and ids match
        if (ministryMatch && idsMatch) {
            super.onReceive(context, intent);
        }
    }
}
