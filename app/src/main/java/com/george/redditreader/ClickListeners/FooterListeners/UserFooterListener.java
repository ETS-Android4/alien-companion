package com.george.redditreader.ClickListeners.FooterListeners;

import android.app.Activity;
import android.view.View;

import com.george.redditreader.Fragments.UserFragment;
import com.george.redditreader.LoadTasks.LoadUserInfoTask;
import com.george.redditreader.enums.LoadType;

/**
 * Created by George on 8/1/2015.
 */
public class UserFooterListener implements View.OnClickListener {

    private Activity activity;
    private UserFragment userFragment;

    public UserFooterListener(Activity activity, UserFragment userFragment) {
        this.activity = activity;
        this.userFragment = userFragment;
    }

    @Override
    public void onClick(View v) {
        userFragment.showMore.setVisibility(View.GONE);
        userFragment.footerProgressBar.setVisibility(View.VISIBLE);
        LoadUserInfoTask task = new LoadUserInfoTask(activity, userFragment, LoadType.extend, userFragment.userContent);
        task.execute();
    }
}
