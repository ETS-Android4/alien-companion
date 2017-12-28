package com.gDyejeekis.aliencompanion.api.utils.httpClient;

import android.os.SystemClock;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.entity.OAuthToken;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.RedditOAuth;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by George on 5/27/2015.
 */
public class RedditHttpClient implements HttpClient {

    public static final String TAG = "RedditHttpClient";

    public static final boolean DEBUG_REQUESTS = false;

    public static final boolean ALWAYS_USE_OKHTTP = true;

    public static final String ALIEN_COMPANION_USER_AGENT = "android:com.gDyejeekis.aliencompanion:v" + MyApplication.currentVersion + " (by /u/ubercharge_ready)";

    private String userAgent = ALIEN_COMPANION_USER_AGENT;

    private OkHttpClient okHttpClient = MyApplication.okHttpClient;

    private String accessToken;

    private User user;

    private boolean renewTokenInstance = false;

    public RedditHttpClient() {
        accessToken = MyApplication.currentAccessToken;
    }

    public RedditHttpClient(User user) {
        accessToken = user.getTokenObject().accessToken;
        this.user = user;
    }

    public void setRenewTokenInstance(boolean flag) {
        renewTokenInstance = flag;
    }

    public Response get(String baseUrl, String urlPath, String cookie) throws RetrievalFailedException {
        if (ALWAYS_USE_OKHTTP) {
            return getWithOkHttp(baseUrl, urlPath, cookie);
        } else {
            return getWithHttpConnection(baseUrl, urlPath, cookie);
        }
    }

    public Response getWithHttpConnection(String baseUrl, String urlPath, String cookie) throws RetrievalFailedException {
        tokenCheck();

        HttpURLConnection connection = null;
        final String url = baseUrl + urlPath;
        Log.d(TAG, "GET request to  " + url);
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            if(RedditOAuth.useOAuth2) connection.setRequestProperty("Authorization", "bearer " + accessToken);
            else connection.setRequestProperty("Cookie", "reddit_session=" + cookie);
            connection.setDoInput(true);
            //connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);


            InputStream inputStream = connection.getInputStream();
            String content = ConvertUtils.convertStreamToString(inputStream, "UTF-8");
            if (DEBUG_REQUESTS) {
                GeneralUtils.printHttpRequestProperties(connection);
                GeneralUtils.printHttpRequestHeaders(connection);
                Log.d(TAG, "response code: " + connection.getResponseCode());
                GeneralUtils.printHttpResponseBody(content);
            }
            inputStream.close();

            Object responseObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, responseObject, connection);

            if (result.getResponseObject() == null) {
                throw new RetrievalFailedException("The given URI path does not exist on Reddit: " + url);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new RetrievalFailedException("Input/output failed when retrieving from URI path: " + url);
        } catch (org.json.simple.parser.ParseException e) {
            throw new RetrievalFailedException("Failed to parse response from GET request to URI path: " + url);
        } finally {
            //if(inputStream != null) {
            //    IOUtils.closeQuietly(inputStream);
            //}
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public Response getWithOkHttp(String baseUrl, String urlPath, String cookie) throws RetrievalFailedException {
        tokenCheck();
        final String url = baseUrl + urlPath;
        Log.d(TAG, "GET request to  " + url);
        try {
            Request.Builder builder = new Request.Builder().url(url);
            builder.addHeader("User-Agent", userAgent);
            addAuthorizatonHeaders(builder, cookie);
            Request request = builder.build();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            String content = response.body().string();
            if (DEBUG_REQUESTS) {
                Log.d(TAG, "request body: " + request.body());
                Log.d(TAG, "request headers: " + request.headers());
                Log.d(TAG, "response code: " + response.code());
                Log.d(TAG, "response headers: " + response.headers());
                GeneralUtils.printHttpResponseBody(content);
            }
            response.close();

            Object parsedObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, parsedObject, null);

            if (result.getResponseObject() == null) {
                throw new RetrievalFailedException("The given URI path does not exist on Reddit: " + url);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new RetrievalFailedException("Input/output failed when retrieving from URI path: " + url);
        } catch (org.json.simple.parser.ParseException e) {
            throw new RetrievalFailedException("Failed to parse response from GET request to URI path: " + url);
        }
    }

    public Response post(String baseUrl, RequestBody body, String urlPath, String cookie) {
        tokenCheck();
        final String url = baseUrl + urlPath;
        Log.d(TAG, "POST request to " + url);
        try {
            Request.Builder builder = new Request.Builder().url(url).post(body);
            builder.addHeader("User-Agent", userAgent);
            addAuthorizatonHeaders(builder, cookie);
            Request request = builder.build();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            String content = response.body().string();
            if (DEBUG_REQUESTS) {
                Log.d(TAG, "request body: " + request.body());
                Log.d(TAG, "request headers: " + request.headers());
                Log.d(TAG, "response code: " + response.code());
                Log.d(TAG, "response headers: " + response.headers());
                GeneralUtils.printHttpResponseBody(content);
            }
            response.close();

            Object parsedObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, parsedObject, null);

            if (result.getResponseObject() == null) {
                throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + url);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new ActionFailedException("Input/output failed when retrieving from URI path: " + url);
        } catch (ParseException e) {
            throw new ActionFailedException("Failed to parse response from POST request to URI path: " + url);
        }
    }

    public Response put(String baseUrl, RequestBody body, String urlPath, String cookie) {
        tokenCheck();
        final String url = baseUrl + urlPath;
        Log.d(TAG, "PUT request to " + url);
        try {
            Request.Builder builder = new Request.Builder().url(url).put(body);
            builder.addHeader("User-Agent", userAgent);
            addAuthorizatonHeaders(builder, cookie);
            Request request = builder.build();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            String content = response.body().string();
            if (DEBUG_REQUESTS) {
                Log.d(TAG, "request body: " + request.body());
                Log.d(TAG, "request headers: " + request.headers());
                Log.d(TAG, "response code: " + response.code());
                Log.d(TAG, "response headers: " + response.headers());
                GeneralUtils.printHttpResponseBody(content);
            }
            response.close();

            Object parsedObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, parsedObject, null);

            if (result.getResponseObject() == null) {
                throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + url);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new ActionFailedException("Input/output failed when retrieving from URI path: " + url);
        } catch (ParseException e) {
            throw new ActionFailedException("Failed to parse response from PUT request to URI path: " + url);
        }
    }

    public Response delete(String baseUrl, RequestBody body, String urlPath, String cookie) {
        tokenCheck();
        final String url = baseUrl + urlPath;
        Log.d(TAG, "DELETE request to " + url);
        try {
            Request.Builder builder = new Request.Builder().url(url).delete(body);
            builder.addHeader("User-Agent", userAgent);
            addAuthorizatonHeaders(builder, cookie);
            Request request = builder.build();
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            String content = response.body().string();
            if (DEBUG_REQUESTS) {
                Log.d(TAG, "request body: " + request.body());
                Log.d(TAG, "request headers: " + request.headers());
                Log.d(TAG, "response code: " + response.code());
                Log.d(TAG, "response headers: " + response.headers());
                GeneralUtils.printHttpResponseBody(content);
            }
            response.close();

            Object parsedObject = new JSONParser().parse(content);
            Response result = new HttpResponse(content, parsedObject, null);

            if (result.getResponseObject() == null) {
                throw new ActionFailedException("Due to unknown reasons, the response was undefined for URI path: " + url);
            } else {
                return result;
            }
        } catch (IOException e) {
            throw new ActionFailedException("Input/output failed when retrieving from URI path: " + url);
        } catch (ParseException e) {
            throw new ActionFailedException("Failed to parse response from DELETE request to URI path: " + url);
        }
    }

    public void setUserAgent(String agent) {
        this.userAgent = agent;
    }

    private void tokenCheck() {
        while(MyApplication.renewingToken && !renewTokenInstance) {
            Log.d(TAG, "Waiting 100ms for access token to be renewed..");
            SystemClock.sleep(100);
        }
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
                            RedditHttpClient httpClient = new RedditHttpClient();
                            httpClient.setRenewTokenInstance(true);
                            OAuthToken token = RedditOAuth.getApplicationToken(httpClient);
                            MyApplication.currentAccount.setToken(token);
                            MyApplication.currentAccessToken = token.accessToken;
                            accessToken = MyApplication.currentAccessToken;
                            MyApplication.renewingToken = false;
                            MyApplication.accountChanges = true;
                        } else {
                            MyApplication.currentAccount.getToken().checkToken();
                            accessToken = MyApplication.currentAccessToken;
                        }
                    }
                }
                else if(!MyApplication.renewingUserToken) {
                    user.getTokenObject().checkToken(user);
                    accessToken = user.getTokenObject().accessToken;
                }
            }
        } catch (Exception e) {
            MyApplication.renewingToken = false;
            Log.e(RedditOAuth.TAG, "Error renewing oauth token");
            e.printStackTrace();
        }
    }

    private void addAuthorizatonHeaders(Request.Builder builder, String cookie) {
        if (RedditOAuth.useOAuth2) {
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
    }

}
