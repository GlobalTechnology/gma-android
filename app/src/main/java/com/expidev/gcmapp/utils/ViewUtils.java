package com.expidev.gcmapp.utils;

import android.content.Context;

/**
 * Created by William.Randall on 1/29/2015.
 */
public class ViewUtils
{
    public static int dpToPixels(Context context, int dp)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(dp * density);
    }
}
