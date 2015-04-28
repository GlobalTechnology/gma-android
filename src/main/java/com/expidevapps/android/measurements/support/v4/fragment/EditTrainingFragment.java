package com.expidevapps.android.measurements.support.v4.fragment;

import static com.expidevapps.android.measurements.Constants.ARG_TRAINING_ID;
import static com.expidevapps.android.measurements.sync.BroadcastUtils.updateTrainingBroadcast;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.expidev.gcmapp.service.TrainingService;
import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.db.Contract;
import com.expidevapps.android.measurements.db.TrainingDao;
import com.expidevapps.android.measurements.model.Training;
import com.expidevapps.android.measurements.support.v4.content.SingleTrainingLoader;

import org.ccci.gto.android.common.app.AlertDialogCompat;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.fragment.AbstractDialogFragment;
import org.ccci.gto.android.common.util.AsyncTaskCompat;

import java.text.ParseException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;

/**
 * Created by matthewfrederick on 2/24/15.
 */
public class EditTrainingFragment extends AbstractDialogFragment
{
    private static final int LOADER_TRAINING = 1;
    
    private static final int CHANGED_NAME = 0;
    private static final int CHANGED_TYPE = 1;
    private static final int CHANGED_DATE = 2;
    
    private long mTrainingId = Training.INVALID_ID;
    @NonNull
    private boolean[] mChanged = new boolean[3];
    @Nullable
    private Training mTraining;

    @Optional
    @Nullable
    @InjectView(R.id.et_training_name)
    TextView mTrainingName;
    @Optional
    @Nullable
    @InjectView(R.id.et_training_type)
    TextView mTrainingType;
    @Optional
    @Nullable
    @InjectView(R.id.et_training_date)
    TextView mTrainingDate;
    @Optional
    @Nullable
    @InjectView(R.id.icon)
    ImageView mIconView;
    
    public static EditTrainingFragment newInstance(final long trainingId)
    {
        final EditTrainingFragment fragment = new EditTrainingFragment();
        
        final Bundle bundle = new Bundle();
        bundle.putLong(ARG_TRAINING_ID, trainingId);
        fragment.setArguments(bundle);
        
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        final Bundle bundle = this.getArguments();
        mTrainingId = bundle.getLong(ARG_TRAINING_ID, Training.INVALID_ID);
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedState)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialogCompat.setView(builder, getActivity(), R.layout.fragment_edit_training);
        return builder.create();
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
    void onSaveChanges()
    {
        if (mTraining != null)
        {
            final Training training = mTraining.clone();
            training.trackingChanges(true);
            if (mTrainingName != null && mChanged[CHANGED_NAME])
            {
                training.setName(mTrainingName.getText().toString());
            }
            if (mTrainingType != null && mChanged[CHANGED_TYPE])
            {
                training.setType(mTrainingType.getText().toString());
            }
            if (mTrainingDate != null && mChanged[CHANGED_DATE])
            {
                try
                {
                    training.setDate(mTrainingDate.getText().toString());
                } catch (ParseException ignored)
                {}
            }
            training.trackingChanges(false);
            
            if (training.isDirty())
            {
                final Context context = getActivity().getApplicationContext();
                final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                final TrainingDao dao = TrainingDao.getInstance(context);

                AsyncTaskCompat.execute(new Runnable() {
                    @Override
                    public void run() {
                        dao.update(training, new String[] {Contract.Training.COLUMN_NAME,
                                Contract.Training.COLUMN_TYPE, Contract.Training.COLUMN_DATE,
                                Contract.Training.COLUMN_DIRTY});

                        broadcastManager
                                .sendBroadcast(updateTrainingBroadcast(training.getMinistryId(), training.getId()));

                        TrainingService.syncDirtyTraining(context);
                    }
                });
            }
        }
        this.dismiss();
    }
    
    @Optional
    @OnClick(R.id.training_cancel)
    void onCancelEdit()
    {
        this.dismiss();   
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
        if (mTrainingType != null)
        {
            mTrainingType.setText(mTraining != null ? mTraining.getType() : null);
        }
        if (mTrainingDate != null)
        {
            mTrainingDate.setText(mTraining != null && mTraining.getDate() != null ? mTraining.getDate().toString(
                    "MM/dd/yyyy") : null);
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
    @OnTextChanged(value = R.id.et_training_type, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void updateType(@Nullable final Editable text)
    {
        if (mTrainingType != null)
        {
            onTextUpdated(mTrainingType, text != null ? text.toString() : "");
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
}
