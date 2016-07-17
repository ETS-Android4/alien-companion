package com.gDyejeekis.aliencompanion.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ChangeLogDialogFragment;
import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurAlbum;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurApiEndpoints;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurGallery;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurHttpClient;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 10/5/2015.
 */
public class GeneralUtils {

    public static final String TAG = "GeneralUtils";

    public static final String MOBILE_USER_AGENT_STRING = "Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; Nexus One Build/FRG83) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";

    public static void showChangeLog(Activity activity) {
        ChangeLogDialogFragment dialog = new ChangeLogDialogFragment();
        dialog.show(activity.getFragmentManager(), "dialog");
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isConnectedOverWifi(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isConnectedOrConnecting();
    }

    public static void deleteAccountData(Context context) {
        context.deleteFile(MyApplication.SAVED_ACCOUNTS_FILENAME);
        SharedPreferences.Editor editor = MyApplication.prefs.edit();
        editor.putString("currentAccountName", "Logged out");
        editor.apply();
        NavDrawerAdapter.currentAccountName = "Logged out";
    }

    public static void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }

    public static void clearSyncedPostsAndComments(Context context, final String subreddit) {
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.startsWith(subreddit)/*filename.length()>=subreddit.length() && filename.substring(0, subreddit.length()).equals(subreddit)*/) return true;
                return false;
            }
        };
        File[] files = context.getFilesDir().listFiles(filenameFilter);
        for(File file : files) {
            Log.d(TAG, "Deleting " + file.getName());
            file.delete();
        }
    }

    public static void clearSyncedPosts(Context context) {
        File dir = context.getFilesDir();
        File[] files = dir.listFiles();
        for (File file : files) {
            //Log.d(TAG, file.getName());
            String filename = file.getName();
            if (!filename.equals(MyApplication.SAVED_ACCOUNTS_FILENAME) && !filename.equals(MyApplication.SYNC_PROFILES_FILENAME)
                    && !filename.equals(MyApplication.OFFLINE_USER_ACTIONS_FILENAME)) file.delete();
        }

        //Log.d(TAG, "Remaining local app files AFTER delete:");
        //listFilesInDir(dir);
    }

    public static void listFilesInDir(File dir) {
        File[] files = dir.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                listFilesInDir(file);
            }
            else {
                Log.d(TAG, file.getName() + " " + file.length());
            }
        }
    }

    public static boolean isExternalStorageAvailable() {

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (mExternalStorageAvailable == true
                && mExternalStorageWriteable == true) {
            return true;
        } else {
            return false;
        }
    }

    public static void clearSyncedImages(Context context) {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        final File folder = new File(dir + "/AlienCompanion");

        File[] files = folder.listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                clearSyncedImages(context, file.getName());
            }
        }
    }

    public static void clearSyncedImages(Context context, final String subreddit) {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        final File folder = new File(dir + "/AlienCompanion/" + subreddit);

        if(folder.isDirectory()) {
            File[] files = folder.listFiles();
            for(File file : files) {
                file.delete();
                deleteFileFromMediaStore(context.getContentResolver(), file);
            }
        }
    }

    public static Object readObjectFromFile(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        fis.close();
        ois.close();
        return object;
    }

    public static void writeObjectToFile(Object object, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        fos.close();
        oos.close();
    }

    /**
     * Search file a file in a directory. Please comment more here, your method is not that standard.
     * @param file the file / folder where to look our file for.
     * @param sDir a directory that must be in the path of the file to find
     * @param toFind the name of file we are looking for.
     * @return the file we were looking for. Null if no such file could be found.
     */
    public static File findFile( File aFile, String sDir, String toFind ){
        if( aFile.isFile() &&
                aFile.getAbsolutePath().contains( sDir ) &&
                aFile.getName().contains( toFind ) ) {
            return aFile;
        } else if( aFile.isDirectory() ) {
            for( File child : aFile.listFiles() ){
                File found = findFile( child, sDir, toFind );
                if( found != null ) {
                    return found;
                }//if
            }//for
        }//else
        return null;
    }//met

    public static void downloadMediaToFile(String url, File file) throws IOException {
        //Open a connection to that URL.
        URLConnection ucon = new URL(url).openConnection();

        //this timeout affects how long it takes for the app to realize there's a connection problem
        ucon.setReadTimeout(5000);
        ucon.setConnectTimeout(10000);

        //Define InputStreams to read from the URLConnection.
        // uses 3KB download buffer
        InputStream is = ucon.getInputStream();
        BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
        FileOutputStream outStream = new FileOutputStream(file);
        byte[] buff = new byte[5 * 1024];

        //Read bytes (and store them) until there is nothing more to read(-1)
        int len;
        while ((len = inStream.read(buff)) != -1) {
            outStream.write(buff, 0, len);
            //Log.d(TAG, "writing buffer to file..");
        }

        //clean up
        outStream.flush();
        outStream.close();
        inStream.close();
    }

    public static ImgurItem getImgurDataFromUrl(ImgurHttpClient httpClient, String url) { //run on background thread
        ImgurItem item;
        String urlLC = url.toLowerCase();
        String id = LinkHandler.getImgurImgId(url);
        if (urlLC.contains("/a/")) {
            JSONObject response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.ALBUM, id)).getResponseObject();
            JSONObject object = (JSONObject) response.get("data");
            item = new ImgurAlbum(object);
        } else if (urlLC.contains("/gallery/")) {
            JSONObject response;
            try {
                response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.GALLERY, id)).getResponseObject();
            } catch (Exception e) {
                e.printStackTrace();
                response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.IMAGE, id)).getResponseObject();
            }
            JSONObject object = (JSONObject) response.get("data");
            item = new ImgurGallery(object);
        } else {
            JSONObject response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.IMAGE, id)).getResponseObject();
            JSONObject object = (JSONObject) response.get("data");
            item = new ImgurImage(object);
        }
        return item;
    }

    //this method makes an API call to gfycat.com
    //public static String getGfycatMobileUrl(String desktopUrl) throws IOException, ParseException {
    //    String url = "http://gfycat.com/cajax/get/" + LinkHandler.getGfycatId(desktopUrl);
    //    Log.d("Gfycat", "GET request to " + url);
    //    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    //    connection.setUseCaches(true);
    //    connection.setRequestMethod("GET");
    //    connection.setDoInput(true);
    //    connection.setConnectTimeout(5000);
    //    connection.setReadTimeout(5000);
//
    //    InputStream inputStream = connection.getInputStream();
//
    //    String content = IOUtils.toString(inputStream, "UTF-8");
    //    IOUtils.closeQuietly(inputStream);
//
    //    Log.d("Gfycat", content);
    //    Object responseObject = new JSONParser().parse(content);
//
    //    JSONObject gfyItem = (JSONObject) ((JSONObject) responseObject).get("gfyItem");
//
    //    return safeJsonToString(gfyItem.get("mobileUrl"));
    //}

    public static String getGfycatMobileUrl(String desktopUrl) {
        String id = LinkHandler.getGfycatId(desktopUrl);
        return "http://thumbs.gfycat.com/" + id + "-mobile.mp4";
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
        Log.d(TAG, "saving account changes..");
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
                    Log.d(TAG, "Failed to save account data");
                    e.printStackTrace();
                }
            }
        });
        Log.d(TAG, "account changes saved");
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

    public static boolean canAccessExternalStorage(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static boolean isImageLink(String url, String domain) {
        if(domain.contains("imgur.com")) return true;
        if(domain.contains("gfycat.com")) return true;
        if(domain.contains("gyazo.com")) return true;
        if(domain.contains("flickr.com")) return true;
        if(domain.equals("i.redd.it")) return true;
        if(domain.equals("i.reddituploads.com")) return true;
        String urlLc = url.toLowerCase();
        if(urlLc.endsWith(".jpg") || urlLc.endsWith(".png") || urlLc.endsWith(".gif") || urlLc.endsWith(".jpeg")) return true;
        return false;
    }

    public static boolean isArticleLink(String url, String domain) {
        if(isImageLink(url, domain)) return false;
        if(isVideoLink(url, domain)) return false;
        if(domain.contains("reddit.com") || domain.equals("redd.it")) return false;
        if(domain.equals("twitter.com")) return false;
        return true;
    }

    public static boolean isVideoLink(String url, String domain) {
        if(domain.contains("youtube.com") || domain.equals("youtu.be")) return true;
        if(domain.equals("dailymotion.com")) return true;
        if(domain.equals("vimeo.com")) return true;
        if(domain.equals("vid.me")) return true;
        if(domain.equals("vine.co")) return true;
        if(domain.equals("liveleak.com")) return true;
        if(domain.equals("twitch.tv")) return true;
        if(domain.equals("oddshot.tv")) return true;
        String urlLc = url.toLowerCase();
        if(urlLc.endsWith(".webm") || urlLc.endsWith(".mp4"))  return true;
        return false;
    }

}
