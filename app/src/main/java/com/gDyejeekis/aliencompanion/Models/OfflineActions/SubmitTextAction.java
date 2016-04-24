package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.action.MarkActions;
import com.gDyejeekis.aliencompanion.api.action.SubmitActions;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sound on 3/4/2016.
 */
public class SubmitTextAction extends OfflineUserAction implements Serializable {

    public static final String ACTION_NAME = "Submit text";

    public static final int ACTION_TYPE = 3;

    private String title;
    private String selfText;
    private String subreddit;

    public SubmitTextAction(String accountName, String title, String selfText, String subreddit) {
        super(accountName);
        this.actionName = ACTION_NAME;
        this.actionType = ACTION_TYPE;
        this.title = title;
        this.selfText = selfText;
        this.subreddit = subreddit;
        this.actionId = ACTION_NAME + "-" + UUID.randomUUID();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public void setSelfText(String selfText) {
        this.selfText = selfText;
    }

    public String getTitle() {
        return title;
    }

    public String getSelfText() {
        return selfText;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void executeAction(Context context) {
        User user = getUserByAccountName(context);

        if(user != null) {
            try {
                SubmitActions submitActions = new SubmitActions(new PoliteRedditHttpClient(user), user);
                submitActions.submitSelfPost(title, selfText, subreddit, "", "");
                actionCompleted = true;
                saveAnyAccountChanges(context);
            } catch (Exception e) {
                actionCompleted = false;
                e.printStackTrace();
            }
        }
    }

}
