package com.github.skoryupina.planyourdaytoday;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoriesActivity extends AppCompatActivity {
    private ArrayAdapter mCategoriesListAdapter;
    private ListView mLvCategories;
    private int mPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        createOrUpdateListView();
        mLvCategories = (ListView)findViewById(R.id.lvCategories);
        mLvCategories.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long arg3) {
                mPosition=pos;
                new AlertDialog.Builder(CategoriesActivity.this)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle(R.string.delete_confirmation)
                        .setMessage(R.string.delete_question)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getApplicationContext(), "Category: " + mLvCategories.getItemAtPosition(mPosition).toString() + " deleted.", Toast.LENGTH_SHORT).show();
                                removeItemFromList(mPosition);
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();



               return false;
            }
        });
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_categories, menu);
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

    public void createOrUpdateListView(){
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        ArrayList<String> listOfCategories = dbHelper.getCategories();
        if (listOfCategories.size()>0) {
            String[] categories = new String[listOfCategories.size()];
            listOfCategories.toArray(categories);
            mCategoriesListAdapter = new ArrayAdapter<>(this,
                    R.layout.item_category, categories);
            ListView lvCategories = (ListView) findViewById(R.id.lvCategories);
            lvCategories.setAdapter(mCategoriesListAdapter);
        }
    }


    public void insertCategory(View v){
        EditText editTextNewCategory = (EditText)findViewById(R.id.newCategoryName);
        String newCategory = editTextNewCategory.getText().toString();
        if (newCategory.length()>0) {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            long id = dbHelper.insertCategory(newCategory);
            if (id != -1) {
                createOrUpdateListView();
            } else {
                Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show();
            }
            editTextNewCategory.setText("", null);
            //editTextNewCategory.clearFocus();
            //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(editTextNewCategory.getWindowToken(), 0);
        }
        else {
            Toast.makeText(this, R.string.empty_category, Toast.LENGTH_SHORT).show();
        }
    }

    public void removeItemFromList(int pPosition) {
        String name = mCategoriesListAdapter.getItem(pPosition).toString();
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        dbHelper.deleteCategory(name);
        createOrUpdateListView();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK, null);
        finish();
    }
}
