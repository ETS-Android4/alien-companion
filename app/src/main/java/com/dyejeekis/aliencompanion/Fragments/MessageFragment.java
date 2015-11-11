package com.dyejeekis.aliencompanion.Fragments;


import android.app.Activity;
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
import android.widget.TextView;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.dyejeekis.aliencompanion.ClickListeners.ShowMoreListener;
import com.dyejeekis.aliencompanion.LoadTasks.LoadMessagesTask;
import com.dyejeekis.aliencompanion.LoadTasks.LoadPostsTask;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Views.DividerItemDecoration;
import com.dyejeekis.aliencompanion.api.retrieval.params.MessageCategory;
import com.dyejeekis.aliencompanion.api.retrieval.params.MessageCategorySort;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.dyejeekis.aliencompanion.enums.LoadType;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public ProgressBar mainProgressBar;
    public RecyclerView contentView;
    public RedditItemListAdapter adapter;
    private LinearLayoutManager layoutManager;
    public SwipeRefreshLayout swipeRefreshLayout;
    public AppCompatActivity activity;
    public MessageCategory category, tempCategory;
    public MessageCategorySort sort;
    public boolean loadMore;
    public boolean hasMore;
    public LoadType currentLoadType;
    public LoadMessagesTask task;

    //public static boolean currentlyLoading = false;

    public static MessageFragment newInstance(RedditItemListAdapter adapter, MessageCategory category, MessageCategorySort sort, boolean hasMore) {
        MessageFragment newInstance = new MessageFragment();
        newInstance.adapter = adapter;
        newInstance.category = category;
        newInstance.sort = sort;
        newInstance.hasMore = hasMore;
        return newInstance;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        loadMore = MainActivity.endlessPosts;
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadMore = MainActivity.endlessPosts;
        if(MainActivity.swipeRefresh && layoutManager.findFirstCompletelyVisibleItemPosition()==0) swipeRefreshLayout.setEnabled(true);
        else swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            if (adapter == null) {
                //currentlyLoading = true;
                currentLoadType = LoadType.init;
                category = MessageCategory.INBOX;
                sort = MessageCategorySort.ALL;
                task = new LoadMessagesTask(activity, this, LoadType.init);
                task.execute();
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

    @Override public void onRefresh() {
        refreshList();
    }

    public void refreshList() {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadMessagesTask(activity, this, LoadType.refresh);
        task.execute();
    }

    public void refreshList(MessageCategory category, MessageCategorySort sort) {
        if(currentLoadType!=null) task.cancel(true);
        currentLoadType = LoadType.refresh;
        swipeRefreshLayout.setRefreshing(true);
        task = new LoadMessagesTask(activity, this, LoadType.refresh, category, sort);
        task.execute();
    }

    public void redrawList() {
        List<RedditItem> items = adapter.redditItems;
        items.remove(items.size()-1);
        adapter = new RedditItemListAdapter(activity, items);
        contentView.setAdapter(adapter);
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


}
