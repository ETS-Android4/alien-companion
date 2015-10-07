package com.dyejeekis.aliencompanion.Fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
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
import com.dyejeekis.aliencompanion.Views.DividerItemDecoration;
import com.dyejeekis.aliencompanion.enums.LoadType;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.api.retrieval.params.SearchSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.TimeSpan;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private AppCompatActivity activity;
    public ProgressBar mainProgressBar;
    public RecyclerView contentView;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    public RedditItemListAdapter postListAdapter;
    public SearchSort searchSort;
    public TimeSpan timeSpan;
    public String searchQuery;
    public String subreddit;
    public boolean hasPosts;
    public boolean loadMore;
    public boolean hasMore = true;

    public static boolean currentlyLoading = false;

    public static SearchFragment newInstance(RedditItemListAdapter adapter, String searchQuery, SearchSort sort, TimeSpan time) {
        SearchFragment newInstance = new SearchFragment();
        newInstance.postListAdapter = adapter;
        newInstance.searchQuery = searchQuery;
        newInstance.searchSort = sort;
        newInstance.timeSpan = time;
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

        if(!currentlyLoading) {
            if (postListAdapter == null) {
                currentlyLoading = true;
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

        return view;
    }

    @Override public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        refreshList();
    }

    //Reload posts list
    public void refreshList() {
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        LoadSearchTask task = new LoadSearchTask(activity, this, LoadType.refresh);
        task.execute();
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
                        setSearchSort(SearchSort.HOT);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_new:
                        setSearchSort(SearchSort.NEW);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_relevance:
                        setSearchSort(SearchSort.RELEVANCE);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_top:
                        setSearchSort(SearchSort.TOP);
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_comments:
                        setSearchSort(SearchSort.COMMENTS);
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
                        setTimeSpan(TimeSpan.HOUR);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_day:
                        setTimeSpan(TimeSpan.DAY);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_week:
                        setTimeSpan(TimeSpan.WEEK);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_month:
                        setTimeSpan(TimeSpan.MONTH);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_year:
                        setTimeSpan(TimeSpan.YEAR);
                        setActionBarSubtitle();
                        refreshList();
                        return true;
                    case R.id.action_sort_all:
                        setTimeSpan(TimeSpan.ALL);
                        setActionBarSubtitle();
                        refreshList();
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
