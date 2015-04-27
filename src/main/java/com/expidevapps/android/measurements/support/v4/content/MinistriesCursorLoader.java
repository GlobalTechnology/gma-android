package com.expidevapps.android.measurements.support.v4.content;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.service.BroadcastUtils;

import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;

public class MinistriesCursorLoader extends CursorBroadcastReceiverLoader {
    private final GmaDao mDao;

    @Nullable
    private final String mGuid;

    public MinistriesCursorLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);
        mDao = GmaDao.getInstance(context);
        mGuid = args != null ? args.getString(ARG_GUID) : null;

        // configure Broadcast listeners
        if(mGuid != null) {
            addIntentFilter(BroadcastUtils.updateAssignmentsFilter(mGuid));
        }
    }

    private static final String[] PROJECTION =
            {Contract.Ministry.COLUMN_ROWID, Contract.Ministry.COLUMN_MINISTRY_ID, Contract.Ministry.COLUMN_NAME};

    @Override
    protected Cursor getCursor() {
        // short-circuit if we don't have a valid identity
        if (mGuid == null) {
            return null;
        }

        return mDao.getCursor(Ministry.class, Contract.Ministry.JOIN_ASSIGNMENT, PROJECTION,
                              Contract.Assignment.SQL_WHERE_GUID, bindValues(mGuid), Contract.Ministry.COLUMN_NAME);
    }
}
