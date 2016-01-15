package com.gDyejeekis.aliencompanion.api.utils.httpClient;

import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.OAuthToken;
import com.gDyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.RedditOAuth;

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

    private String userAgent = "android:com.gDyejeekis.aliencompanion:v" + MyApplication.currentVersion + " (by /u/alien_companion)";

    public Response get(String baseUrl, String urlPath, String cookie) throws RetrievalFailedException {
        tokenCheck();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            if(RedditOAuth.useOAuth2) connection.setRequestProperty("Authorization", "bearer " + MyApplication.currentAccessToken);
            else connection.setRequestProperty("Cookie", "reddit_session=" + cookie);
            connection.setDoInput(true);
            //connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            //printRequestProperties(connection);

            InputStream inputStream = connection.getInputStream();

            String content = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);

            Log.d("inputstream object: ", content);
            Object responseObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, responseObject, connection);

            //printHeaderFields(connection);

            if (result.getResponseObject() == null) {
                throw new RetrievalFailedException("The given URI path does not exist on Reddit: " + baseUrl + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new RetrievalFailedException("Input/output failed when retrieving from URI path: " + baseUrl + urlPath);
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

    public Response post(String baseUrl, String apiParams, String urlPath, String cookie) {
        tokenCheck();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", userAgent);
            if(RedditOAuth.useOAuth2 && cookie == null) {
                if(MyApplication.currentAccessToken!=null) connection.setRequestProperty("Authorization", "bearer " + MyApplication.currentAccessToken);
                else {
                    String userCredentials = RedditOAuth.MY_APP_ID + ":" + RedditOAuth.MY_APP_SECRET;
                    String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), Base64.DEFAULT));
                    connection.setRequestProperty("Authorization", basicAuth);
                }
            }
            else connection.setRequestProperty("Cookie", "reddit_session="+cookie);
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
                throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + baseUrl + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new ActionFailedException("Input/output failed when retrieving from URI path: " + baseUrl + urlPath);
        } catch (ParseException e) {
            throw new ActionFailedException("Failed to parse the response from GET request to URI path: " + baseUrl + urlPath);
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


    private void tokenCheck() {
        try {
            if (RedditOAuth.useOAuth2 && !MyApplication.renewingToken) {
                while(MyApplication.currentAccount==null) {
                    SystemClock.sleep(100);
                }
                if (MyApplication.currentAccessToken == null && !MyApplication.currentAccount.loggedIn) {
                    MyApplication.renewingToken = true;
                    OAuthToken token = RedditOAuth.getApplicationToken(new RedditHttpClient());
                    MyApplication.currentAccount.setToken(token);
                    MyApplication.currentAccessToken = token.accessToken;
                    MyApplication.renewingToken = false;
                    MyApplication.accountChanges = true;
                } else MyApplication.currentAccount.getToken().checkToken();
            }
        } catch (ActionFailedException e) {
            MyApplication.renewingToken = false;
            Log.e("geotest", "Error renewing oauth token");
            e.printStackTrace();
        }
    }

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
