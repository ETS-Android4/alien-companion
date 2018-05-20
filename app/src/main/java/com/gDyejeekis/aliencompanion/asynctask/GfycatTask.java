package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.Response;

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

    public static String getGfycatDirectUrl(String originalUrl) throws IOException, ParseException {
        final String url = "http://gfycat.com/cajax/get/" + LinkUtils.getGfycatId(originalUrl);
        Log.d("Gfycat", "GET request to " + url);
        Request request = new Request.Builder()
                .cacheControl(new CacheControl.Builder().maxStale(24, TimeUnit.HOURS).build())
                .url(url).build();
        Response response = MyApplication.okHttpClient.newCall(request).execute();
        String content = response.body().string();
        response.close();

        Object responseObject = new JSONParser().parse(content);
        JSONObject gfyItem = (JSONObject) ((JSONObject) responseObject).get("gfyItem");
        return safeJsonToString(gfyItem.get("mobileUrl"));
    }

    // simple modify url method for GFYCAT
    public static String getGfycatDirectUrlSimple(String desktopUrl) {
        String id = LinkUtils.getGfycatId(desktopUrl);
        return "http://thumbs.gfycat.com/" + id + "-mobile.mp4";
    }
}
