package com.dyejeekis.aliencompanion.api.utils.httpClient;

import android.util.Log;

import com.dyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.utils.ApiEndpointUtils;

import org.apache.commons.io.IOUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by George on 5/27/2015.
 */
public class RedditHttpClient implements HttpClient, Serializable {

    private String userAgent = "android:com.george.redditreader:v0.1 (by /u/ubercharge_ready)";

    public Response get(String urlPath, String cookie) throws RetrievalFailedException {

        HttpURLConnection connection = null;
        //InputStream inputStream = null;

        try {
            URL url = new URL(ApiEndpointUtils.REDDIT_BASE_URL + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestProperty("Cookie", "reddit_session="+cookie);
            connection.setDoInput(true);
            //connection.setDoOutput(true);
            //connection.setConnectTimeout(5000);
            //connection.setReadTimeout(5000);

            //printRequestProperties(connection);

            InputStream inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);

            Log.d("inputstream object: ", content);
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
            //if(inputStream != null) {
            //    IOUtils.closeQuietly(inputStream);
            //}
            if(connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public Response post(String apiParams, String urlPath, String cookie) {
        HttpURLConnection connection = null;
        //OutputStream outputStream = null;
        //InputStream inputStream = null;
        try {
            URL url = new URL(ApiEndpointUtils.REDDIT_BASE_URL + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestProperty("Cookie", "reddit_session="+cookie);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setChunkedStreamingMode(1000);

            //printRequestProperties(connection);

            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(apiParams);

            writer.flush();
            writer.close();
            outputStream.close();

            InputStream inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);

            Log.d("inputstream object: ", content);
            Object responseObject = new JSONParser().parse(content);

            Response result = new HttpResponse(content, responseObject, connection);

            //printHeaderFields(connection);

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
            //if(outputStream != null) {
            //    IOUtils.closeQuietly(outputStream);
            //    IOUtils.closeQuietly(inputStream);
            //}
            if(connection != null) {
                connection.disconnect();
            }
        }
        //return null;
    }

    public void setUserAgent(String agent) {
        this.userAgent = agent;
    }

    ///**
    // * Convert a API parameters to a appropriate list.
    // *
    // * @param apiParams Input string, for example 'a=2894&b=194'
    // * @return List of name value pairs to pass with the POST request
    // */
    //private List<StringPair> convertRequestStringToList(String apiParams) {
    //    List<StringPair> values = new ArrayList<>();
    //    if (apiParams != null && !apiParams.isEmpty()) {
    //        String[] valuePairs = apiParams.split("&");
    //        for (String valuePair : valuePairs) {
    //            String[] nameValue = valuePair.split("=");
    //            if (nameValue.length == 1) { //there is no cookie if we are not signed in
    //                values.add(new StringPair(nameValue[0], ""));
    //            } else {
    //                values.add(new StringPair(nameValue[0], nameValue[1]));
    //            }
    //        }
    //    }
    //    return values;
    //}

    private void printRequestProperties(HttpURLConnection connection) {
        Log.d("Request properties", "Request method: " + connection.getRequestMethod());
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
        Log.d("Header fields", "--------------------------------------------------------------------------------------------");
    }
}
