package com.gDyejeekis.aliencompanion.Fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
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

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.Adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.AsyncTasks.LoadCommentsTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Views.DividerItemDecoration;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {
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
    public LoadCommentsTask task;

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

        if(!MainActivity.dualPaneActive) {
            post = (Submission) activity.getIntent().getSerializableExtra("post");
            String[] postInfo = activity.getIntent().getStringArrayExtra("postInfo");
            loadFromList = (post != null);
            //loadFromLink = activity.getIntent().getBooleanExtra("loadFromLink", false);
            if (!loadFromList) {
                if (postInfo != null) {
                    post = new Submission(postInfo[1]);
                    post.setSubreddit(postInfo[0]);
                    commentLinkId = postInfo[2];
                    if (commentLinkId != null) showFullCommentsButton = true;
                    if (postInfo[3] != null) parentsShown = Integer.valueOf(postInfo[3]);
                } else {
                    titleUpdated = false;
                    post = new Submission(activity.getIntent().getStringExtra("postId"));
                }
            }
            if(MyApplication.dualPane && activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) MainActivity.dualPaneActive = true;
        }
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
        View rootView = inflater.inflate(R.layout.fragment_post_list, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_postList);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(MyApplication.currentColor);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (MyApplication.swipeRefresh && mLayoutManager.findFirstCompletelyVisibleItemPosition()==0) {
                    swipeRefreshLayout.setEnabled(true);
                }
                else swipeRefreshLayout.setEnabled(false);
            }
        });

        setCommentSort(MyApplication.defaultCommentSort); //TODO: change this for orientation changes
        //if(!currentlyLoading) {
            if (postAdapter == null) {
                //currentlyLoading = true;
                postAdapter = new PostAdapter(activity, this, this);

                if (loadFromList) postAdapter.add(post);
                else progressBar.setVisibility(View.VISIBLE);

                mRecyclerView.setAdapter(postAdapter);

                task = new LoadCommentsTask(activity, this);
                task.execute();
            } else {
                mRecyclerView.setAdapter(postAdapter);
            }
        //}

        //if(!titleUpdated) setActionBarTitle(); //TODO: test for nullpointerexception

        return rootView;
    }

    @Override public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        refreshComments();
    }

    @Override
    public void onClick(View v) {
        int position = mRecyclerView.getChildPosition(v);
        int previousPosition = postAdapter.selectedPosition;
        postAdapter.selectedPosition = -1;
        postAdapter.notifyItemChanged(previousPosition);
        postAdapter.toggleGroup(position);
    }

    @Override
    public boolean onLongClick(View v) {
        int position = mRecyclerView.getChildPosition(v);
        if(!postAdapter.getItemAt(position).isGroup()) {
            int previousPosition = postAdapter.selectedPosition;
            if (position == postAdapter.selectedPosition) postAdapter.selectedPosition = -1;
            else postAdapter.selectedPosition = position;
            postAdapter.notifyItemChanged(previousPosition);
            postAdapter.notifyItemChanged(postAdapter.selectedPosition);
            return true;
        }
        else {
            return false;
        }
    }

    public void loadFullComments() {
        commentLinkId = null;
        refreshComments();
    }

    public void refreshComments() {
        if(!commentsLoaded) task.cancel(true);
        //if(!noResponseObject) {
            commentsLoaded = false;
            postAdapter.commentsRefreshed(post, new ArrayList<Comment>());
        //}

        setActionBarSubtitle();
        task = new LoadCommentsTask(activity, this);
        task.execute();
    }

    public void setCommentSort(CommentSort sort) {
        this.commentSort = sort;
    }

    public void setActionBarTitle() {
        if(!MainActivity.dualPaneActive) {
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
        if(!MainActivity.dualPaneActive) {
            String subtitle;
            if (MyApplication.offlineModeEnabled) subtitle = "offline";
            else {
                if(commentSort==null) commentSort = CommentSort.TOP;
                subtitle = commentSort.value();
            }
            activity.getSupportActionBar().setSubtitle(subtitle);
        }
    }

}
