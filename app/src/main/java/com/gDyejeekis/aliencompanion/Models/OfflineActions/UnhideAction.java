package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class UnhideAction extends OfflineUserAction implements Serializable {

    private String itemFullname;

    public UnhideAction(String accountName, String fullname) {
        super(accountName);
        this.itemFullname = fullname;
        this.actionName = "hide " + itemFullname;
    }

    public String getItemFullname() {
        return itemFullname;
    }
}
