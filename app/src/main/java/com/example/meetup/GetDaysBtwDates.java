package com.example.meetup;

import android.app.Application;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetDaysBtwDates extends Application {
    private String getDaysBtwDates(String date1, String date2) {
        long days;

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy/MMM/dd-HH:mm:ss");

        try {
            Date dt1 = myFormat.parse(date1);
            Date dt2 = myFormat.parse(date2);

            long diff = dt2.getTime() - dt1.getTime();
            days = diff / 1000L / 60L / 60L / 24L;

            if (days == 0) {
                long diff_m = (dt2.getTime() - dt1.getTime()) / 1000 / 60;

                if (diff_m == 0) {
                    return "1m";
                } else if (diff_m >= 60) {
                    return (int) (diff_m / 60) + "h";
                } else {
                    return diff_m + "m";
                }
            } else if (days > 0 && days < 7) {
                return (int) (days) + "d";
            } else if (days >= 7 && days <= 29) {
                return (int) (days / 7) + "w";
            } else if (days >= 30 && days <= 364) {
                return (int) (days / 30) + "M";
            } else if (days >= 365) {
                return (int) (days / 365) + "y";
            }

        } catch (java.text.ParseException e) {
            //
        }
        return null;
    }
}
