package com.expidev.gcmapp.support.v7.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.model.Ministry;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.recyclerview.adapter.CursorAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class MinistryCursorRecyclerViewAdapter extends CursorAdapter<MinistryCursorRecyclerViewAdapter.ViewHolder> {
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_ministry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);

            if (holder.mNameView != null) {
                holder.mNameView.setText(CursorUtils.getString(mCursor, Contract.Ministry.COLUMN_NAME));
            }
        }
    }

    @NonNull
    public String getMinistryId(final int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            return CursorUtils.getNonNullString(mCursor, Contract.Ministry.COLUMN_MINISTRY_ID, Ministry.INVALID_ID);
        }
        return Ministry.INVALID_ID;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Optional
        @Nullable
        @InjectView(R.id.name)
        TextView mNameView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
