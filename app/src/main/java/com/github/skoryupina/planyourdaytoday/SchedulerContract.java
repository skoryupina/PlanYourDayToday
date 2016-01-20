package com.github.skoryupina.planyourdaytoday;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.HashMap;

public final class SchedulerContract {
    //for debug
    private static final String LOG = "LOG";

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Scheduler";
    private static final String DATETIME_TYPE = " DATETIME ";
    private static final String TEXT_TYPE = " TEXT ";
    private static final String REAL_TYPE = " REAL ";
    private static final String INT_TYPE = " INTEGER ";
    private static final String FK = " FOREIGN KEY ";
    private static final String REF = " REFERENCES ";
    private static final String BLOB = " BLOB ";
    private static final String COMMA_SEP = ", ";

    public static final String SQL_CREATE_CATEGORIES =
            "CREATE TABLE " + Category.TABLE_NAME + " (" +
                    Category._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Category.CATEGORY + TEXT_TYPE + " NOT NULL UNIQUE)";

    public static final String SQL_CREATE_PHOTOS =
            "CREATE TABLE " + Photo.TABLE_NAME + " (" +
                    Photo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Photo.IMAGE + BLOB + COMMA_SEP +
                    Photo.RECORD + INT_TYPE + COMMA_SEP +
                    FK + " (" + Photo.RECORD + ") " + REF + Record.TABLE_NAME + " (" + Record._ID + "))";

    public static final String SQL_CREATE_RECORDS =
            "CREATE TABLE " + Record.TABLE_NAME + " (" +
                    Record._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Record.STARTDATETIME + DATETIME_TYPE + COMMA_SEP +
                    Record.FINISHDATETIME + DATETIME_TYPE + COMMA_SEP +
                    Record.CATEGORY + INT_TYPE + COMMA_SEP +
                    Record.DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    Record.DURATION + REAL_TYPE + COMMA_SEP +
                    FK + " (" + Record.CATEGORY + ") " + REF + Category.TABLE_NAME + " (" + Category._ID + "))";

    public static final String SQL_DELETE_CATEGORIES =
            "DROP TABLE IF EXISTS " + Category.TABLE_NAME + ";";

    public static final String SQL_DELETE_PHOTOS =
            "DROP TABLE IF EXISTS " + Photo.TABLE_NAME + ";";

    public static final String SQL_DELETE_RECORDS =
            "DROP TABLE IF EXISTS " + Record.TABLE_NAME + ";";

    public static final String SQL_SELECT_CATEGORIES = "SELECT " + Category.CATEGORY + " FROM " + Category.TABLE_NAME;

    public static final String SQL_SELECT_TASK_CATEGORY = "SELECT " + Category._ID + " FROM " + Category.TABLE_NAME
            + " WHERE " + Category.CATEGORY + " = ";

    public static final String SQL_SELECT_TASKS_GENERAL = "SELECT r." + Record._ID + COMMA_SEP + " c." + Category.CATEGORY + COMMA_SEP + " r." + Record.DURATION + COMMA_SEP
            + " r." + Record.DESCRIPTION + " FROM " + Record.TABLE_NAME + " r " + " JOIN " + Category.TABLE_NAME + " c " +
            " ON " + "r." + Record.CATEGORY + " = " + "c." + Category._ID;

    public static final String SQL_SELECT_DURATION_FOR_CHART = "SELECT  c." + Category.CATEGORY + COMMA_SEP + "SUM (r." + Record.DURATION + ")" +
            " FROM " + Category.TABLE_NAME + " c " + " JOIN " + Record.TABLE_NAME + " r " +
            " ON " + "c." + Category._ID + " = " + "r." + Record.CATEGORY + " GROUP BY r." + Record.CATEGORY;

    public static final String SQL_SELECT_AMOUNT_STATISTICS_TASKS_BY_MONTH = "SELECT c." + Category.CATEGORY + COMMA_SEP + "COUNT (r." + Record.CATEGORY + ") AS COUNT " +
            " FROM " + Category.TABLE_NAME + " c " + "JOIN " + Record.TABLE_NAME + " r " +
            " ON " + "c." + Category._ID + " = " + "r." + Record.CATEGORY +
            " WHERE (strftime('%m', r." + Record.STARTDATETIME + ")='?') AND (strftime('%m', r." + Record.FINISHDATETIME + ")='?') " +
            " GROUP BY r." + Record.CATEGORY +
            " ORDER BY COUNT DESC LIMIT 5";

    public static final String SQL_SELECT_AMOUNT_STATISTICS_TASKS_BY_INTERVAL = "SELECT c." + Category.CATEGORY + COMMA_SEP + "COUNT (r." + Record.CATEGORY + ") AS COUNT " +
            " FROM " + Category.TABLE_NAME + " c " + "JOIN " + Record.TABLE_NAME + " r " +
            " ON " + "c." + Category._ID + " = " + "r." + Record.CATEGORY +
            " WHERE (strftime('%Y-%m-%d', r." + Record.STARTDATETIME + ")>='?') AND (strftime('%Y-%m-%d', r." + Record.FINISHDATETIME + ")<='?') " +
            " GROUP BY r." + Record.CATEGORY +
            " ORDER BY COUNT DESC LIMIT 5";

    public static final String SQL_SELECT_DURATION_STATISTICS_TASKS_BY_MONTH = "SELECT c." + Category.CATEGORY + COMMA_SEP + "SUM (r." + Record.DURATION + ") AS SUM " +
            " FROM " + Category.TABLE_NAME + " c " + "JOIN " + Record.TABLE_NAME + " r " +
            " ON " + "c." + Category._ID + " = " + "r." + Record.CATEGORY +
            " WHERE (strftime('%m', r." + Record.STARTDATETIME + ")='?') AND (strftime('%m', r." + Record.FINISHDATETIME + ")='?') " +
            " GROUP BY r." + Record.CATEGORY +
            " ORDER BY SUM DESC LIMIT 5";

    public static final String SQL_SELECT_DURATION_STATISTICS_TASKS_BY_INTERVAL = "SELECT c." + Category.CATEGORY + COMMA_SEP + "SUM (r." + Record.DURATION + ") AS SUM " +
            " FROM " + Category.TABLE_NAME + " c " + "JOIN " + Record.TABLE_NAME + " r " +
            " ON " + "c." + Category._ID + " = " + "r." + Record.CATEGORY +
            " WHERE (strftime('%Y-%m-%d', r." + Record.STARTDATETIME + ")>='?') AND (strftime('%Y-%m-%d', r." + Record.FINISHDATETIME + ")<='?') " +
            " GROUP BY r." + Record.CATEGORY +
            " ORDER BY SUM DESC LIMIT 5";

    public static final String SQL_SELECT_CHOSEN_STATISTICS_TASKS_BY_MONTH = "SELECT c." + Category.CATEGORY + COMMA_SEP + "SUM (r." + Record.DURATION + ") AS SUM " +
            " FROM " + Category.TABLE_NAME + " c " + "JOIN " + Record.TABLE_NAME + " r " +
            " ON " + "c." + Category._ID + " = " + "r." + Record.CATEGORY +
            " WHERE (strftime('%m', r." + Record.STARTDATETIME + ")='?') AND (strftime('%m', r." + Record.FINISHDATETIME + ")='?') " +
            " AND c." + Category.CATEGORY + " IN " + " (?) " +
            "GROUP BY r." + Record.CATEGORY +
            " ORDER BY SUM DESC";

    public static final String SQL_SELECT_CHOSEN_STATISTICS_TASKS_BY_INTERVAL = "SELECT c." + Category.CATEGORY + COMMA_SEP + "SUM (r." + Record.DURATION + ") AS SUM " +
            " FROM " + Category.TABLE_NAME + " c " + "JOIN " + Record.TABLE_NAME + " r " +
            " ON " + "c." + Category._ID + " = " + "r." + Record.CATEGORY +
            " WHERE (strftime('%Y-%m-%d', r." + Record.STARTDATETIME + ")>='?') AND (strftime('%Y-%m-%d', r." + Record.FINISHDATETIME + ")<='?') " +
            " AND c." + Category.CATEGORY + " IN " + " (?) " +
            " GROUP BY r." + Record.CATEGORY +
            " ORDER BY SUM DESC";

    public static final String SQL_SELECT_RECORD_FULL_INFO = "SELECT r." + Record.STARTDATETIME + COMMA_SEP +
            " r." + Record.FINISHDATETIME + COMMA_SEP + " r." + Record.DURATION +
            " FROM " + Record.TABLE_NAME + " r "+
            " WHERE r." + Record._ID + "='?'";

    public static final String SQL_SELECT_RECORD_IMAGES = "SELECT photo." + Photo.IMAGE +
            " FROM " + Photo.TABLE_NAME + " "+
            " WHERE photo." + Photo.RECORD + "='?'";

    /***
     * When task deleted
     */
    public static final String SQL_DELETE_RECORD_IMAGES = "DELETE " +
            " FROM " + Photo.TABLE_NAME + " "+
            " WHERE photo." + Photo.RECORD + "='?'";

    /***
     * When category deleted select deleting records
     */
    public static final String SQL_SELECT_DELETING_CATEGORY_RECORDS = "SELECT record." + Record._ID +
            " FROM " + Record.TABLE_NAME + " "+
            " WHERE record." + Record.CATEGORY + "= (" + SQL_SELECT_TASK_CATEGORY + "'?')";

    /***
     * When category deleted - deleting images
     */
    public static final String SQL_DELETE_CATEGORY_RECORDS_IMAGES =
            "DELETE FROM photo WHERE  photo." + Photo.RECORD + " IN (" + SQL_SELECT_DELETING_CATEGORY_RECORDS +")";

    /***
     * When category deleted - deleting records
     */
    public static final String SQL_DELETE_CATEGORY_RECORDS =
            "DELETE FROM record WHERE  record." + Record.CATEGORY + "= (" + SQL_SELECT_TASK_CATEGORY + "'?')";

    public static final String SQL_DELETE_CATEGORY =
            "DELETE FROM category WHERE " + Category.CATEGORY + " = '?'";

    public static final String SQL_DELETE_RECORD =
            "DELETE FROM record WHERE  record." + Record._ID + " = '?'";

    public SchedulerContract() {
    }

    public static abstract class Record implements BaseColumns {
        public static final String TABLE_NAME = "record";
        //public static final String ENTRY_ID = "id";
        public static final String STARTDATETIME = "startDateTime";
        public static final String FINISHDATETIME = "finishDateTime";
        public static final String CATEGORY = "category";
        public static final String DESCRIPTION = "description";
        public static final String DURATION = "duration";
    }

    public static abstract class Category implements BaseColumns {
        public static final String TABLE_NAME = "category";
        //public static final String ENTRY_ID = "id";
        public static final String CATEGORY = "name";
    }

    public static abstract class Photo implements BaseColumns {
        public static final String TABLE_NAME = "photo";
        //public static final String ENTRY_ID = "id";
        public static final String IMAGE = "image";
        public static final String RECORD = "record";
    }
}
