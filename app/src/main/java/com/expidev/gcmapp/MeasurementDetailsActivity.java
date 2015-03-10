package com.expidev.gcmapp;

import static com.expidev.gcmapp.Constants.ARG_MCC;
import static com.expidev.gcmapp.Constants.ARG_MEASUREMENT_ID;
import static com.expidev.gcmapp.Constants.ARG_MINISTRY_ID;
import static com.expidev.gcmapp.Constants.ARG_PERIOD;
import static com.expidev.gcmapp.Constants.ARG_PERMLINK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expidev.gcmapp.activity.MeasurementsActivity;
import com.expidev.gcmapp.activity.SettingsActivity;
import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.model.Assignment;
import com.expidev.gcmapp.model.Ministry;
import com.expidev.gcmapp.model.measurement.BreakdownData;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.model.measurement.SixMonthAmounts;
import com.expidev.gcmapp.model.measurement.SubMinistryDetails;
import com.expidev.gcmapp.model.measurement.TeamMemberDetails;
import com.expidev.gcmapp.service.MeasurementsService;
import com.expidev.gcmapp.support.v4.content.CurrentAssignmentLoader;
import com.expidev.gcmapp.support.v4.content.MeasurementDetailsLoader;
import com.expidev.gcmapp.utils.ViewUtils;
import com.expidev.gcmapp.view.HorizontalLineView;
import com.expidev.gcmapp.view.TextHeaderView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.ccci.gto.android.common.api.ApiException;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.joda.time.YearMonth;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;

/**
 * Created by William.Randall on 1/28/2015.
 */
public class MeasurementDetailsActivity extends ActionBarActivity
{
    private final String TAG = getClass().getSimpleName();
    private final MeasurementDetailsLoaderCallbacks measurementDetailsLoaderCallback = new MeasurementDetailsLoaderCallbacks();

    private TheKey mTheKey;

    // The main dataset that includes all the series that go into a chart
    private XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    // The main renderer that includes all the renderers customizing a chart
    private XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

    private XYSeries currentSeries;
    private XYSeriesRenderer currentRenderer;

    // The chart view that displays the data
    private GraphicalView chartView;

    private String measurementName;
    private String ministryName;
    private String measurementId;
    private String ministryId;
    private String mcc;
    private String period;

    private MeasurementDetails measurementDetails;
    private Assignment currentAssignment;

    private static final int LOADER_MEASUREMENT_DETAILS = 1;
    private static final int LOADER_CURRENT_ASSIGNMENT = 2;

    private static final String LOCAL_MEASUREMENTS_TAG = "local";
    private static final String PERSONAL_MEASUREMENTS_TAG = "personal";

    public static void start(@NonNull final Context context, @NonNull final String ministryId,
                             @NonNull final Ministry.Mcc mcc, @NonNull final String permLink,
                             @NonNull final YearMonth period, @NonNull final String measurementId,
                             @Nullable final String measurementName) {
        final Intent intent = new Intent(context, MeasurementDetailsActivity.class);
        intent.putExtra(ARG_MINISTRY_ID, ministryId);
        intent.putExtra(ARG_MCC, mcc.toString());
        intent.putExtra(ARG_PERMLINK, permLink);
        intent.putExtra(ARG_PERIOD, period.toString());
//        intent.putExtra("ministryName", chosenMinistry.getName());
        intent.putExtra(ARG_MEASUREMENT_ID, measurementId);
        intent.putExtra("measurementName", measurementName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_graph);
        mTheKey = TheKeyImpl.getInstance(this);

        measurementId = getIntent().getStringExtra(ARG_MEASUREMENT_ID);
        ministryId = getIntent().getStringExtra(ARG_MINISTRY_ID);
        mcc = getIntent().getStringExtra(ARG_MCC);
        period = getIntent().getStringExtra(ARG_PERIOD);
        measurementName = getIntent().getStringExtra("measurementName");
        ministryName = getIntent().getStringExtra("ministryName");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        setupLoaders();
    }

    private void setupLoaders()
    {
        final LoaderManager manager = this.getSupportLoaderManager();

        Bundle args = new Bundle(5);
        args.putString(Constants.ARG_GUID, mTheKey.getGuid());
        args.putString(ARG_MEASUREMENT_ID, measurementId);
        args.putString(ARG_MINISTRY_ID, ministryId);
        args.putString(ARG_MCC, mcc);
        args.putString(ARG_PERIOD, period);

        manager.initLoader(LOADER_MEASUREMENT_DETAILS, args, measurementDetailsLoaderCallback);
        manager.initLoader(LOADER_CURRENT_ASSIGNMENT, args, new AssignmentLoaderCallbacks());
    }

    private void handleRetrievedMeasurementDetails(MeasurementDetails measurementDetails)
    {
        Log.i(TAG, "Measurement details loaded from local database");
        sortSixMonthAmounts(measurementDetails);
        initializeRenderer(getPeriodsForLabels(measurementDetails.getSixMonthTotalAmounts()));
        initializeSeriesData(measurementDetails);
        renderGraph();
        initializeDataSection(measurementDetails);
    }

    private Set<String> getPeriodsForLabels(List<SixMonthAmounts> sixMonthAmountsList)
    {
        Set<String> periods = new LinkedHashSet<>();

        for(SixMonthAmounts row : sixMonthAmountsList)
        {
            String period = row.getMonth();

            if(!periods.contains(period))
            {
                periods.add(period);
            }
        }

        return periods;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_measurement_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final Intent upIntent = NavUtils.getParentActivityIntent(this);
                MeasurementsActivity.populateIntent(upIntent, mTheKey.getGuid(), ministryId, Ministry.Mcc.fromRaw(mcc),
                                                    YearMonth.parse(period));

                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        // save the current data, for instance when changing screen orientation
        outState.putSerializable("dataset", dataset);
        outState.putSerializable("renderer", renderer);
        outState.putSerializable("current_series", currentSeries);
        outState.putSerializable("current_renderer", currentRenderer);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);

        // restore the current data, for instance when changing the screen orientation
        dataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
        renderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
        currentSeries = (XYSeries) savedState.getSerializable("current_series");
        currentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
    }

    /**
     * Sets the properties for the graph as a whole
     */
    private void initializeRenderer(Set<String> months)
    {
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setMarginsColor(Color.argb(255,191,188,187));
        renderer.setGridColor(Color.argb(255,189,187,186));

        renderer.setAxisTitleTextSize(36f);
        renderer.setChartTitleTextSize(45f);
        renderer.setLabelsTextSize(35f);
        renderer.setLegendTextSize(35f);
        renderer.setXLabelsColor(Color.WHITE);
        renderer.setYLabelsColor(0, Color.WHITE);
        renderer.setLabelsColor(Color.WHITE);

        renderer.setLegendHeight(50);
        renderer.setPointSize(0f);
        renderer.setMargins(new int[]{80, 80, 80, 80});  // top, left, bottom, right
        renderer.setYLabelsPadding(45.0f);

        renderer.setZoomButtonsVisible(false);
        renderer.setPanEnabled(false);
        renderer.setShowGridX(true);

        renderer.setChartTitle(measurementName);
        renderer.setShowLegend(true);
        renderer.setShowLabels(true);

        renderer.setXLabels(0);
        renderer.setYLabels(3);

        renderer.setXAxisMin(0.0d);
        renderer.setXAxisMax(5.0d);

        renderer.clearXTextLabels();

        List<String> labels = new ArrayList<>();
        labels.addAll(months);
        Collections.sort(labels, dateLabelComparator());

        double xIndex = 0.0d;
        for(String month : labels)
        {
            renderer.addXTextLabel(xIndex, month);
            xIndex++;
        }
    }

    private Comparator<String> dateLabelComparator()
    {
        return new Comparator<String>()
        {
            @Override
            public int compare(String lhs, String rhs)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

                try
                {
                    Date leftDate = dateFormat.parse(lhs);
                    Date rightDate = dateFormat.parse(rhs);
                    return leftDate.compareTo(rightDate);
                }
                catch(ParseException e)
                {
                    return 0;
                }
            }
        };
    }

    private void initializeSeriesData(MeasurementDetails measurementDetails)
    {
        if(currentSeries != null)
        {
            currentSeries.clear();
        }
        if(dataset != null)
        {
            dataset.clear();
        }
        initializeTotalSeries(measurementDetails.getSixMonthTotalAmounts());
        initializeLocalSeries(measurementDetails.getSixMonthLocalAmounts());
        initializeMySeries(measurementDetails.getSixMonthPersonalAmounts());
    }

    private void initializeTotalSeries(List<SixMonthAmounts> totalAmounts)
    {
        currentSeries = new XYSeries(legendTitleWithPadding("Total"));

        double xPoint = 0.0d;
        for(SixMonthAmounts point : totalAmounts)
        {
            currentSeries.add(xPoint, point.getAmount());
            xPoint++;
        }

        dataset.addSeries(currentSeries);

        initializeSeriesRenderer(Color.argb(255,111,115,137));
    }

    private void initializeLocalSeries(List<SixMonthAmounts> localAmounts)
    {
        currentSeries = new XYSeries(legendTitleWithPadding("Local"));

        double xPoint = 0.0d;
        for(SixMonthAmounts point : localAmounts)
        {
            currentSeries.add(xPoint, point.getAmount());
            xPoint++;
        }

        dataset.addSeries(currentSeries);

        initializeSeriesRenderer(Color.argb(255,211,44,96));
    }

    private void initializeMySeries(List<SixMonthAmounts> personalAmounts)
    {
        currentSeries = new XYSeries("Me");

        double xPoint = 0.0d;
        for(SixMonthAmounts point : personalAmounts)
        {
            currentSeries.add(xPoint, point.getAmount());
            xPoint++;
        }

        dataset.addSeries(currentSeries);

        initializeSeriesRenderer(Color.argb(255,107,192,72));
    }

    /**
     * This is kind of stupid, but the graph library
     * does not allow for padding the legend titles
     */
    private String legendTitleWithPadding(String legendTitle)
    {
        return legendTitle + "               ";
    }

    /**
     * Sets properties for showing the series data
     */
    private void initializeSeriesRenderer(int color)
    {
        currentRenderer = new XYSeriesRenderer();
        currentRenderer.setDisplayChartValues(false);
        currentRenderer.setDisplayChartValuesDistance(5);
        currentRenderer.setLineWidth(15.0f);
        currentRenderer.setShowLegendItem(true);
        currentRenderer.setColor(color);

        renderer.addSeriesRenderer(currentRenderer);
    }

    private void renderGraph()
    {
        if(chartView == null)
        {
            chartView = ChartFactory.getLineChartView(this, dataset, renderer);
            renderer.setClickEnabled(true);
            renderer.setSelectableBuffer(10);

            chartView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
                    if(seriesSelection == null)
                    {
                        Toast.makeText(MeasurementDetailsActivity.this, "No graph element", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(MeasurementDetailsActivity.this, "Graph element in series index " + seriesSelection.getSeriesIndex() +
                            " data point index " + seriesSelection.getPointIndex() + " was clicked closest point value X=" +
                            seriesSelection.getXValue() + " Y=" + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            LinearLayout layout = (LinearLayout) findViewById(R.id.md_chart);
            layout.addView(chartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        else
        {
            chartView.repaint();
        }
    }

    private void initializeDataSection(MeasurementDetails measurementDetails)
    {
        if(currentAssignment == null)
        {
            return;
        }

        TextHeaderView totalNumberTitle = new TextHeaderView(this);
        totalNumberTitle.setPadding(8, 8, 8, 8);
        totalNumberTitle.setText(ministryName);

        LinearLayout dataSection = (LinearLayout) findViewById(R.id.md_chart_data);
        dataSection.removeAllViews();

        dataSection.addView(totalNumberTitle);

        // Only leaders and inherited leaders can see the local breakdown
        if(currentAssignment.isLeadership())
        {
            List<BreakdownData> localBreakdown = measurementDetails.getLocalBreakdown();

            for(BreakdownData localDataSource : localBreakdown)
            {
                if("total".equals(localDataSource.getSource()))
                {
                    continue;
                }

                LinearLayout row = createRow(localDataSource.getSource(), localDataSource.getAmount());
                dataSection.addView(row);

                dataSection.addView(new HorizontalLineView(this));
            }

            // Only leaders and inherited leaders can edit local values
            EditText localNumber = createInputView(
                measurementDetails.getTotalLocalBreakdown().getAmount(),
                LOCAL_MEASUREMENTS_TAG);
            LinearLayout localDataInputSection = createRow(createNameView("Local"), localNumber);

            dataSection.addView(localDataInputSection);
        }
        else if(currentAssignment.isMember())
        {
            // Members can see the local total, but not edit it
            LinearLayout localDataInputSection = createRow(
                "Local",
                measurementDetails.getTotalLocalBreakdown().getAmount()
            );
            dataSection.addView(localDataInputSection);
        }

        TextHeaderView teamMembersSectionTitle = new TextHeaderView(this);
        teamMembersSectionTitle.setText(R.string.team_members_title);

        dataSection.addView(teamMembersSectionTitle);

        // Inherited leaders do not have personal measurements
        if(!currentAssignment.isInheritedLeader())
        {
            EditText personalNumber = createInputView(
                measurementDetails.getSelfBreakdown().get(0).getAmount(),
                PERSONAL_MEASUREMENTS_TAG);
            LinearLayout personalDataInputSection = createRow(createNameView("You"), personalNumber);

            dataSection.addView(personalDataInputSection);
        }

        // Only leaders and inherited leaders can see team member details
        if(currentAssignment.isLeadership())
        {
            addTeamMemberSection(dataSection, measurementDetails);

            HorizontalLineView horizontalLine = new HorizontalLineView(this);
            dataSection.addView(horizontalLine);
            dataSection.setPadding(10, 8, 8, 8);

            addTeamMemberTotalSection(dataSection, measurementDetails);

        }
        else if(currentAssignment.isMember())
        {
            // Members can view the total team member amount
            addTeamMemberTotalSection(dataSection, measurementDetails);
        }

        // Everyone except self-assigned (and blocked, who can't get to this page) can see sub-ministry details
        if(!currentAssignment.isSelfAssigned())
        {
            addSubMinistriesSection(dataSection, measurementDetails);
        }

        // Only leaders and inherited leaders can see self-assigned details
        if(currentAssignment.isLeadership())
        {
            addSelfAssignedSection(dataSection, measurementDetails);
        }
    }

    private void addTeamMemberSection(LinearLayout dataSection, MeasurementDetails measurementDetails)
    {
        List<TeamMemberDetails> teamMemberDetailsList = measurementDetails.getTeamMemberDetails();

        for(TeamMemberDetails teamMemberDetails : teamMemberDetailsList)
        {
            HorizontalLineView horizontalLine = new HorizontalLineView(this);
            dataSection.addView(horizontalLine);

            String name = teamMemberDetails.getFirstName() + " " + teamMemberDetails.getLastName();

            LinearLayout row = createRow(name, teamMemberDetails.getTotal());
            dataSection.addView(row);
        }
    }

    private void addTeamMemberTotalSection(LinearLayout dataSection, MeasurementDetails measurementDetails)
    {
        int total = 0;
        List<TeamMemberDetails> teamMemberDetailsList = measurementDetails.getTeamMemberDetails();

        for(TeamMemberDetails teamMemberDetails : teamMemberDetailsList)
        {
            total += teamMemberDetails.getTotal();
        }

        total += measurementDetails.getSelfBreakdown().get(0).getAmount();

        LinearLayout row = createRow("TOTAL", total);
        dataSection.addView(row);
    }

    private void addSubMinistriesSection(LinearLayout dataSection, MeasurementDetails measurementDetails)
    {
        List<SubMinistryDetails> subMinistryDetailsList = measurementDetails.getSubMinistryDetails();

        if(subMinistryDetailsList.size() > 0)
        {
            TextHeaderView subMinistryTextView = new TextHeaderView(this);
            subMinistryTextView.setText(R.string.sub_team_ministries_header);

            dataSection.addView(subMinistryTextView);

            for(SubMinistryDetails subMinistryDetails : subMinistryDetailsList)
            {
                LinearLayout row = createRow(subMinistryDetails.getName(), subMinistryDetails.getTotal());
                dataSection.addView(row);
            }
        }
    }

    private void addSelfAssignedSection(LinearLayout dataSection, MeasurementDetails measurementDetails)
    {
        List<TeamMemberDetails> selfAssignedDetails = measurementDetails.getSelfAssignedDetails();
        if(!selfAssignedDetails.isEmpty())
        {
            TextHeaderView selfAssignedHeader = new TextHeaderView(this);
            selfAssignedHeader.setText("Self Assigned");

            dataSection.addView(selfAssignedHeader);

            int index = 0;
            for(TeamMemberDetails selfAssignedRow : selfAssignedDetails)
            {
                if(index > 0)
                {
                    HorizontalLineView horizontalLine = new HorizontalLineView(this);
                    dataSection.addView(horizontalLine);
                }

                String name = selfAssignedRow.getFirstName() + " " + selfAssignedRow.getLastName();
                LinearLayout row = createRow(name, selfAssignedRow.getTotal());
                dataSection.addView(row);
                index++;
            }
        }
    }

    private TextView createNameView(String name)
    {
        TextView nameView = new TextView(this);
        nameView.setText(name);

        LinearLayout.LayoutParams nameLayoutParams = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        nameLayoutParams.weight = 1f;
        nameLayoutParams.setMargins(ViewUtils.dpToPixels(this, 10), 0, 0, 0);

        nameView.setLayoutParams(nameLayoutParams);

        return nameView;
    }

    private TextView createValueView(String value)
    {
        TextView valueView = new TextView(this);
        valueView.setText(value);

        LinearLayout.LayoutParams valueLayoutParams = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        valueLayoutParams.setMargins(0, 0, ViewUtils.dpToPixels(this, 10), 0);

        valueView.setLayoutParams(valueLayoutParams);

        return valueView;
    }

    private EditText createInputView(int value, String tag)
    {
        EditText inputView = new EditText(this);
        inputView.setTag(tag);
        inputView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        inputView.setText(Integer.toString(value));
        inputView.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(!hasFocus)
                {
                    EditText view = (EditText) v;
                    onInputFocusLost(view.getText().toString(), (String) view.getTag());
                }
            }
        });

        LinearLayout.LayoutParams inputLayoutParams = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        inputLayoutParams.setMargins(0, 0, ViewUtils.dpToPixels(this, 20), 0);

        inputView.setLayoutParams(inputLayoutParams);

        return inputView;
    }

    private LinearLayout createRow(String name, int value)
    {
        return createRow(createNameView(name), createValueView(Integer.toString(value)));
    }

    private LinearLayout createRow(TextView nameView, TextView valueView)
    {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(
            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(nameView);
        linearLayout.addView(valueView);

        return linearLayout;
    }

    private void onInputFocusLost(String value, String type)
    {
        try
        {
            switch(type)
            {
                case LOCAL_MEASUREMENTS_TAG:
                    measurementDetails.getTotalLocalBreakdown().setAmount(Integer.parseInt(value));
                    measurementDetails.setLocalValue(Integer.parseInt(value));
                    break;
                case PERSONAL_MEASUREMENTS_TAG:
                    measurementDetails.getSelfBreakdown().get(0).setAmount(Integer.parseInt(value));
                    measurementDetails.setPersonalValue(Integer.parseInt(value));
                    break;
            }

            new SaveMeasurementsToServer().execute(measurementDetails);
        }
        catch(NumberFormatException e)
        {
            Log.w(TAG, "Invalid number: " + value);
        }
    }

    /**
     * This event triggers when measurement details are loaded from local storage
     *
     * @param measurementDetails details for the current measurement
     */
    private void onLoadMeasurementDetails(MeasurementDetails measurementDetails)
    {
        if(measurementDetails != null)
        {
            this.measurementDetails = measurementDetails;
            handleRetrievedMeasurementDetails(measurementDetails);
        }
        else
        {
            Log.i(TAG, "No data for measurement in local database, loading from the API");
            new NewDetailsPageRetrieverTask().execute(measurementId, ministryId, mcc, period);
        }
    }

    private void sortSixMonthAmounts(MeasurementDetails measurementDetails)
    {
        Collections.sort(measurementDetails.getSixMonthTotalAmounts(), sixMonthAmountsComparator());
        Collections.sort(measurementDetails.getSixMonthLocalAmounts(), sixMonthAmountsComparator());
        Collections.sort(measurementDetails.getSixMonthPersonalAmounts(), sixMonthAmountsComparator());
    }

    private Comparator<SixMonthAmounts> sixMonthAmountsComparator()
    {
        return new Comparator<SixMonthAmounts>()
        {
            @Override
            public int compare(SixMonthAmounts lhs, SixMonthAmounts rhs)
            {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

                try
                {
                    Date leftDate = dateFormat.parse(lhs.getMonth());
                    Date rightDate = dateFormat.parse(rhs.getMonth());
                    return leftDate.compareTo(rightDate);
                }
                catch(ParseException e)
                {
                    return 0;
                }
            }
        };
    }

    private void onLoadDetailsFromServer(MeasurementDetails measurementDetails)
    {
        if(measurementDetails != null)
        {
            handleRetrievedMeasurementDetails(measurementDetails);
            MeasurementsService.saveMeasurementDetailsToDatabase(this, measurementDetails);
        }
        else
        {
            Log.w(TAG, "No measurement detail data");
            finish();
        }
    }

    private void onLoadAssignment(@Nullable Assignment assignment)
    {
        if(assignment != null)
        {
            currentAssignment = assignment;
            Log.i(TAG, "Current assignment loaded");

            if(currentAssignment.isBlocked())
            {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.title_dialog_blocked))
                    .setMessage(getString(R.string.disallowed_measurements))
                    .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            setResult(Constants.BLOCKED_MINISTRY);
                            finish();
                        }
                    })
                    .create();

                alertDialog.show();
            }
        }
    }

    private class MeasurementDetailsLoaderCallbacks extends SimpleLoaderCallbacks<MeasurementDetails>
    {
        @Override
        public Loader<MeasurementDetails> onCreateLoader(final int id, @Nullable final Bundle args)
        {
            switch(id)
            {
                case LOADER_MEASUREMENT_DETAILS:
                    return new MeasurementDetailsLoader(MeasurementDetailsActivity.this, args);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(
            @NonNull final Loader<MeasurementDetails> loader,
            @Nullable final MeasurementDetails measurementDetails)
        {
            switch(loader.getId())
            {
                case LOADER_MEASUREMENT_DETAILS:
                    onLoadMeasurementDetails(measurementDetails);
                    break;
            }
        }
    }

    private class AssignmentLoaderCallbacks extends SimpleLoaderCallbacks<Assignment>
    {
        @Override
        public Loader<Assignment> onCreateLoader(final int id, @Nullable final Bundle args)
        {
            switch(id)
            {
                case LOADER_CURRENT_ASSIGNMENT:
                    return new CurrentAssignmentLoader(MeasurementDetailsActivity.this, args);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Assignment> loader, @Nullable final Assignment assignment)
        {
            switch(loader.getId())
            {
                case LOADER_CURRENT_ASSIGNMENT:
                    onLoadAssignment(assignment);
            }
        }
    }

    private class NewDetailsPageRetrieverTask extends AsyncTask<Object, Void, MeasurementDetails>
    {
        @Override
        protected MeasurementDetails doInBackground(Object... params)
        {
            String measurementId = (String) params[0];
            String ministryId = (String) params[1];
            final Ministry.Mcc mcc = Ministry.Mcc.fromRaw((String) params[2]);
            String period = (String) params[3];

            if (measurementId != null && ministryId != null && mcc != Ministry.Mcc.UNKNOWN && period != null) {
                try
                {
                    GmaApiClient apiClient = GmaApiClient.getInstance(MeasurementDetailsActivity.this);
                    JSONObject results = apiClient.getDetailsForMeasurement(measurementId, ministryId, mcc, period);

                    if(results == null)
                    {
                        Log.w(TAG, "No measurement details!");
                        return null;
                    }
                    MeasurementDetails measurementDetails = MeasurementsJsonParser.parseMeasurementDetails(results);
                    measurementDetails.setMeasurementId(measurementId);
                    measurementDetails.setMinistryId(ministryId);
                    measurementDetails.setMcc(mcc);
                    measurementDetails.setPeriod(period);

                    return measurementDetails;
                }
                catch(ApiException e)
                {
                    Log.e(TAG, "Failed to retrieve measurement details from API", e);
                    return null;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(MeasurementDetails measurementDetails)
        {
            onLoadDetailsFromServer(measurementDetails);
        }
    }

    private class SaveMeasurementsToServer extends AsyncTask<MeasurementDetails, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(MeasurementDetails... params)
        {
            MeasurementDetails measurementDetails = params[0];
            GmaApiClient apiClient = GmaApiClient.getInstance(MeasurementDetailsActivity.this);

            try
            {
                List<MeasurementDetails> data = new ArrayList<>();
                data.add(measurementDetails);

                Log.i(TAG, "Saving new measurements to the database");
                MeasurementsService.saveMeasurementDetailsToDatabase(MeasurementDetailsActivity.this, measurementDetails);

                Log.i(TAG, "Sending new measurements to the server");
                return apiClient.updateMeasurementDetails(data, currentAssignment.getId());
            }
            catch(ApiException ae)
            {
                Log.e(TAG, "Error saving measurement details", ae);
            }
            catch(JSONException je)
            {
                Log.e(TAG, "Error parsing measurement details into JSON", je);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            if(success)
            {
                handleRetrievedMeasurementDetails(measurementDetails);
            }
            else
            {
                Log.e(TAG, "Failed to update Server");
            }
        }
    }
}
