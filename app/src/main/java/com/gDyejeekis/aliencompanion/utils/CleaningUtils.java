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

    public static void clearSyncedMedia(Context context) {
        // delete in private internal media directory
        File internalMediaDir = new File(context.getFilesDir(), MyApplication.SYNCED_MEDIA_FILENAME);
        for(File file : internalMediaDir.listFiles()) {
            if(file.isDirectory()) {
                deleteMediaFromDir(context, file);
            }
        }

        if(StorageUtils.isExternalStorageAvailable(context)) {
            // delete in secondary external private directory
            for (File externalDir : ContextCompat.getExternalFilesDirs(context, null)) {
                File externalMediaDir = new File(externalDir, MyApplication.SYNCED_MEDIA_FILENAME);
                if (externalMediaDir.isDirectory()) {
                    for (File file : externalMediaDir.listFiles()) {
                        if (file.isDirectory()) {
                            deleteMediaFromDir(context, file);
                        }
                    }
                }
            }
        }
    }

    public static void clearSyncedMedia(Context context, final String subreddit) {
        String subredditDir = "/" + MyApplication.SYNCED_MEDIA_FILENAME + "/" + subreddit;
        // delete in private internal media directory
        File internalSubredditDir = new File(context.getFilesDir().getAbsolutePath() + subredditDir);
        if(internalSubredditDir.isDirectory()) {
            deleteMediaFromDir(context, internalSubredditDir);
        }

        if(StorageUtils.isExternalStorageAvailable(context)) {
            //delete in secondary external private directory
            for (File externalDir : ContextCompat.getExternalFilesDirs(context, null)) {
                File externalSubredditDir = new File(externalDir.getAbsolutePath() + subredditDir);
                if (externalSubredditDir.isDirectory()) {
                    deleteMediaFromDir(context, externalSubredditDir);
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
                    File namedMediaDir = new File(GeneralUtils.getActiveMediaDir(context).getAbsolutePath() + "/" + name);
                    //Log.d(TAG, "namedMediaDir: " + namedMediaDir.getAbsolutePath());

                    if(namedMediaDir.isDirectory()) {
                        String toFind;
                        if(postLink.contains("imgur.com")) {
                            if(postLink.contains("/a/") || postLink.contains("/gallery/")) {
                                File infoFile = StorageUtils.findFile(activeDir, activeDir.getAbsolutePath(), LinkHandler.getImgurImgId(postLink));
                                if(infoFile != null && infoFile.isFile()) {
                                    ImgurItem albumInfo = (ImgurItem) GeneralUtils.readObjectFromFile(infoFile);
                                    for(ImgurImage img : albumInfo.getImages()) {
                                        toFind = LinkHandler.getImgurImgId(img.getLink());
                                        findDeleteMediaInDir(context, namedMediaDir, toFind);
                                        findDeleteMediaInDir(context, activeDir, toFind + "-thumb");
                                    }
                                    if(infoFile.delete()) {
                                        Log.d(TAG, "Deleted " + infoFile.getAbsolutePath());
                                    }
                                }
                                else {
                                    toFind = LinkHandler.getImgurImgId(postLink);
                                    findDeleteMediaInDir(context, namedMediaDir, toFind);
                                }
                            }
                            else {
                                toFind = LinkHandler.getImgurImgId(postLink);
                                findDeleteMediaInDir(context, namedMediaDir, toFind);
                            }
                        }
                        else if(postLink.contains("gfycat.com")) {
                            toFind = LinkHandler.getGfycatId(postLink);
                            findDeleteMediaInDir(context, namedMediaDir, toFind);
                        }
                        else {
                            toFind = GeneralUtils.urlToFilename(postLink);
                            findDeleteMediaInDir(context, namedMediaDir, toFind);
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
