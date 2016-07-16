package com.gDyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.UserActivity;
import com.gDyejeekis.aliencompanion.Fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerMutliredditItem;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerOtherItem;
import com.gDyejeekis.aliencompanion.MyApplication;
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
        getAdapter().notifyDataSetChanged();
        getDrawerLayout().closeDrawers();

        if(otherItem.getName().equals("Saved")) {
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
            // TODO: 7/16/2016
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
