package com.expidevapps.android.measurements.support.v4.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.View;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Church.Development;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.content.ChurchLoader;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.google.common.collect.Lists;

import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.AsyncTaskCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;

import static android.view.View.GONE;
import static com.expidevapps.android.measurements.Constants.ARG_CHURCH_ID;
import static com.expidevapps.android.measurements.Constants.VISIBILITY;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateChurchesBroadcast;

public class EditChurchFragment extends BaseEditChurchDialogFragment {
    private static final int LOADER_CHURCH = 1;

    private static final int CHANGED_CONTACT_NAME = 0;
    private static final int CHANGED_CONTACT_EMAIL = 1;
    private static final int CHANGED_DEVELOPMENT = 2;
    private static final int CHANGED_SIZE = 3;
    private static final int CHANGED_CONTACT_MOBILE = 4;

    private long mChurchId = Church.INVALID_ID;
    @NonNull
    private boolean[] mChanged = new boolean[5];
    @Nullable
    private Church mChurch;

    @Optional
    @InjectViews({R.id.nameRow})
    List<View> mHiddenViews;

    @NonNull
    public static Bundle buildArgs(@NonNull final String guid, final long churchId) {
        final Bundle args = buildArgs(guid);
        args.putLong(ARG_CHURCH_ID, churchId);
        return args;
    }

    public static EditChurchFragment newInstance(@NonNull final String guid, final long churchId) {
        final EditChurchFragment fragment = new EditChurchFragment();
        fragment.setArguments(buildArgs(guid, churchId));
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

    @Override
    public void onStart() {
        super.onStart();
        this.startLoaders();
        ButterKnife.apply(mHiddenViews, VISIBILITY, GONE);
        updateViews();
    }

    void onLoadChurch(final Church church) {
        mChurch = church;
        updateViews();
    }

    @Override
    protected void onChangeDevelopment(@NonNull final Development development) {
        super.onChangeDevelopment(development);
        mChanged[CHANGED_DEVELOPMENT] =
                !development.equals(mChurch != null ? mChurch.getDevelopment() : Development.UNKNOWN);
    }

    void onTextUpdated(@NonNull final View view, @NonNull final String text) {
        switch (view.getId()) {
            case R.id.contactName:
                mChanged[CHANGED_CONTACT_NAME] =
                        !(mChurch != null ? text.equals(mChurch.getContactName()) : text.isEmpty());
                break;
            case R.id.contactEmail:
                mChanged[CHANGED_CONTACT_EMAIL] =
                        !(mChurch != null ? text.equals(mChurch.getContactEmail()) : text.isEmpty());
                break;
            case R.id.contactMobile:
                mChanged[CHANGED_CONTACT_MOBILE] =
                        !(mChurch != null ? text.equals(mChurch.getContactMobile()) : text.isEmpty());
                break;
            case R.id.size:
                mChanged[CHANGED_SIZE] =
                        !(mChurch != null ? text.equals(Integer.toString(mChurch.getSize())) : text.isEmpty());
                break;
        }
    }

    @Optional
    @OnClick(R.id.save)
    void onSaveChanges() {
        if (mChurch != null) {
            // capture updates
            final ChurchUpdates updates = new ChurchUpdates();
            if (mContactNameView != null && mChanged[CHANGED_CONTACT_NAME]) {
                updates.mContactName = mContactNameView.getText().toString();
            }
            if (mContactEmailView != null && mChanged[CHANGED_CONTACT_EMAIL]) {
                updates.mContactEmail = mContactEmailView.getText().toString();
            }
            if (mContactMobileView != null && mChanged[CHANGED_CONTACT_MOBILE]) {
                updates.mContactMobile = mContactMobileView.getText().toString();
            }
            if (mDevelopmentSpinner != null && mChanged[CHANGED_DEVELOPMENT]) {
                final Object development = mDevelopmentSpinner.getSelectedItem();
                updates.mDevelopment =
                        development instanceof Development ? (Development) development : Development.UNKNOWN;
            }
            if (mSizeView != null && mChanged[CHANGED_SIZE]) {
                try {
                    updates.mSize = Integer.valueOf(mSizeView.getText().toString());
                } catch (final NumberFormatException ignored) {
                }
            }

            // persist changes in the database (if there are any)
            if (updates.hasUpdates()) {
                AsyncTaskCompat.execute(new UpdateChurchRunnable(getActivity(), mGuid, mChurch, updates));
            }
        }

        // dismiss the dialog
        dismiss();
    }

    @Optional
    @OnClick(R.id.delete)
    void onDeleteChurch() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage("Are you sure you want to delete ?")
                .setTitle("Confirm?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //dismiss the dialog
                        dismiss();

                        if(mChurch != null) {
                            AsyncTaskCompat.execute(new DeleteChurchRunnable(getActivity(), mGuid, mChurch));
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();
        manager.initLoader(LOADER_CHURCH, null, new ChurchLoaderCallbacks());
    }

    private void updateViews() {
        updateTitle(mChurch != null ? mChurch.getName() : null);
        updateIcon(mChurch != null ? mChurch.getDevelopment() : Development.UNKNOWN);

        if (mContactNameView != null && !mChanged[CHANGED_CONTACT_NAME]) {
            mContactNameView.setText(mChurch != null ? mChurch.getContactName() : null);
        }
        if (mContactEmailView != null && !mChanged[CHANGED_CONTACT_EMAIL]) {
            mContactEmailView.setText(mChurch != null ? mChurch.getContactEmail() : null);
        }
        if (mContactMobileView != null && !mChanged[CHANGED_CONTACT_MOBILE]) {
            mContactMobileView.setText(mChurch != null ? mChurch.getContactMobile() : null);
        }
        if (mSizeView != null && !mChanged[CHANGED_SIZE]) {
            mSizeView.setText(mChurch != null ? Integer.toString(mChurch.getSize()) : null);
        }
        if (mDevelopmentSpinner != null && mDevelopmentAdapter != null && !mChanged[CHANGED_DEVELOPMENT]) {
            mDevelopmentSpinner.setSelection(
                    mDevelopmentAdapter.getPosition(mChurch != null ? mChurch.getDevelopment() : Development.UNKNOWN));
        }
    }

    @Optional
    @OnTextChanged(value = R.id.contactName, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void updateContactName(@Nullable final Editable text) {
        if (mContactNameView != null) {
            onTextUpdated(mContactNameView, text != null ? text.toString() : "");
        }
    }

    @Optional
    @OnTextChanged(value = R.id.contactEmail, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void updateContactEmail(@Nullable final Editable text) {
        if (mContactEmailView != null) {
            onTextUpdated(mContactEmailView, text != null ? text.toString() : "");
        }
    }

    @Optional
    @OnTextChanged(value = R.id.contactMobile, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void updateContactMobile(@Nullable final Editable text) {
        if (mContactMobileView != null) {
            onTextUpdated(mContactMobileView, text != null ? text.toString() : "");
        }
    }

    @Optional
    @OnTextChanged(value = R.id.size, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void updateSize(@Nullable final Editable text) {
        if (mSizeView != null) {
            onTextUpdated(mSizeView, text != null ? text.toString() : "");
        }
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
                    onLoadChurch(church);
            }
        }
    }

    private static class ChurchUpdates {
        @Nullable
        String mContactName;
        @Nullable
        String mContactEmail;
        @Nullable
        String mContactMobile;
        @Nullable
        Integer mSize;
        @Nullable
        Development mDevelopment;

        boolean hasUpdates() {
            return mContactName != null || mContactEmail != null || mContactMobile != null || mSize != null || mDevelopment != null;
        }
    }

    private static class UpdateChurchRunnable implements Runnable {
        private final Context mContext;
        private final String mGuid;
        private final Church mChurch;
        private final ChurchUpdates mUpdates;

        public UpdateChurchRunnable(@NonNull final Context context, @NonNull final String guid,
                                    @NonNull final Church church, @NonNull final ChurchUpdates updates) {
            mContext = context.getApplicationContext();
            mGuid = guid;
            mChurch = church;
            mUpdates = updates;
        }

        @Override
        public void run() {
            // short-circuit if there aren't any actual updates
            if (!mUpdates.hasUpdates()) {
                return;
            }

            final GmaDao dao = GmaDao.getInstance(mContext);
            final Transaction tx = dao.newTransaction();
            try {
                tx.beginTransactionNonExclusive();

                // short-circuit if we can't get a fresh copy of this church
                final Church church = dao.refresh(mChurch);
                if (church == null) {
                    return;
                }

                // perform requested updates
                final ArrayList<String> projection = Lists.newArrayList(Contract.Church.COLUMN_DIRTY);
                church.trackingChanges(true);
                if (mUpdates.mContactName != null) {
                    church.setContactName(mUpdates.mContactName);
                    projection.add(Contract.Church.COLUMN_CONTACT_NAME);
                }
                if (mUpdates.mContactEmail != null) {
                    church.setContactEmail(mUpdates.mContactEmail);
                    projection.add(Contract.Church.COLUMN_CONTACT_EMAIL);
                }
                if (mUpdates.mContactMobile != null) {
                    church.setContactMobile(mUpdates.mContactMobile);
                    projection.add(Contract.Church.COLUMN_CONTACT_MOBILE);
                }
                if (mUpdates.mSize != null) {
                    church.setSize(mUpdates.mSize);
                    projection.add(Contract.Church.COLUMN_SIZE);
                }
                if (mUpdates.mDevelopment != null) {
                    church.setDevelopment(mUpdates.mDevelopment);
                    projection.add(Contract.Church.COLUMN_DEVELOPMENT);
                }
                church.trackingChanges(false);

                // save changes
                dao.update(church, projection.toArray(new String[projection.size()]));
                tx.setSuccessful();
            } finally {
                tx.end();
            }

            // track this update in GA
            GoogleAnalyticsManager.getInstance(mContext)
                    .sendUpdateChurchEvent(mGuid, mChurch.getMinistryId(), mChurch.getId());

            // broadcast that this church was updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
            broadcastManager.sendBroadcast(updateChurchesBroadcast(mChurch.getMinistryId(), mChurch.getId()));

            // trigger a sync of dirty churches
            GmaSyncService.syncDirtyChurches(mContext, mGuid);
        }
    }

    private static class DeleteChurchRunnable implements Runnable {
        private final Context mContext;
        private final String mGuid;
        private final Church mChurch;

        public DeleteChurchRunnable(@NonNull final Context context, @NonNull final String guid,
                                    @NonNull final Church church) {
            mContext = context.getApplicationContext();
            mGuid = guid;
            mChurch = church;
        }

        @Override
        public void run() {
            final GmaDao dao = GmaDao.getInstance(mContext);

            final Transaction tx = dao.newTransaction();
            try {
                tx.beginTransactionNonExclusive();

                // short-circuit if we can't get a fresh copy of this church
                final Church church = dao.refresh(mChurch);
                if (church == null) {
                    return;
                }

                // mark this church as deleted
                church.setDeletedEndDate();
                dao.update(church, new String[] {Contract.Church.COLUMN_END_DATE, Contract.Church.COLUMN_DIRTY});
                tx.setSuccessful();
            } finally {
                tx.end();
            }

            // track this deletion in GA
            GoogleAnalyticsManager.getInstance(mContext).sendDeleteChurchEvent(mGuid, mChurch.getMinistryId(),
                                                                               mChurch.getId());

            // broadcast that this church was updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
            broadcastManager.sendBroadcast(updateChurchesBroadcast(mChurch.getMinistryId(), mChurch.getId()));

            // trigger a sync of dirty churches
            GmaSyncService.syncDirtyChurches(mContext, mGuid);
        }
    }
}
