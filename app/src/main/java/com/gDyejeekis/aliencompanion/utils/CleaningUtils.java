package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * Created by George on 3/22/2017.
 */

public class CleaningUtils {
    public static final String TAG = "CleaningUtils";

    public static void clearCache(Context context) {
        try {
            File dir = context.getCacheDir();
            StorageUtils.deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearMediaFromCache(File cacheDir, String url) {
        File file = new File(cacheDir, GeneralUtils.urlToFilename(url));
        if(file.delete()) {
            Log.d(TAG, "Deleted " + file.getAbsolutePath() + " from cache");
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

    public static void clearAccountData(Context context) {
        context.deleteFile(MyApplication.SAVED_ACCOUNTS_FILENAME);
        SharedPreferences.Editor editor = MyApplication.prefs.edit();
        editor.putString("currentAccountName", "Logged out");
        editor.apply();
        NavDrawerAdapter.currentAccountName = "Logged out";
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
        // delete in primary external public directory
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File folder = new File(dir + "/AlienCompanion");
        for(File file : folder.listFiles()) {
            if(file.isDirectory()) {
                deleteMediaFromDir(context, file);
            }
        }

        if(StorageUtils.isExternalStorageAvailable(context)) {
            // delete in secondary external private directory
            for (File externalDir : ContextCompat.getExternalFilesDirs(context, null)) {
                File picturesDir = new File(externalDir, "Pictures");
                if (picturesDir.isDirectory()) {
                    for (File file : picturesDir.listFiles()) {
                        if (file.isDirectory()) {
                            deleteMediaFromDir(context, file);
                        }
                    }
                }
            }
        }
    }

    public static void clearSyncedImages(Context context, final String subreddit) {
        // delete in primary external public directory
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File folder = new File(dir + "/AlienCompanion/" + subreddit);
        if(folder.isDirectory()) {
            deleteMediaFromDir(context, folder);
        }

        if(StorageUtils.isExternalStorageAvailable(context)) {
            //delete in secondary external private directory
            for (File externalDir : ContextCompat.getExternalFilesDirs(context, null)) {
                File subredditDir = new File(externalDir.getAbsolutePath() + "/Pictures/" + subreddit);
                if (subredditDir.isDirectory()) {
                    deleteMediaFromDir(context, subredditDir);
                }
            }
        }
    }

    /**
     * Recursively deletes files from dir and the media store
     * @param context
     * @param dir
     */
    private static void deleteMediaFromDir(Context context, File dir) {
        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                deleteMediaFromDir(context, file);
            }
            else {
                file.delete();
                GeneralUtils.deleteFileFromMediaStore(context.getContentResolver(), file);
            }
        }
    }

    public static boolean deleteSyncedPostFromCategory(Context context, final String name, final String id) {
        File activeDir = GeneralUtils.getActiveSyncedDataDir(context);
        File postListFile = new File(activeDir, name + DownloaderService.LOCA_POST_LIST_SUFFIX);
        String postLink = null;
        try {
            // modify post list file
            List<Submission> postList = (List<Submission>) GeneralUtils.readObjectFromFile(postListFile);
            for(Submission post : postList) {
                if(post.getIdentifier().equals(id)) {
                    postLink = post.getURL();
                    postList.remove(post);
                    break;
                }
            }
            GeneralUtils.writeObjectToFile(postList, postListFile);

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
                                File infoFile = StorageUtils.findFile(activeDir, activeDir.getAbsolutePath(), LinkHandler.getImgurImgId(postLink));
                                if(infoFile != null && infoFile.isFile()) {
                                    ImgurItem albumInfo = (ImgurItem) GeneralUtils.readObjectFromFile(infoFile);
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
                            toFind = GeneralUtils.urlToFilename(postLink);
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
        File imgFile = StorageUtils.findFile(dir, dir.getAbsolutePath(), toFind);
        if(imgFile!=null && imgFile.delete()) {
            GeneralUtils.deleteFileFromMediaStore(context.getContentResolver(), imgFile);
            Log.d(TAG, "Deleted " + imgFile.getAbsolutePath());
        }
    }
}
