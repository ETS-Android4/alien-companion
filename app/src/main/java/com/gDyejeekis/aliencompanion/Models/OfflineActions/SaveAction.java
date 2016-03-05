package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class SaveAction extends OfflineUserAction implements Serializable {

    private String itemFullname;

    public SaveAction(String accountName, String fullname) {
        super(accountName);
        this.itemFullname = fullname;
        this.actionName = "save " + itemFullname;
    }

    public String getItemFullname() {
        return itemFullname;
    }

}
