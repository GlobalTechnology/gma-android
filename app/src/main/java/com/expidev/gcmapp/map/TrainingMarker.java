package com.expidev.gcmapp.map;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.Training;

public class TrainingMarker extends Marker<Training> {
    public TrainingMarker(@NonNull final Training obj) {
        super(obj);
    }

    @Override
    public String getName() {
        return mObj.getName();
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @DrawableRes
    @Override
    public int getItemImage() {
        return R.drawable.training;
    }
}
