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
 * Created by sound on 3/18/2016.
 */
public class MediaDownloadTask extends AsyncTask<Void, Void, Boolean> {

    //Downloader task for images, gif and mp4 files

    public static final String TAG = "MediaDownloadTask";

    private String url;
    //private String directory;
    private File file;

    public MediaDownloadTask(String url, File file) {
        this.url = url;
        //this.directory = directory;
        this.file = file;
    }

    @Override
    protected Boolean doInBackground(Void... unused) {
        //final String filename = url.replace("/", "(s)");
        //final File file = new File(directory, filename);

        try {
            GeneralUtils.downloadMediaToFile(url, file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
