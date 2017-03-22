package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.widget.Toast;

import com.gDyejeekis.aliencompanion.MyApplication;

/**
 * Created by George on 6/19/2015.
 */
public class ToastUtils {
    public static final String TAG = "ToastUtils";

    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message, int length) {
        try {
            Toast.makeText(context,  message, length).show();
        } catch (Exception e) {}
    }
}
