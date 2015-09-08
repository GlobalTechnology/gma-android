package com.expidevapps.android.measurements.map;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Training;

public class TrainingItem extends GmaItem<Training> {
    public TrainingItem(@Nullable final Assignment assignment, @NonNull final Training obj) {
        super(assignment, obj);
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
    public String getSnippet(@NonNull final Context context) {
        return null;
    }

    @DrawableRes
    @Override
    public int getItemImage() {
        return R.drawable.ic_training;
    }
}
