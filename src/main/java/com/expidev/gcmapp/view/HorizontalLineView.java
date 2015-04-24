package com.expidev.gcmapp.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.expidev.gcmapp.utils.ViewUtils;

/**
 * Simple horizontal line to separate things in the page
 * Created by William.Randall on 1/29/2015.
 */
public class HorizontalLineView extends View
{
    /**
     * Creates a gray horizontal line
     */
    public HorizontalLineView(Context context)
    {
        this(context, Color.GRAY);
    }

    public HorizontalLineView(Context context, int color)
    {
        super(context);

        setBackgroundColor(color);
        ViewGroup.MarginLayoutParams layoutParams =new ViewGroup.MarginLayoutParams(
            LayoutParams.MATCH_PARENT,
            ViewUtils.dpToPixels(context, 1));

        layoutParams.setMargins(
            0,
            ViewUtils.dpToPixels(context, 5),
            0,
            ViewUtils.dpToPixels(context, 5));

        setLayoutParams(layoutParams);
    }
}
