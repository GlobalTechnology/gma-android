package com.expidevapps.android.measurements.map;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Training;

import java.util.Arrays;

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
    public String getSnippet() {
        return null;
    }

    @DrawableRes
    @Override
    public int getItemImage() {
        return R.drawable.ic_training;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TrainingItem that = (TrainingItem) o;
        return this.mObj.getId() == that.mObj.getId();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new long[] {mObj.getId()});
    }
}
