package com.gDyejeekis.aliencompanion.fragments;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.asynctask.LoadMessagesTask;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategory;
import com.gDyejeekis.aliencompanion.api.retrieval.params.MessageCategorySort;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends RedditContentFragment {

    public static final String TAG = "MessageFragment";

    public MessageCategory category, tempCategory;
    public MessageCategorySort sort;
    public LoadMessagesTask task;

    public static MessageFragment newInstance(RedditItemListAdapter adapter, MessageCategory category, MessageCategorySort sort, boolean hasMore) {
        MessageFragment newInstance = new MessageFragment();
        newInstance.adapter = adapter;
        newInstance.category = category;
        newInstance.sort = sort;
        newInstance.hasMore = hasMore;
        return newInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        mainProgressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        contentView = (RecyclerView) view.findViewById(R.id.recyclerView_postList);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(MyApplication.currentColor);

        setLayoutManager();
        contentView.setHasFixedSize(true);
        setListDividerVisible(true);

        contentView.addOnScrollListener(onScrollListener);

        if(currentLoadType == null) {
            if (adapter == null) {
                //currentlyLoading = true;
                currentLoadType = LoadType.init;
                category = MessageCategory.INBOX;
                if(activity.getIntent().getBooleanExtra("viewNew", false)) {
                    sort = MessageCategorySort.UNREAD;
                }
                else {
                    sort = MessageCategorySort.ALL;
                }
                task = new LoadMessagesTask(activity, this, LoadType.init);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                setActionBarSubtitle();
                mainProgressBar.setVisibility(View.GONE);
                contentView.setAdapter(adapter);
            }
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_compose:
                //start compose message activity
                //ToastUtils.displayShortToast(activity, "Coming soon!");
                Intent intent = new Intent(activity, SubmitActivity.class);
                intent.putExtra("submitType", SubmitType.message);
                activity.startActivity(intent);
                return true;
            case R.id.action_sort:
                showCategoryPopup(activity.findViewById(R.id.action_sort));
                return true;
            case R.id.action_refresh:
                refreshList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setActionBarTitle() {
        activity.getSupportActionBar().setTitle("Messages");
    }

    public void setActionBarSubtitle() {
        String subtitle = category.value();
        if(sort!=null)  subtitle += ": " + sort.value();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        if(category == null || sort == null) {
            category = MessageCategory.INBOX;
            sort = MessageCategorySort.ALL;
        }
        setActionBarSubtitle();
    }

    @Override
    public void refreshList() {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadMessagesTask(activity, this, LoadType.refresh);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void extendList() {
        currentLoadType = LoadType.extend;
        adapter.setLoadingMoreItems(true);
        task = new LoadMessagesTask(activity, this, LoadType.extend);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshList(MessageCategory category, MessageCategorySort sort) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadMessagesTask(activity, this, LoadType.refresh, category, sort);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void redrawList() {
        try {
            List<RedditItem> items = adapter.redditItems;
            items.remove(items.size() - 1); // remove show more item
            adapter = new RedditItemListAdapter(activity, items);
            contentView.setAdapter(adapter);
        } catch (ArrayIndexOutOfBoundsException e) {}
    }

    @Override
    public void setLayoutManager() {
        layoutManager = new LinearLayoutManager(activity);
        contentView.setLayoutManager(layoutManager);
    }

    public void showCategoryPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_message_category);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_message_inbox:
                        showSortPopup(activity.findViewById(R.id.action_sort));
                        tempCategory = MessageCategory.INBOX;
                        return true;
                    case R.id.action__message_sent:
                        refreshList(MessageCategory.SENT, null);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showSortPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_message_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_message_all:
                        //sort = MessageCategorySort.ALL;
                        //setActionBarSubtitle();
                        refreshList(tempCategory, MessageCategorySort.ALL);
                        return true;
                    case R.id.action_message_unread:
                        //sort = MessageCategorySort.UNREAD;
                        //setActionBarSubtitle();
                        refreshList(tempCategory, MessageCategorySort.UNREAD);
                        return true;
                    //case R.id.action_message_messages:
                    //    sort = MessageCategorySort.MESSAGES;
                    //    setActionBarSubtitle();
                    //    refreshList();
                    //    return true;
                    case R.id.action_message_comment_replies:
                        //sort = MessageCategorySort.COMMENT_REPLIES;
                        //setActionBarSubtitle();
                        refreshList(tempCategory, MessageCategorySort.COMMENT_REPLIES);
                        return true;
                    case R.id.action_message_post_replies:
                        //sort = MessageCategorySort.POST_REPLIES;
                        //setActionBarSubtitle();
                        refreshList(tempCategory, MessageCategorySort.POST_REPLIES);
                        return true;
                    case R.id.action_message_mentions:
                        //sort = MessageCategorySort.USERNAME_MENTIONS;
                        //setActionBarSubtitle();
                        refreshList(tempCategory, MessageCategorySort.USERNAME_MENTIONS);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

}
