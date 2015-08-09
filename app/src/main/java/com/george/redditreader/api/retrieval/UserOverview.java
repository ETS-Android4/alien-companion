package com.george.redditreader.api.retrieval;

import android.util.Log;

import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.api.entity.Kind;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.entity.Thing;
import com.george.redditreader.api.entity.User;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.params.TimeSpan;
import com.george.redditreader.api.retrieval.params.UserOverviewSort;
import com.george.redditreader.api.utils.ApiEndpointUtils;
import com.george.redditreader.api.utils.ParamFormatter;
import com.george.redditreader.api.utils.RedditConstants;
import com.george.redditreader.api.utils.httpClient.HttpClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.george.redditreader.api.utils.httpClient.JsonUtils.safeJsonToString;

/**
 * Created by George on 6/16/2015.
 */
public class UserOverview implements ActorDriven {

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
    public UserOverview(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Constructor. The actor is the user who will
     * be used to perform the retrieval.
     *
     * @param httpClient REST Client instance
     * @param actor User instance
     */
    public UserOverview(HttpClient httpClient, User actor) {
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

    public List<Object> parse(String url) throws RetrievalFailedException, RedditError {
        String cookie = (user == null) ? null : user.getCookie();

        // List of submissions
        List<Object> submissions = new ArrayList<>();

        // Send request to reddit server via REST client
        Object response = httpClient.get(url, cookie).getResponseObject();

        if (response instanceof JSONObject) {

            JSONObject object = (JSONObject) response;
            if (object.get("error") != null) {
                throw new RedditError("Response contained error code " + object.get("error") + ".");
            }
            JSONArray array = (JSONArray) ((JSONObject) object.get("data")).get("children");

            // Iterate over the submission results
            JSONObject data;
            Object submission;
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

    protected List<Object> ofUser(String username, String sort, String time, String count, String limit, String after, String before, String show) {

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
        return parse(String.format(ApiEndpointUtils.USER_OVERVIEW, username, params));
    }

    public List<Object> ofUser(String username, UserOverviewSort sort, TimeSpan timeSpan, int count, int limit, Thing after, Thing before, boolean show_given) {

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("The username must be defined.");
        }

        if (limit < -1 || limit > RedditConstants.MAX_LIMIT_LISTING) {
            throw new IllegalArgumentException("The limit needs to be between 0 and 100 (or -1 for default).");
        }

        return ofUser(
                username,
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
