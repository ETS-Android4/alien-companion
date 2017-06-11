package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
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

    public static void clearAllSyncedData(Context context, final String name) {
        clearSyncedRedditData(context, name);
        clearSyncedThumbnails(context, name);
        clearSyncedMedia(context, name);
        clearSyncedArticles(context, name);
    }

    public static void clearAllSyncedData(Context context) {
        clearSyncedRedditData(context);
        clearSyncedThumbnails(context);
        clearSyncedMedia(context);
        clearSyncedArticles(context);
    }

    public static void clearSyncedArticles(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedArticlesDir(context), name);
        if(dir!=null) {
            StorageUtils.deleteRecursive(dir);
        }
    }

    public static void clearSyncedArticles(Context context) {
        File dir = GeneralUtils.getSyncedArticlesDir(context);
        if(dir!=null) {
            StorageUtils.deleteRecursive(dir);
        }
    }

    public static void clearSyncedThumbnails(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedThumbnailsDir(context), name);
        if(dir!=null) {
            StorageUtils.deleteRecursive(dir);
        }
    }

    public static void clearSyncedThumbnails(Context context) {
        File dir = GeneralUtils.getSyncedThumbnailsDir(context);
        if(dir!=null) {
            StorageUtils.deleteRecursive(dir);
        }
    }

    public static void clearSyncedRedditData(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedRedditDataDir(context), name);
        if(dir!=null) {
            StorageUtils.deleteRecursive(dir);
        }
    }

    public static void clearSyncedRedditData(Context context) {
        File dir = GeneralUtils.getSyncedRedditDataDir(context);
        if(dir!=null) {
            StorageUtils.deleteRecursive(dir);
        }
    }

    public static void clearSyncedMedia(Context context) {
        File dir = GeneralUtils.getSyncedMediaDir(context);
        if(dir!=null) {
            StorageUtils.deleteRecursive(dir);
        }
    }

    public static void clearSyncedMedia(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedMediaDir(context), name);
        if(dir!=null) {
            StorageUtils.deleteRecursive(dir);
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
        File activeDir = GeneralUtils.getPreferredSyncDir(context);
        File postListFile = new File(activeDir, name + MyApplication.SYNCED_POST_LIST_SUFFIX);
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
                    File namedMediaDir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedMediaDir(context), name);
                    //Log.d(TAG, "namedMediaDir: " + namedMediaDir.getAbsolutePath());

                    if(namedMediaDir!=null) {
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
