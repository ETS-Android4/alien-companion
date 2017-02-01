package com.gDyejeekis.aliencompanion.models.offline_actions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.action.MarkActions;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class ReportAction extends OfflineUserAction implements Serializable {

    private static final long serialVersionUID = 1234556L;

    public static final String ACTION_NAME = "Report";

    public static final int ACTION_TYPE = 1;

    private String itemFullname;
    private String reportReason;

    public ReportAction(String accountName, String fullname, String reportReason) {
        super(accountName);
        this.actionName = ACTION_NAME;
        this.actionType = ACTION_TYPE;
        this.itemFullname = fullname;
        this.reportReason = reportReason;
        this.actionId = ACTION_NAME + "-" + itemFullname;
    }

    public String getActionPreview() {
        return itemFullname + " (" + reportReason + ")";
    }

    public String getItemFullname() {
        return itemFullname;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void executeAction(Context context) {
        User user = getUserByAccountName(context);

        if(user != null) {
            try {
                MarkActions markActions = new MarkActions(new PoliteRedditHttpClient(user), user);
                markActions.report(itemFullname, reportReason);
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
