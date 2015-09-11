package com.dyejeekis.aliencompanion.api.action;

import com.dyejeekis.aliencompanion.api.entity.User;
import com.dyejeekis.aliencompanion.api.entity.UserInfo;
import com.dyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.dyejeekis.aliencompanion.api.retrieval.ActorDriven;
import com.dyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.Response;

import org.json.simple.JSONObject;

/**
 * Created by George on 8/10/2015.
 */
public class ProfileActions implements ActorDriven {

    private HttpClient httpClient;
    private User user;

    /**
     * Constructor. Global default user (null) is used.
     * @param httpClient HTTP Client instance
     */
    public ProfileActions(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.user = null;
    }

    /**
     * Constructor.
     * @param httpClient HTTP Client instance
     * @param actor User instance
     */
    public ProfileActions(HttpClient httpClient, User actor) {
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
     * This function changes user's password
     * Requires authentication.
     *
     * @param currentPassword	Current password
     * @param newPassword		New password
     * @throws ActionFailedException    	If the action failed
     */
    public boolean changePassword(String currentPassword, String newPassword) throws ActionFailedException {

        // Make the request
        JSONObject object = (JSONObject) update(currentPassword, "", newPassword).getResponseObject();

        // User required
        if (object.toJSONString().contains(".error.USER_REQUIRED")) {
            System.err.println("Change password failed: please login first.");
            return false;
        } // Rate limit exceeded
        else if (object.toJSONString().contains(".error.RATELIMIT.field-ratelimit")) {
            System.err.println("Change password failed: you are doing that too much.");
            return false;
        } // Incorrect password
        else if (object.toJSONString().contains(".error.BAD_PASSWORD")) {
            System.err.println("Change password failed: current password is bad.");
            return false;
        } else {
            return true;
        }

    }

    /**
     * This function updates user's profile.
     * Requires authentication.
     *
     * @param currentPassword   Current password
     * @param email				New e-mail address (can be empty)
     * @param newPassword		New password
     * @throws ActionFailedException    	If the action failed
     */
    public Response update(String currentPassword, String email, String newPassword) throws ActionFailedException {

        // Format parameters
        String params =
                "api_type=json"
                        + "&curpass=" + currentPassword +
                        "&dest=http://reddit.com/" + (!email.equals("") ? "&email=" + email : "") +
                        (!newPassword.equals("") ? "&newpass=" + newPassword + "&verpass=" + newPassword : "")
                        + "&uh=" + user.getModhash();

        // Post request
        return httpClient.post(params, ApiEndpointUtils.USER_UPDATE, user.getCookie());

    }


    /**
     * Get info about the currently authenticated user.
     * Corresponds to the API "/me.json" method
     *
     * @return <code>UserInfo</code>object containing the user's info, or null if the retrieval fails
     * @throws ActionFailedException    	If the action failed
     */
    public UserInfo getUserInformation() throws ActionFailedException {
        if (user.getCookie() == null || user.getModhash() == null) {
            System.err.printf("Please invoke the connect method in order to login the user");
            return null;
        }

        JSONObject jsonObject = (JSONObject) httpClient.get(ApiEndpointUtils.USER_INFO, user.getCookie()).getResponseObject();
        JSONObject info = (JSONObject) jsonObject.get("data");

        return new UserInfo(info);
    }

    /**
     * Returns miscellaneous info about the user
     *
     * @param username The username of the user whose account info you want to retrieve.
     * @return UserInfo object consisting of information about the user identified by "username".
     * @throws ActionFailedException    	If the action failed
     */
    public UserInfo about(String username) throws ActionFailedException {

        // Send GET request to get the account overview

        if(username.equals("[deleted]"))
        {
            throw new ActionFailedException("There is no information associated with the username [deleted]");
        }

        JSONObject object = (JSONObject) httpClient.get(String.format(ApiEndpointUtils.USER_ABOUT, username), null).getResponseObject();
        JSONObject data = (JSONObject) object.get("data");

        // Init account info wrapper
        return data != null ? new UserInfo(data) : null;
    }

    /**
     * Deletes the reddit account.
     * Requires authentication.
     *
     * @param confirm   Confirmation by the user
     * @param message   Optional message explaining the reason for the account's deletion
     * @param password  Account's password
     * @throws ActionFailedException        If the action failed
     */
    public Response delete(Boolean confirm, String message, String password) throws ActionFailedException {

        // Format parameters
        String params =
                "api_type=json"
                        + "&confirm=" + confirm +
                        "&delete_message=" + message
                        + "&passwd=" + password
                        + "&uh=" + user.getModhash()
                        + "&user=" + user.getUsername();

        // Post request
        return httpClient.post(params, ApiEndpointUtils.USER_DELETE, user.getCookie());
    }

    /**
     * Creates a new account.
     *
     * @param username      A valid, unused, username
     * @param email         A valid e-mail address (can be empty)
     * @param newPassword   The account's password
     * @param copyPassword  should be same as newPassword (for validation)
     * @param captcha_iden  the identifier of the CAPTCHA challenge (not necessarily required)
     * @param captcha_sol   the user's response to the CAPTCHA challenge (required if there was a CAPTCHA challenge)
     * @throws ActionFailedException        If the action failed
     */
    public Response register(String username, String email, String newPassword, String copyPassword, String captcha_iden, String captcha_sol) throws ActionFailedException {

        // Format parameters
        String params =
                "api_type=json"
                        + "&captcha=" + captcha_sol
                        +"&email=" + email
                        + "&iden=" + captcha_iden
                        + "&passwd=" + newPassword
                        + "&passwd2=" + copyPassword
                        + "&user=" + username;

        // Post request


        Response result = httpClient.post(params, ApiEndpointUtils.USER_REGISTER, "");//no user, no cookie
        JSONObject object = (JSONObject) result.getResponseObject();


        if (object.toJSONString().contains("BAD_CAPTCHA")) {
            System.err.println("Wrong captcha!");
        }
        else if (object.toJSONString().contains("RATELIMIT")) {
            System.err.println("User creation failed: you are doing that too much.");
        }
        else if (object.toJSONString().contains("USERNAME_TAKEN")) {
            System.err.println("User creation failed: that username is already taken.");
        }
        else if (object.toJSONString().contains("BAD_PASSWORD_MATCH")) {
            System.err.println("User creation failed: passwords do not match.");

        } else {
            this.user = new User(this.httpClient, username, newPassword);
        }

        return result;
    }

}
