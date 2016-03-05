package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class ReportAction extends OfflineUserAction implements Serializable {

    private String itemFullname;
    private String reportReason;

    public ReportAction(String accountName, String fullname, String reportReason) {
        super(accountName);
        this.itemFullname = fullname;
        this.reportReason = reportReason;
        this.actionName = "report " + itemFullname;
    }

    public String getItemFullname() {
        return itemFullname;
    }

    public String getReportReason() {
        return reportReason;
    }

}
