package com.george.redditreader.Utils;

import java.util.Date;

/**
 * Created by George on 6/19/2015.
 */
public class ConvertUtils {

    public static String getSubmissionAge(Double createdUTC) {
        Date createdDate = new Date((long) (createdUTC*1000));
        Date currentDate = new Date();
        long createdTime = createdDate.getTime();
        long currentTime = currentDate.getTime();
        long diffTime = currentTime - createdTime;
        long diffHours = diffTime / (1000 * 60 * 60);
        if(diffHours < 25 && diffHours >= 1) {
            String hrs = (diffHours == 1)? " hr":" hrs";
            return Long.toString(diffHours) + hrs + " ago"; }
        else if(diffHours < 1) {
            long diffMins = diffTime / (1000 * 60);
            String mins = (diffMins == 1)? " min":" mins";
            return Long.toString(diffMins) + mins + " ago";
        }
        else if(diffHours > 24 && diffHours < 731) {
            long diffDays = diffHours / 24;
            String days = (diffDays == 1)? " day":" days";
            return Long.toString(diffDays) + days + " ago";
        }
        else if(diffHours > 730 && diffHours < 8766) {
            long diffMonths = diffHours / 730;
            String months = (diffMonths == 1)? " month":" months";
            return Long.toString(diffMonths) + months + " ago";
        }
        else {
            long diffYears = diffHours / 8765;
            String years = (diffYears == 1)? " year":" years";
            return Long.toString(diffYears) + years + " ago";
        }
    }
}
