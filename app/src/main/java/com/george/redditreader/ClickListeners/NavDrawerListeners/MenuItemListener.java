package com.george.redditreader.ClickListeners.NavDrawerListeners;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.george.redditreader.Activities.EditPrefsActivity;
import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Fragments.EnterRedditDialogFragment;
import com.george.redditreader.Fragments.EnterUserDialogFragment;
import com.george.redditreader.Models.NavDrawer.NavDrawerMenuItem;

/**
 * Created by George on 6/26/2015.
 */
public class MenuItemListener extends NavDrawerListener implements View.OnClickListener {

    public MenuItemListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int position = getRecyclerView().getChildPosition(v);
        NavDrawerMenuItem menuItem = (NavDrawerMenuItem) getAdapter().getItemAt(position);
        getDrawerLayout().closeDrawers();
        switch (menuItem.getMenuType()) {
            case profile:
                break;
            case messages:
                break;
            case user:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showDialogFragment(new EnterUserDialogFragment());
                    }
                }, 200);
                break;
            case subreddit:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showDialogFragment(new EnterRedditDialogFragment());
                    }
                }, 200);
                break;
            case settings:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), EditPrefsActivity.class);
                        getActivity().startActivity(intent);
                    }
                }, 200);
                break;
            case cached:
                break;
        }
    }

    private void showDialogFragment(DialogFragment dialog) {
        FragmentManager fm = getActivity().getFragmentManager();
        dialog.show(fm, "dialog");
    }
}
