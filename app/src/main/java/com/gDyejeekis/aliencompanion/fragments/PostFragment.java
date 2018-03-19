package com.gDyejeekis.aliencompanion.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.activities.ToolbarActivity;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.broadcast_receivers.RedditItemSubmittedReceiver;
import com.gDyejeekis.aliencompanion.enums.CommentNavSetting;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.AmaUsernamesDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.CommentNavDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SearchTextDialogFragment;
import com.gDyejeekis.aliencompanion.utils.LinkUtils;
import com.gDyejeekis.aliencompanion.utils.MoveUpwardLinearLayout;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.asynctask.LoadCommentsTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.views.DividerItemDecoration;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.fab_menu_listeners.CommentFabNavListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    //private static final String GROUPS_KEY = "groups_key";

    public PostAdapter postAdapter;
    public RecyclerView mRecyclerView;
    public LinearLayoutManager mLayoutManager;
    public SwipeRefreshLayout swipeRefreshLayout;
    private ToolbarActivity activity;
    public Submission post;
    public CommentSort commentSort;
    public CommentSort tempSort;
    public ProgressBar progressBar;
    public boolean incompletePostObject = false;
    public String redditVideoUrl;
    public boolean commentsLoaded;
    public boolean showFullCommentsButton;
    public LoadCommentsTask task;
    public Snackbar currentSnackbar;
    private BroadcastReceiver submittedReceiver;

    private static boolean fabOptionsVisible;
    private MoveUpwardLinearLayout fabContainer;
    //private RelativeLayout layoutFabRoot;
    private LinearLayout layoutFabNav;
    private LinearLayout layoutFabOptions;
    private FloatingActionButton fabMain;
    //private FloatingActionButton fabReply;
    private FloatingActionButton fabNavSetting;
    private FloatingActionButton fabNext;
    private FloatingActionButton fabPrevious;
    private Animation showAnimOptions;
    private Animation showAnimCommentNav;
    private Animation hideAnimOptions;
    private Animation hideAnimCommentNav;
    public CommentNavSetting commentNavSetting;
    public CommentFabNavListener commentNavListener;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateSwipeRefreshState();
            updateFabNavOnScroll(dy);
        }
    };

    public View getSnackbarParentView() {
        return fabContainer;
    }

    public static PostFragment newInstance(Submission post) {
        PostFragment postFragment = new PostFragment();
        postFragment.incompletePostObject = false;
        postFragment.post = post;
        postFragment.commentSort = post.getPreferredSort();

        return postFragment;
    }

    public static PostFragment newInstance(PostAdapter postAdapter) {
        PostFragment newInstance = new PostFragment();
        newInstance.commentsLoaded = true;
        newInstance.postAdapter = postAdapter;
        Submission post = (Submission) postAdapter.getItemAt(0);
        newInstance.post = post;
        newInstance.commentSort = post.getPreferredSort();

        return newInstance;
    }

    public static PostFragment newInstance(String url) {
        PostFragment postFragment = new PostFragment();
        postFragment.incompletePostObject = true;
        Submission post = LinkUtils.getRedditPostFromUrl(url);
        postFragment.post = post;
        postFragment.commentSort = post.getPreferredSort();

        return postFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        commentNavListener = new CommentFabNavListener(this);
        if (MyApplication.commentFabNavigation) {
            initFabAnimations();
        }

        if (!MyApplication.dualPaneActive) {
            if(MyApplication.dualPane && activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                MyApplication.dualPaneActive = true;
            }
        }

        String url = activity.getIntent().getStringExtra("url");
        Submission post = (Submission) activity.getIntent().getSerializableExtra("post");
        CommentSort commentSort = (CommentSort) activity.getIntent().getSerializableExtra("commentSort");

        // TODO: 3/6/2018 re-write at some point to take into account newInstance static methods
        if (post == null && url != null) {
            incompletePostObject = true;
            this.post = LinkUtils.getRedditPostFromUrl(url);
            if (this.post==null) {
                if(url.contains("v.redd.it")) {
                    redditVideoUrl = url;
                }
            } else {
                showFullCommentsButton = this.post.getLinkedCommentId() != null;
                this.commentSort = this.post.getPreferredSort();
            }
        } else if (post != null) {
            incompletePostObject = (post.isSelf() == null); // TODO: 3/15/2018 might need to put a better condition here
            this.post = post;
            this.commentSort = post.getPreferredSort();
        }

        if (commentSort != null) {
            this.commentSort = commentSort;
        } else if (this.commentSort == null) {
            this.commentSort = MyApplication.defaultCommentSort;
        }
        this.tempSort = this.commentSort;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reddit_content, container, false);
        initRecyclerView(view);
        initMainProgressBar(view);
        initSwipeRefreshLayout(view);
        initFabNavOptions();

        if (postAdapter == null) {
            //currentlyLoading = true;
            postAdapter = new PostAdapter(activity);

            if(incompletePostObject || post==null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            else {
                postAdapter.add(post);
            }

            mRecyclerView.setAdapter(postAdapter);

            task = new LoadCommentsTask(activity, this, true);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mRecyclerView.setAdapter(postAdapter);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSwipeRefreshState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(postAdapter != null) {
            postAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ToolbarActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        setActionBarSubtitle();
        registerReceivers();
        //updateActionBar = false;
    }

    //@Override
    //public void onDestroyView() {
    //    super.onDestroyView();
    //    activity = null;
    //}

    @Override
    public void onDetach() {
        unregisterReceivers();
        super.onDetach();
        activity = null;
    }

    private void registerReceivers() {
        submittedReceiver = new RedditItemSubmittedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RedditItemSubmittedReceiver.POST_SELF_TEXT_EDIT);
        filter.addAction(RedditItemSubmittedReceiver.COMMENT_SUBMISSION);
        filter.addAction(RedditItemSubmittedReceiver.COMMENT_EDIT);
        activity.registerReceiver(submittedReceiver, filter);
    }

    private void unregisterReceivers() {
        activity.unregisterReceiver(submittedReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_comments:
                refreshPostAndComments();
                return true;
            case R.id.action_sort_comments:
                showSortPopup(activity.findViewById(R.id.action_sort_comments));
                return true;
            case R.id.action_reply:
                submitComment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void submitComment() {
        if (MyApplication.currentUser == null) {
            setSnackbar(ToastUtils.showSnackbar(getSnackbarParentView(),
                    "Must be logged in to comment"));
        } else {
            Intent intent = new Intent(activity, SubmitActivity.class);
            intent.putExtra("submitType", SubmitType.comment);
            intent.putExtra("postName", post.getFullName());
            intent.putExtra("position", 1);
            startActivity(intent);
        }
    }

    public void newCommentSubmitted(int position, Comment comment) {
        if (postAdapter!=null && mLayoutManager!=null) {
            postAdapter.addComment(position, comment);
            mLayoutManager.scrollToPosition(position);
        }
    }

    public void showSortPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_comments_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_comments_top:
                        refreshPostAndComments(CommentSort.TOP);
                        return true;
                    case R.id.action_sort_comments_best:
                        refreshPostAndComments(CommentSort.BEST);
                        return true;
                    case R.id.action_sort_comments_new:
                        refreshPostAndComments(CommentSort.NEW);
                        return true;
                    case R.id.action_sort_comments_old:
                        refreshPostAndComments(CommentSort.OLD);
                        return true;
                    case R.id.action_sort_comments_controversial:
                        refreshPostAndComments(CommentSort.CONTROVERSIAL);
                        return true;
                    case R.id.action_sort_comments_qa:
                        refreshPostAndComments(CommentSort.QA);
                        return true;
                    case R.id.action_sort_comments_random:
                        refreshPostAndComments(CommentSort.RANDOM);
                        return true;
                    case R.id.action_sort_comments_confidence:
                        refreshPostAndComments(CommentSort.CONFIDENCE);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void initRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerView_postList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(onScrollListener);
        //mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        try {
        //            getView().requestFocus();
        //        } catch (Exception e) {}
        //        return false;
        //    }
        //});
    }

    private void initMainProgressBar(View view) {
        progressBar = view.findViewById(R.id.progressBar2);
        progressBar.getIndeterminateDrawable().setColorFilter(MyApplication.colorSecondary, PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.GONE);
    }

    private void initSwipeRefreshLayout(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(MyApplication.colorSecondary);
    }

    public void onFabNavOptionsChanged() {
        fabContainer.removeAllViews();
        initFabNavOptions();
    }

    public void initFabNavOptions() {
        fabContainer = activity.findViewById(R.id.container_fab);
        if (MyApplication.commentFabNavigation) {
            fabContainer.setVisibility(View.VISIBLE);
            View.inflate(activity, R.layout.fab_comment_nav, fabContainer);
            //layoutFabRoot = activity.findViewById(R.id.layout_comment_nav_root);
            //layoutFabRoot.setVisibility(View.VISIBLE);
            layoutFabNav = activity.findViewById(R.id.layout_comment_nav);
            layoutFabOptions = activity.findViewById(R.id.layout_comment_fab_options);
            fabMain = activity.findViewById(R.id.fab_nav);
            fabNavSetting = activity.findViewById(R.id.fab_comment_nav_setting);
            //fabReply = activity.findViewById(R.id.fab_reply);
            fabNext = activity.findViewById(R.id.fab_down);
            fabPrevious = activity.findViewById(R.id.fab_up);
            fabMain.setOnClickListener(commentNavListener);
            fabNavSetting.setOnClickListener(commentNavListener);
            fabNavSetting.setOnLongClickListener(commentNavListener);
            //fabReply.setOnClickListener(commentNavListener);
            //fabReply.setOnLongClickListener(commentNavListener);
            fabNext.setOnClickListener(commentNavListener);
            fabPrevious.setOnClickListener(commentNavListener);
            ColorStateList fabColor = ColorStateList.valueOf(MyApplication.colorSecondary);
            fabMain.setBackgroundTintList(fabColor);
            fabNavSetting.setBackgroundTintList(fabColor);
            //fabReply.setBackgroundTintList(fabColor);
            fabNext.setBackgroundTintList(fabColor);
            fabPrevious.setBackgroundTintList(fabColor);
            setFabOptionsVisible(fabOptionsVisible);
            int fabVisibility = fabOptionsVisible ? View.VISIBLE : View.GONE;
            layoutFabOptions.setVisibility(fabVisibility);
            layoutFabNav.setVisibility(fabVisibility);
            setCommentNavSetting(CommentNavSetting.threads);
        } else {
            fabContainer.removeAllViews();
            fabContainer.setVisibility(View.GONE);
        }
    }

    private void initFabAnimations() {
        showAnimOptions = AnimationUtils.loadAnimation(activity, R.anim.fab_options_show);
        showAnimCommentNav = AnimationUtils.loadAnimation(activity, R.anim.fab_comment_nav_show);
        Animation.AnimationListener showListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                layoutFabNav.setVisibility(View.INVISIBLE);
                layoutFabOptions.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutFabNav.setVisibility(View.VISIBLE);
                layoutFabOptions.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        showAnimOptions.setAnimationListener(showListener);
        showAnimCommentNav.setAnimationListener(showListener);
        hideAnimOptions = AnimationUtils.loadAnimation(activity, R.anim.fab_options_hide);
        hideAnimCommentNav = AnimationUtils.loadAnimation(activity, R.anim.fab_comment_nav_hide);
        Animation.AnimationListener hideListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutFabNav.setVisibility(View.GONE);
                layoutFabOptions.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        hideAnimOptions.setAnimationListener(hideListener);
        hideAnimCommentNav.setAnimationListener(hideListener);
    }

    private void setFabOptionsVisible(boolean flag) {
        if(flag) {
            fabOptionsVisible = true;
            fabMain.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            fabMain.setImageResource(R.drawable.ic_close_black_48dp);
            fabMain.setImageAlpha(138);
        }
        else {
            fabOptionsVisible = false;
            fabMain.setBackgroundTintList(ColorStateList.valueOf(MyApplication.colorSecondary));
            fabMain.setImageResource(R.drawable.ic_navigation_white_48dp);
            fabMain.setImageAlpha(255);
        }
    }

    public void toggleFabNavOptions() {
        if(fabOptionsVisible) {
            setFabOptionsVisible(false);
            layoutFabNav.startAnimation(hideAnimCommentNav);
            layoutFabOptions.startAnimation(hideAnimOptions);
        }
        else {
            setFabOptionsVisible(true);
            layoutFabOptions.startAnimation(showAnimOptions);
            layoutFabNav.startAnimation(showAnimCommentNav);
        }
    }

    private void updateSwipeRefreshState() {
        swipeRefreshLayout.setEnabled(MyApplication.swipeRefresh && isScrolledTop());
    }

    private boolean isScrolledTop() {
        try {
            int topPos = mRecyclerView.getChildAt(0).getTop();
            int firstVisible = mLayoutManager.findFirstVisibleItemPosition();
            return firstVisible == 0 && topPos >= 0;
        } catch (Exception e) {}
        return false;
    }

    private void hideAllFabOnScroll() {
        fabMain.hide();
        //fabReply.hide();
        fabNavSetting.hide();
        fabNext.hide();
        fabPrevious.hide();
    }

    private void showAllFabOnScroll() {
        fabMain.show();
        //fabReply.show();
        fabNavSetting.show();
        fabNext.show();
        fabPrevious.show();
    }

    public void updateFabNavOnScroll(int dy) {
        if(MyApplication.commentFabNavigation) {
            if(postAdapter.getItemCount() == 1 || lastItemCompletelyVisible()
                    || (MyApplication.autoHideCommentFab && dy > AppConstants.FAB_HIDE_ON_SCROLL_THRESHOLD)) {
                hideAllFabOnScroll();
            }
            else if(isScrolledTop()
                    || (MyApplication.autoHideCommentFab && dy < -AppConstants.FAB_HIDE_ON_SCROLL_THRESHOLD)
                    || (!MyApplication.autoHideCommentFab && !lastItemCompletelyVisible())) {
                showAllFabOnScroll();
            }
        }
    }

    private boolean lastItemCompletelyVisible() {
        return mLayoutManager.findLastCompletelyVisibleItemPosition() == postAdapter.getItemCount() - 1;
    }

    @Override public void onRefresh() {
        //swipeRefreshLayout.setRefreshing(false);
        refreshPostAndComments();
    }

    public void loadFullComments() {
        showFullCommentsButton = false;
        post.setLinkedCommentId(null);
        refreshPostAndComments();
    }

    public void refreshPostAndComments(CommentSort sort) {
        this.tempSort = sort;
        refreshPostAndComments();
    }

    public void refreshPostAndComments() {
        postAdapter.selectedPosition = -1;
        dismissSnackbar();
        swipeRefreshLayout.setRefreshing(true);
        if(!commentsLoaded) task.cancel(true);

        commentsLoaded = false;
        //postAdapter.commentsRefreshed(post, new ArrayList<Comment>());

        //setActionBarSubtitle();
        task = new LoadCommentsTask(activity, this, false);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setCommentSort(CommentSort sort) {
        this.commentSort = sort;
    }

    public void setActionBarTitle() {
        if(!MyApplication.dualPaneActive/* || updateActionBar*/) {
            String title;
            if (post!=null && post.getSubreddit() != null) {
                title = post.getSubreddit().toLowerCase();
            } else {
                title = "loading..";
            }
            activity.getSupportActionBar().setTitle(title);
        }
    }

    public void setActionBarSubtitle() {
        if(!MyApplication.dualPaneActive/* || updateActionBar*/) {
            String subtitle;
            if (MyApplication.offlineModeEnabled) {
                subtitle = "loading";
            }
            else {
                // redundant
                //if(commentSort==null) {
                //    commentSort = CommentSort.TOP;
                //}
                subtitle = commentSort.value();
            }
            activity.getSupportActionBar().setSubtitle(subtitle);
        }
    }

    public void setActionBarSubtitle(String subtitle) {
        if(!MyApplication.dualPaneActive/* || updateActionBar*/) {
            if (subtitle == null)
                setActionBarSubtitle();
            else
                activity.getSupportActionBar().setSubtitle(subtitle);
        }
    }

    public void showCommentNavDialog() {
        CommentNavDialogFragment dialog = new CommentNavDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("commentNav", commentNavSetting);
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "dialog");
    }

    public void showSearchTextDialog() {
        SearchTextDialogFragment dialog = new SearchTextDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("searchTerm", commentNavListener.searchQuery);
        bundle.putBoolean("matchCase", commentNavListener.matchCase);
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "dialog");
    }

    public void showTimeFilterDialog() {
        HmsPickerBuilder hpb = new HmsPickerBuilder()
                .setFragmentManager(activity.getSupportFragmentManager())
                .setStyleResId(MyApplication.nightThemeEnabled ? R.style.BetterPickersDialogFragment : R.style.BetterPickersDialogFragment_Light);
        hpb.addHmsPickerDialogHandler(new HmsPickerDialogFragment.HmsPickerDialogHandlerV2() {
            @Override
            public void onDialogHmsSet(int reference, boolean isNegative, int hours, int minutes, int seconds) {
                commentNavListener.setTimeFilter(hours, minutes, seconds);
            }
        });
        hpb.show();
    }

    public void showAmaModeUsernamePicker() {
        AmaUsernamesDialogFragment dialog = new AmaUsernamesDialogFragment();
        String usernames = commentNavListener.getAmaUsernamesString();
        if(usernames==null) {
            String author = ((Submission) postAdapter.getData().get(0)).getAuthor();
            if(author!=null && !author.equals("[deleted]")) {
                usernames = author;
            }
        }
        Bundle bundle = new Bundle();
        bundle.putString("usernames", usernames);
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "dialog");
    }

    public void setCommentNavSetting(CommentNavSetting commentNavSetting) {
        this.commentNavSetting = commentNavSetting;
        fabNavSetting.setImageResource(commentNavSetting.getIconResourceWhite());
    }

}
