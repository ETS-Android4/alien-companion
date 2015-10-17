package com.dyejeekis.aliencompanion.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;

/**
 * Created by sound on 10/5/2015.
 */
public class GeneralUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        else if(dir!= null && dir.isFile())
            return dir.delete();
        else {
            return false;
        }
    }

    public static void shareUrl(Context context, String label, String url) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, label));
    }

    public static int getPortraitWidth(Activity activity) {
        int portraitWidthPixels;
        if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) portraitWidthPixels = activity.getResources().getDisplayMetrics().widthPixels;
        else portraitWidthPixels = activity.getResources().getDisplayMetrics().heightPixels;

        return portraitWidthPixels;
    }

    public static int getPortraitHeight(Activity activity) {
        int portraitHeightPixels;
        if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) portraitHeightPixels = activity.getResources().getDisplayMetrics().heightPixels;
        else portraitHeightPixels = activity.getResources().getDisplayMetrics().widthPixels;

        return portraitHeightPixels;
    }
}
