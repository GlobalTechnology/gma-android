package com.expidevapps.android.measurements.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.model.Training.Completion;

import org.joda.time.LocalDate;

public class TrainingCompletionMapper extends BaseMapper<Completion> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Completion completion) {
        switch (field) {
            case Contract.Training.Completion.COLUMN_ID:
                values.put(field, completion.getId());
                break;
            case Contract.Training.Completion.COLUMN_TRAINING_ID:
                values.put(field, completion.getTrainingId());
                break;
            case Contract.Training.Completion.COLUMN_PHASE:
                values.put(field, completion.getPhase());
                break;
            case Contract.Training.Completion.COLUMN_NUMBER_COMPLETED:
                values.put(field, completion.getNumberCompleted());
                break;
            case Contract.Training.Completion.COLUMN_DATE:
                final LocalDate date = completion.getDate();
                values.put(field, date != null ? date.toString() : null);
                break;
            default:
                super.mapField(values, field, completion);
                break;
        }
    }

    @NonNull
    @Override
    protected Completion newObject(@NonNull final Cursor c) {
        return new Completion();
    }

    @NonNull
    @Override
    public Completion toObject(@NonNull final Cursor c) {
        final Completion completion = super.toObject(c);

        completion.setId(getLong(c, Contract.Training.Completion.COLUMN_ID, Completion.INVALID_ID));
        completion.setTrainingId(getLong(c, Contract.Training.Completion.COLUMN_TRAINING_ID, Training.INVALID_ID));
        completion.setPhase(this.getInt(c, Contract.Training.Completion.COLUMN_PHASE, 0));
        completion.setNumberCompleted(this.getInt(c, Contract.Training.Completion.COLUMN_NUMBER_COMPLETED, 0));
        completion.setDate(getLocalDate(c, Contract.Training.Completion.COLUMN_DATE, null));

        return completion;
    }
}
