package com.example.pedometer;

import java.util.Calendar;

public class TimeAgo {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(int steps) {

        Calendar now = Calendar.getInstance();
        Calendar StepsTime = Calendar.getInstance();
        StepsTime.add(Calendar.MINUTE,-steps);
       final long diff = now.getTimeInMillis() - StepsTime.getTimeInMillis();



        if (StepsTime.getTimeInMillis() > now.getTimeInMillis() || StepsTime.getTimeInMillis() <= 0) {
            return null;
        }

        if (diff < MINUTE_MILLIS) {
            return "الان";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "دقيقة واحدة";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " دقيقة ";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "ساعة واحدة";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " ساعات ";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "يوم واحد";
        } else {
            return diff / DAY_MILLIS + " يوم ";
        }
    }
}