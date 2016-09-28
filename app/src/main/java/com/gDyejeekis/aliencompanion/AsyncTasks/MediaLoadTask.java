package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

import java.io.File;

/**
 * Created by George on 9/28/2016.
 */

/*
    Task for retrieving cached media path and downloading media to cache if not already present
 */

public class MediaLoadTask extends AsyncTask<String, Void, String> {

    public static final String TAG = "MediaLoadTask";

    private File cacheDir;

    private String url;

    public MediaLoadTask(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    @Override
    protected String doInBackground(String... params) {
        url = params[0];
        String cachedPath = GeneralUtils.checkCacheForMedia(cacheDir, url);
        if(cachedPath==null) {
            cachedPath = GeneralUtils.downloadMediaToCache(cacheDir, url);
            GeneralUtils.checkCacheSize(cacheDir);
        }
        return cachedPath;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        try {
            String filename = GeneralUtils.urlToFilename(url);
            File file = new File(cacheDir, filename);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
