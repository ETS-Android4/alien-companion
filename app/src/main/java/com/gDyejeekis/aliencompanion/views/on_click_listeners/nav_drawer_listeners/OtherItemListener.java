package com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerOtherItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;

/**
 * Created by George on 7/16/2016.
 */
public class OtherItemListener extends NavDrawerListener {
    public OtherItemListener(MainActivity activity, RecyclerView.ViewHolder viewHolder) {
        super(activity, viewHolder);
    }

    @Override
    public void onClick(View v) {
        NavDrawerOtherItem otherItem =
                (NavDrawerOtherItem) getAdapter().getItemAt(getViewHolder().getAdapterPosition());
        final String name = otherItem.getName();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (name.equals("Saved")) {
                    getDrawerLayout().closeDrawers();
                    Intent intent = new Intent(getActivity(), UserActivity.class);
                    intent.putExtra("username", MyApplication.currentAccount.getUsername());
                    intent.putExtra("category", UserSubmissionsCategory.SAVED);
                    getActivity().startActivity(intent);
                } else if (name.equals("Synced")) {
                    if (MyApplication.offlineModeEnabled) {
                        changeToSynced();
                    } else {
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getAdapter().switchModeGracefully("synced", false, true);
                            }
                        };
                        getAdapter().showOfflineSwitchDialog(listener);
                    }
                }
            }
        }, AppConstants.NAV_DRAWER_CLOSE_TIME);
    }

    @Override
    public boolean onLongClick(View v) {
        if (MyApplication.longTapSwitchMode) {
            NavDrawerOtherItem otherItem =
                    (NavDrawerOtherItem) getAdapter().getItemAt(getViewHolder().getAdapterPosition());
            if (otherItem.getName().equals("Synced")) {
                if (MyApplication.offlineModeEnabled) changeToSynced();
                else getAdapter().switchModeGracefully("synced", false, true);
                return true;
            }
        }
        return false;
    }

    private void changeToSynced() {
        getDrawerLayout().closeDrawers();
        getAdapter().notifyDataSetChanged();
        getActivity().getListFragment().changeSubreddit("synced", false, true);
    }

}
