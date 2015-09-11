package com.dyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Activities.SettingsActivity;
import com.dyejeekis.aliencompanion.Activities.UserActivity;
import com.dyejeekis.aliencompanion.Fragments.EnterRedditDialogFragment;
import com.dyejeekis.aliencompanion.Fragments.EnterUserDialogFragment;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMenuItem;

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
                        intent.putExtra("username", MainActivity.currentUser.getUsername());
                        getActivity().startActivity(intent);
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME);
                break;
            case messages:
                break;
            case user:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showDialogFragment(new EnterUserDialogFragment());
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME);
                break;
            case subreddit:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showDialogFragment(new EnterRedditDialogFragment());
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME);
                break;
            case settings:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), SettingsActivity.class);
                        getActivity().startActivity(intent);
                    }
                }, MainActivity.NAV_DRAWER_CLOSE_TIME);
                break;
            case cached:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    private void showDialogFragment(DialogFragment dialog) {
        FragmentManager fm = getActivity().getFragmentManager();
        dialog.show(fm, "dialog");
    }
}
