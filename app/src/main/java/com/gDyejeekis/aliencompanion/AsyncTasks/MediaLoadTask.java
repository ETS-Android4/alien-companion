package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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

    private BufferedInputStream inStream;

    public MediaLoadTask(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    @Override
    protected String doInBackground(String... params) {
        url = params[0];
        String cachedPath = GeneralUtils.checkCacheForMedia(cacheDir, url);
        if(cachedPath==null) {
            cachedPath = downloadToCache(new File(cacheDir, GeneralUtils.urlToFilename(url)));
            GeneralUtils.checkCacheSize(cacheDir);
        }
        return cachedPath;
    }

    private String downloadToCache(File file) {
        Log.d(TAG, "Caching media from " + url);
        try {
            //Open a connection to that URL.
            URLConnection ucon = new URL(url).openConnection();

            //this timeout affects how long it takes for the app to realize there's a connection problem
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(10000);

            //Define InputStreams to read from the URLConnection.
            // uses 3KB download buffer
            InputStream is = ucon.getInputStream();
            inStream = new BufferedInputStream(is, 1024 * 5);
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
            Log.d(TAG, "Media cached to " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Failed to cache media to " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    public void cancelOperation() {
        //Log.d(TAG, "Attempting to cancel MediaLoadTask for " + url);
        cancel(true);
        try {
            inStream.close();
        } catch (Exception e) {}
    }

    @Override
    protected void onCancelled() {
        Log.d(TAG, "onCancelled");
        super.onCancelled();

        if(url != null) {
            GeneralUtils.clearMediaFromCache(cacheDir, url);
        }
    }

}
