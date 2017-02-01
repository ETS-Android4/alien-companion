package com.gDyejeekis.aliencompanion.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import java.io.File;

/**
 * Created by sound on 3/18/2016.
 */

/*
    Task for downloading media to file
 */
public class MediaDownloadTask extends AsyncTask<Void, Void, Boolean> {

    public static final String TAG = "MediaDownloadTask";

    private String url;
    private File file;
    private File cacheDir;

    public MediaDownloadTask(String url, File file, File cacheDir) {
        this.url = url;
        this.file = file;
        this.cacheDir = cacheDir;
    }

    @Override
    protected Boolean doInBackground(Void... unused) {
        try {
            String cachedPath = GeneralUtils.checkCacheForMedia(cacheDir, GeneralUtils.urlToFilename(url));
            if(cachedPath == null) {
                Log.d(TAG, "Didn't find media in cache, downloading to " + file.getAbsolutePath());
                GeneralUtils.downloadMediaToFile(url, file);
            }
            else {
                Log.d(TAG, "Found media in cache " + cachedPath + " , copying to " + file.getAbsolutePath());
                GeneralUtils.copy(new File(cachedPath), file);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
