package com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.MessageActivity;
import com.gDyejeekis.aliencompanion.Activities.SettingsActivity;
import com.gDyejeekis.aliencompanion.Activities.UserActivity;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.EnterRedditDialogFragment;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.EnterUserDialogFragment;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMenuItem;
import com.gDyejeekis.aliencompanion.MyApplication;

/**
 * Created by George on 6/26/2015.
 */
public class MenuItemListener extends NavDrawerListener {

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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), UserActivity.class);
                        intent.putExtra("username", MyApplication.currentUser.getUsername());
                        getActivity().startActivity(intent);
                    }
                }, MyApplication.NAV_DRAWER_CLOSE_TIME);
                break;
            case messages:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), MessageActivity.class);
                        getActivity().startActivity(intent);
                    }
                }, MyApplication.NAV_DRAWER_CLOSE_TIME);
                break;
            case user:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EnterUserDialogFragment dialog = new EnterUserDialogFragment();
                        dialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }, MyApplication.NAV_DRAWER_CLOSE_TIME);
                break;
            case subreddit:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EnterRedditDialogFragment dialog = new EnterRedditDialogFragment();
                        dialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }, MyApplication.NAV_DRAWER_CLOSE_TIME);
                break;
            case settings:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), SettingsActivity.class);
                        getActivity().startActivity(intent);
                    }
                }, MyApplication.NAV_DRAWER_CLOSE_TIME);
                break;
            case cached:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

}
