package com.gDyejeekis.aliencompanion.api.entity;

import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;
import com.gDyejeekis.aliencompanion.api.utils.RedditOAuth;

import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Date;

import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToLong;
import static com.gDyejeekis.aliencompanion.Utils.JsonUtils.safeJsonToString;

/**
 * Created by sound on 10/24/2015.
 */
public class OAuthToken implements Serializable {

    public String accessToken;
    public String tokenType;
    public long expiresIn;
    public long expiration;
    public String scope;
    public String refreshToken;
    public String state;

    public OAuthToken(JSONObject obj, boolean hasRefreshToken) {
        try {
            accessToken = safeJsonToString(obj.get("access_token"));
            tokenType = safeJsonToString(obj.get("token_type"));
            expiresIn = safeJsonToLong(obj.get("expires_in"));
            setExpiration(expiresIn);
            scope = safeJsonToString(obj.get("scope"));
            if(hasRefreshToken) refreshToken = safeJsonToString(obj.get("refresh_token"));
            state = safeJsonToString(obj.get("state"));
        } catch (Exception e) {
            Log.e("Api error", "Error creating token object");
        }
    }

    public void setExpiration(long expiresIn) {
        this.expiration = (new Date().getTime())/1000 + expiresIn;
    }

    public void checkToken() { //only run on background thread
        if(new Date().getTime()/1000 >= expiration) {
            MyApplication.renewingToken = true;
            MyApplication.currentAccessToken = null;
            RedditHttpClient httpClient = new RedditHttpClient();
            httpClient.setRenewTokenInstance(true);
            if(refreshToken == null) {
                OAuthToken newToken = RedditOAuth.getApplicationToken(httpClient);
                this.accessToken = newToken.accessToken;
                this.expiresIn = newToken.expiresIn;
                setExpiration(newToken.expiresIn);
            }
            else {
                RedditOAuth.refreshToken(httpClient, this);
            }
            MyApplication.renewingToken = false;
            MyApplication.currentAccessToken = accessToken;
            MyApplication.accountChanges = true;
        }
    }

    public void checkToken(User user) { //run on background thread
        if(new Date().getTime()/1000 >= expiration) {
            MyApplication.renewingUserToken = true;
            accessToken = null;
            RedditOAuth.refreshToken(new RedditHttpClient(user), this);
            MyApplication.renewingUserToken = false;

            MyApplication.accountUsernameChanged = user.getUsername();
            MyApplication.newAccountAccessToken = accessToken;
            MyApplication.accountChanges = true;
        }
    }

    //public OAuthToken(String accessToken, String tokenType, long expiresIn, String scope, String state) {
    //    this.accessToken = accessToken;
    //    this.tokenType = tokenType;
    //    this.expiresIn = expiresIn;
    //    this.scope = scope;
    //    this.state = state;
    //}
}
