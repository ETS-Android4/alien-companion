package com.gDyejeekis.aliencompanion.views.on_click_listeners.NavDrawerListeners;

import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.MessageActivity;
import com.gDyejeekis.aliencompanion.activities.SettingsActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.EnterRedditDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.EnterUserDialogFragment;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerMenuItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.enums.SettingsMenuType;

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
                        intent.putExtra("menuType", SettingsMenuType.headers);
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
