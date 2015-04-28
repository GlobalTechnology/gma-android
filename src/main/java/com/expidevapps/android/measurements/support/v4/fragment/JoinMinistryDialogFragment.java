package com.expidevapps.android.measurements.support.v4.fragment;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.expidevapps.android.measurements.support.v4.content.MinistryLoader;
import com.expidevapps.android.measurements.task.CreateAssignmentTask;

import org.ccci.gto.android.common.app.AlertDialogCompat;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.fragment.AbstractDialogFragment;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class JoinMinistryDialogFragment extends AbstractDialogFragment {
    private static final int LOADER_MINISTRY = 1;

    /* Loader callback objects */
    private final MinistryLoaderCallbacks mLoaderCallbacksMinistry = new MinistryLoaderCallbacks();

    @Optional
    @Nullable
    @InjectView(R.id.name)
    TextView mNameView;
    @Optional
    @Nullable
    @InjectView(R.id.join)
    Button mJoinButton;
    @Optional
    @Nullable
    @InjectView(R.id.progressBar)
    View mProgressView;

    @Nullable
    private String mGuid = null;
    @NonNull
    private String mMinistryId = Ministry.INVALID_ID;

    @Nullable
    private Ministry mMinistry = null;

    public static JoinMinistryDialogFragment newInstance(@NonNull final String guid, @NonNull final String ministryId) {
        final JoinMinistryDialogFragment fragment = new JoinMinistryDialogFragment();

        final Bundle args = new Bundle(2);
        args.putString(ARG_GUID, guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);

        final Bundle args = this.getArguments();
        mGuid = args.getString(ARG_GUID);
        mMinistryId = args.getString(ARG_MINISTRY_ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialogCompat.setView(builder, getActivity(), R.layout.fragment_dialog_join_ministry);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();
        ButterKnife.inject(this, getDialog());
        updateViews();
    }

    void onLoadMinistry(@Nullable final Ministry ministry) {
        mMinistry = ministry;
        updateViews();
    }

    @Optional
    @OnClick(R.id.join)
    void onJoinMinistry() {
        if (mGuid != null) {
            if (mJoinButton != null) {
                mJoinButton.setEnabled(false);
            }
            if (mProgressView != null) {
                mProgressView.setVisibility(View.VISIBLE);
            }

            new CreateAssignmentTask(getActivity(), mMinistryId, Assignment.Role.SELF_ASSIGNED, mGuid) {
                @Override
                protected void onPostExecute(final Assignment assignment) {
                    super.onPostExecute(assignment);

                    if (assignment != null) {
                        // trigger a forced background sync of all assignments
                        GmaSyncService.syncAssignments(getActivity(), assignment.getGuid(), true);
                    }

                    // TODO: we need to handle failed requests

                    onJoinedMinistry();
                }
            }.execute();
        }
    }

    void onJoinedMinistry() {
        dismiss();

        // let parent fragment/activity know we joined the ministry
        final OnJoinMinistryListener listener = FragmentUtils.getListener(this, OnJoinMinistryListener.class);
        if (listener != null) {
            listener.onJoinedMinistry(mMinistryId);
        }
    }

    @Optional
    @OnClick(R.id.cancel)
    void onCancelJoin() {
        dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void updateViews() {
        if (mNameView != null) {
            mNameView.setText(mMinistry != null ? mMinistry.getName() : null);
        }
    }

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();

        final Bundle args = new Bundle(1);
        args.putString(ARG_MINISTRY_ID, mMinistryId);

        manager.initLoader(LOADER_MINISTRY, args, mLoaderCallbacksMinistry);
    }

    public interface OnJoinMinistryListener {
        void onJoinedMinistry(@NonNull String ministryId);
    }

    private class MinistryLoaderCallbacks extends SimpleLoaderCallbacks<Ministry> {
        @Nullable
        @Override
        public Loader<Ministry> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MINISTRY:
                    return new MinistryLoader(getActivity(), args);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Ministry> loader, @Nullable final Ministry ministry) {
            switch (loader.getId()) {
                case LOADER_MINISTRY:
                    onLoadMinistry(ministry);
                    break;
            }
        }
    }
}
