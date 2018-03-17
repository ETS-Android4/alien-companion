package com.gDyejeekis.aliencompanion.views.on_click_listeners.nav_drawer_listeners;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.views.adapters.NavDrawerAdapter;

/**
 * Created by George on 6/26/2015.
 */
public abstract class NavDrawerListener implements View.OnClickListener, View.OnLongClickListener{

    private MainActivity activity;
    private RecyclerView.ViewHolder viewHolder;
    private NavDrawerAdapter adapter;
    private DrawerLayout drawerLayout;

    public NavDrawerListener(MainActivity activity, RecyclerView.ViewHolder viewHolder) {
        this.activity = activity;
        this.viewHolder = viewHolder;
        this.adapter = activity.getNavDrawerAdapter();
        this.drawerLayout = activity.getDrawerLayout();
    }

    protected MainActivity getActivity() {
        return activity;
    }

    protected RecyclerView.ViewHolder getViewHolder() {
        return viewHolder;
    }

    protected NavDrawerAdapter getAdapter() {
        return adapter;
    }

    DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

}
