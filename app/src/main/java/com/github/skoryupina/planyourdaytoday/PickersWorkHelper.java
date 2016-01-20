package com.github.skoryupina.planyourdaytoday;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PickersWorkHelper {
    public static TextView mTvChosen;
    public static FragmentManager mFragmentManager;
    public static Context mContext;
    private static Calendar mStartDateTime = Calendar.getInstance();
    private static Calendar mFinishDateTime = Calendar.getInstance();

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm";

    public static void createDatePicker(){
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(mFragmentManager, "datePicker");
    }

    public static void createTimePicker(){
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(mFragmentManager, "timePicker");;
    }
    //Dates
    public static String[] getAndCheckDatesFromFields(TextView tvfromDate, TextView tvtoDate) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_FORMAT);
        Date startDate = mStartDateTime.getTime();
        Date finishDate = mFinishDateTime.getTime();
        if (finishDate.before(startDate)) {
            tvfromDate.setBackgroundColor(Color.RED);
            tvtoDate.setBackgroundColor(Color.RED);
            return null;
        }
        else {
            return formatDates(new Date[]{startDate,finishDate});
        }
    }

    public static Date[] getAndCheckDatesFromFields(TextView tvstartDate, TextView tvstartTime,TextView tvfinishDate,TextView tvfinishTime) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date startDate = mStartDateTime.getTime();
        Date finishDate = mFinishDateTime.getTime();
        if (finishDate.before(startDate) | finishDate.equals(startDate)) {
            Toast.makeText(mContext, R.string.error_date_time, Toast.LENGTH_SHORT).show();
            tvstartDate.setBackgroundColor(Color.RED);
            tvstartTime.setBackgroundColor(Color.RED);
            tvfinishDate.setBackgroundColor(Color.RED);
            tvfinishTime.setBackgroundColor(Color.RED);
            return null;
        } else {
            return new Date[]{
                    startDate, finishDate
            };
        }
    }

    public static String[] formatDates(Date[] dates){
        String[] datesStr = new String[2];
        datesStr[0] = new SimpleDateFormat(DATE_TIME_FORMAT).format(dates[0]);
        datesStr[1] = new SimpleDateFormat(DATE_TIME_FORMAT).format(dates[1]);
        return datesStr;
    }


    public static String getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        BigDecimal x = new BigDecimal(((double) timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS) / 60));
        x = x.setScale(1, BigDecimal.ROUND_HALF_UP);
        return x.toString();
    }

    public static void setDataInInputField(TextView tv, int day, int month, int year) {
        switch (tv.getId()) {
            case R.id.inputStartDate: {
                mStartDateTime.set(Calendar.DAY_OF_MONTH, day);
                mStartDateTime.set(Calendar.MONTH, month);
                mStartDateTime.set(Calendar.YEAR, year);
                tv.setText(new SimpleDateFormat(DATE_FORMAT).format(mStartDateTime.getTime()));
            }
            break;
            case R.id.inputFinishDate: {
                mFinishDateTime.set(Calendar.DAY_OF_MONTH, day);
                mFinishDateTime.set(Calendar.MONTH, month);
                mFinishDateTime.set(Calendar.YEAR, year);
                tv.setText(new SimpleDateFormat(DATE_FORMAT).format(mFinishDateTime.getTime()));
            }
            break;
        }
    }

    public static void setTimeInInputField(TextView tv, int hours, int min) {
        switch (tv.getId()) {
            case R.id.inputStartTime: {
                mStartDateTime.set(Calendar.HOUR_OF_DAY, hours);
                mStartDateTime.set(Calendar.MINUTE, min);
                tv.setText(new SimpleDateFormat(TIME_FORMAT).format(mStartDateTime.getTime()));
            }
            break;
            case R.id.inputFinishTime: {
                mFinishDateTime.set(Calendar.HOUR_OF_DAY, hours);
                mFinishDateTime.set(Calendar.MINUTE, min);
                tv.setText(new SimpleDateFormat(TIME_FORMAT).format(mFinishDateTime.getTime()));
            }
            break;
        }
    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);

        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            setDataInInputField(mTvChosen, day, month, year);
            mTvChosen.setBackgroundColor(Color.WHITE);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            setTimeInInputField(mTvChosen, hourOfDay, minute);
            mTvChosen.setBackgroundColor(Color.WHITE);
        }
    }
}
