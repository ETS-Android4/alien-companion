package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by George on 3/22/2017.
 */

public class HtmlFormatUtils {

    public static final String TAG = "HtmlFormatUtils";

    public static String modifySpoilerHtml(String html) {
        String pattern = "<a href=\\\\?\"(?:#|\\/)(?:s|b|g|p|c|f|fear)\\\\?\" title=\\\\?\"([\\w\\s.,/#!?$%\\^&\\*;:'’\\[\\]\\{}+=\\-_`~()\"“”]*)\\\\?\">([\\w\\s.,/#!?$%\\^&\\*;:'’\\[\\]\\{}+=\\-_`~()\"“”]*)<\\/a>";

        try {
            Pattern compiledPatter = Pattern.compile(pattern);
            Matcher matcher = compiledPatter.matcher(html);

            if (matcher.find()) {
                //Log.d(TAG, "MATCH FOUND FOR TITLED SPOILERS");
                //Log.d(TAG, "Start index: " + matcher.start());
                //Log.d(TAG, " End index: " + matcher.end());
                //Log.d(TAG, " Found: " + matcher.group());

                String start = html.substring(0, matcher.start());
                String middle = html.substring(matcher.start(), matcher.end());
                String end = html.substring(matcher.end());

                middle = middle.replaceAll("href=\"(#|/)(s|b|g|p|c|f|fear)\"", "href=\"#st\"");
                middle = middle.replace("title=\"" + matcher.group(1) + "\"", "");
                String string = matcher.group(2) + "</a>";
                middle = middle.replace(string, string + "<a href=\"#s\"> " + matcher.group(1) + "</a>");

                //Log.d(TAG, " Modified: " + middle);

                html = start + middle + end;

                html = modifySpoilerHtml(html);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return html;
    }

}
