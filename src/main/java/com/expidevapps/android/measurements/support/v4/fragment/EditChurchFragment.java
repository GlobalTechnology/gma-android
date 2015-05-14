package com.expidevapps.android.measurements.support.v4.fragment;

import static android.view.View.GONE;
import static com.expidevapps.android.measurements.Constants.ARG_CHURCH_ID;
import static com.expidevapps.android.measurements.Constants.VISIBILITY;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateChurchesBroadcast;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
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

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.AsyncTaskCompat;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;

public class EditChurchFragment extends BaseEditChurchDialogFragment {
    private static final int LOADER_CHURCH = 1;

    private static final int CHANGED_CONTACT_NAME = 0;
    private static final int CHANGED_CONTACT_EMAIL = 1;
    private static final int CHANGED_DEVELOPMENT = 2;
    private static final int CHANGED_SIZE = 3;

    private long mChurchId = Church.INVALID_ID;
    @NonNull
    private boolean[] mChanged = new boolean[4];
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
            // update church object
            // we clone mChurch to prevent corrupting the object in the ContentLoader
            final Church church = mChurch.clone();
            church.trackingChanges(true);
            if (mContactNameView != null && mChanged[CHANGED_CONTACT_NAME]) {
                church.setContactName(mContactNameView.getText().toString());
            }
            if (mContactEmailView != null && mChanged[CHANGED_CONTACT_EMAIL]) {
                church.setContactEmail(mContactEmailView.getText().toString());
            }
            if (mDevelopmentSpinner != null && mChanged[CHANGED_DEVELOPMENT]) {
                final Object development = mDevelopmentSpinner.getSelectedItem();
                church.setDevelopment(
                        development instanceof Development ? (Development) development : Development.UNKNOWN);
            }
            if (mSizeView != null && mChanged[CHANGED_SIZE]) {
                try {
                    church.setSize(Integer.parseInt(mSizeView.getText().toString()));
                } catch (final NumberFormatException ignored) {
                }
            }
            church.trackingChanges(false);

            // persist changes in the database (if there are any)
            if (church.isDirty()) {
                final Context context = getActivity().getApplicationContext();
                final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                final GmaDao dao = GmaDao.getInstance(context);

                AsyncTaskCompat.execute(new Runnable() {
                    @Override
                    public void run() {
                        // update in the database
                        dao.update(church, new String[] {Contract.Church.COLUMN_CONTACT_NAME,
                                Contract.Church.COLUMN_CONTACT_EMAIL, Contract.Church.COLUMN_SIZE,
                                Contract.Church.COLUMN_DIRTY});

                        // track this update in GA
                        GoogleAnalyticsManager.getInstance(context)
                                .sendUpdateChurchEvent(mGuid, church.getMinistryId(), church.getId());

                        // broadcast that this church was updated
                        broadcastManager.sendBroadcast(updateChurchesBroadcast(church.getMinistryId(), church.getId()));

                        // trigger a sync of dirty churches
                        GmaSyncService.syncDirtyChurches(context, mGuid);
                    }
                });
            }
        }

        // dismiss the dialog
        this.dismiss();
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
}
