package com.expidev.gcmapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expidev.gcmapp.utils.ViewUtils;

/**
 * Created by William.Randall on 1/30/2015.
 */
public class TextHeaderView extends TextView
{
    public TextHeaderView(Context context)
    {
        super(context);
        init(context);
    }

    public TextHeaderView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        init(context);
    }

    public TextHeaderView(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        setBackgroundColor(0xffcccccc);
        setTextColor(0xde000000);

        ViewGroup.MarginLayoutParams layoutParams =new ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(ViewUtils.dpToPixels(context, 5), 0, 0, 0);
        setLayoutParams(layoutParams);
    }
}
