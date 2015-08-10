package com.george.redditreader.api.utils.httpClient;

import android.util.Log;

import com.george.redditreader.api.exception.ActionFailedException;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.utils.ApiEndpointUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 5/27/2015.
 */
public class RedditHttpClient implements HttpClient {

    private String userAgent = "android:com.george.redditreader:v0.1 (by /u/ubercharge_ready)";

    public Response get(String urlPath, String cookie) throws RetrievalFailedException {

        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(ApiEndpointUtils.REDDIT_BASE_URL + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setDoInput(true);
            //connection.setDoOutput(true);
            //connection.setConnectTimeout(5000);
            //connection.setReadTimeout(5000);

            //printRequestProperties(connection);

            inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");

            //Log.d("inputstream object: ", content);
            Object responseObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, responseObject, connection);

            //printHeaderFields(connection);

            if (result.getResponseObject() == null) {
                throw new RetrievalFailedException("The given URI path does not exist on Reddit: " + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new RetrievalFailedException("Input/output failed when retrieving from URI path: " + urlPath);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
            if(connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public Response post(String apiParams, String urlPath, String cookie) {
        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(ApiEndpointUtils.REDDIT_BASE_URL + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(1000);

            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");

            //Log.d("inputstream object: ", content);
            Object responseObject = new JSONParser().parse(content);

            Response result = new HttpResponse(content, responseObject, connection);

            if (result.getResponseObject() == null) {
                throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new ActionFailedException("Input/output failed when retrieving from URI path: " + urlPath);
        } catch (ParseException e) {
            throw new ActionFailedException("Failed to parse the response from GET request to URI path: " + urlPath);
        } finally {
            if(outputStream != null) {
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(inputStream);
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
        //return null;
    }

    public void setUserAgent(String agent) {
        this.userAgent = agent;
    }

    /**
     * Convert a API parameters to a appropriate list.
     *
     * @param apiParams Input string, for example 'a=2894&b=194'
     * @return List of name value pairs to pass with the POST request
     */
    private List<NameValuePair> convertRequestStringToList(String apiParams) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (apiParams != null && !apiParams.isEmpty()) {
            String[] valuePairs = apiParams.split("&");
            for (String valuePair : valuePairs) {
                String[] nameValue = valuePair.split("=");
                if (nameValue.length == 1) { //there is no cookie if we are not signed in
                    params.add(new BasicNameValuePair(nameValue[0], ""));
                } else {
                    params.add(new BasicNameValuePair(nameValue[0], nameValue[1]));
                }
            }
        }
        return params;
    }

    private void printRequestProperties(HttpURLConnection connection) {
        for (String header : connection.getRequestProperties().keySet()) {
            if (header != null) {
                for (String value : connection.getRequestProperties().get(header)) {
                    Log.d("Request properties", header + ":" + value);
                }
            }
        }
    }

    private void printHeaderFields(HttpURLConnection connection) {
        for (String header : connection.getHeaderFields().keySet()) {
            if (header != null) {
                for (String value : connection.getHeaderFields().get(header)) {
                    Log.d("Header fields", header + ":" + value);
                }
            }
        }
    }
}
