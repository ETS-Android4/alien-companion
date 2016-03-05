package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class NoVoteAction extends OfflineUserAction implements Serializable {

    private String itemFullname;

    public NoVoteAction(String accountName, String fullname) {
        super(accountName);
        this.itemFullname = fullname;
        this.actionName = "no vote " + itemFullname;
    }

    public String getItemFullname() {
        return itemFullname;
    }
}
