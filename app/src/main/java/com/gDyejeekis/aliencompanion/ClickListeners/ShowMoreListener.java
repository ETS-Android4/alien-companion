package com.gDyejeekis.aliencompanion.ClickListeners;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.View;

import com.gDyejeekis.aliencompanion.Fragments.MessageFragment;
import com.gDyejeekis.aliencompanion.Fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.Fragments.SearchFragment;
import com.gDyejeekis.aliencompanion.Fragments.UserFragment;
import com.gDyejeekis.aliencompanion.LoadTasks.LoadMessagesTask;
import com.gDyejeekis.aliencompanion.LoadTasks.LoadPostsTask;
import com.gDyejeekis.aliencompanion.LoadTasks.LoadSearchTask;
import com.gDyejeekis.aliencompanion.LoadTasks.LoadUserContentTask;
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
                postListFragment.task.execute();
                break;
            case user:
                UserFragment userFragment = (UserFragment) fragment;
                //if(userFragment.currentLoadType!=null) userFragment.task.cancel(true);
                userFragment.currentLoadType = LoadType.extend;
                userFragment.userAdapter.setLoadingMoreItems(true);
                userFragment.task = new LoadUserContentTask((Activity) context, userFragment, LoadType.extend);
                userFragment.task.execute();
                break;
            case search:
                SearchFragment searchFragment = (SearchFragment) fragment;
                //if(searchFragment.currentLoadType!=null) searchFragment.task.cancel(true);
                searchFragment.currentLoadType = LoadType.extend;
                searchFragment.postListAdapter.setLoadingMoreItems(true);
                searchFragment.task = new LoadSearchTask(context, searchFragment, LoadType.extend);
                searchFragment.task.execute();
                break;
            case message:
                MessageFragment messageFragment = (MessageFragment) fragment;
                //if(messageFragment.currentLoadType!=null) messageFragment.task.cancel(true);
                messageFragment.currentLoadType = LoadType.extend;
                messageFragment.adapter.setLoadingMoreItems(true);
                messageFragment.task = new LoadMessagesTask(context, messageFragment, LoadType.extend);
                messageFragment.task.execute();
        }
    }
}
