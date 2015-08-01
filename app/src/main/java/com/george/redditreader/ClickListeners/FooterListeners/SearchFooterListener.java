package com.george.redditreader.ClickListeners.FooterListeners;

import android.app.Activity;
import android.view.View;

import com.george.redditreader.Fragments.SearchFragment;
import com.george.redditreader.LoadTasks.LoadSearchTask;
import com.george.redditreader.enums.LoadType;

/**
 * Created by George on 8/1/2015.
 */
public class SearchFooterListener implements View.OnClickListener {

    private Activity activity;
    private SearchFragment searchFragment;

    public SearchFooterListener(Activity activity, SearchFragment searchFragment) {
        this.activity = activity;
        this.searchFragment = searchFragment;
    }

    @Override
    public void onClick(View v) {
        searchFragment.showMore.setVisibility(View.GONE);
        searchFragment.footerProgressBar.setVisibility(View.VISIBLE);
        LoadSearchTask task = new LoadSearchTask(activity, searchFragment, LoadType.extend);
        task.execute();
    }
}
