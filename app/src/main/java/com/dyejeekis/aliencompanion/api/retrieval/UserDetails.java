package com.dyejeekis.aliencompanion.api.retrieval;

import com.dyejeekis.aliencompanion.api.entity.User;
import com.dyejeekis.aliencompanion.api.entity.UserInfo;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import org.json.simple.JSONObject;

import static com.dyejeekis.aliencompanion.api.utils.httpClient.JsonUtils.safeJsonToString;

/**
 * Created by George on 6/16/2015.
 */
public class UserDetails implements ActorDriven {

    /**
     * Handle to HTTP client instance.
     */
    private final HttpClient httpClient;
    private User user;

    /**
     * Constructor.
     * Default general actor will be used.
     * @param httpClient HTTP client handle
     */
    public UserDetails(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Constructor. The actor is the user who will
     * be used to perform the retrieval.
     *
     * @param httpClient HTTP Client instance
     * @param actor User instance
     */
    public UserDetails(HttpClient httpClient, User actor) {
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
     * Parses a JSON feed received from Reddit (URL) into a nice list of Submission objects.
     *
     * @param url 	URL
     * @return 		User Info
     */

    public UserInfo parse(String url) throws RetrievalFailedException, RedditError {

        String cookie = (user == null) ? null : user.getCookie();

        UserInfo userInfo = null;

        Object response = httpClient.get(url, cookie).getResponseObject();

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
