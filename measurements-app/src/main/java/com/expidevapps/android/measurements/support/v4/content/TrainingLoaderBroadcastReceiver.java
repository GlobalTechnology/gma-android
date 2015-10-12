package com.expidevapps.android.measurements.support.v4.content;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import org.ccci.gto.android.common.support.v4.content.LoaderBroadcastReceiver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.EXTRA_TRAINING_IDS;

/**
 * Created by matthewfrederick on 2/24/15.
 */
public class TrainingLoaderBroadcastReceiver extends LoaderBroadcastReceiver
{
    @Nullable
    private final String mMinistryId;
    @NonNull
    private final Set<Long> mIds;
    
    public TrainingLoaderBroadcastReceiver(@NonNull Loader loader, @NonNull final long... ids)
    {
        this(loader, null, ids);
    }
    
    public TrainingLoaderBroadcastReceiver(@NonNull final Loader loader, @Nullable final String ministryId,
                                           @NonNull final long... ids)
    {
        super(loader);
        mMinistryId = ministryId;
        if (ids.length == 0)
        {
            mIds = Collections.emptySet();
        }
        else
        {
            mIds = new HashSet<>(ids.length);
            for (final long id : ids)
            {
                mIds.add(id);
            }
        }
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent)
    {
        final boolean ministryMatch = mMinistryId == null || mMinistryId.equals(intent.getStringExtra(EXTRA_MINISTRY_ID));
        boolean idsMatch = mIds.isEmpty();
        if (!idsMatch)
        {
            final long[] ids = intent.getLongArrayExtra(EXTRA_TRAINING_IDS);
            for (final long id : ids)
            {
                idsMatch = mIds.contains(ids);
                if (idsMatch) break;
            }
        }
        
        if (ministryMatch && idsMatch) super.onReceive(context, intent);
    }
}
