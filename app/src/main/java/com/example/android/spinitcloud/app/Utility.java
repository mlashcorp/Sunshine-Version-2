/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.spinitcloud.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {


    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(com.example.android.spinitcloud.app.R.string.pref_units_key),
                context.getString(com.example.android.spinitcloud.app.R.string.pref_units_metric))
                .equals(context.getString(com.example.android.spinitcloud.app.R.string.pref_units_metric));
    }


    static String formatDate(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(com.example.android.spinitcloud.app.R.string.today);
            int formatId = com.example.android.spinitcloud.app.R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(com.example.android.spinitcloud.app.R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(com.example.android.spinitcloud.app.R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }



    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param assay_type from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForAssayType(int assay_type) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (assay_type >= 200 && assay_type <= 232) {
            return com.example.android.spinitcloud.app.R.drawable.ic_storm;
        } else if (assay_type >= 300 && assay_type <= 321) {
            return com.example.android.spinitcloud.app.R.drawable.ic_light_rain;
        } else if (assay_type >= 500 && assay_type <= 504) {
            return com.example.android.spinitcloud.app.R.drawable.ic_rain;
        } else if (assay_type == 511) {
            return com.example.android.spinitcloud.app.R.drawable.ic_snow;
        } else if (assay_type >= 520 && assay_type <= 531) {
            return com.example.android.spinitcloud.app.R.drawable.ic_rain;
        } else if (assay_type >= 600 && assay_type <= 622) {
            return com.example.android.spinitcloud.app.R.drawable.ic_snow;
        } else if (assay_type >= 701 && assay_type <= 761) {
            return com.example.android.spinitcloud.app.R.drawable.ic_fog;
        } else if (assay_type == 761 || assay_type == 781) {
            return com.example.android.spinitcloud.app.R.drawable.ic_storm;
        } else if (assay_type == 800) {
            return com.example.android.spinitcloud.app.R.drawable.ic_clear;
        } else if (assay_type == 801) {
            return com.example.android.spinitcloud.app.R.drawable.ic_light_clouds;
        } else if (assay_type >= 802 && assay_type <= 804) {
            return com.example.android.spinitcloud.app.R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param assay_type from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getArtResourceForassayType(int assay_type) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (assay_type >= 200 && assay_type <= 232) {
            return com.example.android.spinitcloud.app.R.drawable.art_storm;
        } else if (assay_type >= 300 && assay_type <= 321) {
            return com.example.android.spinitcloud.app.R.drawable.art_light_rain;
        } else if (assay_type >= 500 && assay_type <= 504) {
            return com.example.android.spinitcloud.app.R.drawable.art_rain;
        } else if (assay_type == 511) {
            return com.example.android.spinitcloud.app.R.drawable.art_snow;
        } else if (assay_type >= 520 && assay_type <= 531) {
            return com.example.android.spinitcloud.app.R.drawable.art_rain;
        } else if (assay_type >= 600 && assay_type <= 622) {
            return com.example.android.spinitcloud.app.R.drawable.art_snow;
        } else if (assay_type >= 701 && assay_type <= 761) {
            return com.example.android.spinitcloud.app.R.drawable.art_fog;
        } else if (assay_type == 761 || assay_type == 781) {
            return com.example.android.spinitcloud.app.R.drawable.art_storm;
        } else if (assay_type == 800) {
            return com.example.android.spinitcloud.app.R.drawable.art_clear;
        } else if (assay_type == 801) {
            return com.example.android.spinitcloud.app.R.drawable.art_light_clouds;
        } else if (assay_type >= 802 && assay_type <= 804) {
            return com.example.android.spinitcloud.app.R.drawable.art_clouds;
        }
        return -1;
    }
}