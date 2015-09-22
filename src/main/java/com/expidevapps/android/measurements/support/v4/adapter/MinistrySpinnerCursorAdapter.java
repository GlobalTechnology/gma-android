package com.expidevapps.android.measurements.support.v4.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.expidevapps.android.measurements.db.Contract;


public class MinistrySpinnerCursorAdapter extends SimpleCursorAdapter {

    private Context context;

    private int layout;

    public MinistrySpinnerCursorAdapter (Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.context = context;
        this.layout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Cursor c = getCursor();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);

        int nameCol = c.getColumnIndex(Contract.Ministry.COLUMN_NAME);

        String name = c.getString(nameCol);

        /**
         * Next set the name of the entry.
         */
        TextView name_text = (TextView) v.findViewById(android.R.id.text1);
        if (name_text != null) {
            name_text.setText(name);
        }

        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {

        int nameCol = c.getColumnIndex(Contract.Ministry.COLUMN_NAME);

        String name = c.getString(nameCol);

        /**
         * Next set the name of the entry.
         */
        TextView name_text = (TextView) v.findViewById(android.R.id.text1);
        if (name_text != null) {
            name_text.setText(name);
        }
    }


}
