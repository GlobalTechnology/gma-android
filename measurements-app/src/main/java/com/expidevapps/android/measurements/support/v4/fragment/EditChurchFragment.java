package com.expidevapps.android.measurements.support.v4.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.expidevapps.android.measurements.Constants.ARG_CHURCH_ID;
import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.VISIBILITY;
import static com.expidevapps.android.measurements.model.Task.ADMIN_CHURCH;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateChurchesBroadcast;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.View;
import android.widget.RadioButton;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Church.Development;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.adapter.MinistrySpinnerCursorAdapter;
import com.expidevapps.android.measurements.support.v4.content.AssignmentLoader;
import com.expidevapps.android.measurements.support.v4.content.ChurchLoader;
import com.expidevapps.android.measurements.support.v4.content.MinistriesCursorLoader;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.google.common.collect.Lists;

import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.ccci.gto.android.common.util.BundleCompat;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import butterknife.Optional;

public class EditChurchFragment extends BaseEditChurchDialogFragment {
    private static final int LOADER_CHURCH = 1;
    private static final int LOADER_MINISTRIES = 2;
    private static final int LOADER_ASSIGNMENT = 3;

    private static final int CHANGED_CONTACT_NAME = 0;
    private static final int CHANGED_CONTACT_EMAIL = 1;
    private static final int CHANGED_DEVELOPMENT = 2;
    private static final int CHANGED_SIZE = 3;
    private static final int CHANGED_CONTACT_MOBILE = 4;
    private static final int CHANGED_SECURITY = 5;
    private static final int CHANGED_JESUS_FILM_ACTIVITY = 6;
    private static final int CHANGED_MINISTRY = 7;

    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();
    private final CursorLoaderCallbacks mLoaderCallbacksCursor = new CursorLoaderCallbacks();

    private long mChurchId = Church.INVALID_ID;
    @NonNull
    private boolean[] mChanged = new boolean[8];
    @Nullable
    private Church mChurch;
    @Nullable
    private Assignment mAssignment;
    @Nullable
    private String mMinistryId;

    @Optional
    @InjectViews({R.id.nameRow})
    List<View> mHiddenViews;
    @Nullable
    private MinistrySpinnerCursorAdapter mMinistriesAdapter;
    @Nullable
    private Cursor mMinistries = null;

    @NonNull
    private static Bundle buildArgs(@NonNull final String guid, final long churchId, final String ministryId) {
        final Bundle args = buildArgs(guid);
        args.putLong(ARG_CHURCH_ID, churchId);
        args.putString(ARG_MINISTRY_ID, ministryId);
        return args;
    }

    public static EditChurchFragment newInstance(@NonNull final String guid, final long churchId,
                                                 final String ministryId) {
        final EditChurchFragment fragment = new EditChurchFragment();
        fragment.setArguments(buildArgs(guid, churchId, ministryId));
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = this.getArguments();
        mChurchId = args.getLong(ARG_CHURCH_ID, Church.INVALID_ID);

        mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, Ministry.INVALID_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.startLoaders();
        ButterKnife.apply(mHiddenViews, VISIBILITY, GONE);
        updateViews();
    }

    void onLoadChurch(@Nullable final Church church) {
        final Church old = mChurch;
        mChurch = church;

        // restart assignment loader if the church ministry id changes
        if (mChurch == null || old == null || !mChurch.getMinistryId().equals(old.getMinistryId())) {
            restartAssignmentLoader();
        }

        // update views
        updateViews();
    }

    void onLoadAssignment(@Nullable final Assignment assignment) {
        mAssignment = assignment;

        // update views
        updateViews();
    }

    @Override
    protected void onChangeDevelopment(@NonNull final Development development) {
        super.onChangeDevelopment(development);
        mChanged[CHANGED_DEVELOPMENT] =
                !development.equals(mChurch != null ? mChurch.getDevelopment() : Development.UNKNOWN);
    }

    protected void onChangeSecurity(@NonNull final Church.Security security) {
        mChanged[CHANGED_SECURITY] =
                !security.equals(mChurch != null ? mChurch.getSecurity() : Church.Security.DEFAULT);
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
            if (mSecuritySpinner != null && mChanged[CHANGED_SECURITY]) {
                final Object security = mSecuritySpinner.getSelectedItem();
                updates.mSecurity =
                        security instanceof Church.Security ? (Church.Security) security : Church.Security.DEFAULT;
            }
            if (mJesusFilmActivity != null && mChanged[CHANGED_JESUS_FILM_ACTIVITY]) {
                if (((RadioButton) mJesusFilmActivity.findViewById(R.id.rbYes)).isChecked()) {
                    updates.mJfActivity = true;
                }
                else if (((RadioButton) mJesusFilmActivity.findViewById(R.id.rbNo)).isChecked()) {
                    updates.mJfActivity = false;
                }
            }
            if (mMinistrySpinner != null && mMinistriesAdapter != null && mChanged[CHANGED_MINISTRY]) {
                final Cursor ministry = mMinistriesAdapter.getCursor();
                updates.mMinistry = ministry.getString(ministry.getColumnIndex(Contract.Ministry.COLUMN_MINISTRY_ID));
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
        new AlertDialog.Builder(getActivity()).setTitle(R.string.title_dialog_church_delete)
                .setMessage(R.string.text_dialog_church_delete_confirm)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        //dismiss the dialog
                        dismiss();

                        if (mChurch != null) {
                            AsyncTaskCompat.execute(new DeleteChurchRunnable(getActivity(), mGuid, mChurch));
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();
        final Bundle args = new Bundle();
        args.putString(ARG_GUID, mGuid);

        manager.initLoader(LOADER_CHURCH, null, new ChurchLoaderCallbacks());
        manager.initLoader(LOADER_MINISTRIES, args, mLoaderCallbacksCursor);
    }

    private void restartAssignmentLoader() {
        // shutdown loader if it's running
        getLoaderManager().destroyLoader(LOADER_ASSIGNMENT);

        // start loader if we have a valid church
        if (mChurch != null) {
            final Bundle args = new Bundle(2);
            args.putString(ARG_GUID, mGuid);
            args.putString(ARG_MINISTRY_ID, mChurch.getMinistryId());
            getLoaderManager().initLoader(LOADER_ASSIGNMENT, args, mLoaderCallbacksAssignment);
        }
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
        if (mSecuritySpinner != null && mSecurityAdapter != null && !mChanged[CHANGED_SECURITY]) {
            mSecuritySpinner.setSelection(
                    mSecurityAdapter.getPosition(mChurch != null ? mChurch.getSecurity() : Church.Security.DEFAULT));
        }
        if (mJesusFilmActivity != null && !mChanged[CHANGED_JESUS_FILM_ACTIVITY] && mChurch != null) {
            if (mChurch.isJesusFilmActivity()) {
                ((RadioButton) mJesusFilmActivity.findViewById(R.id.rbYes)).setChecked(true);
            }
            else {
                ((RadioButton) mJesusFilmActivity.findViewById(R.id.rbNo)).setChecked(true);
            }
        }

        // update whether we are editing this church or not
        updateEditMode();
    }

    private void updateEditMode() {
        final boolean editing = mChurch != null && mChurch.canEdit(mAssignment);
        final boolean admin = mAssignment != null && mAssignment.can(ADMIN_CHURCH);

        if (mContactNameView != null) {
            mContactNameView.setEnabled(editing);
        }
        if (mContactEmailView != null) {
            mContactEmailView.setEnabled(editing);
        }
        if (mContactMobileView != null) {
            mContactMobileView.setEnabled(editing);
        }
        if (mSizeView != null) {
            mSizeView.setEnabled(editing);
        }
        if (mDevelopmentSpinner != null) {
            mDevelopmentSpinner.setEnabled(editing);
        }

        if (mSecurityRow != null) {
            mSecurityRow.setVisibility(admin ? VISIBLE : GONE);
        }
        if (mMinistryRow != null) {
            mMinistryRow.setVisibility(admin ? VISIBLE : GONE);
        }

        if (mBottomButtonContainer != null) {
            mBottomButtonContainer.setVisibility(editing ? VISIBLE : GONE);
        }
        if (mJesusFilmActivity != null) {
            mJesusFilmActivity.findViewById(R.id.rbYes).setEnabled(editing);
            mJesusFilmActivity.findViewById(R.id.rbNo).setEnabled(editing);
        }
    }

    void onLoadMinistries(@Nullable final Cursor c) {
        mMinistries = c;

        if (mMinistrySpinner != null && mMinistries != null && !mChanged[CHANGED_MINISTRY]) {

            // generate Adapter for ministries
            mMinistriesAdapter = new MinistrySpinnerCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, mMinistries,
                    new String[] {Contract.Ministry.COLUMN_NAME}, new int[] {android.R.id.text1});
            mMinistriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attach adapter
            mMinistrySpinner.setAdapter(mMinistriesAdapter);

            if (mChurch != null) {
                int position = getPosition(mMinistryId, mMinistries);
                mMinistrySpinner.setSelection(position);
            }
        }
    }

    private int getPosition(String ministryid, Cursor cursor) {
        int i;
        int position = 0;
        cursor.moveToFirst();
        for (i = 0; i < cursor.getCount() - 1; i++) {

            String locationVal = CursorUtils.getNonNullString(cursor, Contract.Ministry.COLUMN_MINISTRY_ID,
                                                              Ministry.INVALID_ID);
            if (locationVal.equals(ministryid)) {
                position = i;
                break;
            } else {
                position = 0;
            }
            cursor.moveToNext();
        }
        return position;
    }

    @Optional
    @OnItemSelected(R.id.ministry)
    void changeMinistry() {
        if (mMinistrySpinner != null && mMinistriesAdapter != null) {
            final Cursor item = mMinistriesAdapter.getCursor();
            final String ministryId =
                    CursorUtils.getNonNullString(item, Contract.Ministry.COLUMN_MINISTRY_ID, Ministry.INVALID_ID);

            mChanged[CHANGED_MINISTRY] = !ministryId.equals(mMinistryId);
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

    @Optional
    @OnCheckedChanged({R.id.rbYes, R.id.rbNo})
    void updateJesusFilmActivity() {
        if (mJesusFilmActivity != null) {
            if ((mChurch.isJesusFilmActivity() && ((RadioButton) mJesusFilmActivity.findViewById(R.id.rbNo)).isChecked()) ||
                    (!mChurch.isJesusFilmActivity() && ((RadioButton) mJesusFilmActivity.findViewById(R.id.rbYes)).isChecked())) {
                mChanged[CHANGED_JESUS_FILM_ACTIVITY] = true;
            }
            else {
                mChanged[CHANGED_JESUS_FILM_ACTIVITY] = false;
            }
        }
    }

    private class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment> {
        @Nullable
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle bundle) {
            switch (id) {
                case LOADER_ASSIGNMENT:
                    return new AssignmentLoader(getActivity(), bundle);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Assignment> loader, @Nullable final Assignment assignment) {
            switch (loader.getId()) {
                case LOADER_ASSIGNMENT:
                    onLoadAssignment(assignment);
                    break;
            }
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

    private class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MINISTRIES:
                    return new MinistriesCursorLoader(getActivity(), args);
                default:
                    return null;
            }
        }

        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_MINISTRIES:
                    onLoadMinistries(cursor);
                    break;
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
        Boolean mJfActivity;
        @Nullable
        Development mDevelopment;
        @Nullable
        Church.Security mSecurity;
        @Nullable
        String mMinistry;

        boolean hasUpdates() {
            return mContactName != null || mContactEmail != null || mContactMobile != null || mSize != null || mDevelopment != null || mSecurity != null || mJfActivity != null || mMinistry != null;
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
                if (mUpdates.mSecurity != null) {
                    church.setSecurity(mUpdates.mSecurity);
                    projection.add(Contract.Church.COLUMN_SECURITY);
                }
                if (mUpdates.mJfActivity != null) {
                    church.setJesusFilmActivity(mUpdates.mJfActivity);
                    projection.add(Contract.Church.COLUMN_JESUS_FILM_ACTIVITY);
                }
                if (mUpdates.mMinistry != null) {
                    church.setMinistryId(mUpdates.mMinistry);
                    projection.add(Contract.Church.COLUMN_MINISTRY_ID);
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
