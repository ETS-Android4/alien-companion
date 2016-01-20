package com.gDyejeekis.aliencompanion.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ChangeLogDialogFragment;
import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 10/5/2015.
 */
public class GeneralUtils {

    public static void showChangeLog(Activity activity) {
        ChangeLogDialogFragment dialog = new ChangeLogDialogFragment();
        dialog.show(activity.getFragmentManager(), "dialog");
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void deleteAccountData(Context context) {
        context.deleteFile(MyApplication.SAVED_ACCOUNTS_FILENAME);
        SharedPreferences.Editor editor = MyApplication.prefs.edit();
        editor.putString("currentAccountName", "Logged out");
        editor.apply();
        NavDrawerAdapter.currentAccountName = "Logged out";
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //Log.d(C.TAG, "Using ClearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        }
        else {
            //Log.d(C.TAG, "Using ClearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr= CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
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

    public static void saveAccountChanges(final Context context) {
        Log.d("geotest", "saving account changes..");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<SavedAccount> oldAccounts = readAccounts(context);
                    List<SavedAccount> updatedAccounts = new ArrayList<>();
                    for (SavedAccount account : oldAccounts) {
                        if (MyApplication.currentAccount.getUsername().equals(account.getUsername())) {
                            updatedAccounts.add(MyApplication.currentAccount);
                        } else updatedAccounts.add(account);
                    }
                    saveAccounts(context, updatedAccounts);
                } catch (Exception e) {
                    Log.d("geotest", "Failed to save account data");
                    e.printStackTrace();
                }
            }
        });
        Log.d("geotest", "account changes saved");
    }

    public static List<SavedAccount> readAccounts(Context context) {
        try {
            FileInputStream fis = context.openFileInput(MyApplication.SAVED_ACCOUNTS_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            List<SavedAccount> savedAccounts = (List<SavedAccount>) is.readObject();
            is.close();
            fis.close();
            return savedAccounts;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveAccounts(Context context, List<SavedAccount> updatedAccounts) {
        try {
            FileOutputStream fos = context.openFileOutput(MyApplication.SAVED_ACCOUNTS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(updatedAccounts);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
