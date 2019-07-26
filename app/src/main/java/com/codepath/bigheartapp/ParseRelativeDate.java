package com.codepath.bigheartapp;

import android.text.format.DateUtils;

import java.util.Date;

public class ParseRelativeDate {
    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(Date rawDate) {
        // reads the data from the date of the tweet

        String relativeDate = "";

        long dateMillis = rawDate.getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                // outputs the date of the tweet as time ago
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();


        return relativeDate;
    }
}