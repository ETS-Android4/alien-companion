package com.dyejeekis.aliencompanion.Fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.ClickListeners.ShowMoreListener;
import com.dyejeekis.aliencompanion.Fragments.DialogFragments.SearchRedditDialogFragment;
import com.dyejeekis.aliencompanion.LoadTasks.LoadSearchTask;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.Views.DividerItemDecoration;
import com.dyejeekis.aliencompanion.enums.LoadType;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.api.retrieval.params.SearchSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.TimeSpan;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private AppCompatActivity activity;
    public ProgressBar mainProgressBar;
    public RecyclerView contentView;
    private LinearLayoutManager layoutManager;
    public SwipeRefreshLayout swipeRefreshLayout;
    public RedditItemListAdapter postListAdapter;
    public SearchSort searchSort, tempSort;
    public TimeSpan timeSpan;
    public String searchQuery;
    public String subreddit;
    public boolean loadMore;
    public boolean hasMore = true;
    public LoadType currentLoadType;

    public static SearchFragment newInstance(RedditItemListAdapter adapter, String searchQuery, SearchSort sort, TimeSpan time, boolean hasMore, LoadType currentLoadType) {
        SearchFragment newInstance = new SearchFragment();
        newInstance.postListAdapter = adapter;
        newInstance.searchQuery = searchQuery;
        newInstance.searchSort = sort;
        newInstance.timeSpan = time;
        newInstance.hasMore = hasMore;
        newInstance.currentLoadType = currentLoadType;
        return newInstance;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        if(searchQuery==null) searchQuery = activity.getIntent().getStringExtra("query");
        subreddit = activity.getIntent().getStringExtra("subreddit");
        //if(subreddit!=null) Log.d("subreddit extra value", subreddit);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMore = MainActivity.endlessPosts;
        if(MainActivity.swipeRefresh && layoutManager.findFirstCompletelyVisibleItemPosition()==0) swipeRefreshLayout.setEnabled(true);
        else swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
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
        swipeRefreshLayout.setColorSchemeColors(MainActivity.currentColor);

        layoutManager = new LinearLayoutManager(activity);
        contentView.setLayoutManager(layoutManager);
        contentView.setHasFixedSize(true);
        contentView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        contentView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (MainActivity.swipeRefresh && layoutManager.findFirstCompletelyVisibleItemPosition() == 0)
                    swipeRefreshLayout.setEnabled(true);
                else swipeRefreshLayout.setEnabled(false);

                int pastVisiblesItems, visibleItemCount, totalItemCount;
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                if (loadMore && hasMore) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount - 6) {
                        loadMore = false;
                        //Log.d("scroll listener", "load more now");
                        ShowMoreListener listener = new ShowMoreListener(activity, activity.getFragmentManager().findFragmentByTag("listFragment"));
                        listener.onClick(recyclerView);
                    }
                }
            }
        });

        if(currentLoadType == null) {
            if (postListAdapter == null) {
                currentLoadType = LoadType.init;
                setSearchSort(SearchSort.RELEVANCE);
                setTimeSpan(TimeSpan.ALL);
                LoadSearchTask task = new LoadSearchTask(activity, this, LoadType.init);
                task.execute();
            } else {
                setActionBarSubtitle();
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(postListAdapter);
            }
        }
        else switch (currentLoadType) {
            case init:
                contentView.setVisibility(View.GONE);
                mainProgressBar.setVisibility(View.VISIBLE);
                break;
            case refresh:
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(postListAdapter);
                swipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                break;
            case extend:
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(postListAdapter);
                postListAdapter.setLoadingMoreItems(true);
                break;
        }

        return view;
    }

    @Override public void onRefresh() {
        refreshList();
    }

    public void refreshList() {
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        LoadSearchTask task = new LoadSearchTask(activity, this, LoadType.refresh);
        task.execute();
    }

    public void refreshList(SearchSort sort, TimeSpan time) {
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        LoadSearchTask task = new LoadSearchTask(activity, this, LoadType.refresh, sort, time);
        task.execute();
    }

    public void changeQuery(String newQuery) {
        currentLoadType = LoadType.init;
        searchQuery = newQuery;
        searchSort = SearchSort.RELEVANCE;
        timeSpan = TimeSpan.ALL;
        setActionBarTitle();
        setActionBarSubtitle();
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        LoadSearchTask task = new LoadSearchTask(activity, this, LoadType.init);
        task.execute();
    }

    public void redrawList() {
        List<RedditItem> items = postListAdapter.redditItems;
        items.remove(items.size() - 1);
        postListAdapter = new RedditItemListAdapter(activity, items);
        contentView.setAdapter(postListAdapter);
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
            case R.id.action_search:
                showDialog(new SearchRedditDialogFragment());
                return true;
            case R.id.action_switch_view:
                showViewsPopup(activity.findViewById(R.id.action_refresh));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialog(DialogFragment fragment) {
        FragmentManager fm = getFragmentManager();
        fragment.show(fm, "dialog");
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

    private void showViewsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_post_views);
        int index;
        switch (MainActivity.currentPostListView) {
            case R.layout.post_list_item_reversed:
                index = 1;
                break;
            case R.layout.post_list_item_card:
                index = 2;
                break;
            default:
                index = 0;
                break;
        }
        popupMenu.getMenu().getItem(index).setChecked(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int resource = -1;
                switch (item.getItemId()) {
                    case R.id.action_list_default:
                        resource = R.layout.post_list_item;
                        break;
                    case R.id.action_list_reversed:
                        resource = R.layout.post_list_item_reversed;
                        break;
                    case R.id.action_cards:
                        resource = R.layout.post_list_item_card;
                        break;
                }
                if (resource != -1 && resource != MainActivity.currentPostListView) {
                    SharedPreferences.Editor editor = MainActivity.prefs.edit();
                    MainActivity.currentPostListView = resource;
                    editor.putInt("postListView", resource);
                    editor.apply();
                    if(currentLoadType==null) redrawList();
                    return true;
                }
                return false;
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
