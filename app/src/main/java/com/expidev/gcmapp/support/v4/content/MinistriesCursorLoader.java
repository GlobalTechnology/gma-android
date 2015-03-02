package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.Constants.ARG_GUID;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.MinistriesDao;
import com.expidev.gcmapp.model.AssociatedMinistry;
import com.expidev.gcmapp.utils.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;

public class MinistriesCursorLoader extends CursorBroadcastReceiverLoader {
    private final MinistriesDao mDao;

    @Nullable
    private final String mGuid;

    public MinistriesCursorLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);
        mDao = MinistriesDao.getInstance(context);
        mGuid = args != null ? args.getString(ARG_GUID) : null;

        // configure Broadcast listeners
        addIntentFilter(BroadcastUtils.updateAssignmentsFilter());
    }

    private static final String[] PROJECTION =
            {Contract.AssociatedMinistry.COLUMN_ROWID, Contract.AssociatedMinistry.COLUMN_MINISTRY_ID,
                    Contract.AssociatedMinistry.COLUMN_NAME};

    @Override
    protected Cursor getCursor() {
        // short-circuit if we don't have a valid identity
        if (mGuid == null) {
            return null;
        }

        return mDao.getCursor(AssociatedMinistry.class, Contract.AssociatedMinistry.JOIN_ASSIGNMENT, PROJECTION,
                              Contract.Assignment.SQL_WHERE_GUID, bindValues(mGuid),
                              Contract.AssociatedMinistry.COLUMN_NAME);
    }
}
