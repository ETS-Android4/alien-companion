package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.AsyncTasks.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public abstract class OfflineUserAction implements Serializable {

    public String actionName;

    public int actionType;

    protected String accountName;

    public boolean actionCompleted;

    public String actionId;

    public OfflineUserAction(String accountName) {
        this.accountName = accountName;
        this.actionCompleted = false;
    }

    public int getActionType() {
        return actionType;
    }

    public String getActionName() {
        return actionName;
    }

    public String getActionId() {
        return actionId;
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OfflineUserAction) {
            OfflineUserAction action = (OfflineUserAction) obj;
            return this.actionId.equals(action.getActionId());
        }
        return false;
    }
}
