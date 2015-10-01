package com.expidevapps.android.measurements.support.v4.content;

import static com.expidevapps.android.measurements.Constants.EXTRA_PREFERENCES;
import static org.ccci.gto.android.common.db.Expression.raw;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.Contract.MeasurementVisibility;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.MeasurementType;
import com.expidevapps.android.measurements.model.UserPreference;
import com.expidevapps.android.measurements.sync.BroadcastUtils;

import org.ccci.gto.android.common.content.IntersectingStringsBroadcastReceiver;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.db.Table;
import org.ccci.gto.android.common.db.support.v4.content.DaoCursorBroadcastReceiverLoader;
import org.ccci.gto.android.common.support.v4.content.BroadcastReceiverLoaderHelper;
import org.ccci.gto.android.common.support.v4.content.LoaderBroadcastReceiver;
import org.ccci.gto.android.common.util.ArrayUtils;

/**
 * This CursorLoader will filter returned MeasurementTypes on a ministries visibility and user's role
 */
public class FilteredMeasurementTypeDaoCursorLoader extends DaoCursorBroadcastReceiverLoader<MeasurementType> {
    private static final Join<MeasurementType, MeasurementVisibility> JOIN_MEASUREMENT_VISIBILITY =
            Contract.MeasurementType.JOIN_MEASUREMENT_VISIBILITY.type("LEFT");

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

        // monitor for changed measurement types
        addIntentFilter(BroadcastUtils.updateMeasurementTypesFilter());
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

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public Join<MeasurementType, ?>[] getJoins() {
        return ArrayUtils.merge(Join.class, super.getJoins(), new Join[] {JOIN_MEASUREMENT_VISIBILITY
                .andOn(raw(Contract.MeasurementVisibility.SQL_WHERE_MINISTRY, mMinistryId))});
    }

    @Nullable
    @Override
    public Expression getWhere() {
        Expression where = super.getWhere();

        // filter based on measurement visibility
        where = where != null ? where.and(Contract.MeasurementType.SQL_WHERE_VISIBLE) :
                Contract.MeasurementType.SQL_WHERE_VISIBLE;

        // take supported_staff UserPreference into account
        final UserPreference pref = mDao.find(UserPreference.class, mGuid, UserPreference.SUPPORTED_STAFF);
        final Boolean value = pref != null ? pref.getValueAsBoolean() : null;
        if (value == null || !value) {
            where = where.and(Contract.MeasurementType.SQL_WHERE_NOT_SUPPORTED_STAFF);
        }

        // return generated where clause
        return where;
    }
}
