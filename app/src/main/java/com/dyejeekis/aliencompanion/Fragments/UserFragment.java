package com.dyejeekis.aliencompanion.Fragments;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.ClickListeners.ShowMoreListener;
import com.dyejeekis.aliencompanion.LoadTasks.LoadPostsTask;
import com.dyejeekis.aliencompanion.LoadTasks.LoadUserContentTask;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.Views.DividerItemDecoration;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.dyejeekis.aliencompanion.enums.LoadType;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public ProgressBar progressBar;
    public RecyclerView contentView;
    private LinearLayoutManager layoutManager;
    public SwipeRefreshLayout swipeRefreshLayout;
    public RedditItemListAdapter userAdapter;
    private AppCompatActivity activity;
    public String username;
    public UserOverviewSort userOverviewSort;
    public UserSubmissionsCategory userContent, tempCategory;
    public boolean loadMore;
    public boolean hasMore = true;
    public LoadType currentLoadType;
    public LoadUserContentTask task;

    //public static boolean currentlyLoading = false;

    public static UserFragment newInstance(RedditItemListAdapter adapter, String username, UserOverviewSort sort, UserSubmissionsCategory category, boolean hasMore) {
        UserFragment newInstance = new UserFragment();
        newInstance.userAdapter = adapter;
        newInstance.username = username;
        newInstance.userOverviewSort = sort;
        newInstance.userContent = category;
        newInstance.hasMore = hasMore;
        return newInstance;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        loadMore = MainActivity.endlessPosts;
        if(username==null) username = activity.getIntent().getStringExtra("username");
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadMore = MainActivity.endlessPosts;
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
        if(userContent == null) {
            userContent = UserSubmissionsCategory.OVERVIEW;
            userOverviewSort = UserOverviewSort.NEW;
        }
        setActionBarSubtitle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
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
            if (userAdapter == null) {
                //currentlyLoading = true;
                currentLoadType = LoadType.init;
                userContent = UserSubmissionsCategory.OVERVIEW;
                userOverviewSort = UserOverviewSort.NEW;
                task = new LoadUserContentTask(activity, this, LoadType.init);
                task.execute();
            } else {
                setActionBarSubtitle();
                progressBar.setVisibility(View.GONE);
                contentView.setAdapter(userAdapter);
            }
        }
        else switch (currentLoadType) {
            case init:
                //Log.d("geo test", "currentLoadType is init");
                contentView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case refresh:
                //Log.d("geo test", "currentLoadType is refersh");
                progressBar.setVisibility(View.GONE);
                contentView.setAdapter(userAdapter);
                swipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                break;
            case extend:
                //Log.d("geo test", "currentLoadType is extend");
                progressBar.setVisibility(View.GONE);
                contentView.setAdapter(userAdapter);
                userAdapter.setLoadingMoreItems(true);
                break;
        }
        //else
        //    Log.d("geo test", "currently loading");

        return view;
    }

    @Override public void onRefresh() {
        //swipeRefreshLayout.setRefreshing(false);
        refreshUser();
    }

    public void refreshUser() {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadUserContentTask(activity, this, LoadType.refresh);
        task.execute();
    }

    public void refreshUser(UserSubmissionsCategory category, UserOverviewSort sort) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadUserContentTask(activity, this, LoadType.refresh, category, sort);
        task.execute();
    }

    public void changeUser(String username) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.init;
        this.username = username;
        this.userContent = UserSubmissionsCategory.OVERVIEW;
        this.userOverviewSort = UserOverviewSort.NEW;
        setActionBarTitle();
        setActionBarSubtitle();
        contentView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        task = new LoadUserContentTask(activity, this, LoadType.init);
        task.execute();
    }

    public void redrawList() {
        List<RedditItem> items = userAdapter.redditItems;
        items.remove(items.size() - 1);
        userAdapter = new RedditItemListAdapter(activity, items);
        contentView.setAdapter(userAdapter);
    }

    public void setActionBarTitle() {
        activity.getSupportActionBar().setTitle(username);
    }

    public void setActionBarSubtitle() {
        String subtitle = userContent.value();
        if(userOverviewSort != null) subtitle = subtitle.concat(": " + userOverviewSort.value());
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                refreshUser();
                return true;
            case R.id.action_sort:
                showContentPopup(activity.findViewById(R.id.action_sort));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showContentPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        if(MainActivity.currentUser!=null && username.equals(MainActivity.currentUser.getUsername())) popupMenu.inflate(R.menu.menu_user_content_account);
        else popupMenu.inflate(R.menu.menu_user_content);
        //popupMenu.inflate(R.menu.menu_user_content_public);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_user_overview:
                        tempCategory = UserSubmissionsCategory.OVERVIEW;
                        showSortPopup();
                        return true;
                    case R.id.action_user_comments:
                        tempCategory = UserSubmissionsCategory.COMMENTS;
                        showSortPopup();
                        return true;
                    case R.id.action_user_submitted:
                        tempCategory = UserSubmissionsCategory.SUBMITTED;
                        showSortPopup();
                        return true;
                    case R.id.action_user_gilded:
                        //userOverviewSort = null;
                        //userContent = UserSubmissionsCategory.GILDED;
                        //setActionBarSubtitle();
                        refreshUser(UserSubmissionsCategory.GILDED, null);
                        return true;
                    case R.id.action_user_upvoted:
                        //userOverviewSort = null;
                        //userContent = UserSubmissionsCategory.LIKED;
                        //setActionBarSubtitle();
                        refreshUser(UserSubmissionsCategory.LIKED, null);
                        return true;
                    case R.id.action_user_downvoted:
                        //userOverviewSort = null;
                        //userContent = UserSubmissionsCategory.DISLIKED;
                        //setActionBarSubtitle();
                        refreshUser(UserSubmissionsCategory.DISLIKED, null);
                        return true;
                    case R.id.action_user_hidden:
                        //userOverviewSort = null;
                        //userContent = UserSubmissionsCategory.HIDDEN;
                        //setActionBarSubtitle();
                        refreshUser(UserSubmissionsCategory.HIDDEN, null);
                        return true;
                    case R.id.action_user_saved:
                        //userOverviewSort = null;
                        //userContent = UserSubmissionsCategory.SAVED;
                        //setActionBarSubtitle();
                        refreshUser(UserSubmissionsCategory.SAVED, null);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showSortPopup() {
        showSortPopup(activity.findViewById(R.id.action_sort));
    }

    private void showSortPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        popupMenu.inflate(R.menu.menu_user_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_new:
                        //userOverviewSort = UserOverviewSort.NEW;
                        //setActionBarSubtitle();
                        refreshUser(tempCategory, UserOverviewSort.NEW);
                        return true;
                    case R.id.action_sort_hot:
                        //userOverviewSort = UserOverviewSort.HOT;
                        //setActionBarSubtitle();
                        refreshUser(tempCategory, UserOverviewSort.HOT);
                        return true;
                    case R.id.action_sort_top:
                        //userOverviewSort = UserOverviewSort.TOP;
                        //setActionBarSubtitle();
                        refreshUser(tempCategory, UserOverviewSort.TOP);
                        return true;
                    case R.id.action_sort_controversial:
                        //userOverviewSort = UserOverviewSort.COMMENTS;
                        //setActionBarSubtitle();
                        refreshUser(tempCategory, UserOverviewSort.COMMENTS);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

}
