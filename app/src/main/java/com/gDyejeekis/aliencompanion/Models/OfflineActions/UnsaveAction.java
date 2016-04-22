package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class UnsaveAction extends OfflineUserAction implements Serializable {

    public static final String ACTION_NAME = "Unsave";

    public static final int ACTION_TYPE = 5;

    private String itemFullname;

    public UnsaveAction(String accountName, String fullname) {
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
