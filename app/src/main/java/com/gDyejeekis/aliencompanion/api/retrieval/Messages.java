package com.gDyejeekis.aliencompanion.api.retrieval;

import android.util.Log;

import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.api.entity.Kind;
import com.gDyejeekis.aliencompanion.api.entity.Message;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategory;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategorySort;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.ParamFormatter;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

import static com.gDyejeekis.aliencompanion.api.utils.httpClient.JsonUtils.safeJsonToString;

/**
 * Created by sound on 10/11/2015.
 */
public class Messages implements ActorDriven {

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
    public Messages(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Constructor. The actor is the user who will
     * be used to perform the retrieval.
     *
     * @param httpClient REST Client instance
     * @param actor User instance
     */
    public Messages(HttpClient httpClient, User actor) {
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

        // Determine cookie
        String cookie = (user == null) ? null : user.getCookie();

        // List of submissions
        List<RedditItem> messages = new LinkedList<>();

        // Send request to reddit server via REST client
        Object response = httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, cookie).getResponseObject();

        if(response instanceof JSONObject) {
            JSONObject object = (JSONObject) response;
            if (object.get("error") != null) {
                throw new RedditError("Response contained error code " + object.get("error") + ".");
            }
            JSONArray array = (JSONArray) ((JSONObject) object.get("data")).get("children");

            // Iterate over the submission results
            JSONObject data;
            RedditItem message;
            for(Object anArray : array) {
                data = (JSONObject) anArray;
                // Make sure it is of the correct kind
                String kind = safeJsonToString(data.get("kind"));
                if (kind != null) {
                    if(kind.equals(Kind.MESSAGE.value()) || kind.equals(Kind.COMMENT.value())) {
                        data = ((JSONObject) data.get("data"));
                        message = new Message(data);
                        messages.add(message);
                    }
                    //else if(kind.equals(Kind.COMMENT.value())) {
                    //    data = ((JSONObject) data.get("data"));
                    //    message = new Comment(data);
                    //    messages.add(message);
                    //}
                }
            }
        }
        else {
            Log.e("Api error", "Cannot cast to JSON Object: '" + response.toString() + "'");
        }

        return messages;
    }

    public List<RedditItem> ofUser(MessageCategory category, MessageCategorySort sort, String count, String limit, String after, String before, String show_all) {
        String endpoint = "";
        if(category == MessageCategory.INBOX) {
            switch (sort) {
                case ALL:
                    endpoint = ApiEndpointUtils.MESSAGE_ALL;
                    break;
                case UNREAD:
                    endpoint = ApiEndpointUtils.MESSAGE_UNREAD;
                    break;
                case MESSAGES:
                    endpoint = ApiEndpointUtils.MESSAGE_MESSAGES;
                    break;
                case COMMENT_REPLIES:
                    endpoint = ApiEndpointUtils.MESSAGE_COMMENT_REPLIES;
                    break;
                case POST_REPLIES:
                    endpoint = ApiEndpointUtils.MESSAGE_POST_REPLIES;
                    break;
                case USERNAME_MENTIONS:
                    endpoint = ApiEndpointUtils.MESSAGE_USERNAME_MENTIONS;
                    break;
            }
        }
        else endpoint = ApiEndpointUtils.MESSAGE_SENT;

        String params = "";

        params = ParamFormatter.addParameter(params, "count", count);
        params = ParamFormatter.addParameter(params, "limit", limit);
        params = ParamFormatter.addParameter(params, "after", after);
        params = ParamFormatter.addParameter(params, "before", before);
        params = ParamFormatter.addParameter(params, "show", show_all);

        return parse(String.format(endpoint, params));
    }

    public List<RedditItem> ofUser(MessageCategory category, MessageCategorySort sort, int count, int limit, Message after, Message before, boolean show_all) throws RetrievalFailedException, RedditError {
        return ofUser(category,
                sort,
                String.valueOf(count),
                String.valueOf(limit),
                (after != null) ? after.getFullName() : "",
                (before != null) ? before.getFullName() : "",
                (show_all) ? "all" : "");
    }
}
