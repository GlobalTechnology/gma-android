package com.expidevapps.android.measurements.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Training;

import org.joda.time.LocalDate;

public class TrainingMapper extends LocationMapper<Training> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Training training) {
        switch (field) {
            case Contract.Training.COLUMN_ID:
                values.put(field, training.getId());
                break;
            case Contract.Training.COLUMN_MINISTRY_ID:
                values.put(field, training.getMinistryId());
                break;
            case Contract.Training.COLUMN_NAME:
                values.put(field, training.getName());
                break;
            case Contract.Training.COLUMN_DATE:
                final LocalDate date = training.getDate();
                values.put(field, date != null ? date.toString() : null);
                break;
            case Contract.Training.COLUMN_TYPE:
                values.put(field, training.getType());
                break;
            case Contract.Training.COLUMN_MCC:
                values.put(field, training.getMcc().toString());
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
        training.setId(getLong(c, Contract.Training.COLUMN_ID, Training.INVALID_ID));
        training.setMinistryId(getNonNullString(c, Contract.Training.COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
        training.setName(this.getString(c, Contract.Training.COLUMN_NAME, null));
        training.setDate(getLocalDate(c, Contract.Training.COLUMN_DATE, null));
        training.setType(this.getString(c, Contract.Training.COLUMN_TYPE, null));
        training.setMcc(this.getString(c, Contract.Training.COLUMN_MCC, null));
        return training;
    }
}
