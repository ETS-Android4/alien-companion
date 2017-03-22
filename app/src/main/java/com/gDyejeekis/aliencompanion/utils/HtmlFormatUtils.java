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

    private TableLayout formatTable(Context context, String text, String subreddit, View.OnClickListener click, View.OnLongClickListener longClick) {
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableLayout table = new TableLayout(context);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        table.setLayoutParams(params);

        final String tableStart = "<table>";
        final String tableEnd = "</table>";
        final String tableHeadStart = "<thead>";
        final String tableHeadEnd = "</thead>";
        final String tableRowStart = "<tr>";
        final String tableRowEnd = "</tr>";
        final String tableColumnStart = "<td>";
        final String tableColumnEnd = "</td>";
        final String tableColumnStartLeft = "<td align=\"left\">";
        final String tableColumnStartRight = "<td align=\"right\">";
        final String tableColumnStartCenter = "<td align=\"center\">";
        final String tableHeaderStart = "<th>";
        final String tableHeaderStartLeft = "<th align=\"left\">";
        final String tableHeaderStartRight = "<th align=\"right\">";
        final String tableHeaderStartCenter = "<th align=\"center\">";
        final String tableHeaderEnd = "</th>";

        int i = 0;
        int columnStart = 0;
        int columnEnd;
        int gravity = Gravity.START;
        boolean columnStarted = false;

        TableRow row = null;

        while (i < text.length()) {
            if (text.charAt(i) != '<') { // quick check otherwise it falls through to else
                i += 1;
            } else if (text.subSequence(i, i + tableStart.length()).toString().equals(tableStart)) {
                i += tableStart.length();
            } else if (text.subSequence(i, i + tableHeadStart.length()).toString().equals(tableHeadStart)) {
                i += tableHeadStart.length();
            } else if (text.subSequence(i, i + tableRowStart.length()).toString().equals(tableRowStart)) {
                row = new TableRow(context);
                row.setLayoutParams(rowParams);
                i += tableRowStart.length();
            } else if (text.subSequence(i, i + tableRowEnd.length()).toString().equals(tableRowEnd)) {
                table.addView(row);
                i += tableRowEnd.length();
            } else if (text.subSequence(i, i + tableEnd.length()).toString().equals(tableEnd)) {
                i += tableEnd.length();
            } else if (text.subSequence(i, i + tableHeadEnd.length()).toString().equals(tableHeadEnd)) {
                i += tableHeadEnd.length();
            } else if (!columnStarted && i + tableColumnStart.length() < text.length()
                    && (text.subSequence(i, i + tableColumnStart.length()).toString().equals(tableColumnStart)
                    || text.subSequence(i, i + tableHeaderStart.length()).toString().equals(tableHeaderStart))) {
                columnStarted = true;
                gravity = Gravity.START;
                i += tableColumnStart.length();
                columnStart = i;
            } else if (!columnStarted && i + tableColumnStartRight.length() < text.length()
                    && (text.subSequence(i, i + tableColumnStartRight.length()).toString().equals(tableColumnStartRight)
                    || text.subSequence(i, i + tableHeaderStartRight.length()).toString().equals(tableHeaderStartRight))) {
                columnStarted = true;
                gravity = Gravity.END;
                i += tableColumnStartRight.length();
                columnStart = i;
            } else if (!columnStarted && i + tableColumnStartCenter.length() < text.length()
                    && (text.subSequence(i, i + tableColumnStartCenter.length()).toString().equals(tableColumnStartCenter)
                    || text.subSequence(i, i + tableHeaderStartCenter.length()).toString().equals(tableHeaderStartCenter))) {
                columnStarted = true;
                gravity = Gravity.CENTER;
                i += tableColumnStartCenter.length();
                columnStart = i;
            } else if (!columnStarted && i + tableColumnStartLeft.length() < text.length()
                    && (text.subSequence(i, i + tableColumnStartLeft.length()).toString().equals(tableColumnStartLeft)
                    || text.subSequence(i, i + tableHeaderStartLeft.length()).toString().equals(tableHeaderStartLeft))) {
                columnStarted = true;
                gravity = Gravity.START;
                i += tableColumnStartLeft.length();
                columnStart = i;
            } else if (text.substring(i).startsWith("<td")) {
                // case for <td colspan="2"  align="left">
                // See last table in https://www.reddit.com/r/GlobalOffensive/comments/51s3r8/virtuspro_vs_vgcyberzen_sl_ileague_s2_finals/
                columnStarted = true;
                i += text.substring(i).indexOf(">") + 1;
                columnStart = i;
            } else if (text.subSequence(i, i + tableColumnEnd.length()).toString().equals(tableColumnEnd)
                    || text.subSequence(i, i + tableHeaderEnd.length()).toString().equals(tableHeaderEnd)) {
                columnEnd = i;

                TextView textView = new TextView(context);
                // TODO: 3/22/2017
                //textView.setTextHtml(text.subSequence(columnStart, columnEnd), subreddit);
                //setStyle(textView, subreddit);
                final ViewGroup.MarginLayoutParams COLUMN_PARAMS = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                COLUMN_PARAMS.setMargins(0, 0, 32, 0);
                textView.setLayoutParams(COLUMN_PARAMS);
                textView.setGravity(gravity);
                if(click != null)
                    textView.setOnClickListener(click);
                if(longClick != null)
                    textView.setOnLongClickListener(longClick);
                if(text.subSequence(i, i + tableHeaderEnd.length()).toString().equals(tableHeaderEnd)){
                    textView.setTypeface(null, Typeface.BOLD);
                }
                if (row != null) {
                    row.addView(textView);
                }

                columnStart = 0;
                columnStarted = false;
                i += tableColumnEnd.length();
            } else {
                i += 1;
            }
        }

        return table;
    }

}
