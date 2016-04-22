package com.gDyejeekis.aliencompanion.api.utils.httpClient;

import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.entity.OAuthToken;
import com.gDyejeekis.aliencompanion.api.entity.User;
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

import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by George on 5/27/2015.
 */
public class RedditHttpClient implements HttpClient, Serializable {

    public static final String TAG = "RedditHttpClient";

    private String userAgent = "android:com.gDyejeekis.aliencompanion:v" + MyApplication.currentVersion + " (by /u/alien_companion)";

    private String accessToken;

    private User user;

    public RedditHttpClient() {
        accessToken = MyApplication.currentAccessToken;
        this.user = null;
    }

    public RedditHttpClient(User user) {
        accessToken = user.getTokenObject().accessToken;
        this.user = user;
    }

    public Response get(String baseUrl, String urlPath, String cookie) throws RetrievalFailedException { //TODO: re-write with okhttp
        tokenCheck();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + urlPath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            if(RedditOAuth.useOAuth2) connection.setRequestProperty("Authorization", "bearer " + accessToken);
            else connection.setRequestProperty("Cookie", "reddit_session=" + cookie);
            connection.setDoInput(true);
            //connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            Log.d(TAG, "GET request to  " + baseUrl + urlPath);
            //printRequestProperties(connection);

            InputStream inputStream = connection.getInputStream();
            //Log.d(TAG, "response code: " + connection.getResponseCode());

            String content = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);

            Log.d(TAG, "inputstream object: " + content);
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

    //public Response post(String baseUrl, String apiParams, String urlPath, String cookie) {
    //    tokenCheck();
//
    //    HttpURLConnection connection = null;
    //    try {
    //        URL url = new URL(baseUrl + urlPath);
    //        connection = (HttpURLConnection) url.openConnection();
    //        connection.setUseCaches(false);
    //        connection.setRequestMethod("POST");
    //        connection.setRequestProperty("User-Agent", userAgent);
    //        //byte[] utf8Bytes = apiParams.getBytes("UTF-8");
    //        //connection.setRequestProperty("Content-Length", String.valueOf(utf8Bytes.length));
    //        if(RedditOAuth.useOAuth2 && cookie == null) {
    //            if(MyApplication.currentAccessToken!=null) connection.setRequestProperty("Authorization", "bearer " + MyApplication.currentAccessToken);
    //            else {
    //                String userCredentials = RedditOAuth.MY_APP_ID + ":" + RedditOAuth.MY_APP_SECRET;
    //                String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), Base64.DEFAULT));
    //                connection.setRequestProperty("Authorization", basicAuth);
    //            }
    //        }
    //        else connection.setRequestProperty("Cookie", "reddit_session="+cookie);
    //        connection.setDoOutput(true);
    //        connection.setDoInput(true);
    //        connection.setChunkedStreamingMode(1000);
//
    //        Log.d("geotest", "POST request to  " + baseUrl + urlPath);
    //        printRequestProperties(connection);
//
    //        OutputStream outputStream = connection.getOutputStream();
    //        //Log.d("geotest", "response code: " + connection.getResponseCode());
//
    //        BufferedWriter writer = new BufferedWriter(
    //                new OutputStreamWriter(outputStream, "UTF-8"));
    //        writer.write(apiParams);
//
    //        writer.flush();
    //        writer.close();
    //        outputStream.close();
//
    //        InputStream inputStream = connection.getInputStream();
//
    //        String content = IOUtils.toString(inputStream, "UTF-8");
    //        IOUtils.closeQuietly(inputStream);
//
    //        Log.d("inputstream object: ", content);
    //        Object responseObject = new JSONParser().parse(content);
//
    //        Response result = new HttpResponse(content, responseObject, connection);
//
    //        printHeaderFields(connection);
//
    //        if (result.getResponseObject() == null) {
    //            throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + baseUrl + urlPath);
    //        } else {
    //            return result;
    //        }
    //    } catch (IOException e) {
    //        throw new ActionFailedException("Input/output failed when retrieving from URI path: " + baseUrl + urlPath);
    //    } catch (ParseException e) {
    //        throw new ActionFailedException("Failed to parse the response from POST request to URI path: " + baseUrl + urlPath);
    //    } finally {
    //        //if(outputStream != null) {
    //        //    IOUtils.closeQuietly(outputStream);
    //        //    IOUtils.closeQuietly(inputStream);
    //        //}
    //        if(connection != null) {
    //            connection.disconnect();
    //        }
    //    }
    //    //return null;
    //}

    public Response post(String baseUrl, RequestBody body, String urlPath, String cookie) {
        tokenCheck();
        Log.d(TAG, "POST request to " + baseUrl + urlPath);
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(baseUrl + urlPath).post(body);
            if (RedditOAuth.useOAuth2 && cookie == null) {
                String authHeader;
                if (accessToken != null) {
                    authHeader = "bearer " + accessToken;
                } else {
                    authHeader = Credentials.basic(RedditOAuth.MY_APP_ID, RedditOAuth.MY_APP_SECRET);
                }
                builder.addHeader("Authorization", authHeader);
            } else {
                builder.addHeader("Cookie", "reddit_session=" + cookie);
            }
            builder.addHeader("User-Agent", userAgent);
            Request request = builder.build();
            okhttp3.Response response = client.newCall(request).execute();
            String content = response.body().string();
            Log.d(TAG, "request body: " + request.body());
            Log.d(TAG, "request headers: " + request.headers());
            Log.d(TAG, "response code: " + response.code());
            Log.d(TAG, "response headers: " + response.headers());
            Log.d(TAG, "inputstream: " + content);

            Object parsedObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, parsedObject, null);

            if (result.getResponseObject() == null) {
                throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + baseUrl + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new ActionFailedException("Input/output failed when retrieving from URI path: " + baseUrl + urlPath);
        } catch (ParseException e) {
            throw new ActionFailedException("Failed to parse the response from POST request to URI path: " + baseUrl + urlPath);
        }
    }

    public Response put(String baseUrl, RequestBody body, String urlPath, String cookie) {
        tokenCheck();
        Log.d(TAG, "PUT request to " + baseUrl + urlPath);
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(baseUrl + urlPath).put(body);
            if (RedditOAuth.useOAuth2 && cookie == null) {
                String authHeader;
                if (accessToken != null) {
                    authHeader = "bearer " + accessToken;
                } else {
                    authHeader = Credentials.basic(RedditOAuth.MY_APP_ID, RedditOAuth.MY_APP_SECRET);
                }
                builder.addHeader("Authorization", authHeader);
            } else {
                builder.addHeader("Cookie", "reddit_session=" + cookie);
            }
            builder.addHeader("User-Agent", userAgent);
            Request request = builder.build();
            okhttp3.Response response = client.newCall(request).execute();
            String content = response.body().string();
            Log.d(TAG, "request body: " + request.body());
            Log.d(TAG, "request headers: " + request.headers());
            Log.d(TAG, "response code: " + response.code());
            Log.d(TAG, "response headers: " + response.headers());
            Log.d(TAG, "inputstream: " + content);

            Object parsedObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, parsedObject, null);

            if (result.getResponseObject() == null) {
                throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + baseUrl + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new ActionFailedException("Input/output failed when retrieving from URI path: " + baseUrl + urlPath);
        } catch (ParseException e) {
            throw new ActionFailedException("Failed to parse the response from PUT request to URI path: " + baseUrl + urlPath);
        }
    }

    public Response delete(String baseUrl, RequestBody body, String urlPath, String cookie) {
        tokenCheck();
        Log.d(TAG, "DELETE request to " + baseUrl + urlPath);
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(baseUrl + urlPath).delete(body);
            if (RedditOAuth.useOAuth2 && cookie == null) {
                String authHeader;
                if (accessToken != null) {
                    authHeader = "bearer " + accessToken;
                } else {
                    authHeader = Credentials.basic(RedditOAuth.MY_APP_ID, RedditOAuth.MY_APP_SECRET);
                }
                builder.addHeader("Authorization", authHeader);
            } else {
                builder.addHeader("Cookie", "reddit_session=" + cookie);
            }
            builder.addHeader("User-Agent", userAgent);
            Request request = builder.build();
            okhttp3.Response response = client.newCall(request).execute();
            String content = response.body().string();
            Log.d(TAG, "request body: " + request.body());
            Log.d(TAG, "request headers: " + request.headers());
            Log.d(TAG, "response code: " + response.code());
            Log.d(TAG, "response headers: " + response.headers());
            Log.d(TAG, "inputstream: " + content);

            Object parsedObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, parsedObject, null);

            if (result.getResponseObject() == null) {
                throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + baseUrl + urlPath);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new ActionFailedException("Input/output failed when retrieving from URI path: " + baseUrl + urlPath);
        } catch (ParseException e) {
            throw new ActionFailedException("Failed to parse the response from DELETE request to URI path: " + baseUrl + urlPath);
        }
    }

    public void setUserAgent(String agent) {
        this.userAgent = agent;
    }


    //private void tokenCheck() {
    //    try {
    //        if (RedditOAuth.useOAuth2 && !MyApplication.renewingToken) {
    //            while(MyApplication.currentAccount==null) {
    //                Log.d(TAG, "MyApplication.currentAccount is null, waiting..");
    //                SystemClock.sleep(100);
    //            }
    //            if (MyApplication.currentAccessToken == null && !MyApplication.currentAccount.loggedIn) {
    //                MyApplication.renewingToken = true;
    //                OAuthToken token = RedditOAuth.getApplicationToken(new RedditHttpClient());
    //                MyApplication.currentAccount.setToken(token);
    //                MyApplication.currentAccessToken = token.accessToken;
    //                MyApplication.renewingToken = false;
    //                MyApplication.accountChanges = true;
    //            } else {
    //                MyApplication.currentAccount.getToken().checkToken();
    //            }
    //        }
    //    } catch (ActionFailedException e) {
    //        MyApplication.renewingToken = false;
    //        Log.e(RedditOAuth.TAG, "Error renewing oauth token");
    //        e.printStackTrace();
    //    }
    //}

    private void tokenCheck() {
        try {
            if(RedditOAuth.useOAuth2) {
                if(user == null) {
                    if(!MyApplication.renewingToken) {
                        while(MyApplication.currentAccount==null) {
                            Log.d(TAG, "MyApplication.currentAccount is null, waiting..");
                            SystemClock.sleep(100);
                        }
                        if (MyApplication.currentAccessToken == null && !MyApplication.currentAccount.loggedIn) {
                            MyApplication.renewingToken = true;
                            OAuthToken token = RedditOAuth.getApplicationToken(new RedditHttpClient());
                            MyApplication.currentAccount.setToken(token);
                            MyApplication.currentAccessToken = token.accessToken;
                            MyApplication.renewingToken = false;
                            MyApplication.accountChanges = true;
                        } else {
                            MyApplication.currentAccount.getToken().checkToken();
                        }
                    }
                }
                else if(!MyApplication.renewingUserToken) {
                    user.getTokenObject().checkToken(user);
                }
            }
        } catch (Exception e) {
            MyApplication.renewingToken = false;
            Log.e(RedditOAuth.TAG, "Error renewing oauth token");
            e.printStackTrace();
        }
    }

    private void printRequestProperties(HttpURLConnection connection) {
        Log.d(TAG, "Request Properties: Request method: " + connection.getRequestMethod());
        for (String header : connection.getRequestProperties().keySet()) {
            if (header != null) {
                for (String value : connection.getRequestProperties().get(header)) {
                    Log.d(TAG, "Request properties: " + header + ":" + value);
                }
            }
        }
    }

    private void printHeaderFields(HttpURLConnection connection) {
        for (String header : connection.getHeaderFields().keySet()) {
            if (header != null) {
                for (String value : connection.getHeaderFields().get(header)) {
                    Log.d(TAG, "Header fields: " + header + ":" + value);
                }
            }
        }
        Log.d(TAG, "--------------------------------------------------------------------------------------------");
    }
}
