package com.expidevapps.android.measurements.map;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Church;

public class ChurchItem extends GmaItem<Church> {
    public ChurchItem(@Nullable final Assignment assignment, @NonNull final Church obj) {
        super(assignment, obj);
    }

    public long getChurchId() {
        return mObj.getId();
    }

    @Override
    public String getName() {
        return mObj.getName();
    }

    @Override
    public String getSnippet() {
        return "Size: " + mObj.getSize();
    }

    @DrawableRes
    @Override
    public int getItemImage() {
        return mObj.getDevelopment().image;
    }
}
