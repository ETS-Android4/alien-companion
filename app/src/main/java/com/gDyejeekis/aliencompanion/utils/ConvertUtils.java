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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                                dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
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
                            plainTextClickable.onClick(view);
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
                                dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
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

    public static SpannableStringBuilder highlightText(SpannableStringBuilder stringBuilder, String toFind, boolean matchCase) {
        int highlightColor = MyApplication.nightThemeEnabled ? Color.BLUE : Color.YELLOW;
        String mainText = stringBuilder.toString();
        if(!matchCase) {
            mainText = mainText.toLowerCase();
            toFind = toFind.toLowerCase();
        }
        int index = mainText.indexOf(toFind);
        while (index>=0) {
            stringBuilder.setSpan(new BackgroundColorSpan(highlightColor), index, index+toFind.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            index = mainText.indexOf(toFind, index+1);
        }

        return stringBuilder;
    }
}
