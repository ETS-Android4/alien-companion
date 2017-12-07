package com.gDyejeekis.aliencompanion.models;

import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.utils.JsonUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by George on 12/7/2017.
 */

public class SubmitActionResponse {

    private RedditItem redditItem;
    private boolean success;
    private String failReason;

    public SubmitActionResponse(JSONObject jsonObject) {
        success = JsonUtils.safeJsonToBoolean(jsonObject.get("success"));
        if (success) {
            redditItem = retrieveRedditItem(jsonObject.get("jquery"));
        }
        else {
            failReason = retrieveFailReason(jsonObject);
        }
    }

    public RedditItem getRedditItem() {
        return redditItem;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailReason() {
        return failReason;
    }

    private RedditItem retrieveRedditItem(Object obj) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject.containsKey("kind")) {
                String kind = JsonUtils.safeJsonToString(jsonObject.get("kind"));
                JSONObject data = (JSONObject) jsonObject.get("data");
                if (kind.equals("t1")) return new Comment(data);
                else if (kind.equals("t3")) return new Submission(data);
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;
            for (int i=0; i<jsonArray.size(); i++) {
                RedditItem item = retrieveRedditItem(jsonArray.get(i));
                if (item != null) return item;
            }
        }
        return null;
    }

    private String retrieveFailReason(JSONObject obj) {
        String jsonString = obj.toJSONString();
        String reason;
        if (jsonString.contains(".error.USER_REQUIRED")) {
            reason = "User not logged in";
        } else if (jsonString.contains(".error.RATELIMIT.field-ratelimit")){
            reason = "Api rate limit exceeded. Try again in a bit.";
        } else if (jsonString.contains(".error.NOT_AUTHOR")) {
            reason = "User is not the author of this post";
        } else if (jsonString.contains(".error.TOO_LONG")) {
            reason = "Submission text is too long";
        } else if (jsonString.contains(".error.NO_TEXT")) {
            reason = "Submission contains no text";
        } else {
            reason = "Submission failed - unknown reason";
        }
        return reason;
    }

}
