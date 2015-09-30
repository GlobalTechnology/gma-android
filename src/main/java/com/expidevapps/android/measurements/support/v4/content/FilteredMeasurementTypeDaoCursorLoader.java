package com.expidevapps.android.measurements.support.v4.content;

import static com.expidevapps.android.measurements.Constants.EXTRA_PREFERENCES;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.UserPreference;
import com.expidevapps.android.measurements.sync.BroadcastUtils;

import org.ccci.gto.android.common.content.IntersectingStringsBroadcastReceiver;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Table;
import org.ccci.gto.android.common.db.support.v4.content.DaoCursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.support.v4.content.BroadcastReceiverLoaderHelper;
import org.ccci.gto.android.common.support.v4.content.LoaderBroadcastReceiver;

/**
 * This CursorLoader will filter returned MeasurementTypes on a ministries visibility and user's role
 */
public class FilteredMeasurementTypeDaoCursorLoader extends DaoCursorBroadcastReceiverLoader<MeasurementType> {
    @NonNull
    private final String mGuid;
    @NonNull
    private final String mMinistryId;
    @NonNull
    private final BroadcastReceiverLoaderHelper mPrefsHelper;

    public FilteredMeasurementTypeDaoCursorLoader(@NonNull final Context context, @NonNull final String guid,
                                                  @NonNull final String ministryId, @Nullable final Bundle args) {
        super(context, GmaDao.getInstance(context), Table.forClass(MeasurementType.class), args);
        mGuid = guid;
        mMinistryId = ministryId;

        // configure preference change broadcast helper
        final IntersectingStringsBroadcastReceiver receiver = new IntersectingStringsBroadcastReceiver();
        receiver.setDelegate(new LoaderBroadcastReceiver(this));
        receiver.setExtraName(EXTRA_PREFERENCES);
        receiver.addValues(UserPreference.SUPPORTED_STAFF);
        mPrefsHelper = new BroadcastReceiverLoaderHelper(this, receiver);
        mPrefsHelper.addIntentFilter(BroadcastUtils.updatePreferencesFilter(mGuid));
    }

    /* BEGIN lifecycle */

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        mPrefsHelper.onStartLoading();
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        mPrefsHelper.onAbandon();
    }

    /* END lifecycle */

    @Nullable
    @Override
    public Expression getWhere() {
        Expression where = super.getWhere();

        // take supported_staff UserPreference into account
        final UserPreference pref = mDao.find(UserPreference.class, mGuid, UserPreference.SUPPORTED_STAFF);
        final Boolean value = pref != null ? pref.getValueAsBoolean() : null;
        if (value == null || !value) {
            where = where != null ? where.and(Contract.MeasurementType.SQL_WHERE_NOT_SUPPORTED_STAFF) :
                    Contract.MeasurementType.SQL_WHERE_NOT_SUPPORTED_STAFF;
        }

        // return generated where clause
        return where;
    }
}
