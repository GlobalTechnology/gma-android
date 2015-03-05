package com.expidev.gcmapp.support.v4.fragment;

import static com.expidev.gcmapp.Constants.ARG_CHURCH_ID;
import static com.expidev.gcmapp.utils.BroadcastUtils.updateChurchesBroadcast;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.db.Contract;
import com.expidev.gcmapp.db.GmaDao;
import com.expidev.gcmapp.model.Church;
import com.expidev.gcmapp.service.MinistriesService;
import com.expidev.gcmapp.support.v4.content.ChurchLoader;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.fragment.AbstractDialogFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;

public class EditChurchFragment extends AbstractDialogFragment {
    private static final int LOADER_CHURCH = 1;

    private static final int CHANGED_CONTACT_NAME = 0;
    private static final int CHANGED_CONTACT_EMAIL = 1;
    private static final int CHANGED_SIZE = 2;

    private long mChurchId = Church.INVALID_ID;
    @NonNull
    private boolean[] mChanged = new boolean[3];
    @Nullable
    private Church mChurch;

    @Optional
    @Nullable
    @InjectView(R.id.title)
    TextView mTitleView;
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
    @Optional
    @Nullable
    @InjectView(R.id.icon)
    ImageView mIconView;

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
    public void onStart() {
        super.onStart();
        this.startLoaders();
        ButterKnife.inject(this, getDialog());
        updateViews();
    }

    void onLoadChurch(final Church church) {
        mChurch = church;
        updateViews();
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

                dao.async(new Runnable() {
                    @Override
                    public void run() {
                        // update in the database
                        dao.update(church, new String[] {Contract.Church.COLUMN_CONTACT_NAME,
                                Contract.Church.COLUMN_CONTACT_EMAIL, Contract.Church.COLUMN_SIZE,
                                Contract.Church.COLUMN_DIRTY});

                        // broadcast that this church was updated
                        broadcastManager.sendBroadcast(updateChurchesBroadcast(church.getMinistryId(), church.getId()));

                        // trigger a sync of dirty churches
                        MinistriesService.syncDirtyChurches(context);
                    }
                });
            }
        }

        // dismiss the dialog
        this.dismiss();
    }

    @Optional
    @OnClick(R.id.cancel)
    void onCancelEdit() {
        this.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();
        manager.initLoader(LOADER_CHURCH, null, new ChurchLoaderCallbacks());
    }

    private void updateViews() {
        if (mIconView != null) {
            final int image;
            switch (mChurch != null ? mChurch.getDevelopment() : Church.Development.UNKNOWN) {
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
        if(mTitleView != null) {
            mTitleView.setText(mChurch != null ? mChurch.getName() : null);
        }
        if (mContactNameView != null && !mChanged[CHANGED_CONTACT_NAME]) {
            mContactNameView.setText(mChurch != null ? mChurch.getContactName() : null);
        }
        if (mContactEmailView != null && !mChanged[CHANGED_CONTACT_EMAIL]) {
            mContactEmailView.setText(mChurch != null ? mChurch.getContactEmail() : null);
        }
        if (mSizeView != null && !mChanged[CHANGED_SIZE]) {
            mSizeView.setText(mChurch != null ? Integer.toString(mChurch.getSize()) : null);
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
