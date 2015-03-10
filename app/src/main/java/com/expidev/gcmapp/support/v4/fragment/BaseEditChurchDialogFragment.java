package com.expidev.gcmapp.support.v4.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.Church;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public abstract class BaseEditChurchDialogFragment extends DialogFragment {
    @Optional
    @Nullable
    @InjectView(R.id.title)
    TextView mTitleView;
    @Optional
    @Nullable
    @InjectView(R.id.icon)
    ImageView mIconView;

    @Optional
    @Nullable
    @InjectView(R.id.name)
    TextView mNameView;
    @Optional
    @Nullable
    @InjectView(R.id.contactName)
    TextView mContactNameView;
    @Optional
    @Nullable
    @InjectView(R.id.contactEmail)
    TextView mContactEmailView;
    @Optional
    @Nullable
    @InjectView(R.id.size)
    TextView mSizeView;

    /* BEGIN lifecycle */

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Dialog onCreateDialog(final Bundle savedState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setView(R.layout.fragment_edit_church);
        } else {
            builder.setView(LayoutInflater.from(getActivity()).inflate(R.layout.fragment_edit_church, null));
        }
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ButterKnife.inject(this, getDialog());
    }

    @Optional
    @OnClick(R.id.cancel)
    protected void onCancel() {
        this.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    protected void updateTitle(@Nullable final String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    protected void updateIcon(@NonNull final Church.Development state) {
        if (mIconView != null) {
            final int image;
            switch (state) {
                case GROUP:
                    image = R.drawable.groupicon;
                    break;
                case CHURCH:
                    image = R.drawable.churchicon;
                    break;
                case MULTIPLYING_CHURCH:
                    image = R.drawable.multiplyingchurchicon;
                    break;
                case TARGET:
                default:
                    image = R.drawable.targeticon;
                    break;
            }
            mIconView.setImageResource(image);
        }
    }
}
