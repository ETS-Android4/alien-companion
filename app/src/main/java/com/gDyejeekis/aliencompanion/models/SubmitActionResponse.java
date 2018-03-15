package com.gDyejeekis.aliencompanion.models;

import android.util.Log;

import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.utils.JsonUtils;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;

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
                Object o = jsonArray.get(i);
                if (LinkUtils.isRedditPostUrl(o.toString())) {
                    return LinkUtils.getRedditPostFromUrl(o.toString());
                } else {
                    RedditItem item = retrieveRedditItem(o);
                    if (item != null) return item;
                }
            }
        }
        return null;
    }

    private String retrieveFailReason(JSONObject obj) {
        String jsonString = obj.toJSONString();
        String reason = "User submission failed";
        if (jsonString.contains(".error.USER_REQUIRED")) {
            reason += ": user not logged in";
        } else if (jsonString.contains(".error.RATELIMIT.field-ratelimit")){
            reason += ": API rate limit exceeded. Try again in a bit.";
        } else if (jsonString.contains(".error.NOT_AUTHOR")) {
            reason += ": user is not the author of this post";
        } else if (jsonString.contains(".error.TOO_LONG")) {
            reason += ": submission text is too long";
        } else if (jsonString.contains(".error.NO_TEXT")) {
            reason += ": submission contains no text";
        } else if (jsonString.contains(".error.ALREADY_SUB.field-url")) {
            reason += ": that link has already been submitted.";
        } else if (jsonString.contains(".error.BAD_CAPTCHA.field-Captcha")) {
            reason += ": the Captcha field was incorrect.";
        }
        return reason;
    }

}
