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
import android.util.Log;
import android.view.View;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.UrlOptionsDialogFragment;

/**
 * Created by George on 3/22/2017.
 */

public class SpanUtils {
    public static final String TAG = "SpanUtils";

    public static SpannableStringBuilder modifyURLSpan(final Context context, CharSequence sequence) {
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);

        // Get an array of URLSpan from SpannableStringBuilder object
        URLSpan[] urlSpans = strBuilder.getSpans(0, strBuilder.length(), URLSpan.class);

        // Add onClick listener for each of URLSpan object
        for (URLSpan span : urlSpans) {
            int start = strBuilder.getSpanStart(span);
            int end = strBuilder.getSpanEnd(span);
            // The original URLSpan needs to be removed to block the behavior of browser opening
            strBuilder.removeSpan(span);

            MyClickableSpan myClickableSpan;

            if(span.getURL()!=null) {
                final String url = span.getURL();
                if (url.substring(0, 2).equals("/s") || url.equals("#s")) {
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
                else if(url.equals("#st")) {
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
                            LinkHandler linkHandler = new LinkHandler(context, url);
                            linkHandler.handleIt();
                        }

                        @Override
                        public boolean onLongClick(View v) {
                            try { //illegalstateexception is thrown if the parent activity is destroyed before the dialog can be shown
                                Bundle args = new Bundle();
                                args.putString("url", url);
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
                            //ds.setUnderlineText(false);
                        }
                    };
                }
                strBuilder.setSpan(myClickableSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
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
