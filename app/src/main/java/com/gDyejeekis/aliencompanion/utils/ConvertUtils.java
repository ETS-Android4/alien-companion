package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.UrlOptionsDialogFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by George on 6/19/2015.
 */
public class ConvertUtils {

    public static final String TAG = "ConvertUtils";

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

    public static String getHrsMinsSecsString(int hours, int minutes, int seconds) {
        List<String> strings = new ArrayList<>();

        String hoursString = null;
        if(hours==1) {
            hoursString = "hour";
        }
        else if(hours > 1) {
            hoursString = hours + " hours";
        }
        if(hoursString!=null) {
            strings.add(hoursString);
        }

        String minutesString = null;
        if(minutes==1) {
            if(hoursString==null) {
                minutesString = "minute";
            }
            else {
                minutesString = "1 minute";
            }
        }
        else if(minutes > 1) {
            minutesString = minutes + " minutes";
        }
        if(minutesString!=null) {
            strings.add(minutesString);
        }

        String secondsString = null;
        if(seconds==1) {
            if(hoursString==null && minutesString==null) {
                secondsString = "second";
            }
            else {
                secondsString = "1 second";
            }
        }
        else if(seconds > 1) {
            secondsString = seconds + " seconds";
        }
        if(secondsString!=null) {
            strings.add(secondsString);
        }

        String string = StringUtils.join(strings.toArray(), ", ");
        int index = string.lastIndexOf(", ");
        if(index>=0) {
            string = new StringBuilder(string).replace(index, index+2, " and ").toString();
        }

        return string;
    }

    public static CharSequence noTrailingwhiteLines(CharSequence text) {
        //fromHtmlCount++;
        //Log.d("geotest", "fromhtml() executed " + fromHtmlCount + " times");
        try {
            while (text.charAt(text.length() - 1) == '\n') {
                text = text.subSequence(0, text.length() - 1);
            }
        } catch (IndexOutOfBoundsException e) {}
        return text;
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static String getDomainName(String url) throws URISyntaxException {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static String URLEncodeString(String string) {
        try {
            return URLEncoder.encode(string, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }

}
