package com.expidevapps.android.measurements.support.v7.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.sync.GmaSyncService;
import com.google.common.collect.Lists;

import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateTrainingBroadcast;

public class TrainingCompletionRecyclerViewAdapter extends RecyclerView.Adapter<TrainingCompletionRecyclerViewAdapter.ViewHolder> {

    private static final int CHANGED_DATE = 0;
    private static final int CHANGED_PARTICIPANTS = 1;

    private ArrayList<Training.Completion> mCompletionList;
    private Context mContext;
    private String mGuid;
    private String mMinistryId;
    private boolean mEditMode;

    @NonNull
    private final boolean[] mChanged = new boolean[2];

    public TrainingCompletionRecyclerViewAdapter(Context context, @NonNull final String guid, @NonNull final String ministryId, List<Training.Completion> completionList, final boolean editMode) {
        this.mCompletionList = new ArrayList<>(completionList);
        this.mContext = context;
        this.mGuid = guid;
        this.mMinistryId = ministryId;
        this.mEditMode = editMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_training_stage, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (mCompletionList != null && mCompletionList.size() > 0) {
            final Training.Completion mCompletion = mCompletionList.get(position);

            if (holder.mPhase != null) {
                holder.mPhase.setText(Integer.toString(mCompletion.getPhase()));
            }
            if (holder.mCompletionDate != null) {
                holder.mCompletionDate.setText(mCompletion.getDate() != null ? DateFormat.getDateInstance(DateFormat.SHORT).format(mCompletion.getDate().toDate()) : "");
                holder.mCompletionDate.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        DatePickerDialog datePickerDialog;

                        Calendar newCalendar = Calendar.getInstance();
                        datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {

                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar newDate = Calendar.getInstance();
                                newDate.set(year, monthOfYear, dayOfMonth);

                                DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                                holder.mCompletionDate.setText(format.format(newDate.getTime()));
                            }

                        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

                        datePickerDialog.show();
                    }
                });
                holder.mCompletionDate.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable text) {
                        Log.d("ITH", "mCompletionDate date changed.");
                        //onTextUpdated(holder.mCompletionDate, text != null ? text.toString() : "");
                        mChanged[CHANGED_DATE] = !(mCompletion != null ? text.toString().equals(mCompletion.getDate()) : text.toString().isEmpty());
                    }
                });
            }
            if (holder.mParticipants != null) {
                holder.mParticipants.setText(Integer.toString(mCompletion.getNumberCompleted()));

                holder.mParticipants.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable text) {
                        Log.d("ITH", "mParticipants changed.");
                        //onTextUpdated(holder.mParticipants, text != null ? text.toString() : "");
                        mChanged[CHANGED_PARTICIPANTS] = !(mCompletion != null ? text.toString().equals(mCompletion.getNumberCompleted()) : text.toString().isEmpty());
                    }
                });
            }

            if (holder.mEditStage != null) {
                if (mEditMode == false) {
                    holder.mEditStage.setVisibility(View.GONE);
                }
                holder.mEditStage.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        holder.mCompletionDate.setEnabled(true);
                        holder.mParticipants.setEnabled(true);

                        holder.mEditStage.setVisibility(View.GONE);
                        holder.mSaveStage.setVisibility(View.VISIBLE);
                    }
                });
            }

            if (holder.mSaveStage != null) {
                if (mEditMode == false) {
                    holder.mEditStage.setVisibility(View.GONE);
                }
                holder.mSaveStage.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Log.d("ITH", "mSaveStage button called.");
                        holder.mCompletionDate.setEnabled(false);
                        holder.mParticipants.setEnabled(false);

                        holder.mEditStage.setVisibility(View.VISIBLE);
                        holder.mSaveStage.setVisibility(View.GONE);

                        final CompletionUpdates updates = new CompletionUpdates();
                        if (holder.mCompletionDate != null && mChanged[CHANGED_DATE]) {
                            updates.mCompletionDate = holder.mCompletionDate.getText().toString();
                            Log.d("ITH", "updates.mCompletionDate: " + updates.mCompletionDate);
                        }

                        if (holder.mParticipants != null && mChanged[CHANGED_PARTICIPANTS]) {
                            updates.mCompletionParticipants = holder.mParticipants.getText().toString();
                            Log.d("ITH", "updates.mCompletionParticipants: " + updates.mCompletionParticipants);
                        }

                        // persist changes in the database (if there are any)
                        if (updates.hasUpdates()) {
                            AsyncTaskCompat.execute(new UpdateTrainingCompletionRunnable(mContext, mGuid, mMinistryId, mCompletion, updates));
                        }
                        else {
                            Log.d("ITH", "No updates to save");
                        }
                    }
                });
            }

            if (holder.mDeleteStage != null) {
                if (mEditMode == false) {
                    holder.mDeleteStage.setVisibility(View.GONE);
                }
                holder.mDeleteStage.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        new AlertDialog.Builder(mContext).setTitle(R.string.title_dialog_training_completion_delete)
                                .setMessage(R.string.text_dialog_training_completion_delete_confirm)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        //dismiss the dialog
                                        //dismiss();
                                        mCompletionList.remove(position);
                                        notifyDataSetChanged();
                                        if (mCompletion != null) {
                                            AsyncTaskCompat.execute(new DeleteTrainingCompletionRunnable(mContext, mGuid, mMinistryId, mCompletion));
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != mCompletionList ? mCompletionList.size() : 0);
    }

    @NonNull
    public Long getCompletionId(final int position) {
        if (mCompletionList != null) {
            return mCompletionList.get(position).getId();
        }
        return Training.Completion.INVALID_ID;
    }

    @NonNull
    public Long getTrainingId(final int position) {
        if (mCompletionList != null) {
            return mCompletionList.get(position).getTrainingId();
        }
        return Training.INVALID_ID;
    }

    public void addItemToCompletionList(Training.Completion completion) {
        this.mCompletionList.add(completion);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Optional
        @Nullable
        @InjectView(R.id.phase)
        TextView mPhase;
        @Optional
        @Nullable
        @InjectView(R.id.completion_date)
        EditText mCompletionDate;
        @Optional
        @Nullable
        @InjectView(R.id.completion_participants)
        EditText mParticipants;
        @Optional
        @Nullable
        @InjectView(R.id.edit_stage)
        ImageButton mEditStage;
        @Optional
        @Nullable
        @InjectView(R.id.save_stage)
        ImageButton mSaveStage;
        @Optional
        @Nullable
        @InjectView(R.id.delete_stage)
        ImageButton mDeleteStage;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    private static class CompletionUpdates {
        @Nullable
        String mCompletionDate;

        @Nullable
        String mCompletionParticipants;

        boolean hasUpdates() {
            return mCompletionDate != null || mCompletionParticipants != null;
        }
    }

    private static class UpdateTrainingCompletionRunnable implements Runnable {
        private final Context mContext;
        private final String mGuid;
        private final String mMinistryId;
        private final Training.Completion mCompletion;
        private final CompletionUpdates mUpdates;

        public UpdateTrainingCompletionRunnable(@NonNull final Context context, @NonNull final String guid, @NonNull final String ministryId,
                                                @NonNull final Training.Completion completion, @NonNull final CompletionUpdates updates) {
            mContext = context.getApplicationContext();
            mGuid = guid;
            mMinistryId = ministryId;
            mCompletion = completion;
            mUpdates = updates;
        }

        @Override
        public void run() {
            // short-circuit if there aren't any actual updates
            if (!mUpdates.hasUpdates()) {
                Log.d("ITH", "No updates to save");
                return;
            }

            final GmaDao dao = GmaDao.getInstance(mContext);
            final Transaction tx = dao.newTransaction();
            try {
                tx.beginTransactionNonExclusive();

                // short-circuit if we can't get a fresh copy of this training completion
                final Training.Completion completion = dao.refresh(mCompletion);
                if (completion == null) {
                    return;
                }
                Log.d("ITH", "Find dirty completion");
                // perform requested updates
                final ArrayList<String> projection = Lists.newArrayList(Contract.Training.Completion.COLUMN_DIRTY);
                completion.trackingChanges(true);
                if (mUpdates.mCompletionParticipants != null) {
                    completion.setNumberCompleted(Integer.valueOf(mUpdates.mCompletionParticipants));
                    projection.add(Contract.Training.Completion.COLUMN_NUMBER_COMPLETED);
                    Log.d("ITH", "Check point 1");
                }

                if (mUpdates.mCompletionDate != null) {
                    try
                    {
                        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        completion.setDate(new LocalDate(format.parse(mUpdates.mCompletionDate)));
                    }
                    catch (ParseException ignored) {
                        Log.e("ParseException", "error Parsing Date string to LocalDate.");
                    }
                    projection.add(Contract.Training.Completion.COLUMN_DATE);
                    Log.d("ITH", "Check point 2");
                }
               completion.trackingChanges(false);

                Log.d("ITH", "call for db update: " + projection.size());
                // save changes
                dao.update(completion, projection.toArray(new String[projection.size()]));
                Log.d("ITH", "call for db update complete: " + projection.toString());
                tx.setSuccessful();
            } finally {
                tx.end();
            }

            // track this update in GA
            /*GoogleAnalyticsManager.getInstance(mContext)
                    .sendUpdateTrainingEvent(mGuid, mTraining.getMinistryId(), mTraining.getMcc(), mTraining.getId());*/

            // broadcast that this training was updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
            broadcastManager.sendBroadcast(updateTrainingBroadcast(mMinistryId, mCompletion.getTrainingId()));

            Log.d("ITH", "TrainingCompletionRecyclerViewAdapter syncDirtyTrainingCompletions mGUID: " + mGuid);
            // trigger a sync of dirty training completion
            GmaSyncService.syncDirtyTrainingCompletions(mContext, mMinistryId, mGuid);
        }
    }

    private static class DeleteTrainingCompletionRunnable implements Runnable {
        private final Context mContext;
        private final String mGuid;
        private final String mMinistryId;
        private final Training.Completion mCompletion;

        public DeleteTrainingCompletionRunnable(@NonNull final Context context, @NonNull final String guid,
                                                @NonNull final String ministryId, @NonNull final Training.Completion completion) {
            mContext = context.getApplicationContext();
            mGuid = guid;
            mMinistryId = ministryId;
            mCompletion = completion;
        }

        @Override
        public void run() {
            // mark this training as deleted
            final GmaDao dao = GmaDao.getInstance(mContext);
            mCompletion.setDeleted(true);
            dao.update(mCompletion, new String[] {Contract.Training.Completion.COLUMN_DELETED});

            // track this delete in GA
            /*GoogleAnalyticsManager.getInstance(mContext).sendDeleteTrainingEvent(mGuid, mCompletion.getMinistryId(),
                    mCompletion.getMcc(), mCompletion.getId());*/

            // broadcast that this training was updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
            broadcastManager.sendBroadcast(updateTrainingBroadcast(mMinistryId, mCompletion.getId()));

            // trigger a sync of dirty training completions
            GmaSyncService.syncDirtyTrainingCompletions(mContext, mMinistryId, mGuid);
        }
    }
}
