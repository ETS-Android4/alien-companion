package com.gDyejeekis.aliencompanion.api.retrieval;

import android.text.method.MultiTapKeyListener;

import com.gDyejeekis.aliencompanion.api.entity.Multireddit;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 1/26/2016.
 */
public class Multireddits implements ActorDriven {

    /**
     * Handle to the REST client instance.
     */
    private final HttpClient httpClient;
    private User user;

    /**
     * Constructor.
     *
     * @param httpClient REST client instance
     */
    public Multireddits(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Constructor.
     * @param httpClient REST Client instance
     * @param actor User instance
     */
    public Multireddits(HttpClient httpClient, User actor) {
        this.httpClient = httpClient;
        this.user = actor;
    }

    /**
     * Switch the current user for the new user who will
     * be used when invoking retrieval requests.
     *
     * @param new_actor New user
     */
    public void switchActor(User new_actor) {
        this.user = new_actor;
    }

    /**
     * Parses a JSON feed from the Reddit (URL) into a nice list of Multireddit objects.
     *
     * @param url 	URL
     * @return 		Listing of submissions
     */
    public List<Multireddit> parse(String url) throws RetrievalFailedException, RedditError {

        // Determine cookie
        String cookie = (user == null) ? null : user.getCookie();

        // List of subreddits
        List<Multireddit> multireddits = new ArrayList<>();

        // Send request to reddit server via REST client
        Object response = httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, cookie).getResponseObject();

        if(response instanceof JSONArray) {
            JSONArray array = (JSONArray) response;

            JSONObject data;
            Multireddit multi;
            for(Object o : array) {
                data = (JSONObject) o;
                data = (JSONObject) data.get("data");
                multi = new Multireddit(data);
                multireddits.add(multi);
            }
        } else {
            System.err.println("Cannot cast to JSON Object: '" + response.toString() + "'");
        }

        return multireddits;
    }

    public List<Multireddit> ofUsername(String username, boolean expand_srs) {
        return parse(String.format(ApiEndpointUtils.MULTIREDDITS_USERNAME, username, "expand_srs="+String.valueOf(expand_srs)));
    }

    public Multireddit ofMultipath(String multipath, boolean expand_srs) {
        String url = String.format(ApiEndpointUtils.MULTIREDDIT_ABOUT, multipath, "expand_srs="+String.valueOf(expand_srs));

        JSONObject object = (JSONObject) httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, null).getResponseObject();

        return new Multireddit(object);
    }

    public List<String> getSubredditsOfMulti(String multipath) {
        Multireddit multireddit = ofMultipath(multipath, false);

        return multireddit.getSubreddits();
    }

}
