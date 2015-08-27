package com.george.redditreader.ClickListeners.NavDrawerListeners;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Adapters.NavDrawerAdapter;
import com.george.redditreader.Fragments.AccountOptionsDialogFragment;
import com.george.redditreader.Fragments.AddAccountDialogFragment;
import com.george.redditreader.Models.NavDrawer.NavDrawerAccount;

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
                        AddAccountDialogFragment dialogFragment = new AddAccountDialogFragment();
                        dialogFragment.show(getActivity().getFragmentManager(), "dialog");
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME);
                break;
            case NavDrawerAccount.TYPE_LOGGED_OUT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //NavDrawerAdapter.currentAccountIndex = 0;
                        SharedPreferences.Editor editor = MainActivity.prefs.edit();
                        editor.putInt("currentAccountIndex", 0);
                        editor.apply();
                        getActivity().changeCurrentUser(null);
                        getAdapter().notifyDataSetChanged();
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME);
                break;
            case NavDrawerAccount.TYPE_ACCOUNT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //NavDrawerAdapter.currentAccountIndex = position - 1;
                        SharedPreferences.Editor editor = MainActivity.prefs.edit();
                        editor.putInt("currentAccountIndex", position - 1);
                        editor.apply();
                        getActivity().changeCurrentUser(getAdapter().accountItems.get(position - 1).savedAccount);
                        getAdapter().notifyDataSetChanged();
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
