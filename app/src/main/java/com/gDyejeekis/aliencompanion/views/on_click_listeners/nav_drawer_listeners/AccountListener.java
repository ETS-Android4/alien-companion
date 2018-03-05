package com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.BrowserActivity;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerAccount;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.api.utils.RedditOAuth;

/**
 * Created by George on 6/26/2015.
 */
public class AccountListener extends NavDrawerListener {

    public AccountListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        final int position = getRecyclerView().getChildPosition(v);
        final NavDrawerAccount accountItem = (NavDrawerAccount) getAdapter().getItemAt(position);
        getDrawerLayout().closeDrawers();
        switch (accountItem.getAccountType()) {
            case NavDrawerAccount.TYPE_ADD:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(RedditOAuth.useOAuth2) {
                            String url = RedditOAuth.getOauthAuthUrl();
                            Intent intent = new Intent(getActivity(), BrowserActivity.class);
                            intent.putExtra("url", url);
                            intent.putExtra("addRedditAccount", true);
                            getActivity().startActivity(intent);
                        }
                    }
                }, AppConstants.NAV_DRAWER_CLOSE_TIME + 75);
                break;
            case NavDrawerAccount.TYPE_LOGGED_OUT:
            case NavDrawerAccount.TYPE_ACCOUNT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor editor = MyApplication.prefs.edit();
                        NavDrawerAccount accountItem = (NavDrawerAccount) getAdapter().getItemAt(position);
                        editor.putString("currentAccountName", accountItem.getName());
                        editor.apply();
                        getActivity().changeCurrentUser(accountItem.savedAccount);
                        getAdapter().setCurrentAccountName(accountItem.getName());
                    }
                }, AppConstants.NAV_DRAWER_CLOSE_TIME);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        final int position = getRecyclerView().getChildPosition(v);
        final NavDrawerAccount accountItem = (NavDrawerAccount) getAdapter().getItemAt(position);
        if(accountItem.getAccountType() == NavDrawerAccount.TYPE_ACCOUNT) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().getNavDrawerAdapter().deleteAccount(accountItem.getName());
                }
            };
            new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyAlertDialogStyle)).setMessage("Remove " + accountItem.getName() + "?")
                    .setPositiveButton("Yes", listener).setNegativeButton("No", null).show();
            return true;
        }
        return false;
    }
}
