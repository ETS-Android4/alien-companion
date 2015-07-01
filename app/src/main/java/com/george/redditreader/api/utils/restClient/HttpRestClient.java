package com.george.redditreader.api.utils.restClient;

import android.util.Log;

import com.george.redditreader.api.exception.InvalidURIException;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.utils.ApiEndpointUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * Created by George on 5/27/2015.
 */
public class HttpRestClient implements RestClient {

    private String userAgent;

    public Response get(String urlPath, String cookie) throws RetrievalFailedException {

        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(ApiEndpointUtils.REDDIT_BASE_URL + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            //connection.setConnectTimeout(5000);
            //connection.setReadTimeout(5000);
            inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");

            //Log.d("inputstream object: ", content);
            Object responseObject = new JSONParser().parse(content);
            Response result = new RestResponse(content, responseObject, connection);

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

        return null;
    }

    public void setUserAgent(String agent) {
        this.userAgent = agent;
    }
}
