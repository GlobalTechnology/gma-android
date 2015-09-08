package com.expidevapps.android.measurements.support.v4.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.Church.Development;
import com.expidevapps.android.measurements.model.Training;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Optional;

import static com.expidevapps.android.measurements.Constants.ARG_GUID;

public abstract class BaseEditTrainingDialogFragment extends DialogFragment {
    @Optional
    @Nullable
    @InjectView(R.id.training_title)
    TextView mTitleView;
    @Optional
    @Nullable
    @InjectView(R.id.et_training_name)
    TextView mTrainingName;
    @Optional
    @Nullable
    @InjectView(R.id.et_training_type)
    Spinner mTrainingTypeSpinner;
    @Optional
    @Nullable
    @InjectView(R.id.et_training_date)
    TextView mTrainingDate;
    @Optional
    @Nullable
    @InjectView(R.id.et_training_participants)
    TextView mTrainingParticipants;
    @Optional
    @Nullable
    @InjectView(R.id.et_training_mcc)
    TextView mTrainingMcc;
    @Optional
    @Nullable
    @InjectView(R.id.icon)
    ImageView mIconView;

    @Nullable
    ArrayAdapter<String> mTrainingTypeAdapter;

    @NonNull
    /* final */ String mGuid;

    @NonNull
    public static Bundle buildArgs(@NonNull final String guid) {
        final Bundle args = new Bundle();
        args.putString(ARG_GUID, guid);
        return args;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = this.getArguments();
        final String guid = args.getString(ARG_GUID);
        if (guid == null) {
            throw new IllegalStateException("cannot create TrainingDialogFragment with invalid guid");
        }
        mGuid = guid;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.fragment_edit_training);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ButterKnife.inject(this, getDialog());
        setupViews();
    }

    protected void onChangeTrainingType(@NonNull final String trainingType) {

    }

    @Optional
    @OnClick(R.id.cancel)
    protected void onCancel() {
        this.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        cleanupViews();
        ButterKnife.reset(this);
    }

    /* END lifecycle */

    private void setupViews() {
        if (mTrainingTypeSpinner != null) {
            // generate set of options
            final EnumSet<Development> types = EnumSet.allOf(Development.class);
            types.remove(Development.UNKNOWN);

            // generate Adapter for training types
            mTrainingTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                    new String[] {Training.TRAINING_TYPE_MC2, Training.TRAINING_TYPE_T4T, Training.TRAINING_TYPE_CPMI, Training.TRAINING_TYPE_OTHER});
            mTrainingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attach adapter
            mTrainingTypeSpinner.setAdapter(mTrainingTypeAdapter);
        }
    }

    private void cleanupViews() {
        mTrainingTypeAdapter = null;
    }

    @Optional
    @OnClick(R.id.et_training_date)
    void onTrainingDateClick() {
        Log.d("ITH", "onTraingDateClick called.");
        DatePickerDialog datePickerDialog;

        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                mTrainingDate.setText(format.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    @Optional
    @OnItemSelected(R.id.et_training_type)
    void changeTrainingType() {
        if (mTrainingTypeSpinner != null) {
            final Object item = mTrainingTypeSpinner.getSelectedItem();
            //this.onChangeTrainingType(item instanceof TrainingType ? (TrainingType) item : TrainingType.UNKNOWN);
            this.onChangeTrainingType((String) item);
        }
    }

    @Optional
    @OnClick(R.id.training_cancel)
    void onCancelEdit()
    {
        this.dismiss();
    }

    protected void updateTitle(@StringRes final int title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    protected void updateTitle(@Nullable final String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    protected void updateIcon(@NonNull final Development state) {
        if (mIconView != null) {
            mIconView.setImageResource(state.image);
        }
    }
}
