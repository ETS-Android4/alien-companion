package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;

/**
 * Created by sound on 8/29/2015.
 */
public class ShowMoreListener implements View.OnClickListener {

    private AppCompatActivity activity;

    public ShowMoreListener(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        RedditContentFragment fragment =
                (RedditContentFragment) activity.getSupportFragmentManager().findFragmentByTag("listFragment");
        if (fragment!=null)
            fragment.extendList();
    }

}
