package com.example.pedometer.helper;

import java.util.Calendar;

public class TimeHelper {

    public static String convertMilliesToString(long dateWithoutTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateWithoutTime);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH)+1;
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        return mDay +"/"+mMonth+"/"+mYear;
    }


     public static long getTodayDate() {
        Calendar mCalendar = Calendar.getInstance();

        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        long dateWithoutTime = mCalendar.getTime().getTime();
        return dateWithoutTime;
    }
}
