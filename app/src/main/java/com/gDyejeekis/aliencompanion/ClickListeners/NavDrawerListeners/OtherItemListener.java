package com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.UserActivity;
import com.gDyejeekis.aliencompanion.Fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerOtherItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;

/**
 * Created by George on 7/16/2016.
 */
public class OtherItemListener extends NavDrawerListener {
    public OtherItemListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int position = getRecyclerView().getChildPosition(v);
        NavDrawerOtherItem otherItem = (NavDrawerOtherItem) getAdapter().getItemAt(position);

        if(otherItem.getName().equals("Saved")) {
            getDrawerLayout().closeDrawers();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivity(), UserActivity.class);
                    intent.putExtra("username", MyApplication.currentAccount.getUsername());
                    intent.putExtra("category", UserSubmissionsCategory.SAVED);
                    getActivity().startActivity(intent);
                }
            }, MyApplication.NAV_DRAWER_CLOSE_TIME);
        }
        else if(otherItem.getName().equals("Synced")) {
            if(MyApplication.offlineModeEnabled) {
                showSyncedPosts();
            }
            else {
                getAdapter().showOfflineSwitchDialog("synced", false, true, null, null);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int position = getRecyclerView().getChildPosition(v);
        NavDrawerOtherItem otherItem = (NavDrawerOtherItem) getAdapter().getItemAt(position);

        if(otherItem.getName().equals("Synced")) {
            if(MyApplication.offlineModeEnabled) {
                showSyncedPosts();
            }
            else {
                getDrawerLayout().closeDrawers();
                ToastUtils.displayShortToast(getActivity(), "Switching to offline mode");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getAdapter().switchMode("synced", false, true, null, null);
                    }
                }, MyApplication.NAV_DRAWER_CLOSE_TIME);
            }
            return true;
        }
        return false;
    }

    private void showSyncedPosts() {
        getAdapter().notifyDataSetChanged();
        getDrawerLayout().closeDrawers();
        PostListFragment listFragment = getActivity().getListFragment();
        listFragment.isMulti = false;
        listFragment.isOther = true;
        listFragment.changeSubreddit("synced");
    }
}
