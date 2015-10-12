package com.expidevapps.android.measurements.map;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Church;

public class ChurchItem extends GmaItem<Church> {
    public ChurchItem(@Nullable final Assignment assignment, @NonNull final Church obj) {
        super(assignment, obj);
    }

    @Override
    public String getName() {
        return mObj.getName();
    }

    public String getCreatedBy() {
        return mObj.getCreatedBy();
    }

    @Override
    public String getSnippet(@NonNull final Context context) {
        return context.getResources()
                .getQuantityString(R.plurals.text_marker_map_church_snippet, mObj.getSize(), mObj.getSize());
    }

    @DrawableRes
    @Override
    public int getItemImage() {
        return mObj.getDevelopment().image;
    }
}
