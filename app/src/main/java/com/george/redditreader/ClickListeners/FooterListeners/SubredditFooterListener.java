package com.george.redditreader.ClickListeners.FooterListeners;

import android.app.Activity;
import android.view.View;

import com.george.redditreader.Fragments.PostListFragment;
import com.george.redditreader.LoadTasks.LoadPostsTask;
import com.george.redditreader.enums.LoadType;

/**
 * Created by George on 8/1/2015.
 */
public class SubredditFooterListener implements View.OnClickListener {

    private Activity activity;
    private PostListFragment postListFragment;

    public SubredditFooterListener(Activity activity, PostListFragment postListFragment) {
        this.activity = activity;
        this.postListFragment = postListFragment;
    }

    @Override
    public void onClick(View v) {
        postListFragment.showMore.setVisibility(View.GONE);
        postListFragment.footerProgressBar.setVisibility(View.VISIBLE);
        LoadPostsTask task = new LoadPostsTask(activity, postListFragment, LoadType.extend);
        task.execute();
    }
}
