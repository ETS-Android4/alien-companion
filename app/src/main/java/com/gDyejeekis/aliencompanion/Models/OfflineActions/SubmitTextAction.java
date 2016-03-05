package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class SubmitTextAction extends OfflineUserAction implements Serializable {

    private String title;
    private String selfText;
    private String subreddit;

    public SubmitTextAction(String accountName, String title, String selfText, String subreddit) {
        super(accountName);
        this.title = title;
        this.selfText = selfText;
        this.subreddit = subreddit;
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

}
