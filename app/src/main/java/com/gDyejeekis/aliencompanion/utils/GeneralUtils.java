package com.gDyejeekis.aliencompanion.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 10/5/2015.
 */
public class GeneralUtils {
    public static final String TAG = "GeneralUtils";

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

    public static File getActiveSyncedDataDir(Context context) {
        if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable(context)) {
            File[] externalDirs = ContextCompat.getExternalFilesDirs(context, null);
            return ((externalDirs.length > 1) ? externalDirs[1] : externalDirs[0]);
        }
        return context.getFilesDir();
    }

    public static File getActiveMediaDir(Context context) {
        File activeDir = getActiveSyncedDataDir(context);
        return new File(activeDir, MyApplication.SYNCED_MEDIA_FILENAME);
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

    public static void downloadToFileSync(String url, File file) throws IOException {
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
        File file = StorageUtils.findFile(cacheDir, cacheDir.getAbsolutePath(), urlToFilename(url));
        if(file!=null) {
            Log.d(TAG, "Found media in cache " + file.getAbsolutePath());
            return file.getAbsolutePath();
        }
        Log.d(TAG, "Didn't find media from " + url + " in cache");
        return null;
    }

    public static String urlToFilename(String url) {
        return url.replaceAll("https?://", "").replace("/", "(s)");
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
        if(domain.contains("giphy.com")) return true;
        if(domain.contains("gyazo.com")) return true;
        if(domain.contains("flickr.com")) return true;
        if(domain.contains("twimg.com")) return true;
        if(domain.contains("photobucket.com")) return true;
        if(domain.contains("deviantart.com")) return true;
        if(domain.equals("instagram.com")) return true;
        if(domain.equals("snapchat.com")) return true;
        if(domain.equals("trbimg.com")) return true;
        if(domain.equals("imgfly.net")) return true;
        if(domain.equals("9gag.com")) return true;
        if(domain.equals("i.redd.it")) return true;
        if(domain.equals("i.reddituploads.com")) return true;
        if(domain.equals("i.redditmedia.com")) return true;
        String urlLc = url.toLowerCase();
        if(urlLc.endsWith(".jpg") || urlLc.endsWith(".png") || urlLc.endsWith(".gif") || urlLc.endsWith(".jpeg")) return true;
        return false;
    }

    public static boolean isArticleLink(String url, String domain) {
        if(isImageLink(url, domain)) return false;
        if(isVideoLink(url, domain)) return false;
        if(domain.contains("reddit.com") || domain.equals("redd.it")) return false;
        if(domain.equals("twitter.com")) return false;
        if(domain.contains("facebook")) return false;
        if(domain.contains("github.com")) return false;
        if(domain.equals("bitbucket.org")) return false;
        if(domain.equals("gitlab.com")) return false;
        if(domain.equals("store.steampowered.com")) return false;
        if(domain.equals("steamcommunity.com")) return false;
        if(domain.equals("origin.com")) return false;
        if(domain.equals("ubisoft.com")) return false;
        if(domain.equals("humblebundle.com")) return false;
        if(domain.equals("strawpoll.me")) return false;
        if(domain.equals("docs.google.com")) return false;
        if(domain.contains("mixtape.moe")) return false;
        return true;
    }

    public static boolean isGifLink(String url, String domain) {
        if(domain.contains("gfycat.com")) return true;
        if(domain.contains("giphy.com")) return true;
        String urlLc = url.toLowerCase();
        if(domain.contains("imgur.com") && urlLc.endsWith(".mp4")) return true;
        if(urlLc.endsWith(".gif") || urlLc.endsWith(".gifv")) return true;
        return false;
    }

    public static boolean isVideoLink(String url, String domain) {
        if(domain.contains("youtube") || domain.equals("youtu.be")) return true;
        if(domain.contains("streamable.com")) return true;
        if(domain.contains("pomf.se")) return true;
        if(domain.contains("dailymotion")) return true;
        if(domain.contains("vimeo.com")) return true;
        if(domain.equals("vid.me")) return true;
        if(domain.equals("vine.co")) return true;
        if(domain.equals("liveleak.com")) return true;
        if(domain.contains("twitch.tv")) return true;
        if(domain.equals("hitbox.tv")) return true;
        if(domain.equals("oddshot.tv")) return true;
        String urlLc = url.toLowerCase();
        if(urlLc.endsWith(".webm") || urlLc.endsWith(".mp4"))  return true;
        return false;
    }

}
