package com.expidev.gcmapp.support.v4.fragment;

import static com.expidev.gcmapp.Constants.ARG_CHURCH_ID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.support.v4.content.ChurchLoader;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.fragment.AbstractDialogFragment;

public class EditChurchFragment extends AbstractDialogFragment {
    private static final int LOADER_CHURCH = 1;

    private long mChurchId = Church.INVALID_ID;

    private Church mChurch;

    public static EditChurchFragment newInstance(final long churchId) {
        final EditChurchFragment fragment = new EditChurchFragment();

        final Bundle args = new Bundle();
        args.putLong(ARG_CHURCH_ID, churchId);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = this.getArguments();
        mChurchId = args.getLong(ARG_CHURCH_ID, Church.INVALID_ID);
    }

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
    public void onViewCreated(final View view, @Nullable final Bundle savedState) {
        super.onViewCreated(view, savedState);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.startLoaders();
    }

    void onUpdateChurch(final Church church) {
        mChurch = church;
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();
        manager.initLoader(LOADER_CHURCH, null, new ChurchLoaderCallbacks());
    }

    private class ChurchLoaderCallbacks extends SimpleLoaderCallbacks<Church> {
        @Override
        public Loader<Church> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_CHURCH:
                    return new ChurchLoader(getActivity(), mChurchId);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Church> loader, @Nullable final Church church) {
            switch (loader.getId()) {
                case LOADER_CHURCH:
                    onUpdateChurch(church);
            }
        }
    }
}
