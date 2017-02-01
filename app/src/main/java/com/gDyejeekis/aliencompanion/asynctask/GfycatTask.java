package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.utils.LinkHandler;

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
 * Created by George on 1/19/2017.
 */

public class GfycatTask extends AsyncTask<String, Void, String> {

    private Context context;

    public GfycatTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            final String originalUrl = params[0];
            return getGfycatDirectUrl(originalUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getContext() {
        return context;
    }


    // this method makes an API call to gfycat.com (synchronously)
    public static String getGfycatDirectUrl(String desktopUrl) throws IOException, ParseException {
        String url = "http://gfycat.com/cajax/get/" + LinkHandler.getGfycatId(desktopUrl);
        Log.d("Gfycat", "GET request to " + url);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setUseCaches(true);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        InputStream inputStream = connection.getInputStream();

        String content = IOUtils.toString(inputStream, "UTF-8");
        IOUtils.closeQuietly(inputStream);

        Log.d("Gfycat", content);
        Object responseObject = new JSONParser().parse(content);

        JSONObject gfyItem = (JSONObject) ((JSONObject) responseObject).get("gfyItem");

        return safeJsonToString(gfyItem.get("mobileUrl"));
    }

    // simple modify url method for GFYCAT
    public static String getGfycatDirectUrlSimple(String desktopUrl) {
        String id = LinkHandler.getGfycatId(desktopUrl);
        return "http://thumbs.gfycat.com/" + id + "-mobile.mp4";
    }
}
