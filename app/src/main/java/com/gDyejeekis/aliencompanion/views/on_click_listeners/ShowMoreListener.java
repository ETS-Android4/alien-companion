package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.support.v4.app.Fragment;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;

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
