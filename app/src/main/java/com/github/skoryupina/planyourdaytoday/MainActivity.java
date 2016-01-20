package com.github.skoryupina.planyourdaytoday;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, PopupMenu.OnMenuItemClickListener {

    //for debug
    private static final String LOG = "LOG";
    public static final int CREATE_TASK = 1;
    public static final int VIEW_CATEGORIES = 2;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    public ArrayList<TaskItem> taskItems;
    public TaskListAdapter adapter;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.plan);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        updateListView();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main_menu, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_help: {
                showPopupHelp();
            }
            break;
            case R.id.action_add: {
                startCreateTaskActivity();
            }
            break;
            case R.id.action_category:{
                startCategoriesActivity();
            }break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void startCreateTaskActivity() {
        Intent intent;
        intent = new Intent(this, NewTaskActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, CREATE_TASK);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            super.onActivityResult(reqCode, resultCode, intent);
            if (reqCode == CREATE_TASK) {
                updateListView();
                Toast.makeText(this, R.string.task_created, Toast.LENGTH_SHORT).show();
            }else if (reqCode == VIEW_CATEGORIES){
                updateListView();
            }

        }
    }

    private void startCategoriesActivity() {
        Intent intent;
        intent = new Intent(this, CategoriesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent,VIEW_CATEGORIES);
    }

    /***
     * Update view of the scheduler items list
     */
    public void updateListView() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        taskItems = dbHelper.getTasks();
        // Create the adapter to convert the array to views
        adapter = new TaskListAdapter(MainActivity.this, R.layout.item_task, taskItems);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvTasks);
        listView.setAdapter(adapter);
        adapter.setListView(listView);
    }

    public void showPopupHelp() {
        View menuItemView = findViewById(R.id.action_help);
        PopupMenu popup = new PopupMenu(this, menuItemView);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.global, popup.getMenu());
        popup.show();

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about: {
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage(R.string.about_toast)
                        .setPositiveButton("OK", null)
                        .show();
            }
            break;

            case R.id.action_exit: {
                //Ask the user if he want to quit
                createExitDialog();
            }
        }
        return true;
    }

    public void createExitDialog() {
        new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.exit_confirmation)
                .setMessage(R.string.exit_question)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Finish activity
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();

    }

/**
 * A placeholder fragment containing a simple view.
 */
public static class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
         /*   ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));*/
    }

}

}
