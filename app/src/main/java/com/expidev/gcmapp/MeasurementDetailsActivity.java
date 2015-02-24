package com.expidev.gcmapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expidev.gcmapp.http.GmaApiClient;
import com.expidev.gcmapp.json.MeasurementsJsonParser;
import com.expidev.gcmapp.model.measurement.BreakdownData;
import com.expidev.gcmapp.model.measurement.MeasurementDetails;
import com.expidev.gcmapp.model.measurement.SixMonthAmounts;
import com.expidev.gcmapp.model.measurement.SubMinistryDetails;
import com.expidev.gcmapp.model.measurement.TeamMemberDetails;
import com.expidev.gcmapp.service.MeasurementsService;
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

/**
 * Created by William.Randall on 1/28/2015.
 */
public class MeasurementDetailsActivity extends ActionBarActivity
{
    private final String TAG = getClass().getSimpleName();
    private final MeasurementDetailsLoaderCallbacks measurementDetailsLoaderCallback = new MeasurementDetailsLoaderCallbacks();

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

    private static final int LOADER_MEASUREMENT_DETAILS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_graph);

        measurementId = getIntent().getStringExtra(Constants.ARG_MEASUREMENT_ID);
        ministryId = getIntent().getStringExtra(Constants.ARG_MINISTRY_ID);
        mcc = getIntent().getStringExtra(Constants.ARG_MCC);
        period = getIntent().getStringExtra(Constants.ARG_PERIOD);
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

        Bundle args = new Bundle(4);
        args.putString(Constants.ARG_MEASUREMENT_ID, measurementId);
        args.putString(Constants.ARG_MINISTRY_ID, ministryId);
        args.putString(Constants.ARG_MCC, mcc);
        args.putString(Constants.ARG_PERIOD, period);

        manager.initLoader(LOADER_MEASUREMENT_DETAILS, args, measurementDetailsLoaderCallback);
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

        renderer.setAxisTitleTextSize(36f);
        renderer.setChartTitleTextSize(40f);
        renderer.setLabelsTextSize(35f);
        renderer.setLegendTextSize(35f);

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
        initializeSeriesRenderer(Color.BLUE);
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

        initializeSeriesRenderer(Color.RED);
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

        initializeSeriesRenderer(Color.GREEN);
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
        TextView totalNumberTitle = (TextView) findViewById(R.id.md_total_number_title);
        totalNumberTitle.setText(ministryName);

        LinearLayout dataSection = (LinearLayout) findViewById(R.id.md_chart_data);

        List<BreakdownData> localBreakdown = measurementDetails.getLocalBreakdown();

        int layoutPosition = 1;
        for(BreakdownData localDataSource : localBreakdown)
        {
            //TODO: Should we skip 0 value rows?
            if("total".equals(localDataSource.getSource())) // || localDataSource.getSource() == 0)
            {
                continue;
            }

            LinearLayout row = createRow(localDataSource.getSource(), localDataSource.getAmount());
            dataSection.addView(row, layoutPosition);
            layoutPosition++;

            dataSection.addView(new HorizontalLineView(this), layoutPosition);
            layoutPosition++;
        }

        List<TeamMemberDetails> teamMemberDetailsList = measurementDetails.getTeamMemberDetails();

        for(TeamMemberDetails teamMemberDetails : teamMemberDetailsList)
        {
            HorizontalLineView horizontalLine = new HorizontalLineView(this);
            dataSection.addView(horizontalLine);

            String name = teamMemberDetails.getFirstName() + " " + teamMemberDetails.getLastName();

            LinearLayout row = createRow(name, teamMemberDetails.getTotal());
            dataSection.addView(row);
        }

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

    private LinearLayout createRow(String name, int value)
    {
        TextView nameView = createNameView(name);
        TextView valueView = createValueView(Integer.toString(value));

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(
            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(nameView);
        linearLayout.addView(valueView);

        return linearLayout;
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

    private class NewDetailsPageRetrieverTask extends AsyncTask<Object, Void, MeasurementDetails>
    {
        @Override
        protected MeasurementDetails doInBackground(Object... params)
        {
            String measurementId = (String) params[0];
            String ministryId = (String) params[1];
            String mcc = (String) params[2];
            String period = (String) params[3];

            if(measurementId != null && ministryId != null && mcc != null && period != null)
            {
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
}
