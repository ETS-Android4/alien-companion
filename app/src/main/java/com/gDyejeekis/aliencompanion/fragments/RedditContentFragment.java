package com.gDyejeekis.aliencompanion.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.utils.GridAutoFitLayoutManager;
import com.gDyejeekis.aliencompanion.views.DividerItemDecoration;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.ShowMoreListener;
import com.gDyejeekis.aliencompanion.views.viewholders.PostGalleryViewHolder;

import java.util.List;

/**
 * Created by George on 2/9/2017.
 */

public abstract class RedditContentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public abstract void refreshList();

    public abstract void extendList();

    public static final String TAG = "RedditContentFragment";

    protected AppCompatActivity activity;
    public RecyclerView contentView;
    protected RecyclerView.LayoutManager layoutManager;
    public RedditItemListAdapter adapter;
    public LoadType currentLoadType;
    public boolean loadMore;
    public boolean hasMore;
    public ProgressBar mainProgressBar;
    public SwipeRefreshLayout swipeRefreshLayout;

    protected final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateSwipeRefreshState();
            updateLoadMoreState(recyclerView);
        }
    };

    private DividerItemDecoration decoration;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        decoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);

        loadMore = MyApplication.endlessPosts;
        //hasMore = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSwipeRefreshState();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    private void updateSwipeRefreshState() {
        swipeRefreshLayout.setEnabled(MyApplication.swipeRefresh && findFirstCompletelyVisiblePostPosition() == 0);
    }

    private void updateLoadMoreState(RecyclerView recyclerView) {
        int pastVisiblesItems, visibleItemCount, totalItemCount;
        visibleItemCount = layoutManager.getChildCount();
        totalItemCount = layoutManager.getItemCount();
        pastVisiblesItems = findFirstVisiblePostPosition();

        if (!MyApplication.offlineModeEnabled && loadMore && hasMore) {
            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount - 6) { // TODO: 2/7/2017  maybe change this constant for gallery view
                loadMore = false;
                //Log.d("scroll listener", "load more now");
                ShowMoreListener listener = new ShowMoreListener(activity.getFragmentManager().findFragmentByTag("listFragment"));
                listener.onClick(recyclerView);
            }
        }
    }

    private int findFirstCompletelyVisiblePostPosition() {
        if(layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        }
        else if(layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
        }
        return -1;
    }

    private int findFirstVisiblePostPosition() {
        if(layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        else if(layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        return -1;
    }

    protected void showViewsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_post_views);
        popupMenu.getMenu().getItem(MyApplication.currentPostListView).setChecked(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                PostViewType viewType = PostViewType.list;
                switch (item.getItemId()) {
                    case R.id.action_list_default:
                        viewType = PostViewType.list;
                        break;
                    case R.id.action_list_reversed:
                        viewType = PostViewType.listReversed;
                        break;
                    case R.id.action_classic:
                        viewType = PostViewType.classic;
                        break;
                    case R.id.action_small_cards:
                        viewType = PostViewType.smallCards;
                        break;
                    case R.id.action_cards:
                        viewType = PostViewType.cards;
                        break;
                    case R.id.action_image_board:
                        viewType = PostViewType.gallery;
                        break;
                }
                if (viewType.value() != MyApplication.currentPostListView) {
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    MyApplication.currentPostListView = viewType.value();
                    editor.putInt("postListView", viewType.value());
                    editor.apply();
                    if(currentLoadType==null) redrawList();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    /**
     * override this in UserFragment
     */
    public void redrawList() {
        try {
            List<RedditItem> items = adapter.redditItems;
            items.remove(items.size() - 1); // TODO: 2/9/2017 ??
            adapter = new RedditItemListAdapter(activity, items);
            setLayoutManager();
            contentView.setAdapter(adapter);
            setListDividerVisible(false);
            setListDividerVisible(PostViewType.hasVisibleListDivider(MyApplication.currentPostListView));
        } catch (ArrayIndexOutOfBoundsException e) {}
    }

    protected void setListDividerVisible(boolean flag) {
        if(flag) {
            contentView.addItemDecoration(decoration);
        }
        else {
            contentView.removeItemDecoration(decoration);
        }
    }

    /**
     * override this in UserFragment
     */
    protected void setLayoutManager() {
        layoutManager = MyApplication.currentPostListView == PostViewType.gallery.value() ?
                new GridAutoFitLayoutManager(activity, PostGalleryViewHolder.GALLERY_COLUMN_WIDTH) : new LinearLayoutManager(activity);
        contentView.setLayoutManager(layoutManager);
    }

    public void colorSchemeChanged() {
        swipeRefreshLayout.setColorSchemeColors(MyApplication.colorPrimary);
        if(adapter!=null) adapter.notifyDataSetChanged();
    }

    public DialogFragment getCurrentDialogFragment() {
        try {
            return (DialogFragment) activity.getFragmentManager().findFragmentByTag("dialog");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onRefresh() {
        refreshList();
    }
}
