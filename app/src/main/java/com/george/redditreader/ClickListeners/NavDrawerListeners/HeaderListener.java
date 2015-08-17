package com.george.redditreader.ClickListeners.NavDrawerListeners;

import android.view.View;

import com.george.redditreader.Activities.MainActivity;

/**
 * Created by George on 6/26/2015.
 */
public class HeaderListener extends NavDrawerListener implements View.OnClickListener {

    public HeaderListener(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onClick(View v) {
        getAdapter().toggleAccountItems();
    }
}
