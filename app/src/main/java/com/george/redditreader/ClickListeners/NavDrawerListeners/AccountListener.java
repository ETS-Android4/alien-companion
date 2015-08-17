package com.george.redditreader.ClickListeners.NavDrawerListeners;

import android.app.Activity;
import android.os.Handler;
import android.view.View;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Fragments.AddAccountDialogFragment;
import com.george.redditreader.Models.NavDrawer.NavDrawerAccount;
import com.george.redditreader.Models.NavDrawer.NavDrawerItem;
import com.george.redditreader.Utils.AppConstants;

/**
 * Created by George on 6/26/2015.
 */
public class AccountListener extends NavDrawerListener implements View.OnClickListener {

    public AccountListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int position = getRecyclerView().getChildPosition(v);
        NavDrawerAccount accountItem = (NavDrawerAccount) getAdapter().getItemAt(position);
        getDrawerLayout().closeDrawers();
        switch (accountItem.getAccountType()) {
            case 0:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AddAccountDialogFragment dialogFragment = new AddAccountDialogFragment();
                        dialogFragment.show(getActivity().getFragmentManager(), "dialog");
                    }
                }, AppConstants.NAV_DRAWER_CLOSE_TIME);
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }
}
