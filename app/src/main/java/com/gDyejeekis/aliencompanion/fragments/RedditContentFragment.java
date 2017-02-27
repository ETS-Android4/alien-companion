package com.gDyejeekis.aliencompanion.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.ToolbarActivity;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.enums.LoadType;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.utils.GridAutoFitLayoutManager;
import com.gDyejeekis.aliencompanion.views.DividerItemDecoration;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.ShowMoreListener;
import com.gDyejeekis.aliencompanion.views.viewholders.PostGalleryViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 2/9/2017.
 */

public abstract class RedditContentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    public abstract void refreshList();

    public abstract void extendList();

    public abstract boolean hasFabNavigation();

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
    public FloatingActionButton fabNav;

    protected final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateSwipeRefreshOnScroll();
            updateToolbarOnScroll(dy);
            updateFabOnScroll(dy);
            updateLoadMoreOnScroll(recyclerView);
        }
    };

    private DividerItemDecoration dividerDecoration;

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

        dividerDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        initFabAnimations();

        loadMore = MyApplication.endlessPosts;
        //hasMore = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSwipeRefreshOnScroll();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    public void goToTop() {
        hideAllFabOptions();
        layoutManager.scrollToPosition(0);
    }

    public void goToBottom() {
        hideAllFabOptions();
        layoutManager.scrollToPosition(adapter.redditItems.size()-1);
    }

    private void updateToolbarOnScroll(int dy) {
        if(MyApplication.autoHideToolbar) {
            if(dy > MyApplication.TOOLBAR_HIDE_ON_SCROLL_THRESHOLD) {
                ((ToolbarActivity)activity).hideToolbar();
                updateSwipeRefreshOffset();
            }
            else if(dy < -MyApplication.TOOLBAR_HIDE_ON_SCROLL_THRESHOLD
                    || findFirstCompletelyVisiblePostPosition() == 0) {
                ((ToolbarActivity)activity).showToolbar();
                updateSwipeRefreshOffset();
            }
        }
    }

    private void updateFabOnScroll(int dy) {
        if(MyApplication.postNavigation && hasFabNavigation()) {
            if (MyApplication.autoHidePostFab) {
                if (dy > MyApplication.FAB_HIDE_ON_SCROLL_THRESHOLD) {
                    hideAllFabOptions();
                    fabNav.hide();
                } else if (dy < -MyApplication.FAB_HIDE_ON_SCROLL_THRESHOLD
                        || findFirstCompletelyVisiblePostPosition() == 0) {
                    fabNav.show();
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
                    editor.putString("defaultView", String.valueOf(viewType.value()));
                    editor.apply();
                    if(currentLoadType==null) redrawList();
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
            adapter = new RedditItemListAdapter(activity, notClicked);
            updateContentView(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeClickedPosts(int viewTypeValue) {
        try {
            hideAllFabOptions();
            List<RedditItem> notClicked = new ArrayList<>();
            for(RedditItem item : adapter.redditItems) {
                if(item instanceof Submission && !((Submission) item).isClicked()) {
                    notClicked.add(item);
                }
            }
            adapter = new RedditItemListAdapter(activity, viewTypeValue, notClicked);
            updateContentView(adapter, viewTypeValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void redrawList() {
        try {
            if(fabOptionsVisible) {
                setFabNavOptionsVisible(false);
            }
            List<RedditItem> items = adapter.redditItems;
            items.remove(items.size() - 1); // remove show more item
            adapter = new RedditItemListAdapter(activity, items);
            updateContentView(adapter);
        } catch (ArrayIndexOutOfBoundsException e) {}
    }

    public void redrawList(int viewTypeValue) {
        try {
            if(fabOptionsVisible) {
                setFabNavOptionsVisible(false);
            }
            List<RedditItem> items = adapter.redditItems;
            items.remove(items.size() - 1); // remove show more item
            adapter = new RedditItemListAdapter(activity, viewTypeValue, items);
            updateContentView(adapter, viewTypeValue);
        } catch (ArrayIndexOutOfBoundsException e) {}
    }

    public void updateContentViewProperties() {
        updateContentViewProperties(MyApplication.currentPostListView);
    }

    public void updateContentViewProperties(int viewTypeValue) {
        contentView.setHasFixedSize(true);
        setLayoutManager(viewTypeValue);
        setListDividerVisible(PostViewType.hasVisibleListDivider(viewTypeValue));
    }

    public void updateContentViewAdapter(RedditItemListAdapter adapter) {
        contentView.setAdapter(adapter);
    }

    public void updateContentView(RedditItemListAdapter adapter, int viewTypeValue) {
        updateContentViewAdapter(adapter);
        updateContentViewProperties(viewTypeValue);
    }

    public void updateContentView(RedditItemListAdapter adapter) {
        updateContentViewAdapter(adapter);
        updateContentViewProperties();
    }

    protected void setListDividerVisible(boolean flag) {
        contentView.removeItemDecoration(dividerDecoration);
        if(flag) {
            contentView.addItemDecoration(dividerDecoration);
        }
    }

    private LinearLayout layoutFabNav;
    private LinearLayout layoutFabNavOptions;
    private boolean fabOptionsVisible;
    private boolean fabSubmitOptionsVisible;

    private FloatingActionButton fabTop;
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
        layoutFabNav = (LinearLayout) view.findViewById(R.id.layout_fab_nav);
        if(MyApplication.postNavigation && hasFabNavigation()) {
            setLayoutFabNavVisible(true);
            layoutFabNavOptions = (LinearLayout) view.findViewById(R.id.layout_fab_nav_options);
            layoutFabNavOptions.setVisibility(View.GONE);
            ColorStateList fabColor = ColorStateList.valueOf(MyApplication.colorSecondary);
            fabNav = (FloatingActionButton) view.findViewById(R.id.fab_nav);
            fabNav.setBackgroundTintList(fabColor);
            fabNav.setOnClickListener(this);
            fabNav.show();

            fabTop = (FloatingActionButton) view.findViewById(R.id.fab_go_top);
            fabRefresh = (FloatingActionButton) view.findViewById(R.id.fab_refresh);
            fabSubmit = (FloatingActionButton) view.findViewById(R.id.fab_submit);
            fabSync = (FloatingActionButton) view.findViewById(R.id.fab_sync);
            fabHideRead = (FloatingActionButton) view.findViewById(R.id.fab_hide_read);
            fabSearch = (FloatingActionButton) view.findViewById(R.id.fab_search);
            fabSubmitLink = (FloatingActionButton) view.findViewById(R.id.fab_submit_link);
            fabSubmitText = (FloatingActionButton) view.findViewById(R.id.fab_submit_text);
            fabTop.setOnClickListener(this);
            fabRefresh.setOnClickListener(this);
            fabSubmit.setOnClickListener(this);
            fabSync.setOnClickListener(this);
            fabHideRead.setOnClickListener(this);
            fabSearch.setOnClickListener(this);
            fabSubmitLink.setOnClickListener(this);
            fabSubmitText.setOnClickListener(this);
            updateFabNavColors();

            setFabIndividualVisibility(false);
        }
        else {
            setLayoutFabNavVisible(false);
        }
    }

    public void updateFabNavColors() {
        ColorStateList fabColor = ColorStateList.valueOf(MyApplication.colorSecondary);
        fabTop.setBackgroundTintList(fabColor);
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
        fabSubmit.setVisibility(subredditFabsVis);
        fabSync.setVisibility(subredditFabsVis);
        fabTop.setVisibility(otherFabsVis);
        fabRefresh.setVisibility(otherFabsVis);
        fabHideRead.setVisibility(otherFabsVis);
        fabSearch.setVisibility(otherFabsVis);
    }

    private void setFabNavOptionsVisible(boolean flag) {
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

    private void setFabSubmitOptionsVisible(boolean flag) {
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

    public void showFabNavOptions() {
        fabNav.setImageResource(R.mipmap.ic_close_grey_48dp);
        fabNav.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        setFabNavOptionsVisible(true);
    }

    public void hideAllFabOptions() {
        try {
            fabNav.setImageResource(R.drawable.ic_navigation_white_36dp);
            fabNav.setBackgroundTintList(ColorStateList.valueOf(MyApplication.colorSecondary));
            if (fabOptionsVisible) {
                setFabNavOptionsVisible(false);
            } else if (fabSubmitOptionsVisible) {
                setFabSubmitOptionsVisible(false);
            }
        } catch (NullPointerException e) {}
    }

    private void toggleNavOptions() {
        if(fabOptionsVisible || fabSubmitOptionsVisible) {
            hideAllFabOptions();
        }
        else {
            showFabNavOptions();
        }
    }

    private void setLayoutFabNavVisible(boolean flag) {
        layoutFabNav.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_nav:
                toggleNavOptions();
                break;
            case R.id.fab_go_top:
                goToTop();
                break;
            case R.id.fab_refresh:
                refreshList();
                break;
            case R.id.fab_submit:
                setFabSubmitOptionsVisible(true);
                break;
            case R.id.fab_sync:
                // TODO: 2/24/2017 add abstraction
                ((PostListFragment)this).addToSyncQueue();
                break;
            case R.id.fab_hide_read:
                // TODO: 2/23/2017 this class should have viewTypeValue field, add abstraction later
                if(this instanceof PostListFragment) {
                    removeClickedPosts(((PostListFragment)this).getCurrentViewTypeValue());
                }
                else if(this instanceof SearchFragment) {
                    removeClickedPosts();
                }
                break;
            case R.id.fab_search:
                // TODO: 2/24/2017 add abstraction
                if(this instanceof PostListFragment) {
                    ((PostListFragment)this).showSearchDialog();
                }
                else if(this instanceof SearchFragment) {
                    ((SearchFragment)this).showSearchDialog();
                }
                break;
            case R.id.fab_submit_link:
                ((PostListFragment)this).startSubmitActivity(SubmitType.link);
                break;
            case R.id.fab_submit_text:
                ((PostListFragment)this).startSubmitActivity(SubmitType.self);
                break;
        }
    }

    /**
     * override this in UserFragment and MessageFragment
     */
    protected void setLayoutManager() {
        setLayoutManager(MyApplication.currentPostListView);
    }

    protected void setLayoutManager(int viewTypeValue) {
        layoutManager = viewTypeValue == PostViewType.gallery.value() ?
                new GridAutoFitLayoutManager(activity, PostGalleryViewHolder.GALLERY_COLUMN_WIDTH) : new LinearLayoutManager(activity);
        contentView.setLayoutManager(layoutManager);
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
        if(activity instanceof ToolbarActivity) {
            ToolbarActivity toolbarActivity = (ToolbarActivity) activity;
            int end = toolbarActivity.toolbarVisible ? toolbarActivity.toolbar.getHeight() : 0;
            swipeRefreshLayout.setProgressViewOffset(false, 0, end + 16);
        }
    }

    private void updateSwipeRefreshOnScroll() {
        swipeRefreshLayout.setEnabled(MyApplication.swipeRefresh && findFirstCompletelyVisiblePostPosition() == 0);
    }

    public void colorSchemeChanged() {
        updateSwipeRefreshColor();
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    //public DialogFragment getCurrentDialogFragment() {
    //    try {
    //        return (DialogFragment) activity.getFragmentManager().findFragmentByTag("dialog");
    //    } catch (Exception e) {
    //        return null;
    //    }
    //}

    @Override
    public void onRefresh() {
        refreshList();
    }
}
