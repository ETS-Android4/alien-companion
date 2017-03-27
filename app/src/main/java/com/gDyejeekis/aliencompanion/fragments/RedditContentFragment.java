package com.gDyejeekis.aliencompanion.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.ToolbarActivity;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.utils.GridAutoFitLayoutManager;
import com.gDyejeekis.aliencompanion.utils.MoveUpwardLinearLayout;
import com.gDyejeekis.aliencompanion.views.DividerItemDecoration;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.ShowMoreListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.fab_menu_listeners.PostFabNavListener;
import com.gDyejeekis.aliencompanion.views.viewholders.PostGalleryViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 2/9/2017.
 */

public abstract class RedditContentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public abstract void refreshList();

    public abstract void extendList();

    public abstract boolean hasFabNavigation();

    public static final String TAG = "RedditContentFragment";

    protected ToolbarActivity activity;
    public RecyclerView contentView;
    protected RecyclerView.LayoutManager layoutManager;
    public RedditItemListAdapter adapter;
    public LoadType currentLoadType;
    public int currentViewTypeValue = MyApplication.currentPostListView;
    public Snackbar currentSnackbar;
    public boolean loadMore;
    public boolean hasMore;
    public ProgressBar mainProgressBar;
    public SwipeRefreshLayout swipeRefreshLayout;
    public FloatingActionButton fabMain;

    protected final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateSwipeRefreshState();
            updateToolbarOnScroll(dy);
            updateFabOnScroll(dy);
            updateLoadMoreOnScroll(recyclerView);
        }
    };

    private DividerItemDecoration dividerDecoration;

    public View getSnackbarParentView() {
        return layoutFabNav;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ToolbarActivity) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        dividerDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        initFabAnimations();

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

    public void setSnackbar(Snackbar snackbar) {
        this.currentSnackbar = snackbar;
    }

    public void dismissSnackbar() {
        if(currentSnackbar!=null) {
            currentSnackbar.dismiss();
            currentSnackbar = null;
        }
    }

    private void updateToolbarOnScroll(int dy) {
        if(MyApplication.autoHideToolbar) {
            if(dy > MyApplication.TOOLBAR_HIDE_ON_SCROLL_THRESHOLD) {
                activity.hideToolbar();
            }
            else if(dy < -MyApplication.TOOLBAR_HIDE_ON_SCROLL_THRESHOLD
                    || findFirstCompletelyVisiblePostPosition() == 0) {
                activity.showToolbar();
            }
        }
    }

    private void updateFabOnScroll(int dy) {
        if(MyApplication.postNavigation && hasFabNavigation()) {
            if (MyApplication.autoHidePostFab) {
                if (dy > MyApplication.FAB_HIDE_ON_SCROLL_THRESHOLD) {
                    hideAllFabOptions();
                    fabMain.hide();
                } else if (dy < -MyApplication.FAB_HIDE_ON_SCROLL_THRESHOLD
                        || findFirstCompletelyVisiblePostPosition() == 0) {
                    fabMain.show();
                }
            }
        }
    }

    private void updateLoadMoreOnScroll(RecyclerView recyclerView) {
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
                    if(RedditContentFragment.this instanceof PostListFragment && MyApplication.rememberPostListView) {
                        String viewPrefKey = MyApplication.getSubredditSpecificViewKey(((PostListFragment)RedditContentFragment.this).subreddit,
                                ((PostListFragment)RedditContentFragment.this).isMulti);
                        editor.putInt(viewPrefKey, selectedViewType.value());
                    }
                    else {
                        MyApplication.currentPostListView = selectedViewType.value();
                        editor.putString("defaultView", String.valueOf(selectedViewType.value()));
                    }
                    editor.apply();
                    updateCurrentViewType();
                    updateFabLayoutGravity();
                    if(currentLoadType==null) {
                        redrawList();
                    }
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public void removeClickedPosts() {
        try {
            hideAllFabOptions();
            List<RedditItem> notClicked = new ArrayList<>();
            for(RedditItem item : adapter.redditItems) {
                if(item instanceof Submission && !((Submission) item).isClicked()) {
                    notClicked.add(item);
                }
            }
            adapter = new RedditItemListAdapter(activity, currentViewTypeValue, notClicked);
            updateContentView(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void redrawList() {
        try {
            if(fabOptionsVisible) {
                setFabMainOptionsVisible(false);
            }
            List<RedditItem> items = adapter.redditItems;
            items.remove(items.size() - 1); // remove show more item
            adapter = new RedditItemListAdapter(activity, currentViewTypeValue, items);
            updateContentView(adapter);
        } catch (Exception e) {}
    }

    protected void updateContentViewProperties() {
        contentView.setHasFixedSize(true);
        setLayoutManager();
        updateListDividerVisibility();
        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    getView().requestFocus();
                } catch (Exception e) {}
                return false;
            }
        });
    }

    public void updateContentViewAdapter(RedditItemListAdapter adapter) {
        contentView.setAdapter(adapter);
    }

    public void updateContentView(RedditItemListAdapter adapter) {
        updateContentViewAdapter(adapter);
        updateContentViewProperties();
    }

    private void setLayoutManager() {
        layoutManager = currentViewTypeValue == PostViewType.gallery.value() ?
                new GridAutoFitLayoutManager(activity, PostGalleryViewHolder.GALLERY_COLUMN_WIDTH) : new LinearLayoutManager(activity);
        contentView.setLayoutManager(layoutManager);
    }

    private void updateListDividerVisibility() {
        contentView.removeItemDecoration(dividerDecoration);
        if(PostViewType.hasVisibleListDivider(currentViewTypeValue)) {
            contentView.addItemDecoration(dividerDecoration);
        }
    }

    public void updateCurrentViewType() {
        if(this instanceof PostListFragment && MyApplication.rememberPostListView) {
            String viewPrefKey = MyApplication.getSubredditSpecificViewKey(((PostListFragment)this).subreddit, ((PostListFragment)this).isMulti);
            currentViewTypeValue = MyApplication.prefs.getInt(viewPrefKey, MyApplication.currentPostListView);
        }
        else if(this instanceof UserFragment && MyApplication.currentPostListView == PostViewType.gallery.value()) {
            currentViewTypeValue = PostViewType.smallCards.value();
        }
        else {
            currentViewTypeValue = MyApplication.currentPostListView;
        }
    }

    private MoveUpwardLinearLayout layoutFabNav;
    private LinearLayout layoutFabNavOptions;
    private boolean fabOptionsVisible;
    private boolean fabSubmitOptionsVisible;

    private FloatingActionButton fabRefresh;
    private FloatingActionButton fabSubmit;
    private FloatingActionButton fabSync;
    private FloatingActionButton fabHideRead;
    private FloatingActionButton fabSearch;
    private FloatingActionButton fabSubmitLink;
    private FloatingActionButton fabSubmitText;

    private Animation showAnimation;
    private Animation hideAnimation;

    private void initFabAnimations() {
        showAnimation = AnimationUtils.loadAnimation(activity, R.anim.fab_options_show);
        showAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                layoutFabNavOptions.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        hideAnimation = AnimationUtils.loadAnimation(activity, R.anim.fab_options_hide);
        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutFabNavOptions.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    protected void initFabNavOptions(View view) {
        layoutFabNav = (MoveUpwardLinearLayout) view.findViewById(R.id.layout_fab_nav);
        if(MyApplication.postNavigation && hasFabNavigation()) {
            layoutFabNav.setVisibility(View.VISIBLE);
            updateFabLayoutGravity();
            layoutFabNavOptions = (LinearLayout) view.findViewById(R.id.layout_fab_nav_options);
            layoutFabNavOptions.setVisibility(View.GONE);

            fabMain = (FloatingActionButton) view.findViewById(R.id.fab_nav);
            fabRefresh = (FloatingActionButton) view.findViewById(R.id.fab_refresh);
            fabSubmit = (FloatingActionButton) view.findViewById(R.id.fab_submit);
            fabSync = (FloatingActionButton) view.findViewById(R.id.fab_sync);
            fabHideRead = (FloatingActionButton) view.findViewById(R.id.fab_hide_read);
            fabSearch = (FloatingActionButton) view.findViewById(R.id.fab_search);
            fabSubmitLink = (FloatingActionButton) view.findViewById(R.id.fab_submit_link);
            fabSubmitText = (FloatingActionButton) view.findViewById(R.id.fab_submit_text);
            PostFabNavListener listener = new PostFabNavListener(this);
            fabMain.setOnClickListener(listener);
            fabRefresh.setOnClickListener(listener);
            fabSubmit.setOnClickListener(listener);
            fabSync.setOnClickListener(listener);
            fabHideRead.setOnClickListener(listener);
            fabSearch.setOnClickListener(listener);
            fabSubmitLink.setOnClickListener(listener);
            fabSubmitText.setOnClickListener(listener);

            fabRefresh.setOnLongClickListener(listener);
            fabSubmit.setOnLongClickListener(listener);
            fabSync.setOnLongClickListener(listener);
            fabHideRead.setOnLongClickListener(listener);
            fabSearch.setOnLongClickListener(listener);
            fabSubmitLink.setOnLongClickListener(listener);
            fabSubmitText.setOnLongClickListener(listener);

            updateFabNavColors();

            fabMain.show();
            setFabIndividualVisibility(false);
        }
        else {
            layoutFabNav.setVisibility(View.GONE);
        }
    }

    public void updateFabLayoutGravity() {
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
        if(currentViewTypeValue == PostViewType.listReversed.value() || MainActivity.dualPaneActive) {
            params.gravity = Gravity.BOTTOM | Gravity.START;
        }
        else {
            params.gravity = Gravity.BOTTOM | Gravity.END;
        }
        layoutFabNav.setLayoutParams(params);
    }

    public void updateFabNavColors() {
        ColorStateList fabColor = ColorStateList.valueOf(MyApplication.colorSecondary);
        fabMain.setBackgroundTintList(fabColor);
        fabRefresh.setBackgroundTintList(fabColor);
        fabSubmit.setBackgroundTintList(fabColor);
        fabSync.setBackgroundTintList(fabColor);
        fabHideRead.setBackgroundTintList(fabColor);
        fabSearch.setBackgroundTintList(fabColor);
        fabSubmitLink.setBackgroundTintList(fabColor);
        fabSubmitText.setBackgroundTintList(fabColor);
    }

    public void updateFabNavAvailability() {
        initFabNavOptions(activity.findViewById(android.R.id.content));
    }

    private void setFabIndividualVisibility(boolean showSubmitFab) {
        int submitFabsVis = showSubmitFab ? View.VISIBLE : View.GONE;
        int subredditFabsVis = !showSubmitFab && this instanceof PostListFragment ? View.VISIBLE : View.GONE;
        int otherFabsVis = !showSubmitFab ? View.VISIBLE : View.GONE;
        fabSubmitLink.setVisibility(submitFabsVis);
        fabSubmitText.setVisibility(submitFabsVis);
        fabSubmit.setVisibility(MyApplication.offlineModeEnabled ? View.GONE : subredditFabsVis);
        fabSync.setVisibility(subredditFabsVis);
        fabRefresh.setVisibility(otherFabsVis);
        fabHideRead.setVisibility(otherFabsVis);
        fabSearch.setVisibility(MyApplication.offlineModeEnabled ? View.GONE : otherFabsVis);
    }

    private void setFabMainOptionsVisible(boolean flag) {
        setFabIndividualVisibility(false);
        if(flag) {
            fabOptionsVisible = true;
            // show FAB options
            layoutFabNavOptions.startAnimation(showAnimation);
        }
        else {
            fabOptionsVisible = false;
            // hide FAB options
            layoutFabNavOptions.startAnimation(hideAnimation);
        }
    }

    public void setFabSubmitOptionsVisible(boolean flag) {
        setFabIndividualVisibility(true);
        fabOptionsVisible = false;
        if(flag) {
            fabSubmitOptionsVisible = true;
            // show submit FABs
            layoutFabNavOptions.startAnimation(showAnimation);
        }
        else {
            fabSubmitOptionsVisible = false;
            // hide submit FABs
            layoutFabNavOptions.startAnimation(hideAnimation);
        }
    }

    public void showFabMainOptions() {
        fabMain.setImageResource(R.mipmap.ic_close_grey_48dp);
        fabMain.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        setFabMainOptionsVisible(true);
    }

    public void hideAllFabOptions() {
        try {
            fabMain.setImageResource(R.mipmap.ic_add_white_24dp);
            fabMain.setBackgroundTintList(ColorStateList.valueOf(MyApplication.colorSecondary));
            if (fabOptionsVisible) {
                setFabMainOptionsVisible(false);
            } else if (fabSubmitOptionsVisible) {
                setFabSubmitOptionsVisible(false);
            }
        } catch (NullPointerException e) {}
    }

    public void toggleFabOptions() {
        if(fabOptionsVisible || fabSubmitOptionsVisible) {
            hideAllFabOptions();
        }
        else {
            showFabMainOptions();
        }
    }

    public void initSwipeRefreshLayout(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        updateSwipeRefreshColor();
        updateSwipeRefreshOffset();
    }

    public void updateSwipeRefreshColor() {
        swipeRefreshLayout.setColorSchemeColors(MyApplication.colorPrimary);
    }

    public void updateSwipeRefreshOffset() {
        int end = activity.toolbarVisible ? activity.toolbar.getHeight() : 0;
        swipeRefreshLayout.setProgressViewOffset(false, 0, end + 32);
    }

    private void updateSwipeRefreshState() {
        swipeRefreshLayout.setEnabled(MyApplication.swipeRefresh && findFirstCompletelyVisiblePostPosition() == 0);
        updateSwipeRefreshOffset();
    }

    public void colorSchemeChanged() {
        updateSwipeRefreshColor();
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh() {
        refreshList();
    }
}
