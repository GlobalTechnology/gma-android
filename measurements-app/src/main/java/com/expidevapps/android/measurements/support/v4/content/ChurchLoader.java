package com.expidevapps.android.measurements.support.v4.content;

import static com.expidevapps.android.measurements.Constants.EXTRA_CHURCH_IDS;

import android.content.Context;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.sync.BroadcastUtils;

import org.ccci.gto.android.common.content.IntersectingLongsBroadcastReceiver;
import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;
import org.ccci.gto.android.common.support.v4.content.LoaderBroadcastReceiver;

public class ChurchLoader extends AsyncTaskBroadcastReceiverLoader<Church> {
    private final GmaDao mDao;

    private final long mId;

    public ChurchLoader(@NonNull final Context context, final long churchId) {
        super(context);
        mDao = GmaDao.getInstance(context);
        mId = churchId;

        // create BroadcastReceiver for this loader
        final IntersectingLongsBroadcastReceiver receiver = new IntersectingLongsBroadcastReceiver();
        receiver.setDelegate(new LoaderBroadcastReceiver(this));
        receiver.setExtraName(EXTRA_CHURCH_IDS);
        receiver.addValues(mId);
        setBroadcastReceiver(receiver);

        // add IntentFilter for updateChurches broadcasts
        addIntentFilter(BroadcastUtils.updateChurchesFilter());
    }

    @Override
    public Church loadInBackground() {
        if (mId != Church.INVALID_ID) {
            return mDao.find(Church.class, mId);
        }
        return null;
    }
}
