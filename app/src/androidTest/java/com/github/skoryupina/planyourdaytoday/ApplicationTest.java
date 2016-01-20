package com.github.skoryupina.planyourdaytoday;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            //String request = SchedulerContract.SQL_SELECT_AMOUNT_STATISTICS_TASKS_BY_MONTH.replaceAll("[?]", String.valueOf(month));
            //Cursor cursor = db.rawQuery(request, null);

            Cursor cursor1 = db.rawQuery(SchedulerContract.SQL_TEST1, null);

            if (cursor1.moveToFirst())
                do {
                    String category = cursor1.getString(0);


                } while (cursor1.moveToNext());
        }
        catch (Exception e){
            e.getMessage();
        }
    }
}