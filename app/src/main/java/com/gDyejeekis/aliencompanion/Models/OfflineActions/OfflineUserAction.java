package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.AsyncTasks.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public abstract class OfflineUserAction implements Serializable {

    protected String accountName;

    protected boolean actionCompleted;

    protected String actionName;

    public OfflineUserAction(String accountName) {
        this.accountName = accountName;
        this.actionCompleted = false;
    }

    public String getActionName() {
        return actionName;
    }

    public boolean isActionCompleted() {
        return actionCompleted;
    }

    public void setActionCompleted(boolean flag) {
        this.actionCompleted = flag;
    }

    public void executeAction(Context context) {
        LoadUserActionTask task = new LoadUserActionTask(context, this);
        task.execute();
    }

}
