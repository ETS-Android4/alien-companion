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

import okhttp3.Request;
import okhttp3.Response;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

/**
 * Created by George on 1/19/2017.
 */

public class StreamableTask extends AsyncTask<String, Void, String> {

    private Context context;

    public StreamableTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            final String originalUrl = params[0];
            return getStreamableDirectUrl(originalUrl);

            //String url = getStreamableDirectUrl(originalUrl);
            //File file = new File(context.getCacheDir(), LinkHandler.getStreamableId(originalUrl) + ".mp4");
            //GeneralUtils.downloadToFileSync(url, file);
            //return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Context getContext() {
        return context;
    }

    // this method makes an API call to api.streamable.com (synchronously)
    //public static String getStreamableDirectUrl(String originalUrl) throws IOException, ParseException {
    //    final String url = "https://api.streamable.com/videos/" + LinkUtils.getStreamableId(originalUrl);
    //    Log.d("Streamable", "GET request to " + url);
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
    //    Log.d("Streamable", content);
    //    Object response = new JSONParser().parse(content);
    //    JSONObject files = (JSONObject) ((JSONObject) response).get("files");
    //    JSONObject mp4 = (JSONObject) files.get("mp4");
//
    //    String directUrl = safeJsonToString(mp4.get("url"));
    //    if(!directUrl.matches("http(s)?\\:\\/\\/.*")) {
    //        if(directUrl.startsWith("//")) {
    //            return "https:" + directUrl;
    //        }
    //        else {
    //            return "https://" + directUrl;
    //        }
    //    }
    //    return directUrl;
    //}

    public static String getStreamableDirectUrl(String originalUrl) throws IOException, ParseException {
        final String url = "https://api.streamable.com/videos/" + LinkUtils.getStreamableId(originalUrl);
        Log.d("Streamable", "GET request to " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = MyApplication.okHttpClient.newCall(request).execute();
        String content = response.body().string();
        response.close();

        Object responseObject = new JSONParser().parse(content);
        JSONObject files = (JSONObject) ((JSONObject) responseObject).get("files");
        JSONObject mp4 = (JSONObject) files.get("mp4");

        String directUrl = safeJsonToString(mp4.get("url"));
        if(!directUrl.matches("http(s)?\\:\\/\\/.*")) {
            if(directUrl.startsWith("//")) {
                return "https:" + directUrl;
            }
            else {
                return "https://" + directUrl;
            }
        }
        return directUrl;
    }
}
