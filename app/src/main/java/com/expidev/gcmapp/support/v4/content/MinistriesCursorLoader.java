package com.expidev.gcmapp.support.v4.content;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;

public class MinistriesCursorLoader extends CursorBroadcastReceiverLoader {
    private final MinistriesDao mDao;

    public MinistriesCursorLoader(@NonNull final Context context) {
        super(context);
        addIntentFilter(BroadcastUtils.updateAssignmentsFilter());
        mDao = MinistriesDao.getInstance(context);
    }

    private static final String[] PROJECTION =
            {Contract.AssociatedMinistry.COLUMN_ROWID, Contract.AssociatedMinistry.COLUMN_MINISTRY_ID,
                    Contract.AssociatedMinistry.COLUMN_NAME};

    @Override
    protected Cursor getCursor() {
        return mDao.getCursor(AssociatedMinistry.class, PROJECTION, null, null, null);
    }
}
