package com.george.redditreader.ClickListeners.NavDrawerListeners;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Adapters.NavDrawerAdapter;
import com.george.redditreader.Models.NavDrawer.NavDrawerItem;

/**
 * Created by George on 6/26/2015.
 */
public abstract class NavDrawerListener implements View.OnClickListener, View.OnLongClickListener{

    private MainActivity activity;
    private RecyclerView recyclerView;
    private NavDrawerAdapter adapter;
    private DrawerLayout drawerLayout;

    public NavDrawerListener(MainActivity activity) {
        this.activity = activity;
        recyclerView = activity.getNavDrawerView();
        adapter = activity.getNavDrawerAdapter();
        drawerLayout = activity.getDrawerLayout();
    }

    protected MainActivity getActivity() {
        return activity;
    }

    protected RecyclerView getRecyclerView() {
        return recyclerView;
    }

    protected NavDrawerAdapter getAdapter() {
        return adapter;
    }

    protected DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

}
