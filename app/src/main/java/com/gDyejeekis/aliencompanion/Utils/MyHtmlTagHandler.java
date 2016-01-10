package com.gDyejeekis.aliencompanion.Utils;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;

import org.xml.sax.XMLReader;

/**
 * Created by George on 8/6/2015.
 */
public class MyHtmlTagHandler implements Html.TagHandler {

    public void handleTag(boolean opening, String tag, Editable output,
                          XMLReader xmlReader) {
        if(tag.equalsIgnoreCase("del")) {
            processStrike(opening, output);
        }
        //else if(tag.equalsIgnoreCase("a")) {
        //    if(isSpoiler(xmlReader)) {
        //        processSpoiler(opening, output, xmlReader);
        //    }
        //}
    }

    //private boolean isSpoiler(XMLReader xmlReader) {
    //    try {
    //        Field elementField = xmlReader.getClass().getDeclaredField("href");
    //        elementField.setAccessible(true);
    //        Log.d("geotest", "field: " + elementField.toString());
    //        try {
    //            String string = (String) elementField.get(xmlReader);
    //            Log.d("geotest", "value: " + string);
    //            if (string.equalsIgnoreCase("#s") || string.equalsIgnoreCase("/s")) return true;
    //        } catch (IllegalAccessException e) {
    //            e.printStackTrace();
    //        }
    //    } catch (NoSuchFieldException e) {
    //        e.printStackTrace();
    //    }
    //    return false;
    //}

    //private void processSpoiler(boolean opening, Editable output, XMLReader xmlReader) {
//
    //}

    private void processStrike(boolean opening, Editable output) {
        int len = output.length();
        if(opening) {
            output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object obj = getLast(output, StrikethroughSpan.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                output.setSpan(new StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private Object getLast(Editable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            for(int i = objs.length;i>0;i--) {
                if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
                    return objs[i-1];
                }
            }
            return null;
        }
    }
}