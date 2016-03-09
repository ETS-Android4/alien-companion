package com.gDyejeekis.aliencompanion.api.imgur;

import android.util.Log;

import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpResponse;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.Response;

import org.apache.commons.io.IOUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by George on 1/3/2016.
 */
public class ImgurHttpClient {

    public static final String CLIENT_ID = "438a0d502455ec6";

    private static final String DEBUG_STRING = "ImgurHttpClient";

    public Response get(String urlPath) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(ImgurApiEndpoints.BASE_IMGUR_URL + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Client-ID " + CLIENT_ID);
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            //Log.d(DEBUG_STRING, "GET request to  " + ImgurApiEndpoints.BASE_IMGUR_URL + urlPath);
            //printRequestProperties(connection);

            InputStream inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);

            //Log.d(DEBUG_STRING, "INPUTSTREAM OBJECT");
            Log.d(DEBUG_STRING, content);
            Object responseObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, responseObject, connection);

            //printHeaderFields(connection);

            if (result.getResponseObject() == null) {
                throw new RetrievalFailedException("The given URI path does not exist on Reddit: " + ImgurApiEndpoints.BASE_IMGUR_URL + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new RetrievalFailedException("Input/output failed when retrieving from URI path: " + ImgurApiEndpoints.BASE_IMGUR_URL + urlPath);
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if(connection!=null) connection.disconnect();
        }

        return null;
    }

    private void printRequestProperties(HttpURLConnection connection) {
        Log.d(DEBUG_STRING, "REQUEST PROPERTIES");
        Log.d(DEBUG_STRING, "Request method: " + connection.getRequestMethod());
        for (String header : connection.getRequestProperties().keySet()) {
            if (header != null) {
                for (String value : connection.getRequestProperties().get(header)) {
                    Log.d("Request properties", header + ":" + value);
                }
            }
        }
    }

    private void printHeaderFields(HttpURLConnection connection) {
        Log.d(DEBUG_STRING, "HEADER FIELDS");
        for (String header : connection.getHeaderFields().keySet()) {
            if (header != null) {
                for (String value : connection.getHeaderFields().get(header)) {
                    Log.d(DEBUG_STRING, header + ":" + value);
                }
            }
        }
        Log.d(DEBUG_STRING, "--------------------------------------------------------------------------------------------");
    }
}
