package com.dyejeekis.aliencompanion.api.entity;

import android.util.Log;

import org.json.simple.JSONObject;

import static com.dyejeekis.aliencompanion.api.utils.httpClient.JsonUtils.safeJsonToBoolean;
import static com.dyejeekis.aliencompanion.api.utils.httpClient.JsonUtils.safeJsonToDouble;
import static com.dyejeekis.aliencompanion.api.utils.httpClient.JsonUtils.safeJsonToLong;
import static com.dyejeekis.aliencompanion.api.utils.httpClient.JsonUtils.safeJsonToString;

/**
 * Created by sound on 10/24/2015.
 */
public class OAuthToken {

    public String accessToken;
    public String tokenType;
    public long expiresIn;
    public String scope;
   // public String refreshToken;
    public String state;

    public OAuthToken(JSONObject obj) {
        try {
            accessToken = safeJsonToString(obj.get("access_token"));
            tokenType = safeJsonToString(obj.get("token_type"));
            expiresIn = safeJsonToLong(obj.get("expires_in"));
            scope = safeJsonToString(obj.get("scope"));
            //refreshToken = safeJsonToString(obj.get("refresh_token"));
            state = safeJsonToString(obj.get("state"));
        } catch (Exception e) {
            Log.e("Api error", "Error creating token object");
        }
    }

    public OAuthToken(String accessToken, String tokenType, long expiresIn, String scope, String state) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.state = state;
    }
}
