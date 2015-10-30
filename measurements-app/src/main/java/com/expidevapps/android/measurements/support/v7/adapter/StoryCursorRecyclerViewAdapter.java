package com.expidevapps.android.measurements.support.v7.adapter;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView;
import org.ccci.gto.android.common.recyclerview.adapter.CursorAdapter;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class StoryCursorRecyclerViewAdapter extends CursorAdapter<StoryCursorRecyclerViewAdapter.ViewHolder> {
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_story, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            holder.bind(mCursor);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Optional
        @Nullable
        @InjectView(R.id.image)
        SimplePicassoImageView mImageView;
        @Optional
        @Nullable
        @InjectView(R.id.title)
        TextView mTitleView;
        @Optional
        @Nullable
        @InjectView(R.id.content)
        TextView mContentView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        void bind(@NonNull final Cursor c) {
            if (mImageView != null) {
                // sanitize image uri
                Uri image = Uri.parse(CursorUtils.getNonNullString(c, Contract.Story.COLUMN_IMAGE, ""));
                final String scheme = image.getScheme();
                if (scheme != null) {
                    switch (scheme.toLowerCase(Locale.US)) {
                        case "http":
                        case "https":
                            break;
                        default:
                            image = null;
                    }
                } else {
                    image = null;
                }

                mImageView.setPicassoUri(image);
            }
            if (mTitleView != null) {
                mTitleView.setText(CursorUtils.getString(c, Contract.Story.COLUMN_TITLE));
            }
            if (mContentView != null) {
                mContentView.setText(CursorUtils.getString(c, Contract.Story.COLUMN_CONTENT));
            }
        }
    }
}
