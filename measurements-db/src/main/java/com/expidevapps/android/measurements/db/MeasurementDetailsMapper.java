package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Guid.COLUMN_GUID;
import static com.expidevapps.android.measurements.db.Contract.Mcc.COLUMN_MCC;
import static com.expidevapps.android.measurements.db.Contract.MeasurementDetails.COLUMN_JSON;
import static com.expidevapps.android.measurements.db.Contract.MeasurementDetails.COLUMN_SOURCE;
import static com.expidevapps.android.measurements.db.Contract.MeasurementDetails.COLUMN_VERSION;
import static com.expidevapps.android.measurements.db.Contract.MeasurementPermLink.COLUMN_PERM_LINK_STUB;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.Period.COLUMN_PERIOD;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.MeasurementDetails;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;

import org.joda.time.YearMonth;

class MeasurementDetailsMapper extends BaseMapper<MeasurementDetails> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final MeasurementDetails details) {
        switch (field) {
            case COLUMN_GUID:
                values.put(field, details.getGuid());
                break;
            case COLUMN_MINISTRY_ID:
                values.put(field, details.getMinistryId());
                break;
            case COLUMN_MCC:
                values.put(field, details.getMcc().toString());
                break;
            case COLUMN_PERM_LINK_STUB:
                values.put(field, details.getPermLink());
                break;
            case COLUMN_PERIOD:
                values.put(field, details.getPeriod().toString());
                break;
            case COLUMN_JSON:
                // JSON, source and version are linked to each other internally, so persist them atomically
                values.put(COLUMN_SOURCE, details.getSource());
                values.put(COLUMN_JSON, details.getRawJson());
                values.put(COLUMN_VERSION, details.getVersion());
                break;
            default:
                super.mapField(values, field, details);
                break;
        }
    }

    @NonNull
    @Override
    protected MeasurementDetails newObject(@NonNull final Cursor c) {
        return new MeasurementDetails(getNonNullString(c, COLUMN_GUID, ""),
                                      getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID),
                                      Mcc.fromRaw(getString(c, COLUMN_MCC)),
                                      getNonNullString(c, COLUMN_PERM_LINK_STUB, ""),
                                      getNonNullYearMonth(c, COLUMN_PERIOD, YearMonth.now()));
    }

    @NonNull
    @Override
    public MeasurementDetails toObject(@NonNull final Cursor c) {
        final MeasurementDetails details = super.toObject(c);

        details.setSource(getString(c, COLUMN_SOURCE, null));
        details.setJson(getString(c, COLUMN_JSON, null), getInt(c, COLUMN_VERSION, 0));

        return details;
    }
}
