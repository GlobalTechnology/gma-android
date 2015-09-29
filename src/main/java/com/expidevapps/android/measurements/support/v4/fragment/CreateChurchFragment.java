package com.expidevapps.android.measurements.support.v4.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Church;
import com.expidevapps.android.measurements.model.Church.Development;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.content.AssignmentLoader;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.google.android.gms.maps.model.LatLng;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.ccci.gto.android.common.util.BundleCompat;

import java.security.SecureRandom;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_LOCATION;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.ARG_PERSON_ID;
import static com.expidevapps.android.measurements.model.Task.ADMIN_CHURCH;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateChurchesBroadcast;

public class CreateChurchFragment extends BaseEditChurchDialogFragment {
    private static final int LOADER_ASSIGNMENT = 1;

    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();

    @SuppressLint("TrulyRandom")
    private static final SecureRandom RAND = new SecureRandom();

    @NonNull
    private String mMinistryId = Ministry.INVALID_ID;
    @Nullable
    private String mPersonId;
    @Nullable
    private LatLng mLocation;
    @Nullable
    private Assignment mAssignment;

    @Optional
    @Nullable
    @InjectView(R.id.save)
    TextView mSaveView;

    @Optional
    @Nullable
    @InjectView(R.id.delete)
    Button mDeleteChurch;

    public static Bundle buildArgs(@NonNull final String guid, @NonNull final String ministryId, @Nullable final String personId,
                                   @NonNull final LatLng location) {
        final Bundle args = buildArgs(guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putString(ARG_PERSON_ID, personId);
        args.putParcelable(ARG_LOCATION, location);
        return args;
    }

    public static CreateChurchFragment newInstance(@NonNull final String guid, @NonNull final String ministryId, @Nullable final String personId,
                                                   @NonNull final LatLng location) {
        final CreateChurchFragment fragment = new CreateChurchFragment();
        fragment.setArguments(buildArgs(guid, ministryId, personId, location));
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        final Bundle args = this.getArguments();
        if (args != null) {
            mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, Ministry.INVALID_ID);
            mPersonId = BundleCompat.getString(args, ARG_PERSON_ID, null);
            mLocation = args.getParcelable(ARG_LOCATION);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaders();
        updateTitle(R.string.title_dialog_church_create);
        setupViews();
    }

    void onLoadAssignment(@Nullable final Assignment assignment) {
        mAssignment = assignment;
        updateViewEditMode();
    }

    @Optional
    @OnClick(R.id.save)
    void onSaveChurch() {
        // create new church object
        final Church church = new Church();
        church.setNew(true);
        church.setMinistryId(mMinistryId);
        church.setCreatedBy(mPersonId);
        if (mLocation != null) {
            church.setLatitude(mLocation.latitude);
            church.setLongitude(mLocation.longitude);
        }
        if (mNameView != null) {
            church.setName(mNameView.getText().toString());
        }
        if (mContactNameView != null) {
            church.setContactName(mContactNameView.getText().toString());
        }
        if (mContactEmailView != null) {
            church.setContactEmail(mContactEmailView.getText().toString());
        }
        if (mContactMobileView != null) {
            church.setContactMobile(mContactMobileView.getText().toString());
        }
        if (mJesusFilmActivity != null) {
            if (((RadioButton) mJesusFilmActivity.findViewById(R.id.rbYes)).isChecked()) {
                church.setJesusFilmActivity(true);
            }
            else if (((RadioButton) mJesusFilmActivity.findViewById(R.id.rbNo)).isChecked()) {
                church.setJesusFilmActivity(false);
            }
            else {
                Toast alertToast = Toast.makeText(getActivity(), getResources().getString(R.string.alrt_church_jesus_film_required), Toast.LENGTH_LONG);
                alertToast.setGravity(Gravity.CENTER, 0 ,0);
                alertToast.show();
                return;
            }
        }

        if (mDevelopmentSpinner != null) {
            final Object development = mDevelopmentSpinner.getSelectedItem();
            if (development instanceof Development) {
                church.setDevelopment((Development) development);
            }
        }
        if (mSizeView != null) {
            try {
                church.setSize(Integer.parseInt(mSizeView.getText().toString()));
            } catch (final NumberFormatException ignored) {
            }
        }
        if (mSecuritySpinner != null && mSecuritySpinner.getVisibility() == VISIBLE) {
            final Object security = mSecuritySpinner.getSelectedItem();
            if (security instanceof Church.Security) {
                church.setSecurity((Church.Security) security);
            }
        }
        else {
            church.setSecurity(Church.Security.DEFAULT);
        }
        // save new church
        AsyncTaskCompat.execute(new CreateChurchRunnable(getActivity().getApplicationContext(), mGuid, church));

        // dismiss the dialog
        this.dismiss();
    }

    /* END lifecycle */

    private void setupViews() {
        if(mSaveView != null) {
            mSaveView.setText(R.string.btn_church_create);
        }

        if(mDeleteChurch != null) {
            mDeleteChurch.setVisibility(GONE);
        }

        if (mMinistryRow != null) {
            mMinistryRow.setVisibility(GONE);
        }

        if (mSecuritySpinner != null && mSecurityAdapter != null) {
            mSecuritySpinner.setSelection(mSecurityAdapter.getPosition(Church.Security.DEFAULT));
        }

        updateViewEditMode();
    }

    private void startLoaders() {
        final Bundle args = new Bundle(2);
        args.putString(ARG_GUID, mGuid);
        args.putString(ARG_MINISTRY_ID, mMinistryId);

        getLoaderManager().initLoader(LOADER_ASSIGNMENT, args, mLoaderCallbacksAssignment);
    }

    private void updateViewEditMode() {
        if (mSecurityRow != null) {
            mSecurityRow.setVisibility(mAssignment != null && mAssignment.can(ADMIN_CHURCH) ? VISIBLE : GONE);
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

    private static class CreateChurchRunnable implements Runnable {
        @NonNull
        private final Context mContext;
        @NonNull
        private final LocalBroadcastManager mBroadcastManager;
        @NonNull
        private final GmaDao mDao;
        @NonNull
        private final String mGuid;
        @NonNull
        private final Church mChurch;

        CreateChurchRunnable(@NonNull final Context context, @NonNull final String guid, @NonNull final Church church) {
            mContext = context;
            mGuid = guid;
            mChurch = church;
            mBroadcastManager = LocalBroadcastManager.getInstance(mContext);
            mDao = GmaDao.getInstance(mContext);
        }

        @Override
        public void run() {
            boolean saved = false;
            while (!saved) {
                // generate a new id
                long id = 0;
                while (id < Integer.MAX_VALUE) {
                    id = RAND.nextLong();
                }
                mChurch.setId(id);

                // update in the database
                try {
                    mDao.insert(mChurch, SQLiteDatabase.CONFLICT_ROLLBACK);
                    saved = true;
                } catch (final SQLException e) {
                    Log.e("CreateChurch", "insert error", e);
                }
            }

            // trigger GA event
            GoogleAnalyticsManager.getInstance(mContext).sendCreateChurchEvent(mGuid, mChurch.getMinistryId());

            // broadcast that this church was created
            mBroadcastManager.sendBroadcast(updateChurchesBroadcast(mChurch.getMinistryId(), mChurch.getId()));

            // trigger a sync of dirty churches
            GmaSyncService.syncDirtyChurches(mContext, mGuid);
        }
    }
}
