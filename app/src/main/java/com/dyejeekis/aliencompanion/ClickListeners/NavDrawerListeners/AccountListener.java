package com.dyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.BrowserActivity;
import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Activities.OAuthActivity;
import com.dyejeekis.aliencompanion.Fragments.DialogFragments.AccountOptionsDialogFragment;
import com.dyejeekis.aliencompanion.Fragments.DialogFragments.AddAccountDialogFragment;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerAccount;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditOAuth;

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
        NavDrawerAccount accountItem = (NavDrawerAccount) getAdapter().getItemAt(position);
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
                            dialogFragment.show(getActivity().getFragmentManager(), "dialog");
                        }
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME + 75);
                break;
            case NavDrawerAccount.TYPE_LOGGED_OUT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //NavDrawerAdapter.currentAccountIndex = 0;
                        SharedPreferences.Editor editor = MainActivity.prefs.edit();
                        editor.putString("currentAccountName", "Logged out");
                        editor.apply();
                        getActivity().changeCurrentUser(null);
                        getAdapter().setCurrentAccountName("Logged out");
                        //getAdapter().notifyDataSetChanged();
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME);
                break;
            case NavDrawerAccount.TYPE_ACCOUNT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //NavDrawerAdapter.currentAccountIndex = position - 1;
                        SharedPreferences.Editor editor = MainActivity.prefs.edit();
                        NavDrawerAccount accountItem = (NavDrawerAccount) getAdapter().getItemAt(position);
                        editor.putString("currentAccountName", accountItem.getName());
                        editor.apply();
                        getActivity().changeCurrentUser(accountItem.savedAccount);
                        getAdapter().setCurrentAccountName(accountItem.getName());
                        //getAdapter().notifyDataSetChanged();
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME);
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
            dialogFragment.show(getActivity().getFragmentManager(), "dialog");
            return true;
        }
        return false;
    }
}
