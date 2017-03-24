package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Mcc.COLUMN_MCC;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.Training.COLUMN_CREATED_BY;
import static com.expidevapps.android.measurements.db.Contract.Training.COLUMN_DATE;
import static com.expidevapps.android.measurements.db.Contract.Training.COLUMN_ID;
import static com.expidevapps.android.measurements.db.Contract.Training.COLUMN_NAME;
import static com.expidevapps.android.measurements.db.Contract.Training.COLUMN_PARTICIPANTS;
import static com.expidevapps.android.measurements.db.Contract.Training.COLUMN_TYPE;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Training;

import org.joda.time.LocalDate;

class TrainingMapper extends LocationMapper<Training> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Training training) {
        switch (field) {
            case COLUMN_ID:
                values.put(field, training.getId());
                break;
            case COLUMN_MINISTRY_ID:
                values.put(field, training.getMinistryId());
                break;
            case COLUMN_NAME:
                values.put(field, training.getName());
                break;
            case COLUMN_DATE:
                final LocalDate date = training.getDate();
                values.put(field, date != null ? date.toString() : null);
                break;
            case COLUMN_TYPE:
                values.put(field, training.getType());
                break;
            case COLUMN_MCC:
                values.put(field, training.getMcc().toString());
                break;
            case COLUMN_PARTICIPANTS:
                values.put(field, training.getParticipants());
                break;
            case COLUMN_CREATED_BY:
                values.put(field, training.getCreatedBy());
                break;
            default:
                super.mapField(values, field, training);
                break;
        }
    }

    @NonNull
    @Override
    protected Training newObject(@NonNull final Cursor c) {
        return new Training();
    }

    @NonNull
    @Override
    public Training toObject(@NonNull final Cursor c) {
        final Training training = super.toObject(c);

        training.setId(getLong(c, COLUMN_ID, Training.INVALID_ID));
        training.setMinistryId(getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
        training.setName(getString(c, COLUMN_NAME, null));
        training.setDate(getLocalDate(c, COLUMN_DATE, null));
        training.setType(getString(c, COLUMN_TYPE, null));
        training.setMcc(getString(c, COLUMN_MCC, null));
        training.setParticipants(getInt(c, COLUMN_PARTICIPANTS, 0));
        training.setCreatedBy(getString(c, COLUMN_CREATED_BY, null));

        return training;
    }
}
