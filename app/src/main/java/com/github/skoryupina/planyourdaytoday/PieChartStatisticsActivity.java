package com.github.skoryupina.planyourdaytoday;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.PercentFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PieChartStatisticsActivity extends AppCompatActivity {

    private RelativeLayout mainRelativeLayout;
    private PieChart mPieChart;
    private ArrayList<String> xVals = new ArrayList<>();
    /*private float[] yData = {1, 40, 5, 7, 8, 20};
    private String[] xData = {"f", "r", "t", "w", "ww", "kate"};*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart_statistics);
        doLayoutConfiguration();
        doPieChartConfiguration();
        addDataToPieChart();
    }

    private void addDataToPieChart() {
        final String PIE_SET_NAME = "Categories";
        final int SLICE_SPACE_CONST = 3;
        final int SELECTION_SHIFT_CONST = 5;
        final float TEXT_SIZE = 13f;
        HashMap<String, Float> listOfCategories = getStatisticsData();

        ArrayList<Entry> yVals = new ArrayList<>();
        Iterator it = listOfCategories.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry<String,Float> entry = (Map.Entry)it.next();
            yVals.add(new Entry(entry.getValue(), i++));
            xVals.add(entry.getKey());
        }

        //create Pie dataset
        PieDataSet dataSet = new PieDataSet(yVals, PIE_SET_NAME);
        dataSet.setSliceSpace(SLICE_SPACE_CONST);
        dataSet.setSelectionShift(SELECTION_SHIFT_CONST);

        //configurate colors
        ArrayList<Integer> colors = createColorsRange();
        dataSet.setColors(colors);

        //instantiate pie chart data object
        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(TEXT_SIZE);
        data.setValueTextColor(Color.GRAY);

        mPieChart.setData(data);


        //customize legends
        Legend l = mPieChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(listOfCategories.size() + 1); //plus name of set line
        l.setYEntrySpace(5); //???

        //undo all highlights
        mPieChart.highlightValues(null);

        //update pie chart
        mPieChart.invalidate();
    }

    private void doLayoutConfiguration() {
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.activityPieChartStatisticsLayout);
        mPieChart = new PieChart(this);
        mainRelativeLayout.addView(mPieChart);
        mainRelativeLayout.setBackgroundColor(Color.LTGRAY);
    }

    private void doPieChartConfiguration() {
        final String PIE_CHART_NAME = "Statistics: All categories";
        //configure pie chart
        mPieChart.setUsePercentValues(true);
        mPieChart.setDescription(PIE_CHART_NAME);

        //enable hole and configure
        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColorTransparent(true);
        mPieChart.setHoleRadius(7);
        mPieChart.setTransparentCircleRadius(10);

        //enable rotation by touch
        mPieChart.setRotationAngle(0);
        mPieChart.setRotationEnabled(true);

        //set a chart value selected listener
        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                //display msg when value selected
                if (entry == null)
                    return;
                Toast.makeText(PieChartStatisticsActivity.this, xVals.get(entry.getXIndex()) + " = " + entry.getVal() + " ч", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private ArrayList<Integer> createColorsRange() {
        //add many colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(c);
        }

        for (int c : ColorTemplate.JOYFUL_COLORS) {
            colors.add(c);
        }

        for (int c : ColorTemplate.COLORFUL_COLORS) {
            colors.add(c);
        }

        for (int c : ColorTemplate.LIBERTY_COLORS) {
            colors.add(c);
        }

        for (int c : ColorTemplate.PASTEL_COLORS) {
            colors.add(c);
        }
        colors.add(ColorTemplate.getHoloBlue());
        return colors;
    }

    private HashMap<String, Float> getStatisticsData() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        HashMap<String, Float> listOfCategories = dbHelper.getCategoriesStatisticForChart();
        return listOfCategories;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pie_chart_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
