package com.github.skoryupina.planyourdaytoday;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;



public class ListStatisticsActivity extends AppCompatActivity {
    private String MENU_ITEM_FOR_STATISTICS_TYPE;
    private int KIND_OF_STATISTICS;
    private final int MONTH = 0;
    private final int INTERVAL = 1;
    private static final String MONTH_SPINNER_TITLE = "Choose month";
    Locale l = Locale.ENGLISH;
    String[] months = new DateFormatSymbols(l).getMonths();
    private ListView mListOfCategoriesStatistic;
    private int mSelectedMonth;
    private static final String mAMOUNT = "Favourite: by amount";
    private static final String mDURATION = "Favourite: by duration";
    private static final String mCHOOSE = "Statistics: Chosen";
    private ArrayList<String> mListOfCategories;
    private TextView mStartDate;
    private TextView mFinishDate;
    private TextView mtvCategories;
    private HashMap<String,Float> categoriesToProcess = new HashMap<>();
    private boolean[] mSelections;// = new boolean[ mListOfCategories.size() ];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_statistics);
        searchPanelConfiguration();

        PickersWorkHelper.mFragmentManager = getFragmentManager();
        PickersWorkHelper.mContext=this;


    }

    private void searchPanelConfiguration(){

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            KIND_OF_STATISTICS = extras.getInt(getString(R.string.kind_of_statistics));
            MENU_ITEM_FOR_STATISTICS_TYPE = extras.getString(getString(R.string.menu_item));
            setTitle(MENU_ITEM_FOR_STATISTICS_TYPE);
            LinearLayout layoutForMonthSearch = (LinearLayout)findViewById(R.id.layoutForMonthSearch);
            LinearLayout layoutForIntervalSearch = (LinearLayout)findViewById(R.id.layoutForIntervalSearch);
            switch (KIND_OF_STATISTICS){
                case MONTH:{
                    layoutForMonthSearch.setVisibility(View.VISIBLE);
                    layoutForIntervalSearch.setVisibility(View.INVISIBLE);
                    spinnerConfiguration();
                }break;
                case INTERVAL:{
                    layoutForMonthSearch.setVisibility(View.INVISIBLE);
                    layoutForIntervalSearch.setVisibility(View.VISIBLE);
                    mStartDate = (TextView) findViewById(R.id.inputStartDate);
                    mFinishDate = (TextView) findViewById(R.id.inputFinishDate);
                }break;
            }
            if (MENU_ITEM_FOR_STATISTICS_TYPE.equals(getString(R.string.favourite_by_choose))){
                DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                mListOfCategories = dbHelper.getCategories();
                mSelections = new boolean[ mListOfCategories.size() ];
                mtvCategories = (TextView) findViewById(R.id.tvCategoriesChosen);
                mtvCategories.setVisibility(View.VISIBLE);
            }
            mListOfCategoriesStatistic = (ListView) findViewById(R.id.lvStatistics);
        }
    }

    private void spinnerConfiguration(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setPrompt(MONTH_SPINNER_TITLE);
        spinner.setSelection(11);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mSelectedMonth = position + 1; //sqlite counts from 1
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_statistics, menu);
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


    public void showDatePickerDialog(View v) {
        PickersWorkHelper.mTvChosen = (TextView) v;
        PickersWorkHelper.createDatePicker();
    }

    public void getStatistics(View v){
        switch (MENU_ITEM_FOR_STATISTICS_TYPE){
            case mAMOUNT:{
                getStatisticsFavouriteByAmount();
            }break;
            case mDURATION:{
                getStatisticsFavouriteByDuration();
            }break;
            case mCHOOSE:{
                getStatisticsForChosenCategories();
            }break;
        }
    }

    private void getStatisticsForChosenCategories() {
        if (categoriesToProcess.size()>0) {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            String[] displayedData = null;
            switch (KIND_OF_STATISTICS) {
                case MONTH: {
                    displayedData = dbHelper.getStatisticsByChoose(String.format("%02d", mSelectedMonth), categoriesToProcess);
                }
                break;
                case INTERVAL: {
                    String[] dates = PickersWorkHelper.getAndCheckDatesFromFields(mStartDate, mFinishDate);
                    if (dates != null) {
                        displayedData = dbHelper.getStatisticsByChoose(dates[0], dates[1], categoriesToProcess);
                    } else {
                        Toast.makeText(this, R.string.error_date_time, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            }
            if (displayedData != null) {
                ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedData);
                mListOfCategoriesStatistic.setAdapter(categoriesAdapter);
            } else {
                Toast.makeText(this, R.string.nothingFound, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, R.string.choose_categories_toast, Toast.LENGTH_SHORT).show();
        }
    }


    public void getStatisticsFavouriteByDuration() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        String[] displayedData = null;
        switch (KIND_OF_STATISTICS){
            case MONTH:{
                displayedData = dbHelper.getStatisticsByDuration(String.format("%02d", mSelectedMonth));
            }break;
            case INTERVAL:{
                String[] dates = PickersWorkHelper.getAndCheckDatesFromFields(mStartDate, mFinishDate);
                if (dates!=null) {
                    displayedData = dbHelper.getStatisticsByDuration(dates[0], dates[1]);
                }
                else {
                    Toast.makeText(this, R.string.error_date_time, Toast.LENGTH_SHORT).show();
                    return;
                }
            }break;
        }
        if (displayedData != null) {
            ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedData);
            mListOfCategoriesStatistic.setAdapter(categoriesAdapter);
        } else{
            Toast.makeText(this, R.string.nothingFound, Toast.LENGTH_SHORT).show();
        }
    }

    public void getStatisticsFavouriteByAmount() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        String[] displayedData = null;
        switch (KIND_OF_STATISTICS){
            case MONTH:{
                displayedData = dbHelper.getStatisticsByAmount(String.format("%02d", mSelectedMonth));
            }break;
            case INTERVAL:{
                 String[] dates = PickersWorkHelper.getAndCheckDatesFromFields(mStartDate, mFinishDate);
               if (dates!=null) {
                   displayedData = dbHelper.getStatisticsByAmount(dates[0], dates[1]);
               }
                else {
                   Toast.makeText(this, R.string.error_date_time, Toast.LENGTH_SHORT).show();
                   return;
               }
            }break;
        }
        if (displayedData != null) {
            ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedData);
            mListOfCategoriesStatistic.setAdapter(categoriesAdapter);
        } else{
            Toast.makeText(this, R.string.nothingFound, Toast.LENGTH_SHORT).show();
        }
    }


    public void showDialog(View view) {
        if (mListOfCategories!=null && mListOfCategories.size()>0) {
            String[] array = new String[mListOfCategories.size()];
            mListOfCategories.toArray(array);
           // boolean[] _selections =  new boolean[ mListOfCategories.size() ];
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.choose_categories))
                    .setMultiChoiceItems(array, mSelections, new DialogSelectionClickHandler())
                    .setPositiveButton("OK", new DialogButtonClickHandler())
                    .show();
        }
        else {
            Toast.makeText(this, R.string.empty_categories_list, Toast.LENGTH_SHORT).show();
        }
    }

    public static class DialogButtonClickHandler implements DialogInterface.OnClickListener
    {
        public void onClick( DialogInterface dialog, int clicked ) {
            switch( clicked ) {
                case DialogInterface.BUTTON_POSITIVE:
                    //printSelectedPlanets();
                    break;
            }
        }
    }

    public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener
    {
        float INITIAL_VALUE = 0f;
        public void onClick( DialogInterface dialog, int clicked, boolean selected )
        {
            if (selected){
                categoriesToProcess.put(mListOfCategories.get(clicked),INITIAL_VALUE);
            }
            else {
                categoriesToProcess.remove(mListOfCategories.get(clicked));
            }
            mSelections[clicked]=selected;
            String values = categoriesToProcess.keySet().toString();
            mtvCategories.setText(values.substring(1,values.length()-1));
        }
    }

}

