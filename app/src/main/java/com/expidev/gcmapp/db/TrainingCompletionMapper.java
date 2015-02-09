package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Training;

public class TrainingCompletionMapper extends BaseMapper<Training.GCMTrainingCompletions> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Training.GCMTrainingCompletions completion) {
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
                values.put(field, dateToString(completion.getDate()));
                break;
            default:
                super.mapField(values, field, completion);
                break;
        }
    }

    @NonNull
    @Override
    protected Training.GCMTrainingCompletions newObject(@NonNull final Cursor c) {
        return new Training.GCMTrainingCompletions();
    }

    @NonNull
    @Override
    public Training.GCMTrainingCompletions toObject(@NonNull final Cursor c) {
        final Training.GCMTrainingCompletions completion = super.toObject(c);

        completion.setId(this.getInt(c, Contract.Training.Completion.COLUMN_ID, 0));
        completion.setTrainingId(this.getInt(c, Contract.Training.Completion.COLUMN_TRAINING_ID, 0));
        completion.setPhase(this.getInt(c, Contract.Training.Completion.COLUMN_PHASE, 0));
        completion.setNumberCompleted(this.getInt(c, Contract.Training.Completion.COLUMN_NUMBER_COMPLETED, 0));
        completion.setDate(stringToDate(this.getString(c, Contract.Training.Completion.COLUMN_DATE, null)));

        return completion;
    }
}
