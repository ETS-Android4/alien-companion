package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

/**
 * Created by George on 1/16/2017.
 */

public class GiphyTask extends AsyncTask<String, Void, String> {

    private Context context;

    public GiphyTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            final String url = params[0];
            return getGiphyDirectUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getContext() {
        return context;
    }

    // this method makes an API call to api.giphy.com (synchronously)
    public static String getGiphyDirectUrl(String originalUrl) throws IOException, ParseException {
        String url = "http://api.giphy.com/v1/gifs/" + LinkUtils.getGiphyId(originalUrl) + "?api_key=" + LinkHandler.GIPHY_API_KEY;
        Log.d("Giphy", "GET request to " + url);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setUseCaches(true);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        InputStream inputStream = connection.getInputStream();
        String content = IOUtils.toString(inputStream, "UTF-8");
        IOUtils.closeQuietly(inputStream);

        Log.d("Giphy", content);
        Object responseObject = new JSONParser().parse(content);
        JSONObject giphyData = (JSONObject) ((JSONObject) responseObject).get("data");
        JSONObject images = (JSONObject) giphyData.get("images");
        JSONObject original = (JSONObject) images.get("original");

        return safeJsonToString(original.get("mp4"));
    }

    // simple modify url method for GIPHY
    public static String getGiphyDirectUrlSimple(String originalUrl) {
        return "http://media.giphy.com/media/" + LinkUtils.getGiphyId(originalUrl) + "/giphy.mp4";
    }
}
