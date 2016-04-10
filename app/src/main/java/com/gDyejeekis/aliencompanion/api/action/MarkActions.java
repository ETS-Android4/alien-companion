package com.gDyejeekis.aliencompanion.api.action;

import android.content.pm.ApplicationInfo;

import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.ActorDriven;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import org.json.simple.JSONObject;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by George on 8/10/2015.
 */
public class MarkActions implements ActorDriven {

    private HttpClient httpClient;
    private User user;

    /**
     * Constructor. Global default user (null) is used.
     * @param httpClient HTTP Client instance
     */
    public MarkActions(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.user = null;
    }

    /**
     * Constructor.
     * @param httpClient HTTP Client instance
     * @param actor User instance
     */
    public MarkActions(HttpClient httpClient, User actor) {
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
     * Mark a post as NSFW
     *
     * @param fullName Full name of the comment or submission
     * @throws ActionFailedException If the action failed
     */
    public boolean markNSFW(String fullName) throws ActionFailedException {

        RequestBody body = new FormBody.Builder().add("id", fullName).add("uh", user.getModhash()).build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.SUBMISSION_MARK_AS_NSFW,
                user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;

    }

    /**
     * Unmark a post as NSFW
     *
     * @param fullName Full name of the comment or submission
     * @throws ActionFailedException If the action failed
     */
    public boolean unmarkNSFW(String fullName) throws ActionFailedException {

        RequestBody body = new FormBody.Builder().add("id", fullName).add("uh", user.getModhash()).build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.SUBMISSION_UNMARK_AS_NSFW,
                user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;
    }

    /**
     * This function saves a submission with a specific category (Reddit Gold feature).
     *
     * @param fullName Full name of the thing
     * @param category Category name
     *
     * @throws ActionFailedException If the action failed
     */
    public boolean save(String fullName, String category) throws ActionFailedException {

        RequestBody body = new FormBody.Builder().add("category", category).add("id", fullName).add("uh", user.getModhash()).build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.SAVE, user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;

    }

    /**
     * This function reports a submission or comment to the moderator of the subreddit it is contained in.
     *
     * @param fullName Full name of the thing
     *
     * @throws ActionFailedException If the action failed
     */
    public boolean report(String fullName, String reason) throws ActionFailedException {

        RequestBody body = new FormBody.Builder().add("thing_id", fullName).add("reason", reason).add("uh", user.getModhash()).build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.REPORT, user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;

    }

    /**
     * This function saves a submission or comment with the given full name.
     * @param fullName Full name of the thing
     *
     * @throws ActionFailedException If the action failed
     */
    public boolean save(String fullName) throws ActionFailedException {

        RequestBody body = new FormBody.Builder().add("id", fullName)/*.add("uh", user.getModhash())*/.build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.SAVE,
                user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;

    }

    /**
     * This function unsaves a submission or comment with the given full name.
     * @param fullName Full name of the thing
     *
     * @throws ActionFailedException If the action failed
     */
    public boolean unsave(String fullName) throws ActionFailedException {

        RequestBody body = new FormBody.Builder().add("id", fullName)/*.add("uh", user.getModhash())*/.build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.UNSAVE,
                user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;
    }

    /**
     * This function hides a submission or comment with the given full name.
     * @param fullName Full name of the thing
     *
     * @throws ActionFailedException If the action failed
     */
    public boolean hide(String fullName) throws ActionFailedException {

        RequestBody body = new FormBody.Builder().add("id", fullName)/*.add("uh", user.getModhash())*/.build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.HIDE,
                user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;

    }

    /**
     * This function unhide a submission or comment with the given full name.
     * @param fullName Full name of the thing
     *
     * @throws ActionFailedException If the action failed
     */
    public boolean unhide(String fullName) throws ActionFailedException {

        RequestBody body = new FormBody.Builder().add("id", fullName)/*.add("uh", user.getModhash())*/.build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.UNHIDE,
                user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;
    }

    /**
     * Vote for a comment or submission with the given full name.
     *
     * @param dir 	Direction (precondition: either -1, 0 or 1)
     * @return Response from reddit.
     */
    public boolean vote(String fullName, int dir) throws ActionFailedException {

        if (dir < -1 || dir > 1) {
            throw new IllegalArgumentException("Vote direction needs to be -1 or 1.");
        }

        RequestBody body = new FormBody.Builder().add("id", fullName).add("dir", String.valueOf(dir))/*.add("uh", user.getModhash())*/.build();
        JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL,
                body,
                ApiEndpointUtils.VOTE,
                user.getCookie()
        ).getResponseObject();

        return object.toJSONString().length() == 2;

    }

    /**
     * Marks all new messages as read.
     *
     */
    public boolean readAllNewMessages() throws ActionFailedException {
        try {
            httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, null, ApiEndpointUtils.READ_ALL_MESSAGES, user.getCookie());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
