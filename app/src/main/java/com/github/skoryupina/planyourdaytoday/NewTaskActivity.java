package com.github.skoryupina.planyourdaytoday;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.DatePickerDialog.OnDateSetListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import javax.xml.datatype.Duration;


public class NewTaskActivity extends FragmentActivity {
    private AutoCompleteTextView mInputCategory;
    public static final int SELECT_PICTURE = 1;
    public GridLayout mGridPhotos;
    private TextView mStartDate;
    private TextView mStartTime;
    private TextView mFinishDate;
    private TextView mFinishTime;
    private AutoCompleteTextView mCategory;
    private EditText mSummary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        mInputCategory = (AutoCompleteTextView) findViewById(R.id.inputCategory);
        mStartDate = (TextView) findViewById(R.id.inputStartDate);
        mStartTime = (TextView) findViewById(R.id.inputStartTime);
        mFinishDate = (TextView) findViewById(R.id.inputFinishDate);
        mFinishTime = (TextView) findViewById(R.id.inputFinishTime);

        mCategory = (AutoCompleteTextView) findViewById(R.id.inputCategory);
        mSummary = (EditText) findViewById(R.id.inputSummary);


        setInputCategoriesAdapter();
        PickersWorkHelper.mFragmentManager = getFragmentManager();
        PickersWorkHelper.mContext=this;

        //initial values for date and time fields

        final Calendar c = Calendar.getInstance();

        PickersWorkHelper.setDataInInputField(mStartDate, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
        PickersWorkHelper.setDataInInputField(mFinishDate, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));

        PickersWorkHelper.setTimeInInputField(mStartTime, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        PickersWorkHelper.setTimeInInputField(mFinishTime, c.get(Calendar.HOUR_OF_DAY) + 1, c.get(Calendar.MINUTE));

        //grid
        mGridPhotos = (GridLayout) findViewById(R.id.gridPhotos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_task, menu);
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

    private void setInputCategoriesAdapter() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        ArrayList<String> listOfCategories = dbHelper.getCategories();
        String[] categories = new String[listOfCategories.size()];
        listOfCategories.toArray(categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        mInputCategory.setAdapter(adapter);
        mInputCategory.setThreshold(1);
    }

    public void showTimePickerDialog(View v) {
        PickersWorkHelper.mTvChosen = (TextView) v;
        PickersWorkHelper.createTimePicker();
    }

    public void showDatePickerDialog(View v) {
        PickersWorkHelper.mTvChosen = (TextView) v;
        PickersWorkHelper.createDatePicker();
    }

    public void chooseImageDialog(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            super.onActivityResult(reqCode, resultCode, intent);
            if (reqCode == SELECT_PICTURE) {
                Uri selectedImageUri = intent.getData();
                String selectedImagePath = getPath(selectedImageUri);
                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
                mGridPhotos.addView(imageView);
                Toast.makeText(this, R.string.picture_added, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void createNewTask(View v) {
        String category = mCategory.getText().toString();
        if (category.length() > 0) {
            Date[] dates = PickersWorkHelper.getAndCheckDatesFromFields(mStartDate, mStartTime, mFinishDate, mFinishTime);
            if (dates != null) {
                String summary = mSummary.getText().toString();
                int countOfImages = mGridPhotos.getChildCount();
                List<byte[]> byteImages = new LinkedList<>();
                if (countOfImages > 0) {
                    for (int i = 0; i < countOfImages; i++) {
                        ImageView image = (ImageView) mGridPhotos.getChildAt(i);
                        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                        byteImages.add(DatabaseHelper.DbBitmapUtility.getBytes(bitmap));
                    }
                }
                String duration = PickersWorkHelper.getDateDiff(dates[0], dates[1], TimeUnit.MINUTES);
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                String[] datesStr = PickersWorkHelper.formatDates(dates);
                long id = dbHelper.insertTask(category,  datesStr[0],  datesStr[1], summary, byteImages, duration);
                if (id > 0) {
                    setResult(Activity.RESULT_OK, null);
                    finish();
                }
            }
        } else {
            Toast.makeText(this, R.string.error_category, Toast.LENGTH_SHORT).show();
        }
    }
}
