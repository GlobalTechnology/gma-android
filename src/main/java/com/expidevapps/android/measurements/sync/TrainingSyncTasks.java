package com.expidevapps.android.measurements.sync;

import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.expidevapps.android.measurements.api.GmaApiClient;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.GmaDao;
import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Training;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.db.Transaction;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.expidevapps.android.measurements.Constants.ARG_MCC;
import static com.expidevapps.android.measurements.Constants.EXTRA_MINISTRY_ID;
import static org.ccci.gto.android.common.db.AbstractDao.bindValues;

class TrainingSyncTasks extends BaseSyncTasks {
    private static final String SYNC_TIME_TRAININGS = "last_synced.trainings";

    private static final long STALE_DURATION_TRAININGS = DAY_IN_MS;

    private static final Object LOCK_DIRTY_TRAININGS = new Object();
    private static final Object LOCK_DIRTY_TRAINING_COMPLETIONS = new Object();

    private static String[] PROJECTION_GET_TRAININGS =
            {Contract.Training.COLUMN_MINISTRY_ID, Contract.Training.COLUMN_MCC, Contract.Training.COLUMN_NAME,
                    Contract.Training.COLUMN_DATE, Contract.Training.COLUMN_TYPE, Contract.Training.COLUMN_LATITUDE,
                    Contract.Training.COLUMN_LONGITUDE};

    private static String[] PROJECTION_GET_COMPLETIONS =
            {Contract.Training.Completion.COLUMN_TRAINING_ID, Contract.Training.Completion.COLUMN_PHASE,
                    Contract.Training.Completion.COLUMN_NUMBER_COMPLETED, Contract.Training.Completion.COLUMN_DATE};

    static void syncTrainings(@NonNull final Context context, @NonNull final String guid, @NonNull final Bundle args)
            throws ApiException {
        // short-circuit if there isn't a valid ministry specified
        final String ministryId = args.getString(EXTRA_MINISTRY_ID);
        if (ministryId == null || ministryId.equals(Ministry.INVALID_ID)) {
            return;
        }

        // short-circuit if this request is for an invalid mcc
        final Ministry.Mcc mcc = Ministry.Mcc.fromRaw(args.getString(ARG_MCC));
        if (mcc == Ministry.Mcc.UNKNOWN) {
            return;
        }
        // short-circuit if we aren't forcing a sync and the data isn't stale
        final GmaDao dao = GmaDao.getInstance(context);
        if (!isForced(args)) {
            final long age = System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_TRAININGS, ministryId, mcc);
            if (age < STALE_DURATION_TRAININGS) {
                return;
            }
        }
        // short-circuit if we fail to fetch trainings from the api
        final GmaApiClient api = GmaApiClient.getInstance(context, guid);
        final List<Training> trainings = api.getTrainings(ministryId, mcc);
        if (trainings == null) {
            return;
        }

        // update trainings in the database (use a transaction to avoid a race condition with updating a training)
        final Transaction tx = dao.newTransaction();
        try {
            tx.beginTransactionNonExclusive();

            final LongSparseArray<Training> current = new LongSparseArray<>();
            for (final Training training : dao.get(Training.class, Contract.Training.SQL_WHERE_MINISTRY_MCC,
                                                   bindValues(ministryId, mcc))) {
                current.put(training.getId(), training);
            }

            long[] ids = new long[current.size() + trainings.size()];
            int j = 0;
            for (final Training training : trainings) {
                final long id = training.getId();
                final Training existing = current.get(id);

                //sync the training completions if available
                if(training.getCompletions().size() != 0) {
                    Log.d("ITH", "syncTrainings: syncTrainingCompletions() called");
                    syncTrainingCompletions(context, training.getId(), training);
                }
                // persist training in database (if it doesn't exist or isn't dirty)
                if (existing == null || !existing.isDirty()) {
                    dao.updateOrInsert(training, PROJECTION_GET_TRAININGS);

                    // mark this id as having been changed
                    ids[j++] = id;
                }

                // remove this training from the list of trainings
                current.remove(id);
            }

            // delete any remaining trainings that weren't returned from the API
            for (int i = 0; i < current.size(); i++) {
                final Training training = current.valueAt(i);
                dao.delete(training);
                ids[j++] = training.getId();
            }

            // update the sync time in the database
            dao.updateLastSyncTime(SYNC_TIME_TRAININGS, ministryId, mcc);

            tx.setSuccessful();

            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            broadcastManager.sendBroadcast(BroadcastUtils.updateTrainingBroadcast(ministryId, Arrays.copyOf(ids, j)));
        } finally {
            tx.end();
        }
    }

    static void syncDirtyTrainings(@NonNull final Context context, @NonNull final String guid,
                                   @NonNull final Bundle args, @NonNull final SyncResult result) throws ApiException {
        synchronized (LOCK_DIRTY_TRAININGS) {
            // short-circuit if there aren't any trainings to process
            final GmaDao dao = GmaDao.getInstance(context);
            final List<Training> trainings = dao.get(Training.class, Contract.Training.SQL_WHERE_NEW_DELETED_OR_DIRTY, null);
            if (trainings.isEmpty()) {
                return;
            }

            // ministry_id => training_id
            final Multimap<String, Long> broadcasts = HashMultimap.create();

            // process all trainings that are dirty
            final GmaApiClient api = GmaApiClient.getInstance(context, guid);
            for (final Training training : trainings) {
                try {
                    if (training.isNew()) {
                        // try creating the training
                        final Training newTraining = api.createTraining(training);

                        // update id of training
                        if (newTraining != null) {
                            dao.delete(training);
                            newTraining.setLastSynced(new Date());
                            dao.updateOrInsert(newTraining, PROJECTION_GET_TRAININGS);

                            // add training to list of broadcasts
                            broadcasts.put(training.getMinistryId(), training.getId());
                            broadcasts.put(training.getMinistryId(), newTraining.getId());

                            // increment the insert counter
                            result.stats.numInserts++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    }
                    else if (training.isDirty()) {
                        // generate dirty JSON
                        final JSONObject json = training.dirtyToJson();

                        // update the church
                        final boolean success = api.updateTraining(training.getId(), json);

                        // was successful update?
                        if (success) {
                            // clear dirty attributes
                            training.setDirty(null);
                            dao.update(training, new String[] {Contract.Training.COLUMN_DIRTY});

                            // add training to list of broadcasts
                            broadcasts.put(training.getMinistryId(), training.getId());

                            // increment update counter
                            result.stats.numUpdates++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    } else if (training.isDeleted()) {
                        // delete this training
                        final boolean success = api.deleteTraining(training.getId());

                        // was this deletion successful?
                        if (success) {
                            // delete the training from the database
                            dao.delete(training);

                            // add training to the list of broadcasts
                            broadcasts.put(training.getMinistryId(), training.getId());

                            // increment delete counter
                            result.stats.numDeletes++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    }
                } catch (final JSONException ignored) {
                    // this shouldn't happen when generating json
                }
            }

            // send broadcasts for each ministryId with trainings that were updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            for (final String ministryId : broadcasts.keySet()) {
                broadcastManager.sendBroadcast(BroadcastUtils.updateTrainingBroadcast(ministryId, Longs.toArray(
                        broadcasts.get(ministryId))));
            }
        }
    }

    static void syncTrainingCompletions(@NonNull final Context context, @NonNull final long trainingId, @NonNull final Training mTraining) {
        Log.d("ITH", "syncTrainingCompletions called");
        final List<Training.Completion> completions = mTraining.getCompletions();
        if (completions == null) {
            return;
        }

        Log.d("ITH", "syncTrainingCompletions completions found: " + completions.size());
        final GmaDao dao = GmaDao.getInstance(context);

        // update training completions in the database (use a transaction to avoid a race condition with updating a completion)
        final Transaction tx = dao.newTransaction();
        try {
            tx.beginTransactionNonExclusive();

            final LongSparseArray<Training.Completion> currentCompletions = new LongSparseArray<>();
            for (final Training.Completion completion : dao.get(Training.Completion.class, Contract.Training.Completion.SQL_WHERE_TRAINING_ID,
                    bindValues(trainingId))) {
                currentCompletions.put(completion.getId(), completion);
            }
            Log.d("ITH", "syncTrainingCompletions: currentCompletions size: " + currentCompletions.size());

            long[] ids = new long[currentCompletions.size() + completions.size()];
            int j = 0;
            for (final Training.Completion completion : completions) {
                final long id = completion.getId();
                final Training.Completion existing = currentCompletions.get(id);

                // persist training completion in database (if it doesn't exist or isn't dirty)
                if (existing == null || !existing.isDirty() || !existing.isDeleted()) {
                    dao.updateOrInsert(completion, PROJECTION_GET_COMPLETIONS);

                    // mark this id as having been changed
                    ids[j++] = id;
                }

                // remove this training completion from the list of completions
                currentCompletions.remove(id);
            }

            // delete any remaining completions that weren't returned from the API
            for (int i = 0; i < currentCompletions.size(); i++) {
                final Training.Completion completion = currentCompletions.valueAt(i);
                dao.delete(completion);
                ids[j++] = completion.getId();
            }

            // update the sync time in the database
            //dao.updateLastSyncTime(SYNC_TIME_TRAININGS, ministryId, mcc);

            tx.setSuccessful();

            //final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            //broadcastManager.sendBroadcast(BroadcastUtils.updateTrainingBroadcast(ministryId, Arrays.copyOf(ids, j)));
        } finally {
            tx.end();
        }
    }

    static void syncDirtyTrainingCompletions(@NonNull final Context context, @NonNull final String guid,
                                             @NonNull final Bundle args, @NonNull final SyncResult result) throws ApiException {
        Log.d("ITH", "syncDirtyTrainingCompletions() called.");
        Log.d("ITH", "TrainingSyncTasks mGUID: " + guid);

        final String ministryId = args.getString(EXTRA_MINISTRY_ID);
        if (ministryId == null || ministryId.equals(Ministry.INVALID_ID)) {
            return;
        }

        synchronized (LOCK_DIRTY_TRAINING_COMPLETIONS) {
            // short-circuit if there aren't any training completions to process
            final GmaDao dao = GmaDao.getInstance(context);
            final List<Training.Completion> completions = dao.get(Training.Completion.class, Contract.Training.Completion.SQL_WHERE_NEW_DELETED_OR_DIRTY, null);
            if (completions.isEmpty()) {
                Log.d("ITH", "syncDirtyTrainingCompletions completions: " + completions.size());
                return;
            }

            // ministry_id => training_id
            final Multimap<String, Long> broadcasts = HashMultimap.create();

            // process all training completions that are dirty
            final GmaApiClient api = GmaApiClient.getInstance(context, guid);
            for (final Training.Completion completion : completions) {
                try {
                    if (completion.isNew()) {
                        Log.d("ITH", "syncDirtyTrainingCompletions: completion training_id: " + completion.getTrainingId());
                        Log.d("ITH", "syncDirtyTrainingCompletions: phase: " + completion.getPhase());
                        Log.d("ITH", "syncDirtyTrainingCompletions: date: " + completion.getDate());
                        Log.d("ITH", "syncDirtyTrainingCompletions: number_completed: " + completion.getNumberCompleted());

                        // try creating the training
                        final Training.Completion newTrainingCompletion = api.createTrainingCompletion(completion.getTrainingId(), completion);

                        // update id of training
                        if (newTrainingCompletion != null) {
                            dao.delete(completion);
                            newTrainingCompletion.setLastSynced(new Date());
                            dao.updateOrInsert(newTrainingCompletion, PROJECTION_GET_COMPLETIONS);

                            // add training to list of broadcasts
                            //broadcasts.put(ministryId, completion.getTrainingId());
                            //broadcasts.put(ministryId, newTrainingCompletion.getTrainingId());

                            // increment the insert counter
                            result.stats.numInserts++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    }
                    else if (completion.isDirty()) {
                        Log.d("ITH", "syncDirtyTrainingCompletions: completion is dirty.");
                        // generate dirty JSON
                        //final JSONObject json = completion.dirtyToJson();
                        final JSONObject json = completion.toJson();

                        // update the training
                        final Training.Completion newTrainingCompletion = api.updateTrainingCompletion(completion.getTrainingId(), completion.getId(), json);

                        // was successful update?
                        if (newTrainingCompletion != null) {
                            // clear dirty attributes
                            completion.setDirty(null);
                            dao.update(completion, new String[]{Contract.Training.Completion.COLUMN_DIRTY});

                            // add training to list of broadcasts
                            broadcasts.put(ministryId, completion.getTrainingId());

                            // increment update counter
                            result.stats.numUpdates++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    }
                    else if (completion.isDeleted()) {
                        // delete this training
                        final boolean success = api.deleteTrainingCompletion(completion.getId());

                        // was this deletion successful?
                        if (success) {
                            // delete the training from the database
                            dao.delete(completion);

                            // add training to the list of broadcasts
                            broadcasts.put(ministryId, completion.getId());

                            // increment delete counter
                            result.stats.numDeletes++;
                        } else {
                            result.stats.numParseExceptions++;
                        }
                    }
                } catch (final JSONException ignored) {
                    // this shouldn't happen when generating json
                }
            }

            // send broadcasts for each ministryId with trainings that were updated
            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            for (final String mId : broadcasts.keySet()) {
                broadcastManager.sendBroadcast(BroadcastUtils.updateTrainingBroadcast(mId, Longs.toArray(
                        broadcasts.get(mId))));
            }        }
    }
}
