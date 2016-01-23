package com.gDyejeekis.aliencompanion.Fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.ClickListeners.ShowMoreListener;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.SearchRedditDialogFragment;
import com.gDyejeekis.aliencompanion.AsyncTasks.LoadPostsTask;
import com.gDyejeekis.aliencompanion.Models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Services.DownloaderService;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.Views.DividerItemDecoration;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

import java.util.List;


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
    public boolean loadMore;
    public boolean hasMore;
    public LoadType currentLoadType;
    public LoadPostsTask task;

    public static PostListFragment newInstance(RedditItemListAdapter adapter, String subreddit, SubmissionSort sort, TimeSpan time, LoadType currentLoadType, boolean hasMore) {
        PostListFragment listFragment = new PostListFragment();
        listFragment.postListAdapter = adapter;
        listFragment.subreddit = subreddit;
        listFragment.submissionSort = sort;
        listFragment.timeSpan = time;
        listFragment.currentLoadType = currentLoadType;
        listFragment.hasMore = hasMore;
        return listFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        if(savedInstanceState!=null) {
            subreddit = savedInstanceState.getString("subreddit");
            submissionSort = (SubmissionSort) savedInstanceState.getSerializable("sort");
            timeSpan = (TimeSpan) savedInstanceState.getSerializable("time");
        }

        loadMore = MyApplication.endlessPosts;
        if(subreddit==null) subreddit = activity.getIntent().getStringExtra("subreddit");
        if(submissionSort==null) submissionSort = (SubmissionSort) activity.getIntent().getSerializableExtra("sort");
        if(timeSpan==null) timeSpan = (TimeSpan) activity.getIntent().getSerializableExtra("time");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("subreddit", subreddit);
        outState.putSerializable("sort", submissionSort);
        outState.putSerializable("time", timeSpan);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadMore = MainActivity.endlessPosts;
        if(MyApplication.swipeRefresh && layoutManager.findFirstCompletelyVisibleItemPosition()==0) swipeRefreshLayout.setEnabled(true);
        else swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        mainProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        contentView = (RecyclerView) view.findViewById(R.id.recyclerView_postList);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(MyApplication.currentColor);

        layoutManager = new LinearLayoutManager(activity);
        contentView.setLayoutManager(layoutManager);
        contentView.setHasFixedSize(true);
        contentView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        contentView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (MyApplication.swipeRefresh && layoutManager.findFirstCompletelyVisibleItemPosition() == 0)
                    swipeRefreshLayout.setEnabled(true);
                else swipeRefreshLayout.setEnabled(false);

                int pastVisiblesItems, visibleItemCount, totalItemCount;
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                if (!MyApplication.offlineModeEnabled && loadMore && hasMore) {
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
            //Log.d("geo test", "currentLoadType is null");
            if (postListAdapter == null) {
                //Log.d("geo test", "postListAdapter is null");
                currentLoadType = LoadType.init;
                //setSubmissionSort(SubmissionSort.HOT);
                if(submissionSort==null) submissionSort = SubmissionSort.HOT;
                setActionBarSubtitle();
                task = new LoadPostsTask(activity, this, LoadType.init);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                setActionBarSubtitle();
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(postListAdapter);
            }
        }
        else switch (currentLoadType) {
            case init:
                //Log.d("geo test", "currentLoadType is init");
                contentView.setVisibility(View.GONE);
                mainProgressBar.setVisibility(View.VISIBLE);
                break;
            case refresh:
                //Log.d("geo test", "currentLoadType is refersh");
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(postListAdapter);
                swipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                break;
            case extend:
                //Log.d("geo test", "currentLoadType is extend");
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(postListAdapter);
                postListAdapter.setLoadingMoreItems(true);
                break;
        }

        return view;
    }

    public void colorSchemeChanged() {
        swipeRefreshLayout.setColorSchemeColors(MyApplication.colorPrimary);
        if(postListAdapter!=null) postListAdapter.notifyDataSetChanged();
    }

    @Override public void onRefresh() {
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
                try {
                    showViewsPopup(activity.findViewById(R.id.action_sort));
                } catch (Exception e) {
                    showViewsPopup(activity.findViewById(R.id.action_refresh));
                } //TODO: find a more suitable anchor
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
                try {
                    showSubmitPopup(activity.findViewById(R.id.action_sort));
                } catch (Exception e) {
                    showSubmitPopup(activity.findViewById(R.id.action_refresh));
                } //TODO: find a more suitable anchor
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showViewsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_post_views);
        int index;
        switch (MyApplication.currentPostListView) {
            case R.layout.post_list_item_reversed:
                index = 1;
                break;
            case R.layout.post_list_item_small_card:
                index = 2;
                break;
            case R.layout.post_list_item_card:
                index = 3;
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
                    case R.id.action_small_cards:
                        resource = R.layout.post_list_item_small_card;
                        break;
                    case R.id.action_cards:
                        resource = R.layout.post_list_item_card;
                        break;
                }
                if (resource != -1 && resource != MyApplication.currentPostListView) {
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    MyApplication.currentPostListView = resource;
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
                        refreshList(SubmissionSort.HOT, null);
                        return true;
                    case R.id.action_sort_new:
                        refreshList(SubmissionSort.NEW, null);
                        return true;
                    case R.id.action_sort_rising:
                        refreshList(SubmissionSort.RISING, null);
                        return true;
                    case R.id.action_sort_top:
                        tempSort = SubmissionSort.TOP;
                        showSortTimePopup(activity.findViewById(R.id.action_sort));
                        return true;
                    case R.id.action_sort_controversial:
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
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadPostsTask(activity, this, LoadType.refresh);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshList(SubmissionSort sort, TimeSpan time) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadPostsTask(activity, this, LoadType.refresh, sort, time);
        task.execute();
    }

    public void changeSubreddit(String subreddit) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.init;
        this.subreddit = subreddit;
        this.submissionSort = SubmissionSort.HOT;
        this.timeSpan = null;
        setActionBarTitle();
        setActionBarSubtitle();
        contentView.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.VISIBLE);
        task = new LoadPostsTask(activity, this, LoadType.init);
        task.execute();
    }

    public void redrawList() {
        try {
            List<RedditItem> items = postListAdapter.redditItems;
            items.remove(items.size() - 1);
            postListAdapter = new RedditItemListAdapter(activity, items);
            contentView.setAdapter(postListAdapter);
        } catch (ArrayIndexOutOfBoundsException e) {}
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
        if(MyApplication.offlineModeEnabled) {
            subtitle = "offline";
        }
        else {
            if (timeSpan == null) subtitle = submissionSort.value();
            else subtitle = submissionSort.value() + ": " + timeSpan.value();
        }
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

}
