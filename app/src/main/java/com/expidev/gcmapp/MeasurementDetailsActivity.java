package com.expidev.gcmapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expidev.gcmapp.utils.ViewUtils;
import com.expidev.gcmapp.view.HorizontalLineView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by William.Randall on 1/28/2015.
 */
public class MeasurementDetailsActivity extends ActionBarActivity
{
    private final String TAG = getClass().getSimpleName();
    private final String PREF_NAME = "gcm_prefs";

    private SharedPreferences preferences;

    // The main dataset that includes all the series that go into a chart
    private XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    // The main renderer that includes all the renderers customizing a chart
    private XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

    private XYSeries currentSeries;
    private XYSeriesRenderer currentRenderer;

    // The chart view that displays the data
    private GraphicalView chartView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_graph);

        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        initializeRenderer();
        initializeSeriesData();
        renderGraph();
        initializeDataSection();
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
    protected void onRestoreInstanceState(Bundle savedState) {
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
    private void initializeRenderer()
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

        renderer.setChartTitle(getString(R.string.measurement_detail_graph_title));
        renderer.setShowLegend(true);
        renderer.setShowLabels(true);


        renderer.setXLabels(0);
        renderer.setYLabels(3);

        renderer.setXAxisMin(0.0d);
        renderer.setXAxisMax(5.0d);

        renderer.clearXTextLabels();
        renderer.addXTextLabel(0.0, "2014-07");
        renderer.addXTextLabel(1.0, "2014-08");
        renderer.addXTextLabel(2.0, "2014-09");
        renderer.addXTextLabel(3.0, "2014-10");
        renderer.addXTextLabel(4.0, "2014-11");
        renderer.addXTextLabel(5.0, "2014-12");
    }

    private void initializeSeriesData()
    {
        initializeTotalSeries();
        initializeLocalSeries();
        initializeMySeries();
    }

    private void initializeTotalSeries()
    {
        currentSeries = new XYSeries(legendTitleWithPadding("Total"));

        currentSeries.add(0.0d, 0.0d);
        currentSeries.add(1.0d, 0.0d);
        currentSeries.add(2.0d, 0.0d);
        currentSeries.add(3.0d, 115.0d);
        currentSeries.add(4.0d, 75.0d);
        currentSeries.add(5.0d, 80.0d);

        dataset.addSeries(currentSeries);

        initializeSeriesRenderer(Color.BLUE);
    }

    private void initializeLocalSeries()
    {
        currentSeries = new XYSeries(legendTitleWithPadding("Local"));

        currentSeries.add(0.0d, 0.0d);
        currentSeries.add(1.0d, 0.0d);
        currentSeries.add(2.0d, 0.0d);
        currentSeries.add(3.0d, 115.0d);
        currentSeries.add(4.0d, 75.0d);
        currentSeries.add(5.0d, 70.0d);

        dataset.addSeries(currentSeries);

        initializeSeriesRenderer(Color.RED);
    }

    private void initializeMySeries()
    {
        currentSeries = new XYSeries("Me");

        currentSeries.add(0.0d, 0.0d);
        currentSeries.add(1.0d, 0.0d);
        currentSeries.add(2.0d, 0.0d);
        currentSeries.add(3.0d, 0.0d);
        currentSeries.add(4.0d, 0.0d);
        currentSeries.add(5.0d, 0.0d);

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

    private void initializeDataSection()
    {
        TextView totalNumberTitle = (TextView) findViewById(R.id.md_total_number_title);
        totalNumberTitle.setText("Guatemala (Local)");

        TextView totalNumberLabel = (TextView) findViewById(R.id.md_total_number_label);
        totalNumberLabel.setText("training");

        int total = 40;
        TextView totalNumberData = (TextView) findViewById(R.id.md_total_number);
        totalNumberData.setText(Integer.toString(total));

        LinearLayout dataSection = (LinearLayout) findViewById(R.id.md_chart_data);
        Map<String, Integer> dummyData = dummyData();

        for(Map.Entry<String, Integer> entry : dummyData.entrySet())
        {
            HorizontalLineView horizontalLine = new HorizontalLineView(this);
            dataSection.addView(horizontalLine);

            String name = entry.getKey();
            Integer number = entry.getValue();

            TextView nameView = createNameView(name);
            TextView valueView = createValueView(number.toString());

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            linearLayout.addView(nameView);
            linearLayout.addView(valueView);

            dataSection.addView(linearLayout);
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

    private Map<String, Integer> dummyData()
    {
        Map<String, Integer> dummyData = new HashMap<String, Integer>();
        dummyData.put("Keith Seabourn", 12);
        dummyData.put("Mark Griffen", 0);
        dummyData.put("Accounts Team", 0);
        dummyData.put("Stefan Dell", 0);
        dummyData.put("Mike Thacker", 0);
        dummyData.put("Ryan Carlson", 0);
        dummyData.put("Matthew Ritsema", 0);

        return dummyData;
    }
}
