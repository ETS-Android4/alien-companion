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
import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

import java.io.File;
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
        File file = new File(cacheDir, LinkUtils.urlToFilename(url));
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
        if(dir.exists()) {
            StorageUtils.deleteRecursive(dir);
            dir.delete();
        }
    }

    public static void clearSyncedArticles(Context context) {
        File dir = GeneralUtils.getSyncedArticlesDir(context);
        if(dir.exists()) {
            StorageUtils.deleteRecursive(dir);
            File[] dirs = dir.listFiles();
            for(File file : dirs) {
                file.delete();
            }
        }
    }

    public static void clearSyncedThumbnails(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedThumbnailsDir(context), name);
        if(dir.exists()) {
            StorageUtils.deleteRecursive(dir);
            dir.delete();
        }
    }

    public static void clearSyncedThumbnails(Context context) {
        File dir = GeneralUtils.getSyncedThumbnailsDir(context);
        if(dir.exists()) {
            StorageUtils.deleteRecursive(dir);
            File[] dirs = dir.listFiles();
            for(File file : dirs) {
                file.delete();
            }
        }
    }

    public static void clearSyncedRedditData(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedRedditDataDir(context), name);
        if(dir.exists()) {
            StorageUtils.deleteRecursive(dir);
            dir.delete();
        }
    }

    public static void clearSyncedRedditData(Context context) {
        File dir = GeneralUtils.getSyncedRedditDataDir(context);
        if(dir.exists()) {
            StorageUtils.deleteRecursive(dir);
            File[] dirs = dir.listFiles();
            for(File file : dirs) {
                file.delete();
            }
        }
    }

    public static void clearSyncedMedia(Context context) {
        File dir = GeneralUtils.getSyncedMediaDir(context);
        if(dir.exists()) {
            StorageUtils.deleteRecursive(dir);
            File[] dirs = dir.listFiles();
            for(File file : dirs) {
                file.delete();
            }
        }
    }

    public static void clearSyncedMedia(Context context, final String name) {
        File dir = GeneralUtils.getNamedDir(GeneralUtils.getSyncedMediaDir(context), name);
        if(dir.exists()) {
            StorageUtils.deleteRecursive(dir);
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
        File postListFile = new File(redditDataDir, name + MyApplication.SYNCED_POST_LIST_SUFFIX);
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
            File thumbFile = new File(thumbDir, id + MyApplication.SYNCED_THUMBNAIL_SUFFIX);
            if (thumbFile.delete()) {
                Log.d(TAG, "Deleted " + thumbFile.getAbsolutePath());
            }

            // delete synced article
            File articleDir = GeneralUtils.checkNamedDir(GeneralUtils.checkSyncedArticlesDir(context), name);
            File articleFile = new File(articleDir, id + MyApplication.SYNCED_ARTICLE_DATA_SUFFIX);
            if (articleFile.delete()) {
                Log.d(TAG, "Deleted " + articleFile.getAbsolutePath());
            }
            File articleImageFile = new File(articleDir, id + MyApplication.SYNCED_ARTICLE_IMAGE_SUFFIX);
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
                                        LinkUtils.getImgurImgId(postLink) + MyApplication.IMGUR_INFO_FILE_NAME);
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
                            toFind = LinkUtils.urlToFilename(postLink);
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

    public static void clearApplicationData(Context context) {
        clearInternalStorageData(context);
        clearExternalStorageData(context);
    }

    public static void clearInternalStorageData(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.d(TAG, "**************** File " + appDir.getAbsolutePath() + "/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static void clearExternalStorageData(Context context) {
        try {
            File externalDirs[] = ContextCompat.getExternalFilesDirs(context, null);
            for (File dir : externalDirs) {
                if (dir.exists()) {
                    deleteDir(dir);
                    Log.d(TAG, "**************** File " + dir.getAbsolutePath() + " DELETED *******************");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this should only be used when updating from a version prior to 1000, clears all images/gifs from the public pictures directory
    public static void clearPublicPicsDirSyncedMedia(Context context) {
        try {
            File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File[] files = publicDir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                    Log.d(TAG, "**************** File " + file.getAbsolutePath() + " DELETED *******************");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
