package com.github.skoryupina.planyourdaytoday;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ekaterina on 14.12.2015.
 */
public class TaskListAdapter extends ArrayAdapter<TaskItem> {
    private int layoutResource;
    private ListView listView;
    public ArrayList<TaskItem> taskItems;
    private Context context;
    private static final String LOG = "LOG";
    private static final int SUBSTRING_START = 0;
    private static final int SUBSTRING_END = 50;

    public static class TaskItemHolder {
        public RelativeLayout mainView;
        public RelativeLayout deleteView;
        public TextView description;
        public TextView category;
        public TextView duration;
    }

    public TaskListAdapter(Context context, int layoutResource, ArrayList<TaskItem> taskItems) {
        super(context, layoutResource, taskItems);
        this.taskItems = taskItems;
        this.layoutResource = layoutResource;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        View workingView = null;
        View row = convertView;
        TaskItemHolder viewHolder;
        // Check if an existing view is being reused, otherwise inflate the view
        // view lookup cache stored in tag
        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(layoutResource, parent, false);
            viewHolder = getTaskItemHolder(row, position);
        } else {
            Log.e(LOG, "---------цикличность---------- ");
            Log.e(LOG, "конверт: " + convertView.toString());
            viewHolder = (TaskItemHolder) row.getTag();
        }
        String description = getItem(position).description;
        if (description.length() > SUBSTRING_END) {
            viewHolder.description.setText(description.substring(SUBSTRING_START, SUBSTRING_END) + "...");
        } else {
            viewHolder.description.setText(description);
        }
        // viewHolder.description.setText(getItem(position).description);

        viewHolder.category.setText(getItem(position).category);
        viewHolder.duration.setText(getItem(position).duration);

        SwipeDetector swipeDetector = new SwipeDetector(viewHolder, position);
        row.setOnTouchListener(swipeDetector);
        return row;
    }

    private TaskItemHolder getTaskItemHolder(View workingView, int position) {
        Object tag = workingView.getTag();
        TaskItemHolder holder = null;

        if (tag == null || !(tag instanceof TaskItemHolder)) {
            holder = new TaskItemHolder();
            holder.mainView = (RelativeLayout) workingView.findViewById(R.id.item_task_mainview);
            holder.deleteView = (RelativeLayout) workingView.findViewById(R.id.item_task_deleteview);

            TaskItem taskItem = getItem(position);

            holder.category = (TextView) workingView.findViewById(R.id.tvCategory);
            holder.duration = (TextView) workingView.findViewById(R.id.tvTime);
            holder.description = (TextView) workingView.findViewById(R.id.tvDescription);
            workingView.setTag(holder);

            // Populate the data into the template view using the data object
            holder.category.setText(taskItem.category);
            String description = taskItem.description;
            if (description.length() > SUBSTRING_END) {
                holder.description.setText(description.substring(SUBSTRING_START, SUBSTRING_END) + "...");
            } else {
                holder.description.setText(description);
            }
        } else {
            holder = (TaskItemHolder) tag;
        }
        return holder;
    }


    @Override
    public int getCount() {
        return taskItems.size();
    }

    @Override
    public TaskItem getItem(int position) {
        return taskItems.get(position);
    }


    public void setListView(ListView view) {
        listView = view;
        LongClickDetector longClickDetector = new LongClickDetector();
        listView.setOnItemLongClickListener(longClickDetector);
    }

    public class LongClickDetector implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                       int pos, long arg3) {
            TaskItem taskItem = getItem(pos);
            Intent intent = new Intent(getContext(), TaskDetails.class);
            intent.putExtra(context.getString(R.string.task_id_details), taskItem.id);
            intent.putExtra(context.getString(R.string.task_caterory_details), taskItem.category);
            intent.putExtra(context.getString(R.string.task_duration_details), taskItem.duration);
            intent.putExtra(context.getString(R.string.task_summary_details), taskItem.description);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getContext().startActivity(intent);
            return false;
        }
    }

    // swipe detector class here
    public class SwipeDetector implements View.OnTouchListener {

        private static final int MIN_DISTANCE = 300;
        private static final int MIN_LOCK_DISTANCE = 30; // disallow motion intercept
        private boolean motionInterceptDisallowed = false;
        private float downX, upX;
        private TaskItemHolder holder;
        private int position;

        public SwipeDetector(TaskItemHolder holder, int position) {
            this.holder = holder;
            this.position = position;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            String logTag = "LOG";
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    Log.d(logTag, "ACTION_DOWN");
                    downX = event.getX();
                    Log.d(logTag, "ACTION_DOWN_X: " + position);
                    return true; // allow other events like Click to be processed
                }

                case MotionEvent.ACTION_MOVE: {
                    Log.d(logTag, "ACTION_MOVE");
                    upX = event.getX();
                    float deltaX = downX - upX;
                    Log.d(logTag, "ACTION_MOVE: " + position);

                    if (Math.abs(deltaX) > MIN_LOCK_DISTANCE && listView != null && !motionInterceptDisallowed) {
                        listView.requestDisallowInterceptTouchEvent(true);
                        motionInterceptDisallowed = true;
                    }

                    if (deltaX > 0) {
                        holder.deleteView.setVisibility(View.GONE);
                    } else {
                        // if first swiped left and then swiped right
                        holder.deleteView.setVisibility(View.VISIBLE);
                    }

                    swipe(-(int) deltaX);
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    Log.d(logTag, "ACTION_UP");
                    upX = event.getX();
                    float deltaX = upX - downX;
                    if (deltaX > MIN_DISTANCE) {
                        // left or right
                        swipeRemove();
                    }else if (Math.abs(deltaX) > MIN_DISTANCE) {
                        TaskItem taskItem = getItem(position);
                        Intent intent = new Intent(getContext(), TaskDetails.class);
                        intent.putExtra(context.getString(R.string.task_id_details), taskItem.id);
                        intent.putExtra(context.getString(R.string.task_caterory_details), taskItem.category);
                        intent.putExtra(context.getString(R.string.task_duration_details), taskItem.duration);
                        intent.putExtra(context.getString(R.string.task_summary_details), taskItem.description);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getContext().startActivity(intent);
                        swipe(0);
                    }
                    else {
                        swipe(0);
                    }

                    if (listView != null) {
                        listView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    }

                    holder.deleteView.setVisibility(View.VISIBLE);
                    return true;
                }

                case MotionEvent.ACTION_CANCEL: {
                    Log.d(logTag, "ACTION_CANCEL");
                    holder.deleteView.setVisibility(View.VISIBLE);
                    swipe(0);
                    return false;
                }
            }

            return true;
        }

        private void swipe(int distance) {
            View animationView = holder.mainView;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
            params.rightMargin = -distance;
            params.leftMargin = distance;
            animationView.setLayoutParams(params);
        }

        private void swipeRemove() {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            dbHelper.deleteTask(getItem(position).id);
            remove(getItem(position));
            ((MainActivity) context).updateListView();
            notifyDataSetChanged();
        }
    }


}
