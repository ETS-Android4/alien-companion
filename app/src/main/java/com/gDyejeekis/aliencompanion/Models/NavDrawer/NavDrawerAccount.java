package com.gDyejeekis.aliencompanion.Models.NavDrawer;

import com.gDyejeekis.aliencompanion.Adapters.NavDrawerAdapter;
import com.gDyejeekis.aliencompanion.Models.SavedAccount;

/**
 * Created by George on 6/26/2015.
 */
public class NavDrawerAccount implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_ACCOUNT;
    }

    private String name;

    private int type;

    public static final int TYPE_ADD = 0;
    public static final int TYPE_LOGGED_OUT = 1;
    public static final int TYPE_ACCOUNT = 2;

    public SavedAccount savedAccount;

    //public NavDrawerAccount(String accountName) {
    //    this.name = accountName;
    //    this.type = TYPE_ACCOUNT;
    //}

    public NavDrawerAccount(int type) {
        this.type = type;
        switch (type) {
            case TYPE_ADD:
                name = "Add account";
                break;
            case TYPE_LOGGED_OUT:
                name = "Logged out";
                break;
        }
    }

    public NavDrawerAccount(SavedAccount account, boolean loggedOut) {
        this.type = TYPE_LOGGED_OUT;
        this.savedAccount = account;
        this.name = "Logged out";
    }

    public NavDrawerAccount(SavedAccount account) {
        this.type = TYPE_ACCOUNT;
        this.savedAccount = account;
        name = account.getUsername();
    }

    public String getName() {
        return name;
    }

    public int getAccountType() {
        return type;
    }

    //public void setAccountName(String accountName) {
    //    this.name = accountName;
    //}

}
