package com.expidevapps.android.measurements.support.v4.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.google.android.gms.maps.model.LatLng;

import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.ccci.gto.android.common.util.BundleCompat;
import org.joda.time.LocalDate;

import java.security.SecureRandom;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

import static com.expidevapps.android.measurements.Constants.ARG_LOCATION;
import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.ARG_PERSON_ID;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateTrainingBroadcast;

public class CreateTrainingFragment extends BaseEditTrainingDialogFragment {
    @SuppressLint("TrulyRandom")
    private static final SecureRandom RAND = new SecureRandom();

    @NonNull
    private String mMinistryId = Ministry.INVALID_ID;
    @Nullable
    private String mPersonId;
    @NonNull
    private Mcc mMcc = Mcc.UNKNOWN;
    @Nullable
    private LatLng mLocation;

    @Optional
    @Nullable
    @InjectView(R.id.training_update)
    TextView mSaveView;

    @Optional
    @Nullable
    @InjectView(R.id.training_delete)
    Button mDeleteTraining;

    public static Bundle buildArgs(@NonNull final String guid, @NonNull final String ministryId, @NonNull final Mcc mcc, @Nullable final String personId,
                                   @NonNull final LatLng location) {
        final Bundle args = buildArgs(guid);
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putString(ARG_MCC, mcc.toString());
        args.putString(ARG_PERSON_ID, personId);
        args.putParcelable(ARG_LOCATION, location);
        return args;
    }

    public static CreateTrainingFragment newInstance(@NonNull final String guid, @NonNull final String ministryId,
                                                     @NonNull final Mcc mcc, @Nullable final String personId, @NonNull final LatLng location) {
        final CreateTrainingFragment fragment = new CreateTrainingFragment();
        fragment.setArguments(buildArgs(guid, ministryId, mcc, personId, location));
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        final Bundle args = this.getArguments();
        if (args != null) {
            mMinistryId = BundleCompat.getString(args, ARG_MINISTRY_ID, Ministry.INVALID_ID);
            mMcc = Mcc.fromRaw(args.getString(ARG_MCC));
            mPersonId = BundleCompat.getString(args, ARG_PERSON_ID, null);
            mLocation = args.getParcelable(ARG_LOCATION);
        } else {
            mMinistryId = Ministry.INVALID_ID;
            mMcc = Mcc.UNKNOWN;
            mPersonId = null;
            mLocation = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTitle(R.string.title_dialog_training_create);
        setTrainingDate(LocalDate.now());
        setupViews();
    }

    @Optional
    @OnClick(R.id.training_update)
    void onSaveTraining() {
        // create new church object
        final Training training = new Training();
        training.setNew(true);
        training.setMinistryId(mMinistryId);
        training.setMcc(mMcc);
        training.setDate(mTrainingDate);
        training.setCreatedBy(mPersonId);
        if (mLocation != null) {
            training.setLatitude(mLocation.latitude);
            training.setLongitude(mLocation.longitude);
        }
        if (mTrainingName != null) {
            if (!mTrainingName.getText().toString().isEmpty()) {
                training.setName(mTrainingName.getText().toString());
            }
            else {
                return;
            }
        }
        if (mTrainingTypeSpinner != null) {
            final Object trainingType = mTrainingTypeSpinner.getSelectedItem();
            training.setType(((String) trainingType).equalsIgnoreCase(Training.TRAINING_TYPE_OTHER) ? "" : (String) trainingType);
        }
        if (mTrainingParticipants != null) {
            training.setParticipants(
                    !mTrainingParticipants.getText().toString().isEmpty() ? Integer.valueOf(mTrainingParticipants.getText().toString()) : 0);
        }

        // save new church
        AsyncTaskCompat.execute(new CreateTrainingRunnable(getActivity().getApplicationContext(), mGuid, training));

        // dismiss the dialog
        this.dismiss();
    }

    /* END lifecycle */

    private void setupViews() {
        if (mTrainingMcc != null) {
            mTrainingMcc.setText(mMcc.toString());
        }
        if(mSaveView != null) {
            mSaveView.setText(R.string.btn_training_create);
        }

        if(mDeleteTraining != null) {
            mDeleteTraining.setVisibility(View.GONE);
        }

        if(mStagesData != null) {
            mStagesData.setVisibility(View.GONE);
        }

        if(mTrainingData != null) {
            mTrainingData.setVisibility(View.VISIBLE);
        }

        if(mShowStages != null) {
            mShowStages.setEnabled(false);
        }
    }

    private static class CreateTrainingRunnable implements Runnable {
        @NonNull
        private final Context mContext;
        @NonNull
        private final LocalBroadcastManager mBroadcastManager;
        @NonNull
        private final GmaDao mDao;
        @NonNull
        private final String mGuid;
        @NonNull
        private final Training mTraining;

        CreateTrainingRunnable(@NonNull final Context context, @NonNull final String guid, @NonNull final Training training) {
            mContext = context;
            mGuid = guid;
            mTraining = training;
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
                mTraining.setId(id);

                // update in the database
                try {
                    mDao.insert(mTraining, SQLiteDatabase.CONFLICT_ROLLBACK);
                    saved = true;
                } catch (final SQLException e) {
                    Log.e("CreateTraining", "insert error", e);
                }
            }

            // trigger GA event
            GoogleAnalyticsManager.getInstance(mContext).sendCreateTrainingEvent(mGuid, mTraining.getMinistryId(),
                                                                                 mTraining.getMcc());

            // broadcast that this training was created
            mBroadcastManager.sendBroadcast(updateTrainingBroadcast(mTraining.getMinistryId(), mTraining.getId()));

            // trigger a sync of dirty trainings
            GmaSyncService.syncDirtyTrainings(mContext, mGuid);
        }
    }
}
