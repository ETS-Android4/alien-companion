package com.george.redditreader.ClickListeners;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.View;

import com.george.redditreader.Fragments.PostListFragment;
import com.george.redditreader.Fragments.SearchFragment;
import com.george.redditreader.Fragments.UserFragment;
import com.george.redditreader.LoadTasks.LoadPostsTask;
import com.george.redditreader.LoadTasks.LoadSearchTask;
import com.george.redditreader.LoadTasks.LoadUserContentTask;
import com.george.redditreader.enums.LoadType;

/**
 * Created by sound on 8/29/2015.
 */
public class ShowMoreListener implements View.OnClickListener {

    private enum FragmentType {
        subreddit, user, search
    }

    private Context context;
    private Fragment fragment;
    private FragmentType type;

    public ShowMoreListener(Context context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;

        if(fragment instanceof PostListFragment) type = FragmentType.subreddit;
        else if(fragment instanceof UserFragment) type = FragmentType.user;
        else if(fragment instanceof SearchFragment) type = FragmentType.search;
        else throw new IllegalArgumentException("invalid fragment type");
    }

    @Override
    public void onClick(View v) {
        switch (type) {
            case subreddit:
                PostListFragment postListFragment = (PostListFragment) fragment;
                postListFragment.postListAdapter.setLoadingMoreItems(true);
                LoadPostsTask subredditTask = new LoadPostsTask(context, postListFragment, LoadType.extend);
                subredditTask.execute();
                break;
            case user:
                UserFragment userFragment = (UserFragment) fragment;
                userFragment.userAdapter.setLoadingMoreItems(true);
                LoadUserContentTask userTask = new LoadUserContentTask((Activity) context, userFragment, LoadType.extend, userFragment.userContent);
                userTask.execute();
                break;
            case search:
                SearchFragment searchFragment = (SearchFragment) fragment;
                //searchFragment.searchAdapter.setLoadingMoreItems(true);
                LoadSearchTask searchTask = new LoadSearchTask(context, searchFragment, LoadType.extend);
                searchTask.execute();
                break;
        }
    }
}
