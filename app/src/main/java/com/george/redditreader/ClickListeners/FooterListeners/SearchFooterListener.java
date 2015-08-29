package com.george.redditreader.ClickListeners.FooterListeners;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.george.redditreader.Fragments.SearchFragment;
import com.george.redditreader.LoadTasks.LoadSearchTask;
import com.george.redditreader.enums.LoadType;

/**
 * Created by George on 8/1/2015.
 */
public class SearchFooterListener implements View.OnClickListener { //TODO: to be deleted

    //private Activity activity;
    private Context context;
    private SearchFragment searchFragment;

    public SearchFooterListener(Context context, SearchFragment searchFragment) {
        this.context = context;
        this.searchFragment = searchFragment;
    }

    @Override
    public void onClick(View v) {
        searchFragment.showMore.setVisibility(View.GONE);
        searchFragment.footerProgressBar.setVisibility(View.VISIBLE);
        LoadSearchTask task = new LoadSearchTask(context, searchFragment, LoadType.extend);
        task.execute();
    }
}
