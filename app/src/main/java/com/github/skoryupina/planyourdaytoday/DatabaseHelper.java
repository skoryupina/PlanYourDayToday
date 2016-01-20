package com.github.skoryupina.planyourdaytoday;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.skoryupina.planyourdaytoday.SchedulerContract.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseHelper extends SQLiteOpenHelper {
    //for debug
    private static final String LOG = "LOG";
    private static final String HOURS = " h";
    private static Context mContext;


    public DatabaseHelper(Context context) {
        super(context, SchedulerContract.DATABASE_NAME, null, SchedulerContract.DATABASE_VERSION);
        mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SchedulerContract.SQL_CREATE_PHOTOS);
            db.execSQL(SchedulerContract.SQL_CREATE_RECORDS);
            db.execSQL(SchedulerContract.SQL_CREATE_CATEGORIES);
        } catch (SQLException e) {
            Log.d(LOG, e.getMessage());
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SchedulerContract.SQL_DELETE_PHOTOS);
        db.execSQL(SchedulerContract.SQL_DELETE_RECORDS);
        db.execSQL(SchedulerContract.SQL_DELETE_CATEGORIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public ArrayList<String> getCategories() {
        ArrayList<String> listOfCategories = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SchedulerContract.SQL_SELECT_CATEGORIES, null);
        if (cursor.moveToFirst())
            do {
                String nameOfCategory = cursor.getString(cursor.getColumnIndexOrThrow(Category.CATEGORY));
                listOfCategories.add(nameOfCategory);
            } while (cursor.moveToNext());
        cursor.close();
        return listOfCategories;
    }

    public HashMap<String, Float> getCategoriesStatisticForChart() {
        final int DURATION_INDEX = 1;
        HashMap<String, Float> allCategoriesStatistics = new HashMap<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(SchedulerContract.SQL_SELECT_DURATION_FOR_CHART, null);
            if (cursor.moveToFirst())
                do {
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(Category.CATEGORY));
                    Float duration = cursor.getFloat(DURATION_INDEX);
                    allCategoriesStatistics.put(category, duration);
                } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
        }
        return allCategoriesStatistics;
    }


    /***
     * Get top categories statistics by amount of tasks criteria
     *
     * @param month
     * @return
     */
    public String[] getStatisticsByAmount(String month) {
        String request = SchedulerContract.SQL_SELECT_AMOUNT_STATISTICS_TASKS_BY_MONTH.replaceAll("[?]", month);
        return processCursor(request);
    }

    /***
     * Get top categories statistics by amount of tasks criteria
     *
     * @param from
     * @param to
     * @return
     */
    public String[] getStatisticsByAmount(String from, String to) {
        String request = SchedulerContract.SQL_SELECT_AMOUNT_STATISTICS_TASKS_BY_INTERVAL
                .replaceFirst("[?]", from)
                .replaceFirst("[?]", to);
        return processCursor(request);
    }

    /***
     * Get top categories statistics by duration criteria
     *
     * @param month
     * @return
     */
    public String[] getStatisticsByDuration(String month) {
        String request = SchedulerContract.SQL_SELECT_DURATION_STATISTICS_TASKS_BY_MONTH.replaceAll("[?]", month);
        return processCursor(request);
    }


    /***
     * Get top categories statistics by duration criteria
     *
     * @param from
     * @param to
     * @return
     */
    public String[] getStatisticsByDuration(String from, String to) {
        String request = SchedulerContract.SQL_SELECT_DURATION_STATISTICS_TASKS_BY_INTERVAL
                .replaceFirst("[?]", from)
                .replaceFirst("[?]", to);
        return processCursor(request);
    }

    /***
     * Get top categories statistics by duration criteria
     *
     * @param month
     * @return
     */
    public String[] getStatisticsByChoose(String month, HashMap<String, Float> mapList) {
        String categoriesSet = prepareCategoriesForRequest(mapList.keySet());
        String request = SchedulerContract.SQL_SELECT_CHOSEN_STATISTICS_TASKS_BY_MONTH
                .replaceFirst("[?]", month)
                .replaceFirst("[?]", month)
                .replaceFirst("[?]", categoriesSet);
        return processCursorChosenCategories(request, mapList);
    }


    /***
     * Get top categories statistics by duration criteria
     *
     * @param from
     * @param to
     * @return
     */
    public String[] getStatisticsByChoose(String from, String to, HashMap<String, Float> mapList) {
        String categoriesSet = prepareCategoriesForRequest(mapList.keySet());
        String request = SchedulerContract.SQL_SELECT_CHOSEN_STATISTICS_TASKS_BY_INTERVAL
                .replaceFirst("[?]", from)
                .replaceFirst("[?]", to)
                .replaceFirst("[?]", categoriesSet);
        return processCursorChosenCategories(request, mapList);
    }

    private String prepareCategoriesForRequest(Set<String> categories) {
        StringBuilder result = new StringBuilder();
        for (String s : categories) {
            result.append("\'").append(s).append("\'").append(",");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }


    private String[] processCursorChosenCategories(String request, HashMap<String, Float> mapList) {
        try {
            final int INDEX = 1;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(request, null);
            if (cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(Category.CATEGORY));
                    float count = cursor.getFloat(INDEX);
                    mapList.put(category, count);
                } while (cursor.moveToNext());
                cursor.close();
                return formatResults(mapList);
            } else {
                cursor.close();
                return null;
            }
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private String[] formatResults(HashMap<String, Float> mapList) {
        String[] result = new String[mapList.size()];
        Iterator it = mapList.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry<String, Float> entry = (Map.Entry) it.next();
            String tab = mContext.getString(R.string.tab);
            result[i++] = entry.getKey() + tab + entry.getValue().toString() + " hour(s)";
        }
        return result;
    }


    private String[] processCursor(String request) {
        try {
            final int INDEX = 1;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(request, null);
            if (cursor.moveToFirst()) {
                ArrayList<String> displayedData = new ArrayList<>();
                do {
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(Category.CATEGORY));
                    String count = cursor.getString(INDEX);
                    int length = mContext.getString(R.string.tab).length() - 1;
                    displayedData.add(category + mContext.getString(R.string.tab) + count);
                } while (cursor.moveToNext());
                cursor.close();
                return displayedData.toArray(new String[displayedData.size()]);
            } else {
                cursor.close();
                return null;
            }
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public ArrayList<TaskItem> getTasks() {
        ArrayList<TaskItem> listOfTasks = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(SchedulerContract.SQL_SELECT_TASKS_GENERAL, null);
            if (cursor.moveToFirst())
                do {
                    TaskItem taskItem = new TaskItem();
                    taskItem.id = cursor.getString(cursor.getColumnIndexOrThrow(Record._ID));
                    taskItem.category = cursor.getString(cursor.getColumnIndexOrThrow(Category.CATEGORY));
                    taskItem.description = cursor.getString(cursor.getColumnIndexOrThrow(Record.DESCRIPTION));
                    taskItem.duration = cursor.getString(cursor.getColumnIndexOrThrow(Record.DURATION)) + HOURS;
                    listOfTasks.add(taskItem);
                } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
        }
        return listOfTasks;
    }

    public void getTask(TaskItem taskItem) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String request = SchedulerContract.SQL_SELECT_RECORD_FULL_INFO
                    .replaceFirst("[?]", taskItem.id);
            Cursor cursor = db.rawQuery(request, null);
            if (cursor.moveToFirst()) {
                taskItem.startDateTime = cursor.getString(cursor.getColumnIndexOrThrow(Record.STARTDATETIME));
                taskItem.endDateTime = cursor.getString(cursor.getColumnIndexOrThrow(Record.FINISHDATETIME));
            }
            cursor.close();

        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
        }
    }

    public void getImages(TaskItem taskItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        String request = SchedulerContract.SQL_SELECT_RECORD_IMAGES
                .replaceFirst("[?]", taskItem.id);
        Cursor cursor = db.rawQuery(request, null);
        if (cursor.moveToFirst()) {
            taskItem.images = new LinkedList<>();
            do {
            taskItem.images.add(cursor.getBlob(cursor.getColumnIndexOrThrow(Photo.IMAGE)));
            } while (cursor.moveToNext());
        }
    }

    public long insertCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Category.CATEGORY, category);
        long newRowId = db.insert(
                Category.TABLE_NAME,
                null,
                values);

        return newRowId;
    }

    public long insertTask(String category, String startDateTime, String finishDateTime, String summary, List<byte[]> images, String duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        long categoryId;
        long newRowId = -1;
        try {
            Cursor cursor = db.rawQuery(SchedulerContract.SQL_SELECT_TASK_CATEGORY + "\'" + category + "\'", null);
            if (cursor.moveToFirst()) {
                categoryId = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Category._ID)));
            } else {
                categoryId = insertCategory(category);
            }

            ContentValues values = new ContentValues();

            values.put(Record.STARTDATETIME, startDateTime);
            values.put(Record.FINISHDATETIME, finishDateTime);
            values.put(Record.CATEGORY, categoryId);
            values.put(Record.DESCRIPTION, summary);
            values.put(Record.DURATION, duration);

            newRowId = db.insert(
                    Record.TABLE_NAME,
                    null,
                    values);
            int taskId = (int) newRowId;
            //insert Images
            if (images.size() > 0) {
                insertImages(db, taskId, images);
            }
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
        }
        return newRowId;
    }

    private void insertImages(SQLiteDatabase db, int taskId, List<byte[]> images) {
        try {
            for (byte[] image : images) {
                ContentValues values = new ContentValues();
                values.put(Photo.RECORD, taskId);
                values.put(Photo.IMAGE, image);
                long newRowId = db.insert(
                        Photo.TABLE_NAME,
                        null,
                        values);
            }
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
        }
    }

    public void deleteCategory(String name) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String request = SchedulerContract.SQL_DELETE_CATEGORY_RECORDS_IMAGES.replaceAll("[?]", name);
            db.execSQL(request);
            request = SchedulerContract.SQL_DELETE_CATEGORY_RECORDS
                    .replaceAll("[?]", name);
            db.execSQL(request);
            request = SchedulerContract.SQL_DELETE_CATEGORY
                    .replaceAll("[?]", name);
            db.execSQL(request);
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
        }
    }

    public void deleteTask(String id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String request = SchedulerContract.SQL_DELETE_RECORD_IMAGES.replaceAll("[?]", id);
            db.execSQL(request);
            request = SchedulerContract.SQL_DELETE_RECORD.replaceAll("[?]", id);
            db.execSQL(request);
        } catch (Exception e) {
            Log.d(LOG, e.getMessage());
        }
    }

    public void deletePhotosForTask(){

    }


    public static class DbBitmapUtility {
        // convert from bitmap to byte array
        public static byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert from byte array to bitmap
        public static Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }
}


