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
import android.util.Log;
import android.view.View;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.content.SingleTrainingLoader;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.google.common.collect.Lists;

import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_TRAINING_ID;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateTrainingBroadcast;

public class EditTrainingFragment extends BaseEditTrainingDialogFragment {
    private static final int LOADER_TRAINING = 1;
    
    private static final int CHANGED_NAME = 0;
    private static final int CHANGED_TYPE = 1;
    private static final int CHANGED_DATE = 2;

    @NonNull
    /* final */ String mGuid;
    private long mTrainingId = Training.INVALID_ID;
    @NonNull
    private final boolean[] mChanged = new boolean[3];
    @Nullable
    private Training mTraining;

    public static EditTrainingFragment newInstance(@NonNull final String guid, final long trainingId) {
        final EditTrainingFragment fragment = new EditTrainingFragment();
        
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_GUID, guid);
        bundle.putLong(ARG_TRAINING_ID, trainingId);
        fragment.setArguments(bundle);
        
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Bundle args = this.getArguments();
        final String guid = args.getString(ARG_GUID);
        if (guid == null) {
            throw new IllegalStateException("cannot create EditTrainingFragment with invalid guid");
        }
        mGuid = guid;
        mTrainingId = args.getLong(ARG_TRAINING_ID, Training.INVALID_ID);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.startLoaders();
        ButterKnife.inject(this, getDialog());
        updateViews();
    }
    
    void onLoadTraining(final Training training)
    {
        mTraining = training;
        updateViews();
    }

    @Override
    protected void onChangeTrainingType(@NonNull final String trainingType) {
        super.onChangeTrainingType(trainingType);
        mChanged[CHANGED_TYPE] =
                !trainingType.equals(mTraining != null ? mTraining.getType() : Training.TRAINING_TYPE_OTHER);
    }

    void onTextUpdated(@NonNull final View view, @NonNull final String text)
    {
        switch (view.getId())
        {
            case R.id.et_training_name:
                mChanged[CHANGED_NAME] = !(mTraining != null ? text.equals(mTraining.getName()) : text.isEmpty());
                break;
            case R.id.et_training_type:
                mChanged[CHANGED_TYPE] = !(mTraining != null ? text.equals(mTraining.getType()) : text.isEmpty());
                break;
            case R.id.et_training_date:
                mChanged[CHANGED_DATE] = !(mTraining != null && mTraining.getDate() != null ?
                        text.equals(mTraining.getDate().toString("MM/dd/yyyy")) : text.isEmpty());
                break;
        }
    }
    
    @Optional
    @OnClick(R.id.training_update)
    void onSaveChanges() {
        if (mTraining != null) {
            // capture updates
            final TrainingUpdates updates = new TrainingUpdates();
            if (mTrainingName != null && mChanged[CHANGED_NAME]) {
                updates.mTrainingName = mTrainingName.getText().toString();
            }
            /*if (mTrainingType != null && mChanged[CHANGED_TYPE]) {
                updates.mTrainingType = mTrainingType.getText().toString();
            }*/
            if (mTrainingTypeSpinner != null && mChanged[CHANGED_TYPE]) {
                final Object trainingType = mTrainingTypeSpinner.getSelectedItem();

                updates.mTrainingType = ((String) trainingType).equalsIgnoreCase(Training.TRAINING_TYPE_OTHER) ? "" : (String) trainingType;
            }
            if (mTrainingDate != null && mChanged[CHANGED_DATE]) {
                updates.mTrainingDate = mTrainingDate.getText().toString();
            }

            // persist changes in the database (if there are any)
            if (updates.hasUpdates()) {
                AsyncTaskCompat.execute(new UpdateTrainingRunnable(getActivity(), mGuid, mTraining, updates));
            }
        }

        // dismiss the dialog
        dismiss();
    }

    @Optional
    @OnClick(R.id.training_delete)
    void onDeleteTraining() {
        Log.d("ITH", "onDeleteTraining called.");

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage("Are you sure you want to delete ?")
                .setTitle("Confirm?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //dismiss the dialog
                        dismiss();

                        if(mTraining != null) {
                            AsyncTaskCompat.execute(new DeleteTrainingRunnable(getActivity(), mGuid, mTraining));
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

    @Override
    public void onStop()
    {
        super.onStop();
        ButterKnife.reset(this);
    }

    private void startLoaders()
    {
        final LoaderManager manager = this.getLoaderManager();
        manager.initLoader(LOADER_TRAINING, null, new TrainingLoaderCallBacks());
    }
    
    private void updateViews()
    {
        if (mTrainingName != null)
        {
            mTrainingName.setText(mTraining != null ? mTraining.getName() : null);
        }
        if (mTrainingDate != null)
        {
            mTrainingDate.setText(mTraining != null && mTraining.getDate() != null ? mTraining.getDate().toString(
                    "MM/dd/yyyy") : null);
        }
        if (mTrainingMcc != null) {
            mTrainingMcc.setText(mTraining != null ? mTraining.getMcc().name() : null);
        }
        if (mTrainingTypeSpinner != null && mTrainingTypeAdapter != null && !mChanged[CHANGED_TYPE]) {
            mTrainingTypeSpinner.setSelection(
                    mTrainingTypeAdapter.getPosition(mTraining != null ? mTraining.getType() : Training.TRAINING_TYPE_OTHER));
        }
    }
    
    @Optional
    @OnTextChanged(value = R.id.et_training_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void updateName(@Nullable final Editable text)
    {
        if (mTrainingName != null)
        {
            onTextUpdated(mTrainingName, text != null ? text.toString() : "");
        }
    }
    
    @Optional
    @OnTextChanged(value = R.id.et_training_date, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void updateDate(@Nullable final Editable text)
    {
        if (mTrainingDate != null)
        {
            onTextUpdated(mTrainingDate, text != null ? text.toString() : "");
        }
    }
    
    private class TrainingLoaderCallBacks extends SimpleLoaderCallbacks<Training>
    {
        @Nullable
        @Override
        public Loader<Training> onCreateLoader(int id, @Nullable Bundle bundle)
        {
            switch (id)
            {
                case LOADER_TRAINING:
                    return new SingleTrainingLoader(getActivity(), mTrainingId);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Training> trainingLoader, @Nullable Training training)
        {
            switch (trainingLoader.getId())
            {
                case LOADER_TRAINING:
                    onLoadTraining(training);
            }
        }
    }

    private static class TrainingUpdates {
        @Nullable
        String mTrainingName;
        @Nullable
        String mTrainingType;
        @Nullable
        String mTrainingDate;

        boolean hasUpdates() {
            return mTrainingName != null || mTrainingType != null || mTrainingDate != null;
        }
    }

    private static class UpdateTrainingRunnable implements Runnable {
        private final Context mContext;
        private final String mGuid;
        private final Training mTraining;
        private final TrainingUpdates mUpdates;

        public UpdateTrainingRunnable(@NonNull final Context context, @NonNull final String guid,
                                    @NonNull final Training training, @NonNull final TrainingUpdates updates) {
            mContext = context.getApplicationContext();
            mGuid = guid;
            mTraining = training;
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

                // short-circuit if we can't get a fresh copy of this training
                final Training training = dao.refresh(mTraining);
                if (training == null) {
                    return;
                }
                // perform requested updates
                final ArrayList<String> projection = Lists.newArrayList(Contract.Training.COLUMN_DIRTY);
                training.trackingChanges(true);
                if (mUpdates.mTrainingName != null) {
                    training.setName(mUpdates.mTrainingName);
                    projection.add(Contract.Training.COLUMN_NAME);
                }
                if (mUpdates.mTrainingType != null) {
                    training.setType(mUpdates.mTrainingType);
                    projection.add(Contract.Training.COLUMN_TYPE);
                }
                if (mUpdates.mTrainingDate != null) {
                    try
                    {
                        DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                        training.setDate(new LocalDate(format.parse(mUpdates.mTrainingDate)));
                    }
                    catch (ParseException ignored) {
                        Log.e("ParseException", "error Parsing Date string to LocalDate.");
                    }
                    projection.add(Contract.Training.COLUMN_DATE);
                }

                training.trackingChanges(false);

                // save changes
                dao.update(training, projection.toArray(new String[projection.size()]));
                tx.setSuccessful();
            } finally {
                tx.end();
            }

            // track this update in GA
            GoogleAnalyticsManager.getInstance(mContext)
                    .sendUpdateTrainingEvent(mGuid, mTraining.getMinistryId(), mTraining.getMcc(), mTraining.getId());

            // broadcast that this training was updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
            broadcastManager.sendBroadcast(updateTrainingBroadcast(mTraining.getMinistryId(), mTraining.getId()));

            // trigger a sync of dirty trainings
            GmaSyncService.syncDirtyTrainings(mContext, mGuid);
        }
    }

    private static class DeleteTrainingRunnable implements Runnable {
        private final Context mContext;
        private final String mGuid;
        private final Training mTraining;

        public DeleteTrainingRunnable(@NonNull final Context context, @NonNull final String guid,
                                      @NonNull final Training training) {
            mContext = context.getApplicationContext();
            mGuid = guid;
            mTraining = training;
        }

        @Override
        public void run() {
            // mark this training as deleted
            final GmaDao dao = GmaDao.getInstance(mContext);
            mTraining.setDeleted(true);
            dao.update(mTraining, new String[] {Contract.Training.COLUMN_DELETED});

            // track this delete in GA
            GoogleAnalyticsManager.getInstance(mContext).sendDeleteTrainingEvent(mGuid, mTraining.getMinistryId(),
                                                                                 mTraining.getMcc(), mTraining.getId());

            // broadcast that this training was updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
            broadcastManager.sendBroadcast(updateTrainingBroadcast(mTraining.getMinistryId(), mTraining.getId()));

            // trigger a sync of dirty trainings
            GmaSyncService.syncDirtyTrainings(mContext, mGuid);
        }
    }
}
