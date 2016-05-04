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
public class SubmitLinkAction extends OfflineUserAction implements Serializable {

    public static final String ACTION_NAME = "Submit link";

    public static final int ACTION_TYPE = 2;

    private String title;
    private String link;
    private String subreddit;

    public SubmitLinkAction(String accountName, String title, String link, String subreddit) {
        super(accountName);
        this.actionName = ACTION_NAME;
        this.actionType = ACTION_TYPE;
        this.title = title;
        this.link = link;
        this.subreddit = subreddit;
        this.actionId = ACTION_NAME + "-" + UUID.randomUUID();
    }

    public String getActionPreview() {
        if(title.length() <= PREVIEW_LENGTH) {
            return title;
        }
        return title.substring(0, PREVIEW_LENGTH - 1);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void executeAction(Context context) {
        User user = getUserByAccountName(context);

        if(user != null) {
            try {
                SubmitActions submitActions = new SubmitActions(new PoliteRedditHttpClient(user), user);
                submitActions.submitLink(title, link, subreddit, "", "");
                actionCompleted = true;
                saveAnyAccountChanges(context);
            } catch (Exception e) {
                actionFailed = true;
                actionCompleted = false;
                e.printStackTrace();
            }
        }
    }

}
