package com.george.redditreader.Models.NavDrawer;

import com.george.redditreader.Adapters.NavDrawerAdapter;

/**
 * Created by George on 6/26/2015.
 */
public class NavDrawerSubreddits implements NavDrawerItem {

    public int getType() {
        return NavDrawerAdapter.VIEW_TYPE_SUBREDDITS;
    }
}
