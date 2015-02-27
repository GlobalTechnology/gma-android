package com.expidev.gcmapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expidev.gcmapp.activity.SettingsActivity;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.Measurement;
import com.expidev.gcmapp.service.MeasurementsService;
import com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader;
import com.expidev.gcmapp.support.v4.content.MeasurementsLoader;
import com.expidev.gcmapp.utils.ViewUtils;
import com.expidev.gcmapp.view.TextHeaderView;

import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.json.JSONArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;

/**
 * Created by William.Randall on 2/3/2015.
 */
public class MeasurementsActivity extends ActionBarActivity
{
    private final String TAG = this.getClass().getSimpleName();

    private final String PREF_NAME = "gcm_prefs";

    private final AssignmentLoaderCallbacks mLoaderCallbacksAssignment = new AssignmentLoaderCallbacks();
    private final MeasurementsLoaderCallbacks measurementsLoaderCallbacks = new MeasurementsLoaderCallbacks();

    private TheKey mTheKey;
    private SharedPreferences preferences;

    @Nullable
    private Assignment mAssignment = null;
    @Nullable
    private Ministry chosenMinistry;
    private String currentPeriod = null;

    private static final int LOADER_MEASUREMENTS = 2;
    private static final int LOADER_CURRENT_ASSIGNMENT = 3;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurements);
        mTheKey = TheKeyImpl.getInstance(this);

        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        currentPeriod = preferences.getString("currentPeriod", null);
        startLoaders();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == Constants.REQUEST_EXIT)
        {
            if(resultCode == Constants.BLOCKED_MINISTRY)
            {
                finish();
            }
        }
    }

    private void startLoaders()
    {
        final LoaderManager manager = this.getSupportLoaderManager();

        // build args for the loader
        final Bundle args = new Bundle(2);
        args.putBoolean(CurrentAssignmentLoader.ARG_LOAD_MINISTRY, true);
        args.putString(Constants.ARG_GUID, mTheKey.getGuid());

        manager.initLoader(LOADER_CURRENT_ASSIGNMENT, args, mLoaderCallbacksAssignment);
    }

    private void restartMeasurementLoader(String period) {
        final LoaderManager manager = this.getSupportLoaderManager();

        Bundle args = new Bundle(3);
        args.putString(Constants.ARG_MINISTRY_ID,
            mAssignment != null ? mAssignment.getMinistryId() : Ministry.INVALID_ID);
        args.putString(Constants.ARG_MCC,
            (mAssignment != null ? mAssignment.getMcc() : Ministry.Mcc.UNKNOWN).toString());
        args.putString(Constants.ARG_PERIOD, period);

        manager.restartLoader(LOADER_MEASUREMENTS, args, measurementsLoaderCallbacks);
    }

    private void drawLayout(List<Measurement> measurements) {
        if (mAssignment == null || chosenMinistry == null) {
            return;
        }

        TextView titleView = (TextView) findViewById(R.id.measurement_ministry_name);
        titleView.setText(chosenMinistry.getName() + " (" + mAssignment.getMcc() + ")");

        TextView periodView = (TextView) findViewById(R.id.currentPeriod);
        if(currentPeriod == null)
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            Calendar thisMonth = Calendar.getInstance();

            currentPeriod = dateFormat.format(thisMonth.getTime());
            preferences.edit().putString("currentPeriod", currentPeriod).apply();
        }

        periodView.setText(currentPeriod);

        List<Measurement> sortedMeasurements = sortMeasurements(measurements);

        LinearLayout dataContainer = (LinearLayout) findViewById(R.id.measurement_data_Layout);

        // Clear out the data container in case the user is coming back from the measurement details page
        dataContainer.removeAllViews();

        if(mAssignment.isLeadership() || mAssignment.isMember())
        {
            buildLayoutForDrillDown(sortedMeasurements, dataContainer);
        }
        else if(mAssignment.isSelfAssigned())
        {
            buildLayoutForDataEntry(sortedMeasurements, dataContainer);
        }
    }

    private void buildLayoutForDrillDown(List<Measurement> sortedMeasurements, LinearLayout dataContainer)
    {
        String previousColumn = sortedMeasurements.get(0).getColumn();
        String firstMeasurementId = sortedMeasurements.get(0).getMeasurementId();

        for(Measurement measurement : sortedMeasurements)
        {
            String column = measurement.getColumn();

            if(!column.equals(previousColumn) || measurement.getMeasurementId().equals(firstMeasurementId))
            {
                // Add the new header and the data
                TextHeaderView headerView = new TextHeaderView(this);
                headerView.setText(column);
                dataContainer.addView(headerView);

                previousColumn = column;
            }
            LinearLayout row = createDataRow(
                measurement.getName(),
                measurement.getTotal(),
                measurement);

            dataContainer.addView(row);
        }
    }

    private void buildLayoutForDataEntry(List<Measurement> sortedMeasurements, LinearLayout dataContainer)
    {
        String previousColumn = sortedMeasurements.get(0).getColumn();
        String firstMeasurementId = sortedMeasurements.get(0).getMeasurementId();

        for(Measurement measurement : sortedMeasurements)
        {
            String column = measurement.getColumn();

            if(!column.equals(previousColumn) || measurement.getMeasurementId().equals(firstMeasurementId))
            {
                // Add the new header and the data
                TextHeaderView headerView = new TextHeaderView(this);
                headerView.setText(column);
                dataContainer.addView(headerView);

                previousColumn = column;
            }
            LinearLayout row = createDataEntryRow(measurement);

            dataContainer.addView(row);
        }
    }

    private LinearLayout createDataRow(String name, int total, final Measurement measurement)
    {
        TextView nameView = createNameView(name);
        TextView totalView = createTotalView(total);
        TextView arrowView = createArrowView();

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        row.addView(nameView);
        row.addView(totalView);
        row.addView(arrowView);
        row.setClickable(true);
        row.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drillIntoMeasurementDetails(measurement);
            }
        });

        return row;
    }

    private TextView createNameView(String name)
    {
        TextView nameView = new TextView(this);
        nameView.setText(name);

        LinearLayout.LayoutParams nameLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameLayoutParams.weight = 1f;
        nameLayoutParams.setMargins(ViewUtils.dpToPixels(this, 10), 0, 0, 0);

        nameView.setLayoutParams(nameLayoutParams);

        return nameView;
    }

    private TextView createTotalView(int total)
    {
        TextView totalView = new TextView(this);
        totalView.setText(Integer.toString(total));

        LinearLayout.LayoutParams totalLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        totalLayoutParams.setMargins(0, 0, ViewUtils.dpToPixels(this, 5), 0);

        totalView.setLayoutParams(totalLayoutParams);

        return totalView;
    }

    private LinearLayout createDataEntryRow( Measurement measurement)
    {
        TextView nameView = createNameView(measurement.getName());
        EditText dataInputView = createDataInputView(measurement.getTotal(), measurement);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        row.addView(nameView);
        row.addView(dataInputView);
        row.setClickable(false);

        return row;
    }

    private EditText createDataInputView(int total, Measurement measurement)
    {
        EditText dataInputView = new EditText(this);
        dataInputView.setText(Integer.toString(total));
        dataInputView.setTag(measurement);
        dataInputView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        dataInputView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(!hasFocus)
                {
                    EditText view = (EditText) v;
                    onInputFocusLost(view.getText().toString(), (Measurement) view.getTag());
                }
            }
        });

        LinearLayout.LayoutParams dataInputLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dataInputLayoutParams.setMargins(0, 0, ViewUtils.dpToPixels(this, 5), 0);

        dataInputView.setLayoutParams(dataInputLayoutParams);

        return dataInputView;
    }

    private TextView createArrowView()
    {
        TextView arrowView = new TextView(this);
        arrowView.setText(">");

        LinearLayout.LayoutParams arrowLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        arrowLayoutParams.setMargins(0, 0, ViewUtils.dpToPixels(this, 10), 0);

        arrowView.setLayoutParams(arrowLayoutParams);

        return arrowView;
    }

    /**
     * Group measurements together by column (alphabetically), then
     * sort within each group by section (win, build, send)
     */
    List<Measurement> sortMeasurements(List<Measurement> measurements)
    {
        List<Measurement> sortedMeasurements = new ArrayList<>(measurements.size());

        // First create a list of columns
        List<String> distinctColumns = new ArrayList<>();
        for(Measurement measurement : measurements)
        {
            if(!distinctColumns.contains(measurement.getColumn()))
            {
                distinctColumns.add(measurement.getColumn());
            }
        }

        // Sort the columns alphabetically
        Collections.sort(distinctColumns);

        // First group the measurements into their column groups
        for(String column : distinctColumns)
        {
            List<Measurement> measurementGroup = new ArrayList<>();

            for(Measurement measurement : measurements)
            {
                if(measurement.getColumn().equals(column))
                {
                    measurementGroup.add(measurement);
                }
            }

            // Now sort it according to sort_order
            Collections.sort(measurementGroup, sortOrderComparator());
            sortedMeasurements.addAll(measurementGroup);
        }

        return sortedMeasurements;
    }

    private void onInputFocusLost(String value, Measurement measurement)
    {
        try
        {
            measurement.setTotal(Integer.parseInt(value));
            new SaveMeasurementsToLocalDatabase().execute(measurement);
        }
        catch(NumberFormatException e)
        {
            Log.w(TAG, "Invalid number: " + value);
        }
    }

    private Comparator<Measurement> sortOrderComparator()
    {
        return new Comparator<Measurement>()
        {
            @Override
            public int compare(Measurement lhs, Measurement rhs)
            {
                return lhs.getSortOrder() < rhs.getSortOrder()
                    ? -1
                    : (lhs.getSortOrder() == rhs.getSortOrder() ? 0 : 1);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_measurements, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void drillIntoMeasurementDetails(Measurement measurement)
    {
        Intent goToMeasurementDetails = new Intent(this, MeasurementDetailsActivity.class);
        goToMeasurementDetails.putExtra(Constants.ARG_MEASUREMENT_ID, measurement.getMeasurementId());
        goToMeasurementDetails.putExtra(
            Constants.ARG_MINISTRY_ID, chosenMinistry != null ? chosenMinistry.getMinistryId() : Ministry.INVALID_ID);
        goToMeasurementDetails.putExtra("ministryName", chosenMinistry.getName());
        goToMeasurementDetails.putExtra(Constants.ARG_MCC, (mAssignment != null ? mAssignment.getMcc() :
                Ministry.Mcc.UNKNOWN).toString());
        goToMeasurementDetails.putExtra("measurementName", measurement.getName());

        if(currentPeriod != null)
        {
            goToMeasurementDetails.putExtra(Constants.ARG_PERIOD, currentPeriod);
        }

        startActivityForResult(goToMeasurementDetails, Constants.REQUEST_EXIT);
    }

    public void goToPreviousPeriod(View view)
    {
        TextView periodView = (TextView)findViewById(R.id.currentPeriod);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        String currentPeriodString = periodView.getText().toString();

        int year = Integer.parseInt(currentPeriodString.substring(0, 4));
        // subtract 1 because months are 0 based in Calendar, but visually they are 1 based
        int month = Integer.parseInt(currentPeriodString.substring(5)) - 1;

        Calendar previousMonth = Calendar.getInstance();
        previousMonth.set(year, month, 1);
        previousMonth.add(Calendar.MONTH, -1);

        String previousPeriodString = dateFormat.format(previousMonth.getTime());

        // Change the period text to show the new period
        periodView.setText(previousPeriodString);
        currentPeriod = previousPeriodString;
        preferences.edit().putString("currentPeriod", currentPeriod).apply();

        // Clear the data, so the user knows when the new data is up
        LinearLayout dataContainer = (LinearLayout) findViewById(R.id.measurement_data_Layout);
        dataContainer.removeAllViews();

        restartMeasurementLoader(previousPeriodString);
    }

    public void goToNextPeriod(View view)
    {
        TextView periodView = (TextView)findViewById(R.id.currentPeriod);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        String currentPeriodString = periodView.getText().toString();

        int year = Integer.parseInt(currentPeriodString.substring(0, 4));
        // subtract 1 because months are 0 based in Calendar, but visually they are 1 based
        int month = Integer.parseInt(currentPeriodString.substring(5)) - 1;

        Calendar currentTime = Calendar.getInstance();

        if(currentTime.get(Calendar.MONTH) == month && currentTime.get(Calendar.YEAR) == year)
        {
            Log.i(TAG, "User attempted to go to the next period, but that is in the future!");
            return;
        }

        Calendar nextMonth = Calendar.getInstance();
        nextMonth.set(year, month, 1);
        nextMonth.add(Calendar.MONTH, 1);

        String nextPeriodString = dateFormat.format(nextMonth.getTime());

        // Change the period text to show the new period
        periodView.setText(nextPeriodString);
        currentPeriod = nextPeriodString;
        preferences.edit().putString("currentPeriod", currentPeriod).apply();

        // Clear the data, so the user knows when the new data is up
        LinearLayout dataContainer = (LinearLayout) findViewById(R.id.measurement_data_Layout);
        dataContainer.removeAllViews();

        restartMeasurementLoader(nextPeriodString);
    }

    void onLoadCurrentAssignment(@Nullable final Assignment assignment) {
        mAssignment = assignment;

        if(mAssignment != null && mAssignment.isBlocked())
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_dialog_blocked))
                .setMessage(getString(R.string.disallowed_measurements))
                .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        finish();
                    }
                })
                .create();

            alertDialog.show();
        }

        chosenMinistry = mAssignment != null ? mAssignment.getMinistry() : null;

        if (chosenMinistry != null) {
            Log.i(TAG, "Associated Ministries retrieved");
            restartMeasurementLoader(currentPeriod);
        }
        else
        {
            Log.w(TAG, "No associated ministries");
        }
    }

    /**
     * This event is triggered when measurements are loaded from local storage
     *
     * @param measurements the list of measurements to display
     */
    void onLoadMeasurements(@Nullable final List<Measurement> measurements)
    {
        Log.i(TAG, "Measurements loaded from local storage");

        if(measurements != null && !measurements.isEmpty())
        {
            drawLayout(measurements);
        }
        else
        {
            Log.w(TAG, "No measurement data in local database, try searching from API");
            new NewMeasurementsPageRetrieverTask(mAssignment).execute(currentPeriod);
        }
    }

    private class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment> {
        @Nullable
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle bundle) {
            switch (id) {
                case LOADER_CURRENT_ASSIGNMENT:
                    return new CurrentAssignmentLoader(MeasurementsActivity.this, bundle);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Assignment> loader, @Nullable final Assignment assignment) {
            switch (loader.getId()) {
                case LOADER_CURRENT_ASSIGNMENT:
                    onLoadCurrentAssignment(assignment);
                    break;
            }
        }
    }

    private class MeasurementsLoaderCallbacks extends SimpleLoaderCallbacks<List<Measurement>>
    {
        @Override
        public Loader<List<Measurement>> onCreateLoader(final int id, @Nullable final Bundle args)
        {
            switch(id)
            {
                case LOADER_MEASUREMENTS:
                    return new MeasurementsLoader(MeasurementsActivity.this, args);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(
            @NonNull final Loader<List<Measurement>> loader,
            @Nullable final List<Measurement> measurements)
        {
            switch(loader.getId())
            {
                case LOADER_MEASUREMENTS:
                    onLoadMeasurements(measurements);
                    break;
            }
        }
    }

    private void onLoadMeasurementsFromServer(List<Measurement> measurements)
    {
        if(measurements != null)
        {
            drawLayout(measurements);

            // Save the measurements to the database for quicker loading next time
            MeasurementsService.saveMeasurementsToDatabase(MeasurementsActivity.this, measurements);
        }
        else
        {
            Log.w(TAG, "No measurement data");
        }
    }

    private class NewMeasurementsPageRetrieverTask extends AsyncTask<String, Void, List<Measurement>> {
        private final Assignment assignment;

        private NewMeasurementsPageRetrieverTask(final Assignment assignment) {
            this.assignment = assignment;
        }

        @Override
        protected List<Measurement> doInBackground(String... periods) {
            String period = periods[0];

            if (assignment != null) {
                try {
                    GmaApiClient apiClient = GmaApiClient.getInstance(MeasurementsActivity.this);

                    JSONArray results =
                            apiClient.searchMeasurements(assignment.getMinistryId(), assignment.getMcc(), period);

                    if (results == null) {
                        Log.w(TAG, "No measurements found!");
                        return null;
                    }

                    return MeasurementsJsonParser
                            .parseMeasurements(results, assignment.getMinistryId(), assignment.getMcc().toString(),
                                               period);
                } catch (ApiException e) {
                    Log.e(TAG, "Failed to retrieve measurements from API", e);
                    return null;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Measurement> measurements)
        {
            onLoadMeasurementsFromServer(measurements);
        }
    }

    private class SaveMeasurementsToLocalDatabase extends AsyncTask<Measurement, Void, Void>
    {

        @Override
        protected Void doInBackground(Measurement... params)
        {
            List<Measurement> measurements = new ArrayList<>();
            measurements.addAll(Arrays.asList(params));
            MeasurementsService.saveMeasurementsToDatabase(MeasurementsActivity.this, measurements);
            return null;
        }
    }
}
