package com.expidev.gcmapp.db;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.measurement.MinistryMeasurement;

public class MinistryMeasurementMapper extends BaseMapper<MinistryMeasurement> {
    @NonNull
    @Override
    protected MinistryMeasurement newObject(@NonNull final Cursor cursor) {
        return null;
    }
}
