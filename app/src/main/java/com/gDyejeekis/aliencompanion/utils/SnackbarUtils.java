package com.gDyejeekis.aliencompanion.utils;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;

/**
 * Created by George on 3/22/2017.
 */

public class SnackbarUtils {
    public static final String TAG = "SnackbarUtils";

    public static void showSnackbar(View view, String text) {
        showSnackbar(view, text, Snackbar.LENGTH_SHORT);
    }

    public static void showSnackbar(View view, String text, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        TextView txtv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        txtv.setTextColor(Color.WHITE);
        txtv.setMaxLines(3);
        snackbar.show();
    }

    public static void showSnackbar(View view, String text, String actionText, View.OnClickListener listener) {
        showSnackbar(view, text, actionText, listener, Snackbar.LENGTH_LONG);
    }

    public static void showSnackbar(View view, String text, String actionText, View.OnClickListener listener, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        snackbar.setAction(actionText, listener);
        snackbar.setActionTextColor(MyApplication.linkColor);
        TextView txtv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        txtv.setTextColor(Color.WHITE);
        txtv.setMaxLines(3);
        snackbar.show();
    }
}
