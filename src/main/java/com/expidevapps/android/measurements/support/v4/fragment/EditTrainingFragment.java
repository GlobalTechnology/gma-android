package com.expidevapps.android.measurements.support.v4.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Assignment;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.service.GoogleAnalyticsManager;
import com.expidevapps.android.measurements.support.v4.content.AssignmentLoader;
import com.expidevapps.android.measurements.support.v4.content.SingleTrainingLoader;
import com.expidevapps.android.measurements.support.v7.adapter.TrainingCompletionRecyclerViewAdapter;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.recyclerview.decorator.DividerItemDecoration;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.joda.time.LocalDate;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.expidevapps.android.measurements.Constants.ARG_GUID;
import static com.expidevapps.android.measurements.Constants.ARG_MINISTRY_ID;
import static com.expidevapps.android.measurements.Constants.ARG_TRAINING_ID;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateTrainingBroadcast;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

public class EditTrainingFragment extends BaseEditTrainingDialogFragment {
    private static final int LOADER_TRAINING = 1;
    private static final int LOADER_ASSIGNMENT = 2;
    
    private static final int CHANGED_NAME = 0;
    private static final int CHANGED_TYPE = 1;
    private static final int CHANGED_DATE = 2;

    @SuppressLint("TrulyRandom")
    private static final SecureRandom RAND = new SecureRandom();

    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();

    private long mTrainingId = Training.INVALID_ID;
    @NonNull
    private final boolean[] mChanged = new boolean[3];
    @Nullable
    private Training mTraining;
    @Nullable
    private Assignment mAssignment;

    @Nullable
    private TrainingCompletionRecyclerViewAdapter mTrainingCompletionAdapter;

    public static EditTrainingFragment newInstance(@NonNull final String guid, final long trainingId) {
        final EditTrainingFragment fragment = new EditTrainingFragment();
        
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_GUID, guid);
        bundle.putLong(ARG_TRAINING_ID, trainingId);
        fragment.setArguments(bundle);
        
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Bundle args = this.getArguments();
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

    void onLoadTraining(@Nullable final Training training) {
        final Training old = mTraining;
        mTraining = training;
        if (!mChanged[CHANGED_DATE]) {
            setTrainingDate(mTraining != null ? mTraining.getDate() : null);
        }

        // restart assignment loader if the training ministry changed
        if (old == null || mTraining == null || !old.getMinistryId().equals(mTraining.getMinistryId())) {
            restartAssignmentLoader();
        }

        updateViews();
    }

    void onLoadAssignment(@Nullable final Assignment assignment) {
        mAssignment = assignment;

        updateViews();
    }

    @Override
    protected void onChangeTrainingDate(@Nullable final LocalDate date) {
        super.onChangeTrainingDate(date);
        mChanged[CHANGED_DATE] = !Objects.equal(mTraining != null ? mTraining.getDate() : null, date);
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
            if (mTrainingTypeSpinner != null && mChanged[CHANGED_TYPE]) {
                final Object trainingType = mTrainingTypeSpinner.getSelectedItem();

                updates.mTrainingType = ((String) trainingType).equalsIgnoreCase(Training.TRAINING_TYPE_OTHER) ? "" : (String) trainingType;
            }
            if (mChanged[CHANGED_DATE]) {
                updates.mTrainingDate = mTrainingDate;
                updates.mTrainingDateChanged = mChanged[CHANGED_DATE];
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
        new AlertDialog.Builder(getActivity()).setTitle(R.string.title_dialog_training_delete).setMessage(
                R.string.text_dialog_training_delete_confirm)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        //dismiss the dialog
                        dismiss();

                        if (mTraining != null) {
                            AsyncTaskCompat.execute(new DeleteTrainingRunnable(getActivity(), mGuid, mTraining));
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

    @Optional
    @OnClick(R.id.add_new_stage)
    void onAddNewStageTap() {
        if(mTraining != null) {

            //create new training stage
            final Training.Completion completion = new Training.Completion();
            completion.setNew(true);
            completion.setTrainingId(mTrainingId);
            completion.setPhase(getLastCompletionPhase() + 1);

            if (mNewCompletionDateLabel != null) {
                if (!mNewCompletionDateLabel.getText().toString().isEmpty()) {
                    completion.setDate(mNewCompletionDate);
                }
                else {
                    return;
                }
            }
            if (mNewCompletionParticipants != null) {
                try {
                    completion.setNumberCompleted(Integer.valueOf(mNewCompletionParticipants.getText().toString()));
                }
                catch(final Exception ignored) {
                    Log.e("Exception", "error Parsing Date string to LocalDate.");
                }
            }

            updateTrainingCompletions(completion);
            resetNewCompletionView();

            // save new training completion
            AsyncTaskCompat.execute(new CreateTrainingCompletionRunnable(getActivity().getApplicationContext(), mGuid, mTraining.getMinistryId(), completion));
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void startLoaders()
    {
        final LoaderManager manager = this.getLoaderManager();
        manager.initLoader(LOADER_TRAINING, null, new TrainingLoaderCallBacks());
    }

    private void restartAssignmentLoader() {
        // shutdown loader if it's running
        getLoaderManager().destroyLoader(LOADER_ASSIGNMENT);

        // start loader if we have a valid training
        if (mTraining != null) {
            final Bundle args = new Bundle(2);
            args.putString(ARG_GUID, mGuid);
            args.putString(ARG_MINISTRY_ID, mTraining.getMinistryId());
            getLoaderManager().initLoader(LOADER_ASSIGNMENT, args, mLoaderCallbacksAssignment);
        }
    }


    private void updateViews() {
        if(mStagesData != null) {
            mStagesData.setVisibility(GONE);
        }

        if(mTrainingData != null) {
            mTrainingData.setVisibility(VISIBLE);
        }

        if (mTableRowParticipants != null) {
            mTableRowParticipants.setVisibility(GONE);
        }

        if (mTrainingName != null && !mChanged[CHANGED_NAME]) {
            mTrainingName.setText(mTraining != null ? mTraining.getName() : null);
        }
        if (mTrainingMcc != null) {
            mTrainingMcc.setText(mTraining != null ? mTraining.getMcc().name() : null);
        }
        if (mTrainingTypeSpinner != null && mTrainingTypeAdapter != null && !mChanged[CHANGED_TYPE]) {
            mTrainingTypeSpinner.setSelection(
                    mTrainingTypeAdapter.getPosition(mTraining != null ? mTraining.getType() : Training.TRAINING_TYPE_OTHER));
        }

        updateEditMode();
    }

    private void updateEditMode() {
        final boolean editMode = mTraining != null && mTraining.canEdit(mAssignment);
        if (mTrainingName != null) {
            mTrainingName.setEnabled(editMode);
        }
        if (mTrainingMcc != null) {
            mTrainingMcc.setEnabled(editMode);
        }
        if (mTrainingTypeSpinner != null) {
            mTrainingTypeSpinner.setEnabled(editMode);
        }
        if (mTrainingDateLabel != null) {
            mTrainingDateLabel.setEnabled(editMode);
        }

        if (mStagesView != null) {
            mStagesView.setEnabled(false);
        }

        if (mAddStageContainer != null) {
            mAddStageContainer.setVisibility(editMode ? VISIBLE : GONE);
        }

        if (mBottomButtonContainer != null) {
            mBottomButtonContainer.setVisibility(editMode ? VISIBLE : GONE);
        }

        if (mStagesView != null && mTraining != null) {
            setupStagesView(editMode);
        }
    }

    private void setupStagesView(final boolean editMode) {
        if (mStagesView != null) {
            mStagesView.setHasFixedSize(true);
            mStagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mStagesView.addItemDecoration(new DividerItemDecoration(getActivity(), VERTICAL));

            mTrainingCompletionAdapter =
                    new TrainingCompletionRecyclerViewAdapter(getActivity(), mGuid, mTraining.getMinistryId(),
                                                              mTraining.getCompletions(), editMode);
            mStagesView.setAdapter(mTrainingCompletionAdapter);
        }
    }

    private void updateTrainingCompletions(Training.Completion completion) {
        if (mStagesView != null && mTrainingCompletionAdapter != null) {
            mTrainingCompletionAdapter.addItemToCompletionList(completion);
        }
    }

    private void resetNewCompletionView() {
        if (mNewCompletionParticipants != null) {
            mNewCompletionDate = LocalDate.now();
            mNewCompletionDateLabel.setText("");
            mNewCompletionParticipants.setText("");
            mNewCompletionParticipants.clearFocus();
        }
    }

    private int getLastCompletionPhase() {
        //TODO: we shouldn't be accessing the database on the UI/Main thread, it may lead to the UI freezing
        final GmaDao dao = GmaDao.getInstance(getActivity());
        final List<Training.Completion> trainingCompletions = dao.get(Training.Completion.class, Contract.Training.Completion.SQL_WHERE_NOT_DELETED_AND_TRAINING_ID, bindValues(mTraining.getId()));

        if (trainingCompletions.size() > 0) {
            ArrayList<Training.Completion> completionList = new ArrayList<>(trainingCompletions);
            Collections.sort(completionList, new Comparator<Training.Completion>() {
                public int compare(Training.Completion completion1, Training.Completion completion2) {
                    return completion1.getPhase() - completion2.getPhase();
                }
            });
            return  completionList.get(completionList.size() - 1).getPhase();
        }
        else {
            return 0;
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
        LocalDate mTrainingDate;
        boolean mTrainingDateChanged = false;

        boolean hasUpdates() {
            return mTrainingName != null || mTrainingType != null || mTrainingDateChanged;
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
                if (mUpdates.mTrainingDateChanged) {
                    training.setDate(mUpdates.mTrainingDate);
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

    private static class CreateTrainingCompletionRunnable implements Runnable {
        @NonNull
        private final Context mContext;
        @NonNull
        private final LocalBroadcastManager mBroadcastManager;
        @NonNull
        private final GmaDao mDao;
        @NonNull
        private final String mGuid;
        @NonNull
        private final String mMinistryId;
        @NonNull
        private final Training.Completion mCompletion;

        CreateTrainingCompletionRunnable(@NonNull final Context context, @NonNull final String guid, @NonNull final String ministryId, @NonNull final Training.Completion completion) {
            mContext = context;
            mGuid = guid;
            mMinistryId = ministryId;
            mCompletion = completion;
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
                mCompletion.setId(id);

                // update in the database
                try {
                    mDao.insert(mCompletion, SQLiteDatabase.CONFLICT_ROLLBACK);
                    saved = true;
                } catch (final SQLException e) {
                    Log.e("CreateCompletion", "insert error", e);
                }
            }

            // trigger GA event
            //GoogleAnalyticsManager.getInstance(mContext).sendCreateTrainingEvent(mGuid, mTraining.getMinistryId(), mTraining.getMcc());

            // broadcast that this training completion was created
            mBroadcastManager.sendBroadcast(updateTrainingBroadcast(mMinistryId, mCompletion.getTrainingId()));

            // trigger a sync of dirty training completions
            GmaSyncService.syncDirtyTrainingCompletions(mContext, mMinistryId, mGuid);
        }
    }
}
