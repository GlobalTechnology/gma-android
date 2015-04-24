package com.expidev.gcmapp.map;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.expidev.gcmapp.model.Church;

public class ChurchItem extends GmaItem<Church> {
    public ChurchItem(@NonNull final Church obj) {
        super(obj);
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

    @Override
    public boolean isDraggable() {
        return true;
    }
}
