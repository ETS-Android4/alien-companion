package com.gDyejeekis.aliencompanion.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SearchRedditDialogFragment;
import com.gDyejeekis.aliencompanion.asynctask.LoadSearchTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SearchSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends RedditContentFragment {

    public static final String TAG = "SearchFragment";

    public SearchSort searchSort, tempSort;
    public TimeSpan timeSpan;
    public String searchQuery;
    public String subreddit;
    public LoadType currentLoadType;
    public LoadSearchTask task;

    public static SearchFragment newInstance(RedditItemListAdapter adapter, String searchQuery, SearchSort sort, TimeSpan time, boolean hasMore, LoadType currentLoadType) {
        SearchFragment newInstance = new SearchFragment();
        newInstance.adapter = adapter;
        newInstance.searchQuery = searchQuery;
        newInstance.searchSort = sort;
        newInstance.timeSpan = time;
        newInstance.hasMore = hasMore;
        newInstance.currentLoadType = currentLoadType;
        return newInstance;
    }

    @Override
    public boolean hasFabNavigation() {
        return true;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if(bundle!=null) {
            subreddit = bundle.getString("subreddit");
            searchQuery = bundle.getString("query");
            searchSort = (SearchSort) bundle.getSerializable("sort");
            timeSpan = (TimeSpan) bundle.getSerializable("time");
        }

        if(searchQuery==null) searchQuery = activity.getIntent().getStringExtra("query");
        if(subreddit==null) subreddit = activity.getIntent().getStringExtra("subreddit");
        //if(subreddit!=null) Log.d("subreddit extra value", subreddit);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("subreddit", subreddit);
        outState.putString("query", searchQuery);
        outState.putSerializable("sort", searchSort);
        outState.putSerializable("time", timeSpan);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        if(searchSort == null || timeSpan == null) {
            searchSort = SearchSort.RELEVANCE;
            timeSpan = TimeSpan.ALL;
        }
        setActionBarSubtitle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        mainProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        contentView = (RecyclerView) view.findViewById(R.id.recyclerView_postList);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(MyApplication.currentColor);

        updateContentViewProperties();
        initFabNavOptions(view);

        contentView.addOnScrollListener(onScrollListener);

        if(currentLoadType == null) {
            if (adapter == null) {
                currentLoadType = LoadType.init;
                if(searchSort==null) setSearchSort(SearchSort.RELEVANCE);
                if(timeSpan == null) setTimeSpan(TimeSpan.ALL);
                task = new LoadSearchTask(activity, this, LoadType.init);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                setActionBarSubtitle();
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
            }
        }
        else switch (currentLoadType) {
            case init:
                contentView.setVisibility(View.GONE);
                mainProgressBar.setVisibility(View.VISIBLE);
                break;
            case refresh:
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
                swipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                break;
            case extend:
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
                adapter.setLoadingMoreItems(true);
                break;
        }

        return view;
    }

    @Override
    public void extendList() {
        currentLoadType = LoadType.extend;
        adapter.setLoadingMoreItems(true);
        task = new LoadSearchTask(activity, this, LoadType.extend);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void refreshList() {
        refreshList(searchSort, timeSpan);
    }

    public void refreshList(SearchSort sort, TimeSpan time) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadSearchTask(activity, this, LoadType.refresh, sort, time);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        hideAllFabOptions();
    }

    public void changeQuery(String newQuery) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.init;
        searchQuery = newQuery;
        searchSort = SearchSort.RELEVANCE;
        timeSpan = TimeSpan.ALL;
        setActionBarTitle();
        setActionBarSubtitle();
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        task = new LoadSearchTask(activity, this, LoadType.init);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                refreshList();
                return true;
            case R.id.action_sort:
                showSortPopup(activity.findViewById(R.id.action_sort));
                return true;
            case R.id.action_hide_read:
                removeClickedPosts();
                return true;
            case R.id.action_search:
                showSearchDialog();
                return true;
            case R.id.action_switch_view:
                showViewsPopup(activity.findViewById(R.id.action_refresh));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showSearchDialog() {
        hideAllFabOptions();
        SearchRedditDialogFragment dialog = new SearchRedditDialogFragment();
        dialog.show(activity.getSupportFragmentManager(), "dialog");
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
    }

    public void showSortPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_search_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hot:
                        tempSort = SearchSort.HOT;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_new:
                        tempSort = SearchSort.NEW;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_relevance:
                        tempSort = SearchSort.RELEVANCE;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_top:
                        tempSort = SearchSort.TOP;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_comments:
                        tempSort = SearchSort.COMMENTS;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showSortTimePopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_search_sort_time);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hour:
                        refreshList(tempSort, TimeSpan.HOUR);
                        return true;
                    case R.id.action_sort_day:
                        refreshList(tempSort, TimeSpan.DAY);
                        return true;
                    case R.id.action_sort_week:
                        refreshList(tempSort, TimeSpan.WEEK);
                        return true;
                    case R.id.action_sort_month:
                        refreshList(tempSort, TimeSpan.MONTH);
                        return true;
                    case R.id.action_sort_year:
                        refreshList(tempSort, TimeSpan.YEAR);
                        return true;
                    case R.id.action_sort_all:
                        refreshList(tempSort, TimeSpan.ALL);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    public void setSearchSort(SearchSort searchSort) {
        this.searchSort = searchSort;
    }

    public void setTimeSpan(TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
    }

    public void setActionBarTitle() {
        activity.getSupportActionBar().setTitle(searchQuery);
    }

    public void setActionBarSubtitle() {
        activity.getSupportActionBar().setSubtitle(searchSort.value()+": "+timeSpan.value());
    }

}
