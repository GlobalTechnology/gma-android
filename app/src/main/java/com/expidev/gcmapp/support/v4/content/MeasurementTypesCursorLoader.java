package com.expidev.gcmapp.support.v4.content;

import static com.expidev.gcmapp.model.measurement.MeasurementType.ARG_COLUMN;
import static org.ccci.gto.android.common.db.AbstractDao.ARG_PROJECTION;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.GmaDao;
import com.expidev.gcmapp.model.measurement.MeasurementType;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;

import java.util.ArrayList;
import java.util.List;

public class MeasurementTypesCursorLoader extends CursorBroadcastReceiverLoader {
    @NonNull
    private final GmaDao mDao;

    // filters
    @Nullable
    private final MeasurementType.Column mColumn;

    @NonNull
    private final String[] mProjection;
    @NonNull
    private final String mOrderBy;

    public MeasurementTypesCursorLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);
        mDao = GmaDao.getInstance(context);

        // load filters from args
        final String column = args != null ? args.getString(ARG_COLUMN) : null;
        mColumn = column != null ? MeasurementType.Column.fromRaw(column) : null;

        // load query params from args
        final String[] projection = args != null ? args.getStringArray(ARG_PROJECTION) : null;
        mProjection = projection != null ? projection : mDao.getFullProjection(MeasurementType.class);
        mOrderBy = Contract.MeasurementType.COLUMN_SORT_ORDER;
    }

    @Nullable
    @Override
    protected Cursor getCursor() {
        // build where based on filters defined
        final StringBuilder where = new StringBuilder();
        final List<Object> whereParams = new ArrayList<>();
        if (mColumn != null) {
            where.append(Contract.MeasurementType.SQL_WHERE_COLUMN);
            whereParams.add(mColumn);
        }

        // issue request
        return mDao.getCursor(MeasurementType.class, mProjection, where.toString(),
                              AbstractDao.bindValues(whereParams.toArray(new Object[whereParams.size()])), mOrderBy);
    }
}
