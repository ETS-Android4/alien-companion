package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sound on 3/12/2016.
 */
public class GifDownloaderTask extends AsyncTask<String, Void, byte[]> {

    @Override
    protected byte[] doInBackground(String... params) {
        String url = params[0];
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection!=null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
