package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.MessageFragment;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;
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

    private RedditContentFragment fragment;

    public ShowMoreListener(Fragment fragment) {
        this.fragment = (RedditContentFragment) fragment;
    }

    @Override
    public void onClick(View v) {
        fragment.extendList();
    }
}
