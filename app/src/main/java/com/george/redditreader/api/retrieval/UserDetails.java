package com.george.redditreader.api.retrieval;

import android.util.Log;

import com.george.redditreader.api.entity.User;
import com.george.redditreader.api.entity.UserInfo;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.utils.ApiEndpointUtils;
import com.george.redditreader.api.utils.restClient.RestClient;

import org.json.simple.JSONObject;

import static com.george.redditreader.api.utils.restClient.JsonUtils.safeJsonToString;

/**
 * Created by George on 6/16/2015.
 */
public class UserDetails implements ActorDriven {

    /**
     * Handle to REST client instance.
     */
    private final RestClient restClient;
    private User user;

    /**
     * Constructor.
     * Default general actor will be used.
     * @param restClient REST client handle
     */
    public UserDetails(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Constructor. The actor is the user who will
     * be used to perform the retrieval.
     *
     * @param restClient REST Client instance
     * @param actor User instance
     */
    public UserDetails(RestClient restClient, User actor) {
        this.restClient = restClient;
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
     * Parses a JSON feed received from Reddit (URL) into a nice list of Submission objects.
     *
     * @param url 	URL
     * @return 		User Info
     */

    public UserInfo parse(String url) throws RetrievalFailedException, RedditError {

        String cookie = (user == null) ? null : user.getCookie();

        UserInfo userInfo = null;

        Object response = restClient.get(url, cookie).getResponseObject();

        if(response instanceof JSONObject) {

            JSONObject object = (JSONObject) response;
            if (object.get("error") != null) {
                throw new RedditError("Response contained error code " + object.get("error") + ".");
            }

            String kind = safeJsonToString(object.get("kind"));
            if(kind != null) {
                JSONObject data = (JSONObject) object.get("data");
                userInfo = new UserInfo(data);
            }
        }
        return userInfo;
    }

    public UserInfo ofUser(String username) {

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("The username must be defined.");
        }

        return parse(String.format(ApiEndpointUtils.USER_ABOUT, username));
    }
}
