package com.gDyejeekis.aliencompanion.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ChangeLogDialogFragment;
import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Services.DownloaderService;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurAlbum;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurApiEndpoints;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurGallery;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurHttpClient;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

import org.json.simple.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 10/5/2015.
 */
public class GeneralUtils {

    public static final String CURRENT_DEBUG_TAG = "CurrentDebug";

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

    public static File getActiveDir(Context context) {
        if(MyApplication.preferExternalStorage && StorageUtils.isExternalStorageAvailable(context)) {
            File[] externalDirs = ContextCompat.getExternalFilesDirs(context, null);
            return ((externalDirs.length > 1) ? externalDirs[1] : externalDirs[0]);
        }
        return context.getFilesDir();
    }

    public static File getActiveMediaDir(Context context) {
        File activeDir = getActiveDir(context);
        File mediaDir;
        if(activeDir.equals(context.getFilesDir())) {
            mediaDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AlienCompanion");
        }
        else {
            mediaDir = new File(activeDir.getAbsolutePath() + "/Pictures");
        }
        return mediaDir;
    }

    public static void clearSyncedPostsAndComments(Context context, final String subreddit) {
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.startsWith(subreddit)) {
                    return true;
                }
                return false;
            }
        };
        File[] files = context.getFilesDir().listFiles(filenameFilter);
        for(File file : files) {
            Log.d(TAG, "Deleting " + file.getName());
            file.delete();
        }

        if(StorageUtils.isExternalStorageAvailable(context)) {
            for (File externalDir : ContextCompat.getExternalFilesDirs(context, null)) {
                for (File file : externalDir.listFiles(filenameFilter)) {
                    file.delete();
                }
            }
        }
    }

    public static void clearSyncedPosts(Context context) {
        for (File file : context.getFilesDir().listFiles()) {
            //Log.d(TAG, file.getName());
            String filename = file.getName();
            if (!filename.equals(MyApplication.SAVED_ACCOUNTS_FILENAME) && !filename.equals(MyApplication.SYNC_PROFILES_FILENAME)
                    && !filename.equals(MyApplication.OFFLINE_USER_ACTIONS_FILENAME)) {
                file.delete();
            }
        }

        if(StorageUtils.isExternalStorageAvailable(context)) {
            for (File externalDir : ContextCompat.getExternalFilesDirs(context, null)) {
                for (File file : externalDir.listFiles()) {
                    file.delete();
                }
            }
        }

        //Log.d(TAG, "Remaining local app files AFTER delete:");
        //listFilesInDir(dir);
    }

    public static void clearSyncedImages(Context context) {
        //delete in primary external public directory
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File folder = new File(dir + "/AlienCompanion");
        for(File file : folder.listFiles()) {
            if(file.isDirectory()) {
                deletePicsFromDirectory(context, file);
            }
        }

        if(StorageUtils.isExternalStorageAvailable(context)) {
            //delete in secondary external private directory
            for (File externalDir : ContextCompat.getExternalFilesDirs(context, null)) {
                File picturesDir = new File(externalDir, "Pictures");
                if (picturesDir.isDirectory()) {
                    for (File file : picturesDir.listFiles()) {
                        if (file.isDirectory()) {
                            deletePicsFromDirectory(context, file);
                        }
                    }
                }
            }
        }
    }

    public static void clearSyncedImages(Context context, final String subreddit) {
        //delete in primary external public directory
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File folder = new File(dir + "/AlienCompanion/" + subreddit);
        if(folder.isDirectory()) {
            deletePicsFromDirectory(context, folder);
        }

        if(StorageUtils.isExternalStorageAvailable(context)) {
            //delete in secondary external private directory
            for (File externalDir : ContextCompat.getExternalFilesDirs(context, null)) {
                File subredditDir = new File(externalDir.getAbsolutePath() + "/Pictures/" + subreddit);
                if (subredditDir.isDirectory()) {
                    deletePicsFromDirectory(context, subredditDir);
                }
            }
        }
    }

    private static void deletePicsFromDirectory(Context context, File dir) {
        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                deletePicsFromDirectory(context, file);
            }
            else {
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
     * @param aFile the file / folder where to look our file for.
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

    // Don't call on main thread
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

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Return the size of a directory in bytes
     */
    private static long dirSize(File dir, boolean recursive) {

        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for(int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if(recursive && fileList[i].isDirectory()) {
                    result += dirSize(fileList [i], true);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }

    public static File oldestFileInDir(File dir) {
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        long firstMod = Long.MAX_VALUE;
        File choice = null;
        for (File file : files) {
            if (file.lastModified() < firstMod) {
                choice = file;
                firstMod = file.lastModified();
            }
        }
        return choice;
    }

    public static Bitmap getBitmapFromPath(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    public static void checkCacheSize(File cacheDir) {
        if(dirSize(cacheDir, false) >= MyApplication.IMAGES_CACHE_LIMIT) {
            File toDelete = oldestFileInDir(cacheDir);
            long length = toDelete.length();
            if(toDelete.delete()) {
                Log.d(TAG, length + " bytes cleared from cache");
            }
            checkCacheSize(cacheDir);
        }
    }

    public static String checkCacheForMedia(File cacheDir, String url) {
        File file = findFile(cacheDir, cacheDir.getAbsolutePath(), urlToFilename(url));
        if(file!=null) {
            Log.d(TAG, "Found media in cache " + file.getAbsolutePath());
            return file.getAbsolutePath();
        }
        Log.d(TAG, "Didn't find media from " + url + " in cache");
        return null;
    }

    public static void clearMediaFromCache(File cacheDir, String url) {
        File file = new File(cacheDir, urlToFilename(url));
        if(file.delete()) {
            Log.d(TAG, "Deleted " + file.getAbsolutePath() + " from cache");
        }
    }

    public static String urlToFilename(String url) {
        return url.replaceAll("https?://", "").replace("/", "(s)");
    }

    public static boolean deleteSyncedPostFromCategory(Context context, final String name, final String id) {
        File activeDir = getActiveDir(context);
        File postListFile = new File(activeDir, name + DownloaderService.LOCA_POST_LIST_SUFFIX);
        String postLink = null;
        try {
            // modify post list file
            List<Submission> postList = (List<Submission>) readObjectFromFile(postListFile);
            for(Submission post : postList) {
                if(post.getIdentifier().equals(id)) {
                    postLink = post.getURL();
                    postList.remove(post);
                    break;
                }
            }
            writeObjectToFile(postList, postListFile);

            // delete synced files for current post
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.contains(name) && filename.contains(id);
                }
            };
            File[] files = activeDir.listFiles(filter);
            for (File file : files) {
                if (file.delete()) {
                    Log.d(TAG, "Deleted " + file.getAbsolutePath());
                }
            }

            // delete any corresponding synced media (images/GIF)
            if(postLink!=null) {
                if(postLink.contains("imgur.com") || postLink.contains("gfycat.com") || postLink.endsWith(".jpg") || postLink.endsWith(".jpeg") || postLink.endsWith(".png") || postLink.endsWith(".gif")) {
                    File namedDir;
                    // internal storage active
                    if(activeDir.getAbsolutePath().equals(context.getFilesDir().getAbsolutePath())) {
                        namedDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AlienCompanion/" + name);
                    }
                    // external storage active
                    else {
                        namedDir = new File(activeDir.getAbsolutePath() + "/Pictures/" + name);
                    }
                    //Log.d(TAG, "namedDir: " + namedDir.getAbsolutePath());

                    if(namedDir.isDirectory()) {
                        String toFind;
                        if(postLink.contains("imgur.com")) {
                            if(postLink.contains("/a/") || postLink.contains("/gallery/")) {
                                File infoFile = findFile(activeDir, activeDir.getAbsolutePath(), LinkHandler.getImgurImgId(postLink));
                                if(infoFile != null && infoFile.isFile()) {
                                    ImgurItem albumInfo = (ImgurItem) readObjectFromFile(infoFile);
                                    for(ImgurImage img : albumInfo.getImages()) {
                                        toFind = LinkHandler.getImgurImgId(img.getLink());
                                        findDeleteMediaInDir(context, namedDir, toFind);
                                        findDeleteMediaInDir(context, activeDir, toFind + "-thumb");
                                    }
                                    if(infoFile.delete()) {
                                        Log.d(TAG, "Deleted " + infoFile.getAbsolutePath());
                                    }
                                }
                                else {
                                    toFind = LinkHandler.getImgurImgId(postLink);
                                    findDeleteMediaInDir(context, namedDir, toFind);
                                }
                            }
                            else {
                                toFind = LinkHandler.getImgurImgId(postLink);
                                findDeleteMediaInDir(context, namedDir, toFind);
                            }
                        }
                        else if(postLink.contains("gfycat.com")) {
                            toFind = LinkHandler.getGfycatId(postLink);
                            findDeleteMediaInDir(context, namedDir, toFind);
                        }
                        else {
                            toFind = urlToFilename(postLink);
                            findDeleteMediaInDir(context, namedDir, toFind);
                        }

                    }

                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void findDeleteMediaInDir(Context context, File dir, String toFind) {
        File imgFile = findFile(dir, dir.getAbsolutePath(), toFind);
        if(imgFile!=null && imgFile.delete()) {
            deleteFileFromMediaStore(context.getContentResolver(), imgFile);
            Log.d(TAG, "Deleted " + imgFile.getAbsolutePath());
        }
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
        if(domain.contains("giphy.com")) return true;
        if(domain.contains("gyazo.com")) return true;
        if(domain.contains("flickr.com")) return true;
        if(domain.contains("twimg.com")) return true;
        if(domain.contains("photobucket.com")) return true;
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

    // TODO: 1/7/2017 further improve this
    public static boolean isArticleLink(String url, String domain) {
        if(isImageLink(url, domain)) return false;
        if(isVideoLink(url, domain)) return false;
        if(domain.contains("reddit.com") || domain.equals("redd.it")) return false;
        if(domain.equals("twitter.com")) return false;
        if(domain.contains("github.com")) return false;
        if(domain.equals("bitbucket.org")) return false;
        if(domain.equals("gitlab.com")) return false;
        if(domain.equals("store.steampowered.com")) return false;
        if(domain.equals("steamcommunity.com")) return false;
        if(domain.equals("origin.com")) return false;
        if(domain.equals("ubisoft.com")) return false;
        return true;
    }

    public static boolean isVideoLink(String url, String domain) {
        if(domain.contains("youtube") || domain.equals("youtu.be")) return true;
        if(domain.contains("streamable.com")) return true;
        if(domain.contains("mixtape.moe")) return true;
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
