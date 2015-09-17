package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.MeasurementTypeLocalization.COLUMN_DESCRIPTION;
import static com.expidevapps.android.measurements.db.Contract.MeasurementTypeLocalization.COLUMN_LOCALE;
import static com.expidevapps.android.measurements.db.Contract.MeasurementTypeLocalization.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.MeasurementTypeLocalization.COLUMN_NAME;
import static com.expidevapps.android.measurements.db.Contract.MeasurementTypeLocalization.COLUMN_PERM_LINK_STUB;
import static com.expidevapps.android.measurements.model.MeasurementType.INVALID_PERM_LINK_STUB;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.MeasurementTypeLocalization;
import com.expidevapps.android.measurements.model.Ministry;

import org.ccci.gto.android.common.util.LocaleCompat;

import java.util.Locale;

public class MeasurementTypeLocalizationMapper extends BaseMapper<MeasurementTypeLocalization> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final MeasurementTypeLocalization localization) {
        switch (field) {
            case COLUMN_PERM_LINK_STUB:
                values.put(field, localization.getPermLinkStub());
                break;
            case COLUMN_MINISTRY_ID:
                values.put(field, localization.getMinistryId());
                break;
            case COLUMN_LOCALE:
                values.put(field, LocaleCompat.toLanguageTag(localization.getLocale()));
                break;
            case COLUMN_NAME:
                values.put(field, localization.getName());
                break;
            case COLUMN_DESCRIPTION:
                values.put(field, localization.getDescription());
                break;
            default:
                super.mapField(values, field, localization);
                break;
        }
    }

    @NonNull
    @Override
    protected MeasurementTypeLocalization newObject(@NonNull final Cursor c) {
        return new MeasurementTypeLocalization(getNonNullString(c, COLUMN_PERM_LINK_STUB, INVALID_PERM_LINK_STUB),
                                               getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID),
                                               getNonNullLocale(c, COLUMN_LOCALE, Locale.US));
    }

    @NonNull
    @Override
    public MeasurementTypeLocalization toObject(@NonNull final Cursor c) {
        final MeasurementTypeLocalization localization = super.toObject(c);

        localization.setName(getString(c, COLUMN_NAME, null));
        localization.setDescription(getString(c, COLUMN_DESCRIPTION, null));

        return localization;
    }
}
