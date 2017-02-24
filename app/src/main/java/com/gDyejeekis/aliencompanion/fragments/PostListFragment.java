package com.gDyejeekis.aliencompanion.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.PleaseWaitDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SearchRedditDialogFragment;
import com.gDyejeekis.aliencompanion.asynctask.LoadPostsTask;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ShowSyncedDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SubredditSidebarDialogFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostListFragment extends RedditContentFragment {

    public static final String TAG = "PostListFragment";

    public String subreddit;
    public boolean isMulti = false;
    public boolean isOther = false;
    public SubmissionSort submissionSort;
    private SubmissionSort tempSort;
    public TimeSpan timeSpan;
    public LoadPostsTask task;

    public static PostListFragment newInstance(RedditItemListAdapter adapter, String subreddit, boolean isMulti, SubmissionSort sort, TimeSpan time, LoadType currentLoadType, boolean hasMore) {
        PostListFragment listFragment = new PostListFragment();
        listFragment.adapter = adapter;
        listFragment.subreddit = subreddit;
        listFragment.isMulti = isMulti;
        listFragment.submissionSort = sort;
        listFragment.timeSpan = time;
        listFragment.currentLoadType = currentLoadType;
        listFragment.hasMore = hasMore;
        return listFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null) {
            subreddit = savedInstanceState.getString("subreddit");
            isMulti = savedInstanceState.getBoolean("isMulti");
            isOther = savedInstanceState.getBoolean("isOther");
            submissionSort = (SubmissionSort) savedInstanceState.getSerializable("sort");
            timeSpan = (TimeSpan) savedInstanceState.getSerializable("time");
        }

        if(subreddit==null) subreddit = activity.getIntent().getStringExtra("subreddit");
        if(!isMulti) isMulti = activity.getIntent().getBooleanExtra("isMulti", false);
        if(!isOther) isOther = activity.getIntent().getBooleanExtra("isOther", false);
        if(submissionSort==null) submissionSort = (SubmissionSort) activity.getIntent().getSerializableExtra("sort");
        if(timeSpan==null) timeSpan = (TimeSpan) activity.getIntent().getSerializableExtra("time");
    }

    @Override
    protected void showViewsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_post_views);
        //final int currentViewTypeValue = MyApplication.rememberPostListView ? MyApplication.prefs.getInt(MyApplication.getSubredditSpecificViewKey(subreddit, isMulti), MyApplication.currentPostListView)
        //        : MyApplication.currentPostListView;
        final int currentViewTypeValue = adapter.viewTypeValue;
        popupMenu.getMenu().getItem(currentViewTypeValue).setChecked(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                PostViewType selectedViewType = PostViewType.list;
                switch (item.getItemId()) {
                    case R.id.action_list_default:
                        selectedViewType = PostViewType.list;
                        break;
                    case R.id.action_list_reversed:
                        selectedViewType = PostViewType.listReversed;
                        break;
                    case R.id.action_classic:
                        selectedViewType = PostViewType.classic;
                        break;
                    case R.id.action_small_cards:
                        selectedViewType = PostViewType.smallCards;
                        break;
                    case R.id.action_cards:
                        selectedViewType = PostViewType.cards;
                        break;
                    case R.id.action_image_board:
                        selectedViewType = PostViewType.gallery;
                        break;
                }
                // TODO: 2/10/2017 this conditional flow might need changing OR reset all subreddit specific views on option disable
                if (selectedViewType.value() != currentViewTypeValue) {
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    if(MyApplication.rememberPostListView) {
                        editor.putInt(MyApplication.getSubredditSpecificViewKey(subreddit, isMulti), selectedViewType.value());
                    }
                    else {
                        MyApplication.currentPostListView = selectedViewType.value();
                        editor.putString("defaultView", String.valueOf(selectedViewType.value()));
                    }
                    editor.apply();
                    if(currentLoadType==null) redrawList(selectedViewType.value());
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();

        // ask whether to remember subreddit specific views
        if(!MyApplication.askedRememberPostView) {
            MyApplication.askedRememberPostView = true;
            final SharedPreferences.Editor editor = MyApplication.prefs.edit();
            editor.putBoolean("askedRememberView", true);
            editor.apply();

            final String text = "Remember the last used view option for each subreddit/multireddit?";
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MyApplication.rememberPostListView = true;
                    editor.putBoolean("rememberView", true);
                    editor.apply();
                }
            };
            new AlertDialog.Builder(activity).setMessage(text).setPositiveButton("Remember", listener)
                    .setNegativeButton("Cancel", null).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("subreddit", subreddit);
        outState.putBoolean("isMulti", isMulti);
        outState.putBoolean("isOther", isOther);
        outState.putSerializable("sort", submissionSort);
        outState.putSerializable("time", timeSpan);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        mainProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        contentView = (RecyclerView) view.findViewById(R.id.recyclerView_postList);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(MyApplication.currentColor);

        updateContentViewProperties(getCurrentViewTypeValue());
        setFabNavOptions(this, view);

        contentView.addOnScrollListener(onScrollListener);

        if(currentLoadType == null) {
            //Log.d("geo test", "currentLoadType is null");
            if (adapter == null) {
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
                contentView.setAdapter(adapter);
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
                contentView.setAdapter(adapter);
                swipeRefreshLayout.post(new Runnable() {
                    @Override public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                break;
            case extend:
                //Log.d("geo test", "currentLoadType is extend");
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
                adapter.setLoadingMoreItems(true);
                break;
        }

        return view;
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
                showSearchDialog();
                return true;
            case R.id.action_hide_read:
                removeClickedPosts(getCurrentViewTypeValue());
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
            //case R.id.action_hide_read:
            //    postListAdapter.hideReadPosts();
            //    return true;
            case R.id.action_view_sidebar:
                if(subreddit == null || subreddit.equalsIgnoreCase("all")) {
                    String string = (subreddit==null) ? "the front page" : "/r/all";
                    ToastUtils.displayShortToast(activity, "No sidebar for " + string);
                }
                else {
                    SubredditSidebarDialogFragment dialog = new SubredditSidebarDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("subreddit", subreddit);
                    dialog.setArguments(bundle);
                    dialog.show(activity.getFragmentManager(), "dialog");
                }
                return true;
            case R.id.action_view_synced:
                ShowSyncedDialogFragment syncedDialog = new ShowSyncedDialogFragment();
                syncedDialog.show(activity.getSupportFragmentManager(), "dialog");
                break;
            case R.id.action_pending_actions:
                Intent intent = new Intent(activity, PendingUserActionsActivity.class);
                activity.startActivity(intent);
                break;
            case R.id.action_clear_synced:
                final String messageEnd;
                if(subreddit==null) {
                    messageEnd = "the frontpage";
                }
                else {
                    messageEnd = "'" + subreddit + "'";
                }
                final String message = "Delete all synced posts, comments, images and articles for " + messageEnd + "?";
                new AlertDialog.Builder(activity).setMessage(message).setNegativeButton("No", null).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final PleaseWaitDialogFragment dialogFragment = new PleaseWaitDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("message", "Clearing synced data for " + messageEnd);
                        dialogFragment.setArguments(args);
                        dialogFragment.show(activity.getSupportFragmentManager(), "dialog");

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                String folderName = (subreddit==null) ? "frontpage" : subreddit;
                                if(isMulti) {
                                    folderName = "multi=" + folderName;
                                }
                                GeneralUtils.clearSyncedPostsAndComments(activity, folderName);
                                GeneralUtils.clearSyncedImages(activity, folderName);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                dialogFragment.dismiss();
                                String toastMessage = "Synced data for " + messageEnd + " cleared";
                                ToastUtils.displayShortToast(activity, toastMessage);
                            }
                        }.execute();
                    }
                }).show();
                return true;
            case R.id.action_sync_profiles:
                intent = new Intent(activity, SyncProfilesActivity.class);
                activity.startActivity(intent);
                return true;
            case R.id.action_sync_posts:
                addToSyncQueue();
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

    public void showSearchDialog() {
        SearchRedditDialogFragment searchDialog = new SearchRedditDialogFragment();
        Bundle args = new Bundle();
        args.putString("subreddit", subreddit);
        searchDialog.setArguments(args);
        searchDialog.show(activity.getSupportFragmentManager(), "dialog");
    }

    public void addToSyncQueue() {
        String toastMessage;
        if(GeneralUtils.isNetworkAvailable(activity)) {
            if(MyApplication.syncOverWifiOnly && !GeneralUtils.isConnectedOverWifi(activity)) {
                toastMessage = "Syncing over mobile data connection is disabled";
            }
            else {
                String filename = (subreddit == null) ? "frontpage" : subreddit;
                toastMessage = filename + " added to sync queue";
                Intent intent = new Intent(activity, DownloaderService.class);
                intent.putExtra("sort", submissionSort);
                intent.putExtra("time", timeSpan);
                intent.putExtra("subreddit", subreddit);
                intent.putExtra("isMulti", isMulti);
                activity.startService(intent);
            }
        }
        else {
            toastMessage = "Network connection unavailable";
        }
        ToastUtils.displayShortToast(activity, toastMessage);
    }

    public int getCurrentViewTypeValue() {
        return MyApplication.rememberPostListView ? MyApplication.prefs.getInt(MyApplication.getSubredditSpecificViewKey(subreddit, isMulti), MyApplication.currentPostListView)
                : MyApplication.currentPostListView;
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
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        if(submissionSort == null) submissionSort = SubmissionSort.HOT;
        setActionBarSubtitle();
    }

    @Override
    public void extendList() {
        currentLoadType = LoadType.extend;
        adapter.setLoadingMoreItems(true);
        task = new LoadPostsTask(activity, this, LoadType.extend);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean hasFabNavigation() {
        return true;
    }

    @Override
    public void refreshList() {
        refreshList(submissionSort, timeSpan);
    }

    public void refreshList(SubmissionSort sort, TimeSpan time) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadPostsTask(activity, this, LoadType.refresh, sort, time);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if(fabOptionsVisible) {
            setFabNavOptionsVisible(false);
        }
    }

    public void changeSubreddit(String subreddit, boolean isMulti, boolean isOther) {
        this.isMulti = isMulti;
        this.isOther = isOther;
        if(isMulti) {
            subreddit = subreddit.substring(subreddit.indexOf('=') + 1);
        }
        changeSubreddit(subreddit);
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
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if(fabOptionsVisible) {
            setFabNavOptionsVisible(false);
        }
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
        if(isMulti) title  = title.concat(" multi");
        activity.getSupportActionBar().setTitle(title);
    }

    public void setActionBarSubtitle() {
        String subtitle;
        if(MyApplication.offlineModeEnabled) {
            subtitle = getOfflineSubtitle();
        }
        else {
            if (timeSpan == null) subtitle = submissionSort.value();
            else subtitle = submissionSort.value() + ": " + timeSpan.value();
        }
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private String getOfflineSubtitle() {
        try {
            String filename = "";
            if(isMulti) filename = MyApplication.MULTIREDDIT_FILE_PREFIX;
            filename += (subreddit == null) ? "frontpage" : subreddit;
            filename = filename.concat(DownloaderService.LOCA_POST_LIST_SUFFIX);
            File file = new File(GeneralUtils.getActiveDir(activity), filename);
            //double lastModified = (double) file.lastModified();
            //return ConvertUtils.getSubmissionAge(lastModified);
            if(file.exists()) {
                //return new Date(file.lastModified()).toString();
                return ((isOther) ? "updated " : "synced ") + ConvertUtils.getSubmissionAge((double) file.lastModified() / 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((isOther) ? "no posts" : "not synced");
    }

}
