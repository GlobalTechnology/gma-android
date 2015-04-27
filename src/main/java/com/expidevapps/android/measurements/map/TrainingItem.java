package com.expidevapps.android.measurements.map;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Training;

public class TrainingItem extends GmaItem<Training> {
    public TrainingItem(@NonNull final Training obj) {
        super(obj);
    }
    
    public long getTrainingId()
    {
        return mObj.getId();
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
        return R.drawable.ic_training;
    }
}
