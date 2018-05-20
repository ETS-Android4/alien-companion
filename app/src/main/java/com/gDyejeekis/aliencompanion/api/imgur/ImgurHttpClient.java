package com.gDyejeekis.aliencompanion.api.imgur;

import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpResponse;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.Response;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Request;

/**
 * Created by George on 1/3/2016.
 */
public class ImgurHttpClient {

    private static final String TAG = "ImgurHttpClient";

    public static final String CLIENT_ID = "438a0d502455ec6";

    public static final boolean DEBUG_REQUESTS = false;

    public Response get (String urlPath) throws RetrievalFailedException {
        final String url = ImgurApiEndpoints.BASE_IMGUR_URL + urlPath;
        Log.d(TAG, "GET request to " + url);
        try {
            Request request = new Request.Builder()
                    .cacheControl(new CacheControl.Builder().maxStale(24, TimeUnit.HOURS).build())
                    .url(url).addHeader("Authorization", "Client-ID " + CLIENT_ID)
                    .build();
            okhttp3.Response response = MyApplication.okHttpClient.newCall(request).execute();
            String content = response.body().string();
            if (DEBUG_REQUESTS) {
                Log.d(TAG, "request body: " + request.body());
                Log.d(TAG, "request headers: " + request.headers());
                Log.d(TAG, "response code: " + response.code());
                Log.d(TAG, "response headers: " + response.headers());
                GeneralUtils.printHttpResponseBody(TAG, content);
            }
            response.close();

            Object parsedObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, parsedObject, null);

            if (result.getResponseObject() == null) {
                throw new RetrievalFailedException("The given URI path does not exist on Reddit: " + url);
            } else {
                return result;
            }
        } catch (ParseException e) {
            throw new RetrievalFailedException("Failed to parse response from GET request to URI path: " + url);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RetrievalFailedException("Input/output failed when retrieving from URI path: " + url);
        }
    }

}
