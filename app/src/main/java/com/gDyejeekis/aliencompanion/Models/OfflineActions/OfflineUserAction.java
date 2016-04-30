package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Models.SavedAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.entity.User;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sound on 3/4/2016.
 */
public abstract class OfflineUserAction implements Serializable {

    protected String actionName;

    protected int actionType;

    protected String accountName;

    protected boolean actionCompleted;

    protected boolean actionFailed;

    //protected String actionFailedReason;

    protected String actionId;

    public OfflineUserAction(String accountName) {
        this.accountName = accountName;
        this.actionCompleted = false;
        this.actionFailed = false;
    }

    public void setActionFailed(boolean flag) {
        actionFailed = flag;
    }

    public boolean isActionFailed() {
        return actionFailed;
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

    //public void executeAction(Context context) {
    //    LoadUserActionTask task = new LoadUserActionTask(context, this);
    //    task.execute();
    //}

    public abstract void executeAction(Context context); //run on background thread

    protected User getUserByAccountName(Context context) {
        try {
            List<SavedAccount> savedAccounts = MyApplication.readAccounts(context);

            for(SavedAccount account : savedAccounts) {
                if(account.getUsername().equals(accountName)) {
                    return new User(null, accountName, account.getToken());
                }
            }
        } catch (Exception e) {
            Log.e(MyApplication.TAG, "Failed to find saved account with username: " + accountName);
            e.printStackTrace();
        }
        return null;
    }

    protected void saveAnyAccountChanges(Context context) {
        if(MyApplication.accountChanges) {
            Log.d(MyApplication.TAG, "Saving user specific account changes..");
            MyApplication.accountChanges = false;

            try {
                List<SavedAccount> savedAccounts = MyApplication.readAccounts(context);
                for (SavedAccount account : savedAccounts) {
                    if (account.getUsername().equals(MyApplication.accountUsernameChanged)) {
                        account.getToken().accessToken = MyApplication.newAccountAccessToken;
                        savedAccounts.set(savedAccounts.indexOf(account), account);
                        GeneralUtils.writeObjectToFile(savedAccounts, new File(context.getFilesDir(), MyApplication.SAVED_ACCOUNTS_FILENAME));
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(MyApplication.TAG, "Error saving user specific account changes..");
                e.printStackTrace();
            }
            MyApplication.accountUsernameChanged = null;
            MyApplication.newAccountAccessToken = null;
        }
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
