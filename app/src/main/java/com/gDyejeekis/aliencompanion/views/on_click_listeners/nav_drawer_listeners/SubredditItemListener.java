package com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners;

import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.nav_drawer.NavDrawerSubredditItem;
import com.gDyejeekis.aliencompanion.MyApplication;

/**
 * Created by George on 6/26/2015.
 */
public class SubredditItemListener extends NavDrawerListener {

    public SubredditItemListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int position = getRecyclerView().getChildPosition(v);
        NavDrawerSubredditItem subreddit = (NavDrawerSubredditItem) getAdapter().getItemAt(position);
        getAdapter().notifyDataSetChanged();
        getDrawerLayout().closeDrawers();

        PostListFragment listFragment = getActivity().getListFragment();

        String subredditName = (subreddit.getName()!=null) ? subreddit.getName().toLowerCase() : null;

        listFragment.isMulti = false;
        listFragment.isOther = false;
        listFragment.changeSubreddit(subredditName);
    }

    @Override
    public boolean onLongClick(View v) {
        if(MyApplication.longTapSwitchMode) {
            int position = getRecyclerView().getChildPosition(v);
            NavDrawerSubredditItem subreddit = (NavDrawerSubredditItem) getAdapter().getItemAt(position);
            String subredditName = (subreddit.getName() != null) ? subreddit.getName().toLowerCase() : null;
            getAdapter().switchModeGracefully(subredditName, false);
            return true;
        }
        return false;
    }
}
