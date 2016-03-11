package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 3/11/2016.
 */
public class GfycatTask extends AsyncTask<String, Void, String> {

    public static final String TAG = "GfycatTask";

    private Context context;

    public GfycatTask(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0].replaceAll("https?://(www.)?gfycat.com/", "http://gfycat.com/cajax/get/");
        Log.d(TAG, "GET request to " + url);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setUseCaches(true);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            InputStream inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);

            Log.d(TAG, content);
            Object responseObject = new JSONParser().parse(content);

            JSONObject gfyItem = (JSONObject) ((JSONObject) responseObject).get("gfyItem");

            return safeJsonToString(gfyItem.get("mobileUrl"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
