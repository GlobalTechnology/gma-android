package com.expidev.gcmapp.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidev.gcmapp.model.Training;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrainingMapper extends BaseMapper<Training> {
    @NonNull
    @Override
    public ContentValues toContentValues(@NonNull final Training training) {
        return this.toContentValues(training, Contract.Training.PROJECTION_ALL);
    }

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
                values.put(field, dateToString(training.getDate()));
                break;
            case Contract.Training.COLUMN_TYPE:
                values.put(field, training.getType());
                break;
            case Contract.Training.COLUMN_MCC:
                values.put(field, training.getMcc());
                break;
            case Contract.Training.COLUMN_LATITUDE:
                values.put(field, training.getLatitude());
                break;
            case Contract.Training.COLUMN_LONGITUDE:
                values.put(field, training.getLongitude());
                break;
            default:
                super.mapField(values, field, training);
                break;
        }
        super.mapField(values, field, training);
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
        training.setId(this.getInt(c, Contract.Training.COLUMN_ID, 0));
        training.setMinistryId(this.getString(c, Contract.Training.COLUMN_MINISTRY_ID, null));
        training.setName(this.getString(c, Contract.Training.COLUMN_NAME, null));
        training.setDate(stringToDate(this.getString(c, Contract.Training.COLUMN_DATE)));
        training.setType(this.getString(c, Contract.Training.COLUMN_TYPE, null));
        training.setMcc(this.getString(c, Contract.Training.COLUMN_MCC, null));
        training.setLatitude(this.getDouble(c, Contract.Training.COLUMN_LATITUDE, 0));
        training.setLongitude(this.getDouble(c, Contract.Training.COLUMN_LONGITUDE, 0));
        return training;
    }

    private final static SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    private Date stringToDate(@Nullable final String string) {
        if (string != null) {
            try {
                return FORMAT_DATE.parse(string);
            } catch (final ParseException ignored) {
            }
        }
        return null;
    }

    @Nullable
    private String dateToString(@Nullable final Date date) {
        return date != null ? FORMAT_DATE.format(date) : null;
    }
}
