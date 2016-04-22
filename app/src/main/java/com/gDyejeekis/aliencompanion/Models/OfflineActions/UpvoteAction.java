package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class UpvoteAction extends OfflineUserAction implements Serializable {

    public static final String ACTION_NAME = "Upvote";

    public static final int ACTION_TYPE = 4;

    private String itemFullname;

    public UpvoteAction(String accountName, String fullname) {
        super(accountName);
        this.actionName = ACTION_NAME;
        this.actionType = ACTION_TYPE;
        this.itemFullname = fullname;
        this.actionId = ACTION_NAME + "-" + itemFullname;
    }

    public String getItemFullname() {
        return itemFullname;
    }

    public void executeAction() {

    }

}
