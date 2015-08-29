package com.george.redditreader.ClickListeners.FooterListeners;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.george.redditreader.Fragments.PostListFragment;
import com.george.redditreader.LoadTasks.LoadPostsTask;
import com.george.redditreader.enums.LoadType;

/**
 * Created by George on 8/1/2015.
 */
public class SubredditFooterListener implements View.OnClickListener { //TODO: to be deleted

    //private Activity activity;
    private Context context;
    private PostListFragment postListFragment;

    public SubredditFooterListener(Context context, PostListFragment postListFragment) {
        this.context = context;
        this.postListFragment = postListFragment;
    }

    @Override
    public void onClick(View v) {
        //postListFragment.showMore.setVisibility(View.GONE);
        //postListFragment.footerProgressBar.setVisibility(View.VISIBLE);
        //postListFragment.postListAdapter.loadingMoreItems = true;
        //postListFragment.postListAdapter.notifyDataSetChanged();
        postListFragment.postListAdapter.setLoadingMoreItems(true);
        LoadPostsTask task = new LoadPostsTask(context, postListFragment, LoadType.extend);
        task.execute();
    }
}
