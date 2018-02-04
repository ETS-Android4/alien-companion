package com.gDyejeekis.aliencompanion.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.gDyejeekis.aliencompanion.activities.ChangelogActivity;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ChangeLogDialogFragment;
import com.gDyejeekis.aliencompanion.models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurAlbum;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurApiEndpoints;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurGallery;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurHttpClient;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

import org.json.simple.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by sound on 10/5/2015.
 */
public class GeneralUtils {
    public static final String TAG = "GeneralUtils";

    public static void showChangeLog(Activity activity) {
        // dialog changelog
        //ChangeLogDialogFragment dialog = new ChangeLogDialogFragment();
        //dialog.show(activity.getFragmentManager(), "dialog");
        // activity changelog
        Intent intent = new Intent(activity, ChangelogActivity.class);
        activity.startActivity(intent);
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

    public static boolean isLargeScreen(Context context) {
        return getScreenSizeInches(context) > 6.4;
    }

    public static boolean isVeryLargeScreen(Context context) {
        return getScreenSizeInches(context) > 9.4;
    }

    public static double getScreenSizeInches(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        double density = dm.density * 160;
        double x = Math.pow(dm.widthPixels / density, 2);
        double y = Math.pow(dm.heightPixels / density, 2);
        double screenInches = Math.sqrt(x + y);

        return screenInches;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void addFileToMediaStore(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
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

    public static File getPreferredSyncDir(Context context) {
        if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable(context)) {
            File[] externalDirs = ContextCompat.getExternalFilesDirs(context, null);
            return ((externalDirs.length > 1) ? externalDirs[1] : externalDirs[0]);
        }
        return context.getFilesDir();
    }

    public static File getSyncedMediaDir(Context context) {
        return new File(getPreferredSyncDir(context), MyApplication.SYNCED_MEDIA_DIR_NAME);
    }

    public static File checkSyncedMediaDir(Context context) {
        File file = getSyncedMediaDir(context);
        return checkDir(file) ? file : null;
    }

    public static File getSyncedArticlesDir(Context context) {
        return new File(getPreferredSyncDir(context), MyApplication.SYNCED_ARTICLES_DIR_NAME);
    }

    public static File checkSyncedArticlesDir(Context context) {
        File file = getSyncedArticlesDir(context);
        return checkDir(file) ? file : null;
    }

    public static File getSyncedRedditDataDir(Context context) {
        return new File(getPreferredSyncDir(context), MyApplication.SYNCED_REDDIT_DATA_DIR_NAME);
    }

    public static File checkSyncedRedditDataDir(Context context) {
        File file = getSyncedRedditDataDir(context);
        return checkDir(file) ? file : null;
    }

    public static File getSyncedThumbnailsDir(Context context) {
        return new File(getPreferredSyncDir(context), MyApplication.SYNCED_THUMBNAILS_DIR_NAME);
    }

    public static File checkSyncedThumbnailsDir(Context context) {
        File file = getSyncedThumbnailsDir(context);
        return checkDir(file) ? file : null;
    }

    public static File getNamedDir(File parentDir, String name) {
        return new File(parentDir, name);
    }

    public static File checkNamedDir(File parentDir, String name) {
        File file = getNamedDir(parentDir, name);
        return checkDir(file) ? file : null;
    }

    public static boolean checkDir(File file) {
        if(file.exists() && file.isDirectory()) {
            return true;
        }
        else {
            boolean success = file.mkdir();
            if(!success) {
                Log.e(TAG, "Failed to create directory " + file.getAbsolutePath());
            }
            return success;
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

    //public static void downloadToFileSync(String url, File file) throws IOException {
    //    // Open a connection to that URL.
    //    URLConnection ucon = new URL(url).openConnection();
//
    //    // this timeout affects how long it takes for the app to realize there's a connection problem
    //    ucon.setReadTimeout(5000);
    //    ucon.setConnectTimeout(5000);
//
    //    // Define InputStreams to read from the URLConnection.
    //    // uses 3KB download buffer
    //    InputStream is = ucon.getInputStream();
    //    BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
    //    FileOutputStream outStream = new FileOutputStream(file);
    //    byte[] buff = new byte[5 * 1024];
//
    //    // Read bytes (and store them) until there is nothing more to read(-1)
    //    int len;
    //    while ((len = inStream.read(buff)) != -1) {
    //        outStream.write(buff, 0, len);
    //    }
//
    //    // clean up
    //    outStream.flush();
    //    outStream.close();
    //    inStream.close();
    //}

    public static void downloadToFileSync(String fileURL, File file) throws IOException {
        final int BUFFER_SIZE = 4096;
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            //String fileName = ConvertUtils.urlToFilename(fileURL);
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            //if (disposition != null) {
            //    // extracts file name from header field
            //    int index = disposition.indexOf("filename=");
            //    if (index > 0) {
            //        fileName = disposition.substring(index + 10,
            //                disposition.length() - 1);
            //    }
            //} else {
            //    // extracts file name from URL
            //    fileName = ConvertUtils.urlToFilename(fileURL);
            //}

            Log.d(TAG, "Content-Type = " + contentType);
            Log.d(TAG, "Content-Disposition = " + disposition);
            Log.d(TAG, "Content-Length = " + contentLength);
            Log.d(TAG, "fileName = " + file.getName());

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            //String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(file);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            Log.d(TAG, "File downloaded");
        } else {
            Log.d(TAG, "No file to download. Server replied HTTP code: " + responseCode);

            //Log.d(TAG, "New location: " + httpConn.getHeaderField("Location"));
        }
        httpConn.disconnect();
    }

    public static String getFinalUrlRedirect(String urlString) {
        HttpURLConnection con = null;
        try {
            URL url = new URL(urlString);
            con = (HttpURLConnection) (url.openConnection());
            con.setInstanceFollowRedirects(false);
            con.connect();
            int resCode = con.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_SEE_OTHER
                    || resCode == HttpURLConnection.HTTP_MOVED_PERM
                    || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = con.getHeaderField("Location");
                if (location.startsWith("/")) {
                    location = url.getProtocol() + "://" + url.getHost() + location;
                }
                Log.d(TAG, "Redirect location: " + location);
                return getFinalUrlRedirect(location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(con!=null) {
                con.disconnect();
            }
        }
        return urlString;
    }

    public static ImgurItem getImgurDataFromUrl(ImgurHttpClient httpClient, String url) { //run on background thread
        ImgurItem item;
        String urlLC = url.toLowerCase();
        String id = LinkUtils.getImgurImgId(url);
        if (urlLC.contains("/a/") || urlLC.contains("/topic/")) {
            JSONObject response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.ALBUM, id)).getResponseObject();
            JSONObject object = (JSONObject) response.get("data");
            item = new ImgurAlbum(object);
        }
        else if (urlLC.contains("/gallery/")) {
            JSONObject response;
            try {
                response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.GALLERY, id)).getResponseObject();
                item = new ImgurGallery((JSONObject) response.get("data"));
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.IMAGE, id)).getResponseObject();
                    item = new ImgurImage((JSONObject) response.get("data"));
                } catch (Exception r) {
                    r.printStackTrace();
                    response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.ALBUM, id)).getResponseObject();
                    item = new ImgurAlbum((JSONObject) response.get("data"));
                }
            }
        }
        else {
            JSONObject response = (JSONObject) httpClient.get(String.format(ImgurApiEndpoints.IMAGE, id)).getResponseObject();
            JSONObject object = (JSONObject) response.get("data");
            item = new ImgurImage(object);
        }
        return item;
    }

    public static Bitmap getBitmapFromPath(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    public static void checkCacheSize(File cacheDir) {
        if(StorageUtils.dirSize(cacheDir, false) >= MyApplication.IMAGES_CACHE_LIMIT) {
            File toDelete = StorageUtils.oldestFileInDir(cacheDir);
            long length = toDelete.length();
            if(toDelete.delete()) {
                Log.d(TAG, length + " bytes cleared from cache");
            }
            checkCacheSize(cacheDir);
        }
    }

    public static String checkCacheForMedia(File cacheDir, String url) {
        File file = StorageUtils.findFile(cacheDir, cacheDir.getAbsolutePath(), LinkUtils.urlToFilename(url));
        if(file!=null) {
            Log.d(TAG, "Found media in cache " + file.getAbsolutePath());
            return file.getAbsolutePath();
        }
        Log.d(TAG, "Didn't find media from " + url + " in cache");
        return null;
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
                    List<SavedAccount> updatedAccounts = new ArrayList<>();
                    for (SavedAccount account : readAccounts(context)) {
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

    public static boolean isValidSubreddit(String s) {
        final String pattern = "^[a-zA-Z0-9\\_]*$";
        return s.matches(pattern);
    }

    public static boolean isValidUsername(String s) {
        final String pattern = "^[a-zA-Z0-9\\_\\-]*$";
        return s.matches(pattern);
    }

    public static boolean isAlphaNumeric(String s){
        final String pattern = "^[a-zA-Z0-9]*$";
        return s.matches(pattern);
    }

    public static boolean containsAlphaNumeric(String s){
        final String pattern = ".*[a-zA-Z0-9]+.*";
        return s.matches(pattern);
    }

    public static boolean isValidDomain(String s) {
        final String pattern = "^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9](?:\\.[a-zA-Z]{2,})+$";
        return s.matches(pattern);
    }

    public static void clearField(EditText field, String hint) {
        clearField(field, hint, MyApplication.textHintColor);
    }

    public static void clearField(EditText field, String hint, int color) {
        field.setText("");
        field.setHint(hint);
        field.setHintTextColor(color);
    }

    public static void printHttpRequestProperties(HttpURLConnection connection) {
        Log.d(TAG, "REQUEST PROPERTIES");
        Log.d(TAG, "Request method: " + connection.getRequestMethod());
        for (String header : connection.getRequestProperties().keySet()) {
            if (header != null) {
                for (String value : connection.getRequestProperties().get(header)) {
                    Log.d("Request properties", header + ":" + value);
                }
            }
        }
    }

    public static void printHttpRequestHeaders(HttpURLConnection connection) {
        Log.d(TAG, "HEADER FIELDS");
        for (String header : connection.getHeaderFields().keySet()) {
            if (header != null) {
                for (String value : connection.getHeaderFields().get(header)) {
                    Log.d(TAG, header + ":" + value);
                }
            }
        }
    }

    public static void printHttpResponseBody(String responseString) {
        int maxLogSize = 800;
        for(int i = 0; i <= responseString.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > responseString.length() ? responseString.length() : end;
            Log.v(TAG, "response: " + responseString.substring(start, end));
        }
    }

    public static Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity)cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper)cont).getBaseContext());

        return null;
    }

    public static boolean containsProfanity(String input) {
        input = input.toLowerCase();
        for (String word : RedditConstants.PROFANE_WORDS) {
            if (input.contains(word)) return true;
        }
        return false;
    }
}
