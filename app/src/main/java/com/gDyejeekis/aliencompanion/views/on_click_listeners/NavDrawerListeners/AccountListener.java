package com.gDyejeekis.aliencompanion.views.on_click_listeners.NavDrawerListeners;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.OAuthActivity;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.AccountOptionsDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.AddAccountDialogFragment;
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
                            //Log.d("geotest", url);
                            Intent intent = new Intent(getActivity(), OAuthActivity.class);
                            intent.putExtra("url", url);
                            getActivity().startActivity(intent);
                        }
                        else {
                            AddAccountDialogFragment dialogFragment = new AddAccountDialogFragment();
                            dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                        }
                    }
                }, MyApplication.NAV_DRAWER_CLOSE_TIME + 75);
                break;
            //case NavDrawerAccount.TYPE_LOGGED_OUT:
            //    new Handler().postDelayed(new Runnable() {
            //        @Override
            //        public void run() {
            //            //NavDrawerAdapter.currentAccountIndex = 0;
            //            SharedPreferences.Editor editor = MainActivity.prefs.edit();
            //            editor.putString("currentAccountName", "Logged out");
            //            editor.apply();
            //            getActivity().changeCurrentUser(null);
            //            getAdapter().setCurrentAccountName("Logged out");
            //            //getAdapter().notifyDataSetChanged();
            //        }
            //    }, MainActivity.NAV_DRAWER_CLOSE_TIME);
            //    break;
            case NavDrawerAccount.TYPE_LOGGED_OUT:
            case NavDrawerAccount.TYPE_ACCOUNT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //NavDrawerAdapter.currentAccountIndex = position - 1;
                        SharedPreferences.Editor editor = MyApplication.prefs.edit();
                        NavDrawerAccount accountItem = (NavDrawerAccount) getAdapter().getItemAt(position);
                        editor.putString("currentAccountName", accountItem.getName());
                        editor.apply();
                        getActivity().changeCurrentUser(accountItem.savedAccount);
                        getAdapter().setCurrentAccountName(accountItem.getName());
                        //getAdapter().notifyDataSetChanged();
                    }
                }, MyApplication.NAV_DRAWER_CLOSE_TIME);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        final int position = getRecyclerView().getChildPosition(v);
        NavDrawerAccount accountItem = (NavDrawerAccount) getAdapter().getItemAt(position);
        if(accountItem.getAccountType() == NavDrawerAccount.TYPE_ACCOUNT) {
            Bundle args = new Bundle();
            args.putString("accountName", accountItem.getName());
            AccountOptionsDialogFragment dialogFragment = new AccountOptionsDialogFragment();
            dialogFragment.setArguments(args);
            dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
            return true;
        }
        return false;
    }
}
