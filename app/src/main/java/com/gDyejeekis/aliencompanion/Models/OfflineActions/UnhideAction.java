package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.action.MarkActions;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class UnhideAction extends OfflineUserAction implements Serializable {

    public static final String ACTION_NAME = "Unhide";

    public static final int ACTION_TYPE = 6;

    private String itemFullname;

    public UnhideAction(String accountName, String fullname, String preview) {
        super(accountName);
        this.actionName = ACTION_NAME;
        this.actionType = ACTION_TYPE;
        this.itemFullname = fullname;
        setActionPreview(preview);
        this.actionId = ACTION_NAME + "-" + itemFullname;
    }

    public String getActionPreview() {
        return actionPreview;
    }

    public String getItemFullname() {
        return itemFullname;
    }

    public void executeAction(Context context) {
        User user = getUserByAccountName(context);

        if(user != null) {
            try {
                MarkActions markActions = new MarkActions(new PoliteRedditHttpClient(user), user);
                markActions.unhide(itemFullname);
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
