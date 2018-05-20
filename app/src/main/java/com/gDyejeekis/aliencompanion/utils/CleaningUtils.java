package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurImage;
import com.gDyejeekis.aliencompanion.api.imgur.ImgurItem;

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
            Log.e(TAG, "Error deleting cache directory");
            e.printStackTrace();
        }
    }

    public static void clearMediaFromCache(File cacheDir, String url) {
        File file = new File(cacheDir, LinkUtils.getFilenameFromUrl(url));
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

    public static void clearAppWebviewData(Context context) {
        File rootDir = context.getCacheDir().getParentFile();
        File webviewDir = new File(rootDir, "app_webview");
        StorageUtils.deleteDir(webviewDir);
    }

    public static boolean clearAccountData(Context context) {
        return deletePrivateFile(context, AppConstants.SAVED_ACCOUNTS_FILENAME);
    }

    public static boolean clearSyncProfiles(Context context) {
        return deletePrivateFile(context, AppConstants.SYNC_PROFILES_FILENAME);
    }

    public static boolean clearFilterProfiles(Context context) {
        return deletePrivateFile(context, AppConstants.FILTER_PROFILES_FILENAME);
    }

    public static boolean clearOfflineActions(Context context) {
        return deletePrivateFile(context, AppConstants.OFFLINE_USER_ACTIONS_FILENAME);
    }

    public static boolean deletePrivateFile(Context context, String name) {
        boolean success = context.deleteFile(name);
        Log.d(TAG, (success ? "Successfully deleted " : "Failed to delete ") + name);
        return success;
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
        if(dir.exists()) {
            StorageUtils.deleteFileRecursive(dir);
            dir.delete();
        }
    }

    public static void clearSyncedArticles(Context context) {
        File dir = GeneralUtils.getSyncedArticlesDir(context);
        if(dir.exists()) {
            StorageUtils.deleteFileRecursive(dir);
            File[] dirs = dir.listFiles();
            for(File file : dirs) {
                file.delete();
            }
        }
    }

    public static void clearSyncedThumbnails(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedThumbnailsDir(context), name);
        if(dir.exists()) {
            StorageUtils.deleteFileRecursive(dir);
            dir.delete();
        }
    }

    public static void clearSyncedThumbnails(Context context) {
        File dir = GeneralUtils.getSyncedThumbnailsDir(context);
        if(dir.exists()) {
            StorageUtils.deleteFileRecursive(dir);
            File[] dirs = dir.listFiles();
            for(File file : dirs) {
                file.delete();
            }
        }
    }

    public static void clearSyncedRedditData(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedRedditDataDir(context), name);
        if(dir.exists()) {
            StorageUtils.deleteFileRecursive(dir);
            dir.delete();
        }
    }

    public static void clearSyncedRedditData(Context context) {
        File dir = GeneralUtils.getSyncedRedditDataDir(context);
        if(dir.exists()) {
            StorageUtils.deleteFileRecursive(dir);
            File[] dirs = dir.listFiles();
            for(File file : dirs) {
                file.delete();
            }
        }
    }

    public static void clearSyncedMedia(Context context) {
        File dir = GeneralUtils.getSyncedMediaDir(context);
        if(dir.exists()) {
            StorageUtils.deleteFileRecursive(dir);
            File[] dirs = dir.listFiles();
            for(File file : dirs) {
                file.delete();
            }
        }
    }

    public static void clearSyncedMedia(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedMediaDir(context), name);
        if(dir.exists()) {
            StorageUtils.deleteFileRecursive(dir);
            dir.delete();
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

    public static boolean clearSyncedPostFromCategory(Context context, final String name, final String id) {
        File redditDataDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedRedditDataDir(context), name);
        File postListFile = new File(redditDataDir, name + AppConstants.SYNCED_POST_LIST_SUFFIX);
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

            // delete post file
            File postFile = new File(redditDataDir, id);
            if (postFile.delete()) {
                Log.d(TAG, "Deleted " + postFile.getAbsolutePath());
            }

            // delete synced thumbnail
            File thumbDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedThumbnailsDir(context), name);
            File thumbFile = new File(thumbDir, id + AppConstants.SYNCED_THUMBNAIL_SUFFIX);
            if (thumbFile.delete()) {
                Log.d(TAG, "Deleted " + thumbFile.getAbsolutePath());
            }

            // delete synced article
            File articleDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedArticlesDir(context), name);
            File articleFile = new File(articleDir, id + AppConstants.SYNCED_ARTICLE_DATA_SUFFIX);
            if (articleFile.delete()) {
                Log.d(TAG, "Deleted " + articleFile.getAbsolutePath());
            }
            File articleImageFile = new File(articleDir, id + AppConstants.SYNCED_ARTICLE_IMAGE_SUFFIX);
            if (articleImageFile.delete()) {
                Log.d(TAG, "Deleted " + articleImageFile.getAbsolutePath());
            }

            // delete any corresponding synced media (images/GIF)
            if(postLink!=null) {
                if(postLink.contains("imgur.com") || postLink.contains("gfycat.com") || postLink.endsWith(".jpg") || postLink.endsWith(".jpeg") || postLink.endsWith(".png") || postLink.endsWith(".gif")) {
                    File namedMediaDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedMediaDir(context), name);
                    //Log.d(TAG, "namedMediaDir: " + namedMediaDir.getAbsolutePath());

                    if(namedMediaDir!=null) {
                        String toFind;
                        if(postLink.contains("imgur.com")) {
                            if(postLink.contains("/a/") || postLink.contains("/gallery/")) {
                                File infoFile = StorageUtils.findFile(namedMediaDir, namedMediaDir.getAbsolutePath(),
                                        LinkUtils.getImgurImgId(postLink) + AppConstants.IMGUR_INFO_FILE_NAME);
                                if(infoFile != null && infoFile.isFile()) {
                                    ImgurItem albumInfo = (ImgurItem) GeneralUtils.readObjectFromFile(infoFile);
                                    for(ImgurImage img : albumInfo.getImages()) {
                                        toFind = LinkUtils.getImgurImgId(img.getLink());
                                        findDeleteFile(namedMediaDir, toFind);
                                    }
                                    if(infoFile.delete()) {
                                        Log.d(TAG, "Deleted " + infoFile.getAbsolutePath());
                                    }
                                }
                                else {
                                    toFind = LinkUtils.getImgurImgId(postLink);
                                    findDeleteFile(namedMediaDir, toFind);
                                }
                            }
                            else {
                                toFind = LinkUtils.getImgurImgId(postLink);
                                findDeleteFile(namedMediaDir, toFind);
                            }
                        }
                        else if(postLink.contains("gfycat.com")) {
                            toFind = LinkUtils.getGfycatId(postLink);
                            findDeleteFile(namedMediaDir, toFind);
                        }
                        else {
                            toFind = LinkUtils.getFilenameFromUrl(postLink);
                            findDeleteFile(namedMediaDir, toFind);
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

    public static void findDeleteFile(File dir, String toFind) {
        File file = StorageUtils.findFile(dir, dir.getAbsolutePath(), toFind);
        if(file!=null && file.delete()) {
            Log.d(TAG, "Deleted " + file.getAbsolutePath());
        }
    }

    // clears all data in the app's internal storage root directory except for the files excluded by the filter
    public static void clearInternalStorageData(Context context, FilenameFilter filenameFilter) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            File[] children = appDir.listFiles(filenameFilter);
            for (File child : children) {
                boolean deleted = StorageUtils.deleteDir(child, filenameFilter);
                Log.d(TAG, "**************** File " + child.getAbsolutePath() + (deleted ? " DELETED" : " CLEARED") + " *******************");
            }
        }
    }

    // clears all data in all external storage directories
    public static void clearExternalStorageData(Context context) {
        try {
            File externalDirs[] = ContextCompat.getExternalFilesDirs(context, null);
            for (File dir : externalDirs) {
                if (dir.exists()) {
                    StorageUtils.deleteDir(dir);
                    Log.d(TAG, "**************** File " + dir.getAbsolutePath() + " DELETED *******************");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // only use when updating from a version code lower than 1000 (not 0), clears all images/gifs from the public pictures directory (probably best to not use at all)
    public static void clearPublicPicsDirSyncedMedia(Context context) {
        try {
            File publicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), AppConstants.SAVED_PICTURES_PUBLIC_DIR_NAME);
            File[] files = publicDir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    StorageUtils.deleteDir(file);
                    Log.d(TAG, "**************** File " + file.getAbsolutePath() + " DELETED *******************");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
