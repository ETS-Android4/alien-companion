package com.gDyejeekis.aliencompanion.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.activities.ToolbarActivity;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.asynctask.LoadCommentsTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.views.DividerItemDecoration;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    //private static final String GROUPS_KEY = "groups_key";

    public PostAdapter postAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AppCompatActivity activity;
    public Submission post;
    public CommentSort commentSort;
    public ProgressBar progressBar;
    private boolean loadFromList;
    public boolean noResponseObject; //TODO: check this variable
    public String commentLinkId;
    public int parentsShown = -1;
    public boolean titleUpdated;
    public boolean commentsLoaded;
    public boolean showFullCommentsButton;
    private LinearLayout layoutFab;
    public LoadCommentsTask task;

    private RelativeLayout commentFabRoot;
    private FloatingActionButton fabMain;
    private FloatingActionButton fabReply;
    private FloatingActionButton fabNavSetting;
    private FloatingActionButton fabSearch;
    private FloatingActionButton fabRefresh;
    private FloatingActionButton fabGoToTop;
    private FloatingActionButton fabNext;
    private FloatingActionButton fabPrevious;

    private boolean updateActionBar = false;

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateToolbarOnScroll(dy);
        }
    };

    private void updateToolbarOnScroll(int dy) {
        if(MyApplication.autoHideToolbar) {
            if(dy > MyApplication.TOOLBAR_HIDE_ON_SCROLL_THRESHOLD) {
                ((ToolbarActivity)activity).hideToolbar();
            }
            else if(dy < -MyApplication.TOOLBAR_HIDE_ON_SCROLL_THRESHOLD
                    || mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                ((ToolbarActivity)activity).showToolbar();
            }
        }
    }

    //public static boolean currentlyLoading = false;

    public static PostFragment newInstance(Submission post) {
        PostFragment postFragment = new PostFragment();
        postFragment.post = post;
        postFragment.loadFromList = true;

        return postFragment;
    }

    public static PostFragment newInstance(PostAdapter postAdapter) {
        PostFragment newInstance = new PostFragment();
        newInstance.postAdapter = postAdapter;
        newInstance.post = (Submission) postAdapter.getItemAt(0);
        newInstance.commentsLoaded = true;

        return newInstance;
    }

    public static PostFragment newInstance(String[] postInfo) {
        PostFragment postFragment = new PostFragment();
        postFragment.loadFromList = false;

        postFragment.post = new Submission(postInfo[1]);
        postFragment.post.setSubreddit(postInfo[0]);
        postFragment.commentLinkId = postInfo[2];
        if (postFragment.commentLinkId != null) postFragment.showFullCommentsButton = true;
        if (postInfo[3] != null) postFragment.parentsShown = Integer.valueOf(postInfo[3]);

        return postFragment;
    }

    public static PostFragment newInstance(String postId) {
        PostFragment postFragment = new PostFragment();
        postFragment.loadFromList = false;

        postFragment.titleUpdated = false;
        postFragment.post = new Submission(postId);

        return postFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        String[] postInfo = activity.getIntent().getStringArrayExtra("postInfo");

        if(!MainActivity.dualPaneActive) {
            post = (Submission) activity.getIntent().getSerializableExtra("post");
            loadFromList = (post != null);

            if (!loadFromList) {
                initPostFromUrl(postInfo);
            }
            if(MyApplication.dualPane && activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) MainActivity.dualPaneActive = true;
        }
        else {
            if(post==null) {
                updateActionBar = true;
                initPostFromUrl(postInfo);
            }
        }
    }

    private void initPostFromUrl(String[] postInfo) {
        if (postInfo != null) {
            initPostFromInfoArray(postInfo);
        } else {
            titleUpdated = false;
            post = new Submission(activity.getIntent().getStringExtra("postId"));
        }
    }

    private void initPostFromInfoArray(String[] postInfo) {
        post = new Submission(postInfo[1]);
        post.setSubreddit(postInfo[0]);
        commentLinkId = postInfo[2];
        if (commentLinkId != null) showFullCommentsButton = true;
        if (postInfo[3] != null) parentsShown = Integer.valueOf(postInfo[3]);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MyApplication.swipeRefresh && mLayoutManager.findFirstCompletelyVisibleItemPosition()==0){
            swipeRefreshLayout.setEnabled(true);
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        activity = null;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setActionBarTitle();
        if(commentSort == null) commentSort = CommentSort.TOP;
        setActionBarSubtitle();
        updateActionBar = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_comments:
                refreshComments();
                return true;
            case R.id.action_sort_comments:
                showSortPopup(activity.findViewById(R.id.action_sort_comments));
                return true;
            case R.id.action_reply:
                Intent intent = new Intent(activity, SubmitActivity.class);
                intent.putExtra("submitType", SubmitType.comment);
                intent.putExtra("postName", post.getFullName());
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showSortPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_comments_sort);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort_comments_top:
                        setCommentSort(CommentSort.TOP);
                        refreshComments();
                        return true;
                    case R.id.action_sort_comments_best:
                        setCommentSort(CommentSort.BEST);
                        refreshComments();
                        return true;
                    case R.id.action_sort_comments_new:
                        setCommentSort(CommentSort.NEW);
                        refreshComments();
                        return true;
                    case R.id.action_sort_comments_old:
                        setCommentSort(CommentSort.OLD);
                        refreshComments();
                        return true;
                    case R.id.action_sort_comments_controversial:
                        setCommentSort(CommentSort.CONTROVERSIAL);
                        refreshComments();
                        return true;
                    case R.id.action_sort_comments_qa:
                        setCommentSort(CommentSort.QA);
                        refreshComments();
                        return true;
                    //case R.id.action_sort_comments_random:
                    //    setCommentSort(CommentSort.RANDOM);
                    //    refreshComments();
                    //    return true;
                    //case R.id.action_sort_comments_confidence:
                    //    setCommentSort(CommentSort.CONFIDENCE);
                    //    refreshComments();
                    //    return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_postList);
        layoutFab = (LinearLayout) rootView.findViewById(R.id.layout_fab);
        if(MyApplication.commentNavigation) {
            FloatingActionButton fab_up = (FloatingActionButton) rootView.findViewById(R.id.fab_up);
            FloatingActionButton fab_down = (FloatingActionButton) rootView.findViewById(R.id.fab_down);
            ColorStateList color = (MyApplication.currentBaseTheme >= MyApplication.DARK_THEME) ?
                    ColorStateList.valueOf(Color.parseColor("#404040")) : ColorStateList.valueOf(MyApplication.colorPrimary);
            fab_up.setBackgroundTintList(color);
            fab_down.setBackgroundTintList(color);
            fab_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    previousParentComment();
                }
            });
            fab_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextParentComment();
                }
            });
        }
        else {
            layoutFab.setVisibility(View.GONE);
        }
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(MyApplication.currentColor);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(onScrollListener);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(MyApplication.swipeRefresh) {
                    if (mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                        swipeRefreshLayout.setEnabled(true);
                    }
                    else {
                        swipeRefreshLayout.setEnabled(false);
                    }
                }

                if(MyApplication.commentNavigation) {
                    if (mLayoutManager.findLastCompletelyVisibleItemPosition() == postAdapter.getData().size() - 1) {
                        layoutFab.setVisibility(View.GONE);
                    } else {
                        layoutFab.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        setCommentSort(MyApplication.defaultCommentSort); //TODO: change this for orientation changes
        //if(!currentlyLoading) {
            if (postAdapter == null) {
                //currentlyLoading = true;
                postAdapter = new PostAdapter(activity);

                if (loadFromList) postAdapter.add(post);
                else progressBar.setVisibility(View.VISIBLE);

                mRecyclerView.setAdapter(postAdapter);

                task = new LoadCommentsTask(activity, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mRecyclerView.setAdapter(postAdapter);
            }
        //}

        //if(!titleUpdated) setActionBarTitle(); //TODO: test for nullpointerexception

        return rootView;
    }

    private void initFabNavOptions(View view) {
        commentFabRoot = (RelativeLayout) view.findViewById(R.id.layout_comment_nav_root);
        if(MyApplication.commentNavigation) {
            commentFabRoot.setVisibility(View.VISIBLE);
            // TODO: 3/3/2017
        }
        else {
            commentFabRoot.setVisibility(View.GONE);
        }
    }

    private void initRecyclerView(View view) {
        // TODO: 3/3/2017
    }

    @Override public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        refreshComments();
    }

    private void nextParentComment() {
        int start = mLayoutManager.findFirstVisibleItemPosition();
        int index = postAdapter.findNextParentCommentIndex(start);
        if(index!=-1) {
            mLayoutManager.scrollToPositionWithOffset(index, 0);
        }
        if(mLayoutManager.findLastCompletelyVisibleItemPosition() == postAdapter.getData().size() - 1) {
            layoutFab.setVisibility(View.GONE);
        }
    }

    private void previousParentComment() {
        int start = mLayoutManager.findFirstVisibleItemPosition();
        int index = postAdapter.findPreviousParentCommentIndex(start);
        if(index!=-1) {
            mLayoutManager.scrollToPositionWithOffset(index, 0);
        }
    }

    public void loadFullComments() {
        commentLinkId = null;
        refreshComments();
    }

    public void refreshComments() {
        postAdapter.selectedPosition = -1;
        if(!commentsLoaded) task.cancel(true);
        //if(!noResponseObject) {
            commentsLoaded = false;
            postAdapter.commentsRefreshed(post, new ArrayList<Comment>());
        //}

        setActionBarSubtitle();
        task = new LoadCommentsTask(activity, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setCommentSort(CommentSort sort) {
        this.commentSort = sort;
    }

    public void setActionBarTitle() {
        if(!MainActivity.dualPaneActive || updateActionBar) {
            String title;
            if (post.getSubreddit() != null) {
                title = post.getSubreddit().toLowerCase();
                titleUpdated = true;
            } else {
                title = "Loading..";
                titleUpdated = false;
            }
            activity.getSupportActionBar().setTitle(title);
        }
    }

    public void setActionBarSubtitle() {
        if(!MainActivity.dualPaneActive || updateActionBar) {
            String subtitle;
            if (MyApplication.offlineModeEnabled) subtitle = getOfflineSubtitle();
            else {
                if(commentSort==null) commentSort = CommentSort.TOP;
                subtitle = commentSort.value();
            }
            activity.getSupportActionBar().setSubtitle(subtitle);
        }
    }

    private String getOfflineSubtitle() {
        File postFile = null;

        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.endsWith(post.getIdentifier())) return true;
                return false;
            }
        };
        File[] files = GeneralUtils.getActiveDir(activity).listFiles(filenameFilter);
        for(File file : files) {
            if(postFile == null) postFile = file;
            else if(file.lastModified() > postFile.lastModified()) postFile = file;
        }

        if(postFile!=null) {
            //return new Date(postFile.lastModified()).toString();
            return "synced " + ConvertUtils.getSubmissionAge((double) postFile.lastModified() / 1000);
        }
        return "not synced";
    }

}
