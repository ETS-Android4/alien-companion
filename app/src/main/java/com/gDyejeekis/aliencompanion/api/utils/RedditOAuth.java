package com.gDyejeekis.aliencompanion.api.utils;

import android.util.Log;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.RandomString;
import com.gDyejeekis.aliencompanion.api.entity.OAuthToken;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import org.json.simple.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by sound on 10/22/2015.
 */
public class RedditOAuth {

    public static final boolean useOAuth2 = true;

    // This line is GAE specific!  for detecting when running on local admin
    public static final boolean production = false;

    public static final String OAUTH_API_DOMAIN = "https://oauth.reddit.com";

    // Step 1. Send user to auth URL
    public static final String OAUTH_AUTH_URL = "https://www.reddit.com/api/v1/authorize.compact?";

    // Step 2. Reddit sends user to REDIRECT_URI
    private static final String REDIRECT_URI = production ? "my app domain here" + "/auth" //TODO: look into app domain for redirect uri
            : "redditoauthtest://response";

    // Step 3. Get token
    public static final String OAUTH_TOKEN_URL = "/api/v1/access_token";

    // I think it is easier to create 2 reddit apps (one with 127.0.0.1 redirect URI)
    public static final String MY_APP_ID = production ? "u4geEADEYRqWUw" : "u4geEADEYRqWUw";
    public static final String MY_APP_SECRET = production ? "" : ""; //installed apps can't keep a secret

    public static final boolean USE_IMPLICIT_GRANT_FLOW = false;

    public static final String RESPONSE_TYPE_CODE = "code";

    public static final String RESPONSE_TYPE_TOKEN = "token";

    public static final String RESPONSE_TYPE_STRING = (USE_IMPLICIT_GRANT_FLOW) ? RESPONSE_TYPE_TOKEN : RESPONSE_TYPE_CODE;

    public static final boolean permanentAccess = true; //The implicit grant flow does not allow permanent tokens.

    public static final String OAUTH_TOKEN_DURATION_TEMPORARY = "temporary";

    public static final String OAUTH_TOKEN_DURATION_PERMANENT = "permanent";

    public static final String OAUTH_TOKEN_DURATION_STRING = (permanentAccess && !USE_IMPLICIT_GRANT_FLOW) ? OAUTH_TOKEN_DURATION_PERMANENT : OAUTH_TOKEN_DURATION_TEMPORARY;

    public static final String SCOPE_ID = "identity";

    public static final String SCOPE_EDIT = "edit";

    public static final String SCOPE_FLAIR = "flair";

    public static final String SCOPE_HISTORY = "history";

    public static final String SCOPE_MOD_CONFIG = "modconfig";

    public static final String SCOPE_MOD_FLAIR = "modflair";

    public static final String SCOPE_MOD_LOG = "modlog";

    public static final String SCOPE_MOD_POSTS = "modposts";

    public static final String SCOPE_MOD_WIKI = "modwiki";

    public static final String SCOPE_MY_SUBREDDITS = "mysubreddits";

    public static final String SCOPE_PRIVATE_MESSAGES = "privatemessages";

    public static final String SCOPE_READ = "read";

    public static final String SCOPE_REPORT = "report";

    public static final String SCOPE_SAVE = "save";

    public static final String SCOPE_SUBMIT = "submit";

    public static final String SCOPE_SUBSCRIBE = "subscribe";

    public static final String SCOPE_VOTE = "vote";

    public static final String SCOPE_WIKI_EDIT = "wikiedit";

    public static final String SCOPE_WIKI_READ = "wikiread";

    public static final String SCOPES = SCOPE_ID + "," + SCOPE_MY_SUBREDDITS + "," + SCOPE_EDIT + "," + SCOPE_FLAIR  + "," + SCOPE_HISTORY + "," + SCOPE_PRIVATE_MESSAGES + "," +
            SCOPE_READ + "," + SCOPE_REPORT + "," + SCOPE_SAVE + "," + SCOPE_SUBMIT + "," + SCOPE_SUBSCRIBE + "," + SCOPE_VOTE;

    // Field name in responses
    public static final String ACCESS_TOKEN_NAME = "access_token";
    public static final String REFRESH_TOKEN_NAME = "refresh_token";

    public static String getOauthAuthUrl() {
        String randomString = new RandomString(10).nextString(); //TODO: make random string unique
        return OAUTH_AUTH_URL + "client_id=" + MY_APP_ID + "&response_type=" + RESPONSE_TYPE_STRING + "&state=" + randomString
                + "&redirect_uri=" + REDIRECT_URI + "&duration=" + OAUTH_TOKEN_DURATION_STRING + "&scope=" + SCOPES;
    }

    public static String getAuthorizationCode(String redirectUrl) {
        String code = null;

        String pattern = "redditoauthtest:\\/\\/response\\?state=.*&code=(.*)";
        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(redirectUrl);
        if(matcher.find()) {
            code = matcher.group(1);
        }
        Log.d("geotest", "auth code: " + code);

        return code;
    }

    public static OAuthToken getOAuthToken(HttpClient httpClient, String oauthCode) {
        Log.d("geotest", "retrieving account token..");
        RequestBody body = new FormBody.Builder().add("grant_type", "authorization_code").add("code", oauthCode).add("redirect_uri", REDIRECT_URI).build();
        //JSONObject jsonObject = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_BASE_URL_SECURE, "grant_type=authorization_code&code=" + oauthCode +"&redirect_uri="
        //        + REDIRECT_URI, OAUTH_TOKEN_URL, null).getResponseObject();
        JSONObject jsonObject = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_BASE_URL_SECURE, body, OAUTH_TOKEN_URL, null).getResponseObject();
        return new OAuthToken(jsonObject, true);
    }

    public static void refreshToken(HttpClient httpClient, OAuthToken token) {
        Log.d("geotest", "refreshing token..");
        RequestBody body = new FormBody.Builder().add("grant_type", "refresh_token").add("refresh_token", token.refreshToken).build();
        JSONObject jsonObject = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_BASE_URL_SECURE, body, OAUTH_TOKEN_URL,
                null).getResponseObject();
        OAuthToken refreshedToken = new OAuthToken(jsonObject, false);
        token.accessToken = refreshedToken.accessToken;
        token.expiresIn = refreshedToken.expiresIn;
        token.setExpiration(refreshedToken.expiresIn);
    }

    //public static OAuthToken getOAuthToken(String url) {
    //    return null;
    //}

    public static OAuthToken getApplicationToken(HttpClient httpClient) {
        Log.d("geotest", "retrieving application token..");
        //Log.d("geotest", "post data: " + "grant_type=https://oauth.reddit.com/grants/installed_client&device_id=" + MyApplication.deviceID);
        RequestBody body = new FormBody.Builder().add("grant_type", "https://oauth.reddit.com/grants/installed_client").add("device_id", MyApplication.deviceID).build();
        JSONObject jsonObject = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_BASE_URL_SECURE, body, OAUTH_TOKEN_URL, null).getResponseObject();
        return new OAuthToken(jsonObject, false);
    }

}
