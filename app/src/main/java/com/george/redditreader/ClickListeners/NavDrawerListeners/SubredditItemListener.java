package com.george.redditreader.ClickListeners.NavDrawerListeners;

import android.app.Activity;
import android.view.View;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Fragments.PostListFragment;
import com.george.redditreader.Models.NavDrawer.NavDrawerSubredditItem;
import com.george.redditreader.api.retrieval.params.SubmissionSort;

/**
 * Created by George on 6/26/2015.
 */
public class SubredditItemListener extends NavDrawerListener implements View.OnClickListener {

    public SubredditItemListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        int position = getRecyclerView().getChildPosition(v);
        NavDrawerSubredditItem subreddit = (NavDrawerSubredditItem) getAdapter().getItemAt(position);
        getDrawerLayout().closeDrawers();

        PostListFragment listFragment = getActivity().getListFragment();
        listFragment.setSubmissionSort(SubmissionSort.HOT);
        listFragment.setSubreddit(subreddit.getName());
        listFragment.refreshList();
    }
}
