package com.github.skoryupina.planyourdaytoday;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskDetails extends AppCompatActivity {
    private TaskItem taskItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        requestData();
        fillTextViews();
        fillImages();
    }

    private void requestData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            taskItem = new TaskItem();
            taskItem.id = extras.getString(getString(R.string.task_id_details));
            taskItem.category = extras.getString(getString(R.string.task_caterory_details));
            taskItem.duration = extras.getString(getString(R.string.task_duration_details));
            taskItem.description = extras.getString(getString(R.string.task_summary_details));
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            dbHelper.getTask(taskItem);
        }
    }

    private void fillTextViews(){
        TextView tvCategory = (TextView)findViewById(R.id.inputCategory);
        TextView tvFromDate = (TextView)findViewById(R.id.inputStartDate);
        TextView tvToDate = (TextView)findViewById(R.id.inputFinishDate);
        TextView tvDuration = (TextView)findViewById(R.id.tvDuration);
        TextView tvSummary = (TextView)findViewById(R.id.inputSummary);

        tvCategory.append(": " + taskItem.category);
        tvFromDate.append(": " + taskItem.startDateTime);
        tvToDate.append(": " + taskItem.endDateTime);
        tvDuration.append(": " + taskItem.duration);
        tvSummary.append(": " + taskItem.description);
    }

    private void fillImages(){
        //grid
        GridLayout mGridPhotos = (GridLayout) findViewById(R.id.gridPhotos);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.getImages(taskItem);
        if (taskItem.images!=null) {
            for (byte[] img : taskItem.images) {
                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(DatabaseHelper.DbBitmapUtility.getImage(img));
                imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
                mGridPhotos.addView(imageView);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_details, menu);
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
