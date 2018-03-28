package com.gDyejeekis.aliencompanion.api.retrieval;

import android.util.Log;

import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Kind;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.entity.Thing;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.ParamFormatter;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

/**
 * Created by George on 6/16/2015.
 */
public class UserMixed implements ActorDriven {

    /**
     * Handle to REST client instance.
     */
    private final HttpClient httpClient;
    private User user;

    /**
     * Constructor.
     * Default general actor will be used.
     * @param httpClient REST client handle
     */
    public UserMixed(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Constructor. The actor is the user who will
     * be used to perform the retrieval.
     *
     * @param httpClient REST Client instance
     * @param actor User instance
     */
    public UserMixed(HttpClient httpClient, User actor) {
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

    public List<RedditItem> parse(String url) throws RetrievalFailedException, RedditError {
        Log.d("parse url", url);

        String cookie = (user == null) ? null : user.getCookie();

        // List of submissions
        List<RedditItem> submissions = new ArrayList<>();

        // Send request to reddit server via REST client
        Object response = httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, cookie).getResponseObject();

        if (response instanceof JSONObject) {

            JSONObject object = (JSONObject) response;
            if (object.get("error") != null) {
                throw new RedditError("Response contained error code " + object.get("error") + ".");
            }
            JSONArray array = (JSONArray) ((JSONObject) object.get("data")).get("children");

            // Iterate over the submission results
            JSONObject data;
            RedditItem submission;
            for (Object anArray : array) {
                data = (JSONObject) anArray;

                // Make sure it is of the correct kind
                String kind = safeJsonToString(data.get("kind"));
                if (kind != null) {
                    data = ((JSONObject) data.get("data"));
                    if (kind.equals(Kind.LINK.value())) {

                        // Create and add submission
                        submission = new Submission(data);
                        submissions.add(submission);
                    }
                    else if(kind.equals(Kind.COMMENT.value())) {
                        submission = new Comment(data);
                        submissions.add(submission);
                    }
                }
            }

        } else {
            Log.e("Api error", "Cannot cast to JSON Object: '" + response.toString() + "'");
        }

        // Finally return list of submissions
        return submissions;
    }

    protected List<RedditItem> ofUser(String username, String userContent, String sort, String time, String count, String limit, String after, String before, String show) {

        // Format parameters
        String params = "";
        params = ParamFormatter.addParameter(params, "sort", sort);
        params = ParamFormatter.addParameter(params, "t", time);
        params = ParamFormatter.addParameter(params, "count", count);
        params = ParamFormatter.addParameter(params, "limit", limit);
        params = ParamFormatter.addParameter(params, "after", after);
        params = ParamFormatter.addParameter(params, "before", before);
        params = ParamFormatter.addParameter(params, "show", show);

        // Retrieve submissions from the given URL
        return parse(String.format(ApiEndpointUtils.USER_MIXED, username, userContent, params));
    }

    public List<RedditItem> ofUser(String username, UserSubmissionsCategory userContent, UserOverviewSort sort, TimeSpan timeSpan, int count, int limit, Thing after, Thing before, boolean show_given) {

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("The username must be defined.");
        }

        //if (limit < -1 || limit > RedditConstants.MAX_LIMIT_LISTING) {
        //    throw new IllegalArgumentException("The limit needs to be between 0 and 100 (or -1 for default).");
        //}
        if (limit > RedditConstants.MAX_LIMIT_LISTING) {
            limit = RedditConstants.MAX_LIMIT_LISTING;
        }

        return ofUser(
                username,
                (userContent != null) ? userContent.value() : "",
                (sort != null) ? sort.value() : "",
                (timeSpan != null) ? timeSpan.value() : "",
                String.valueOf(count),
                String.valueOf(limit),
                (after != null) ? after.getFullName() : "",
                (before != null) ? before.getFullName() : "",
                (show_given) ? "given" : ""
        );
    }
}
