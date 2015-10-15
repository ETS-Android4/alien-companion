package com.dyejeekis.aliencompanion.Fragments;


import android.app.Activity;
import android.content.Intent;
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
import com.dyejeekis.aliencompanion.Activities.SubmitActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.ClickListeners.ShowMoreListener;
import com.dyejeekis.aliencompanion.Fragments.DialogFragments.SearchRedditDialogFragment;
import com.dyejeekis.aliencompanion.LoadTasks.LoadPostsTask;
import com.dyejeekis.aliencompanion.Services.DownloaderService;
import com.dyejeekis.aliencompanion.Utils.GeneralUtils;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.Views.DividerItemDecoration;
import com.dyejeekis.aliencompanion.enums.LoadType;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.dyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.dyejeekis.aliencompanion.enums.SubmitType;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public RedditItemListAdapter postListAdapter;
    public ProgressBar mainProgressBar;
    public RecyclerView contentView;
    private LinearLayoutManager layoutManager;
    public SwipeRefreshLayout swipeRefreshLayout;
    public String subreddit;
    private AppCompatActivity activity;
    public SubmissionSort submissionSort;
    private SubmissionSort tempSort;
    public TimeSpan timeSpan;
    public boolean hasPosts;
    public boolean loadMore;

    public static boolean currentlyLoading = false;

    public static PostListFragment newInstance(RedditItemListAdapter adapter, String subreddit, SubmissionSort sort, TimeSpan time) {
        PostListFragment listFragment = new PostListFragment();
        listFragment.postListAdapter = adapter;
        listFragment.subreddit = subreddit;
        listFragment.submissionSort = sort;
        listFragment.timeSpan = time;
        return listFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        if(subreddit==null) subreddit = activity.getIntent().getStringExtra("subreddit");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMore = MainActivity.endlessPosts;
        if(MainActivity.swipeRefresh && layoutManager.findFirstCompletelyVisibleItemPosition()==0) swipeRefreshLayout.setEnabled(true);
        else swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
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

                if (!MainActivity.offlineModeEnabled && loadMore) {
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
                setSubmissionSort(SubmissionSort.HOT);
                LoadPostsTask task = new LoadPostsTask(activity, this, LoadType.init);
                task.execute();
            } else {
                setActionBarSubtitle();
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(postListAdapter);
            }
        }

        return view;
    }

    public void colorSchemeChanged() {
        swipeRefreshLayout.setColorSchemeColors(MainActivity.colorPrimary);
        if(postListAdapter!=null) postListAdapter.notifyDataSetChanged();
    }

    @Override public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        refreshList();
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
                SearchRedditDialogFragment searchDialog = new SearchRedditDialogFragment();
                Bundle args = new Bundle();
                args.putString("subreddit", subreddit);
                searchDialog.setArguments(args);
                searchDialog.show(activity.getFragmentManager(), "dialog");
                return true;
            case R.id.action_switch_view:
                showViewsPopup(activity.findViewById(R.id.action_refresh));
                return true;
            //case R.id.action_toggle_hidden:
            //    MainActivity.showHiddenPosts = !MainActivity.showHiddenPosts;
            //    if(MainActivity.showHiddenPosts) item.setChecked(true);
            //    else item.setChecked(false);
            //    refreshList();
            //    return true;
            case R.id.action_hide_read:
                postListAdapter.hideReadPosts();
                return true;
            case R.id.action_sync_posts:
                String toastMessage;
                if(GeneralUtils.isNetworkAvailable(activity)) {
                    String filename = (subreddit == null) ? "frontpage" : subreddit;
                    toastMessage = filename + " added to sync queue";
                    Intent intent = new Intent(activity, DownloaderService.class);
                    intent.putExtra("sort", submissionSort);
                    intent.putExtra("time", timeSpan);
                    intent.putExtra("subreddit", subreddit);
                    activity.startService(intent);
                }
                else toastMessage = "Check your connection and try again";
                ToastUtils.displayShortToast(activity, toastMessage);
                return true;
            case R.id.action_submit_post:
                showSubmitPopup(activity.findViewById(R.id.action_refresh)); //TODO: put correct anchor
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showViewsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_post_views);
        int index;
        switch (MainActivity.currentPostListView) {
            case R.layout.post_list_item_reversed:
                index = 1;
                break;
            default:
                index = 0;
                break;
        }
        popupMenu.getMenu().getItem(index).setChecked(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = MainActivity.prefs.edit();
                int resource = -1;
                switch (item.getItemId()) {
                    case R.id.action_list_default:
                        resource = R.layout.post_list_item;
                        break;
                    case R.id.action_list_reversed:
                        resource = R.layout.post_list_item_reversed;
                        break;
                }
                if (resource != -1) {
                    MainActivity.currentPostListView = resource;
                    editor.putInt("postListView", resource);
                    editor.apply();
                    refreshList();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void showSubmitPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_post_type);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(activity, SubmitActivity.class);
                intent.putExtra("subreddit", subreddit);
                switch (item.getItemId()) {
                    case R.id.action_submit_link:
                        intent.putExtra("submitType", SubmitType.link);
                        startActivity(intent);
                        return true;
                    case R.id.action_submit_text:
                        intent.putExtra("submitType", SubmitType.self);
                        startActivity(intent);
                        return true;
                    //case R.id.action_submit_image: //TODO: implement direct image posting
                    //    return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    public void showSortPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_posts_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hot:
                        setSubmissionSort(SubmissionSort.HOT);
                        refreshList();
                        return true;
                    case R.id.action_sort_new:
                        setSubmissionSort(SubmissionSort.NEW);
                        refreshList();
                        return true;
                    case R.id.action_sort_rising:
                        setSubmissionSort(SubmissionSort.RISING);
                        refreshList();
                        return true;
                    case R.id.action_sort_top:
                        //setSubmissionSort(SubmissionSort.TOP);
                        //refreshList();
                        tempSort = SubmissionSort.TOP;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_controversial:
                        //setSubmissionSort(SubmissionSort.CONTROVERSIAL);
                        //refreshList();
                        tempSort = SubmissionSort.CONTROVERSIAL;
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
        popupMenu.inflate(R.menu.menu_posts_sort_time);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_hour:
                        setSubmissionSort(TimeSpan.HOUR);
                        refreshList();
                        return true;
                    case R.id.action_sort_day:
                        setSubmissionSort(TimeSpan.DAY);
                        refreshList();
                        return true;
                    case R.id.action_sort_week:
                        setSubmissionSort(TimeSpan.WEEK);
                        refreshList();
                        return true;
                    case R.id.action_sort_month:
                        setSubmissionSort(TimeSpan.MONTH);
                        refreshList();
                        return true;
                    case R.id.action_sort_year:
                        setSubmissionSort(TimeSpan.YEAR);
                        refreshList();
                        return true;
                    case R.id.action_sort_all:
                        setSubmissionSort(TimeSpan.ALL);
                        refreshList();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
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
    public void onDestroyView() {
        super.onDestroyView();
        activity = null;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        if(submissionSort == null) submissionSort = SubmissionSort.HOT;
        setActionBarSubtitle();
    }

    public void refreshList() {
        //Log.d("PostListFragment", "Refreshing posts...");
        //contentView.setVisibility(View.GONE);
        //mainProgressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        LoadPostsTask task = new LoadPostsTask(activity, this, LoadType.refresh);
        task.execute();
    }

    public void changeSubreddit(String subreddit) {
        this.subreddit = subreddit;
        this.submissionSort = SubmissionSort.HOT;
        this.timeSpan = null;
        setActionBarTitle();
        setActionBarSubtitle();
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        LoadPostsTask task = new LoadPostsTask(activity, this, LoadType.init);
        task.execute();
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
        setActionBarTitle();
    }

    public void setSubmissionSort(SubmissionSort sort) {
        this.timeSpan = null;
        this.submissionSort = sort;
        setActionBarSubtitle();
    }

    public void setSubmissionSort(TimeSpan time) {
        this.submissionSort = tempSort;
        this.timeSpan = time;
        setActionBarSubtitle();
    }

    public void setActionBarTitle() {
        String title = (subreddit == null) ? "frontpage" : subreddit;
        activity.getSupportActionBar().setTitle(title);
    }

    public void setActionBarSubtitle() {
        String subtitle;
        if(MainActivity.offlineModeEnabled) {
            subtitle = "offline";
        }
        else {
            if (timeSpan == null) subtitle = submissionSort.value();
            else subtitle = submissionSort.value() + ": " + timeSpan.value();
        }
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

}
