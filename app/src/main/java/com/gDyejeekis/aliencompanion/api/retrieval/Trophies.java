package com.gDyejeekis.aliencompanion.api.retrieval;

import android.util.Log;

import com.gDyejeekis.aliencompanion.api.entity.Kind;
import com.gDyejeekis.aliencompanion.api.entity.Trophy;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static com.gDyejeekis.aliencompanion.api.utils.httpClient.JsonUtils.safeJsonToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 6/20/2015.
 */
public class Trophies {

    private HttpClient httpClient;

    public Trophies(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<Trophy> parse(String url) throws RetrievalFailedException, RedditError {

        List<Trophy> trophies = new ArrayList<>();

        Object response = httpClient.get(url, null).getResponseObject();

        if(response instanceof JSONObject) {

            JSONObject object = (JSONObject) response;
            if (object.get("error") != null) {
                throw new RedditError("Response contained error code " + object.get("error") + ".");
            }
            JSONArray array = (JSONArray) ((JSONObject) object.get("data")).get("trophies");

            JSONObject data;
            Trophy trophy;
            for (Object anArray : array) {
                data = (JSONObject) anArray;

                String kind = safeJsonToString(data.get("kind"));
                if (kind != null) {
                    if (kind.equals(Kind.AWARD.value())) {

                        data = ((JSONObject) data.get("data"));
                        trophy = new Trophy(data);
                        trophies.add(trophy);
                    }
                }
            }

        } else {
            Log.e("Api error", "Cannot cast to JSON Object: '" + response.toString() + "'");
        }

        return trophies;
    }

    public List<Trophy> ofUser(String username) throws RetrievalFailedException, RedditError {

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("The username must be defined.");
        }

        return parse(String.format(ApiEndpointUtils.USER_TROPHIES, username));
    }
}
