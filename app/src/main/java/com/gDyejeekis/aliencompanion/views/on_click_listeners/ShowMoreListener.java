package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.MessageFragment;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.fragments.SearchFragment;
import com.gDyejeekis.aliencompanion.fragments.UserFragment;
import com.gDyejeekis.aliencompanion.asynctask.LoadMessagesTask;
import com.gDyejeekis.aliencompanion.asynctask.LoadPostsTask;
import com.gDyejeekis.aliencompanion.asynctask.LoadSearchTask;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserContentTask;
import com.gDyejeekis.aliencompanion.enums.LoadType;

/**
 * Created by sound on 8/29/2015.
 */
public class ShowMoreListener implements View.OnClickListener {

    private enum FragmentType {
        subreddit, user, search, message
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
        else if(fragment instanceof MessageFragment) type = FragmentType.message;
        else throw new IllegalArgumentException("invalid fragment type");
    }

    @Override
    public void onClick(View v) {
        switch (type) {
            case subreddit:
                PostListFragment postListFragment = (PostListFragment) fragment;
                //if(postListFragment.currentLoadType!=null) postListFragment.task.cancel(true);
                postListFragment.currentLoadType = LoadType.extend;
                postListFragment.postListAdapter.setLoadingMoreItems(true);
                postListFragment.task = new LoadPostsTask(context, postListFragment, LoadType.extend);
                postListFragment.task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case user:
                UserFragment userFragment = (UserFragment) fragment;
                //if(userFragment.currentLoadType!=null) userFragment.task.cancel(true);
                userFragment.currentLoadType = LoadType.extend;
                userFragment.userAdapter.setLoadingMoreItems(true);
                userFragment.task = new LoadUserContentTask((Activity) context, userFragment, LoadType.extend);
                userFragment.task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case search:
                SearchFragment searchFragment = (SearchFragment) fragment;
                //if(searchFragment.currentLoadType!=null) searchFragment.task.cancel(true);
                searchFragment.currentLoadType = LoadType.extend;
                searchFragment.postListAdapter.setLoadingMoreItems(true);
                searchFragment.task = new LoadSearchTask(context, searchFragment, LoadType.extend);
                searchFragment.task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case message:
                MessageFragment messageFragment = (MessageFragment) fragment;
                //if(messageFragment.currentLoadType!=null) messageFragment.task.cancel(true);
                messageFragment.currentLoadType = LoadType.extend;
                messageFragment.adapter.setLoadingMoreItems(true);
                messageFragment.task = new LoadMessagesTask(context, messageFragment, LoadType.extend);
                messageFragment.task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}
