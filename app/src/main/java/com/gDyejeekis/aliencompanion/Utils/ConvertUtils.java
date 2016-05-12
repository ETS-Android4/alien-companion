package com.gDyejeekis.aliencompanion.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.UrlOptionsDialogFragment;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

/**
 * Created by George on 6/19/2015.
 */
public class ConvertUtils {

    public static final String TAG = "ConvertUtils";

    //private static int fromHtmlCount = 0;

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

    //public static String getFileAge(long lastModified) {
    //    String lastSync = "";
//
//
//
    //    return lastSync;
    //}

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

    public static void preparePostsText(Context context, List<RedditItem> items) { //TODO: delete this shit
        try {
            for (RedditItem item : items) {
                if (item.getMainText() != null) {
                    //item.storePreparedText((SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(item.getMainText(), null, new MyHtmlTagHandler())));
                    item.storePreparedText(ConvertUtils.modifyURLSpan(context, item.getPreparedText()));
                }
            }
        } catch (NullPointerException e) {}
    }

    public static SpannableStringBuilder modifyURLSpan(final Context context, CharSequence sequence) {
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);

        // Get an array of URLSpan from SpannableStringBuilder object
        URLSpan[] urlSpans = strBuilder.getSpans(0, strBuilder.length(), URLSpan.class);

        // Add onClick listener for each of URLSpan object
        for (final URLSpan span : urlSpans) {
            int start = strBuilder.getSpanStart(span);
            int end = strBuilder.getSpanEnd(span);
            // The original URLSpan needs to be removed to block the behavior of browser opening
            strBuilder.removeSpan(span);

            MyClickableSpan myClickableSpan;

            if(span.getURL()!=null) {
                if (span.getURL().substring(0, 2).equals("/s") || span.getURL().equals("#s")) {
                    myClickableSpan = new MyClickableSpan() {

                        boolean spoilerHidden = true;
                        TextPaint textPaint;

                        @Override
                        public boolean onLongClick(View widget) {
                            return false;
                        }

                        @Override
                        public void onClick(View widget) {
                            spoilerHidden = !spoilerHidden;
                            updateDrawState(textPaint);
                            widget.invalidate();
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            int backgroundColor = (MyApplication.nightThemeEnabled) ? context.getResources().getColor(R.color.darker_gray) : Color.BLACK;
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            ds.bgColor = backgroundColor;
                            if (spoilerHidden) ds.setColor(backgroundColor);
                            else ds.setColor(Color.WHITE);
                            textPaint = ds;
                        }
                    };
                }
                else if(span.getURL().equals("#st")) {
                    myClickableSpan = new MyClickableSpan() {
                        @Override
                        public boolean onLongClick(View widget) {
                            return false;
                        }

                        @Override
                        public void onClick(View view) {

                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            int backgroundColor = (MyApplication.nightThemeEnabled) ? context.getResources().getColor(R.color.darker_gray) : Color.BLACK;
                            super.updateDrawState(ds);
                            ds.setUnderlineText(true);
                            ds.bgColor = backgroundColor;
                            ds.setColor(Color.YELLOW);
                        }
                    };
                }
                else {
                    myClickableSpan = new MyClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            LinkHandler linkHandler = new LinkHandler(context, span.getURL());
                            linkHandler.handleIt();
                        }

                        @Override
                        public boolean onLongClick(View v) {
                            try { //illegalstateexception is thrown if the parent activity is destroyed before the dialog can be shown
                                Bundle args = new Bundle();
                                args.putString("url", span.getURL());
                                UrlOptionsDialogFragment dialogFragment = new UrlOptionsDialogFragment();
                                dialogFragment.setArguments(args);
                                dialogFragment.show(((Activity) context).getFragmentManager(), "dialog");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(MyApplication.linkColor);
                        }
                    };
                }
                strBuilder.setSpan(myClickableSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return strBuilder;
    }

    public static SpannableStringBuilder modifyURLSpan(final Context context, CharSequence sequence,
                                                       final MyClickableSpan plainTextClickable) {
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);

        // Get an array of URLSpan from SpannableStringBuilder object
        URLSpan[] urlSpans = strBuilder.getSpans(0, strBuilder.length(), URLSpan.class);

        if(urlSpans.length==0) {
            strBuilder.setSpan(plainTextClickable, 0, strBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return strBuilder;
        }

        int plainTextSpanStart = 0;

        // Add onClick listener for each of URLSpan object
        for (final URLSpan span : urlSpans) {
            final int start = strBuilder.getSpanStart(span);
            final int end = strBuilder.getSpanEnd(span);
            // The original URLSpan needs to be removed to block the behavior of browser opening
            strBuilder.removeSpan(span);

            try {
                strBuilder.setSpan(new MyClickableSpan() {
                    @Override
                    public boolean onLongClick(View widget) {
                        return plainTextClickable.onLongClick(widget);
                    }

                    @Override
                    public void onClick(View widget) {
                        plainTextClickable.onClick(widget);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        plainTextClickable.updateDrawState(ds);
                    }
                }, plainTextSpanStart, start, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            plainTextSpanStart = end;

            MyClickableSpan myClickableSpan;

            if(span.getURL()!=null) {
                if (span.getURL().substring(0, 2).equals("/s") || span.getURL().equals("#s")) {
                    myClickableSpan = new MyClickableSpan() {

                        boolean spoilerHidden = true;
                        TextPaint textPaint;

                        @Override
                        public boolean onLongClick(View widget) {
                            return plainTextClickable.onLongClick(widget);
                            //return false;
                        }

                        @Override
                        public void onClick(View widget) {
                            spoilerHidden = !spoilerHidden;
                            updateDrawState(textPaint);
                            widget.invalidate();
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            int backgroundColor = (MyApplication.nightThemeEnabled) ? context.getResources().getColor(R.color.darker_gray) : Color.BLACK;
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            ds.bgColor = backgroundColor;
                            if (spoilerHidden) ds.setColor(backgroundColor);
                            else ds.setColor(Color.WHITE);
                            textPaint = ds;
                        }
                    };
                }
                else if(span.getURL().equals("#st")) {
                    myClickableSpan = new MyClickableSpan() {
                        @Override
                        public boolean onLongClick(View widget) {
                            return plainTextClickable.onLongClick(widget);
                            //return false;
                        }

                        @Override
                        public void onClick(View view) {

                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            int backgroundColor = (MyApplication.nightThemeEnabled) ? context.getResources().getColor(R.color.darker_gray) : Color.BLACK;
                            super.updateDrawState(ds);
                            ds.setUnderlineText(true);
                            ds.bgColor = backgroundColor;
                            ds.setColor(Color.YELLOW);
                        }
                    };
                }
                else {
                    myClickableSpan = new MyClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            LinkHandler linkHandler = new LinkHandler(context, span.getURL());
                            linkHandler.handleIt();
                        }

                        @Override
                        public boolean onLongClick(View v) {
                            //Log.d("geotest", "start: " + start + " end: " + end);
                            try { //illegalstateexception is thrown if the parent activity is destroyed before the dialog can be shown
                                Bundle args = new Bundle();
                                args.putString("url", span.getURL());
                                UrlOptionsDialogFragment dialogFragment = new UrlOptionsDialogFragment();
                                dialogFragment.setArguments(args);
                                dialogFragment.show(((Activity) context).getFragmentManager(), "dialog");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return true;
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(MyApplication.linkColor);

                            //ds.bgColor = Color.RED; //enable for debugging clickable link spans
                        }
                    };
                }
                strBuilder.setSpan(myClickableSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        if(strBuilder.length() > plainTextSpanStart) {
            strBuilder.setSpan(plainTextClickable, plainTextSpanStart, strBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return strBuilder;
    }
}
