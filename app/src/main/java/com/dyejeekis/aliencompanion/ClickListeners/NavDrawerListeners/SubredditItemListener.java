package com.dyejeekis.aliencompanion.ClickListeners.NavDrawerListeners;

import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Fragments.PostListFragment;
import com.dyejeekis.aliencompanion.Models.NavDrawer.NavDrawerSubredditItem;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;

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
        listFragment.setSubmissionSort(SubmissionSort.HOT);
        String subredditName = (subreddit.getName()!=null) ? subreddit.getName().toLowerCase() : null;
        listFragment.setSubreddit(subredditName);
        listFragment.refreshList();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
