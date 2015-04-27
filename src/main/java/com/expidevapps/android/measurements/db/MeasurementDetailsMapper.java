package com.expidevapps.android.measurements.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.MeasurementDetails;
import com.expidevapps.android.measurements.model.Ministry;

import org.joda.time.YearMonth;

public class MeasurementDetailsMapper extends BaseMapper<MeasurementDetails> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final MeasurementDetails details) {
        switch (field) {
            case Contract.MeasurementDetails.COLUMN_GUID:
                values.put(field, details.getGuid());
                break;
            case Contract.MeasurementDetails.COLUMN_MINISTRY_ID:
                values.put(field, details.getMinistryId());
                break;
            case Contract.MeasurementDetails.COLUMN_MCC:
                values.put(field, details.getMcc().toString());
                break;
            case Contract.MeasurementDetails.COLUMN_PERM_LINK_STUB:
                values.put(field, details.getPermLink());
                break;
            case Contract.MeasurementDetails.COLUMN_PERIOD:
                values.put(field, details.getPeriod().toString());
                break;
            case Contract.MeasurementDetails.COLUMN_JSON:
                // JSON and version are linked to each other internally, so persist them atomically
                values.put(Contract.MeasurementDetails.COLUMN_JSON, details.getRawJson());
                values.put(Contract.MeasurementDetails.COLUMN_VERSION, details.getVersion());
                break;
            default:
                super.mapField(values, field, details);
                break;
        }
    }

    @NonNull
    @Override
    protected MeasurementDetails newObject(@NonNull final Cursor c) {
        return new MeasurementDetails(getNonNullString(c, Contract.MeasurementDetails.COLUMN_GUID, ""),
                                      getNonNullString(c, Contract.MeasurementDetails.COLUMN_MINISTRY_ID,
                                                       Ministry.INVALID_ID),
                                      Ministry.Mcc.fromRaw(getString(c, Contract.MeasurementDetails.COLUMN_MCC)),
                                      getNonNullString(c, Contract.MeasurementDetails.COLUMN_PERM_LINK_STUB, ""),
                                      getNonNullYearMonth(c, Contract.MeasurementDetails.COLUMN_PERIOD,
                                                          YearMonth.now()));
    }

    @NonNull
    @Override
    public MeasurementDetails toObject(@NonNull final Cursor c) {
        final MeasurementDetails details = super.toObject(c);
        details.setJson(getString(c, Contract.MeasurementDetails.COLUMN_JSON, null),
                        getInt(c, Contract.MeasurementDetails.COLUMN_VERSION, 0));
        return details;
    }
}
