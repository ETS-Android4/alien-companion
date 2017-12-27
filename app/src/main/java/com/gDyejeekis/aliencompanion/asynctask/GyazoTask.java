package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Request;
import okhttp3.Response;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

/**
 * Created by George on 1/16/2017.
 */

public class GyazoTask extends AsyncTask<String, Void, String> {

    private Context context;

    public GyazoTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            final String url = params[0];
            return getGyazoDirectUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getContext() {
        return context;
    }

    // this method makes an API call to api.gyazo.com (synchronously)
    //public static String getGyazoDirectUrl(String originalUrl) throws IOException, ParseException {
    //    final String url = "https://api.gyazo.com/api/oembed?url=" + originalUrl;
    //    Log.d("Gyazo", "GET request to " + url);
    //    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    //    connection.setUseCaches(true);
    //    connection.setRequestMethod("GET");
    //    connection.setDoInput(true);
    //    connection.setConnectTimeout(5000);
    //    connection.setReadTimeout(5000);
//
    //    InputStream inputStream = connection.getInputStream();
    //    String content = IOUtils.toString(inputStream, "UTF-8");
    //    IOUtils.closeQuietly(inputStream);
//
    //    Log.d("Gyazo", content);
    //    JSONObject gyazoJson = (JSONObject) new JSONParser().parse(content);
//
    //    return safeJsonToString(gyazoJson.get("url"));
    //}

    public static String getGyazoDirectUrl(String originalUrl) throws IOException, ParseException {
        final String url = "https://api.gyazo.com/api/oembed?url=" + originalUrl;
        Log.d("Gyazo", "GET request to " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = MyApplication.okHttpClient.newCall(request).execute();
        String content = response.body().string();
        response.close();

        JSONObject gyazoJson = (JSONObject) new JSONParser().parse(content);
        return safeJsonToString(gyazoJson.get("url"));
    }

}
