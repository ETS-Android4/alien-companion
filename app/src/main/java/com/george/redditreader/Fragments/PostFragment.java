package com.george.redditreader.Fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.Activities.SubmitActivity;
import com.george.redditreader.Adapters.PostAdapter;
import com.george.redditreader.ClickListeners.ShowMoreListener;
import com.george.redditreader.LoadTasks.LoadCommentsTask;
import com.george.redditreader.R;
import com.george.redditreader.Views.DividerItemDecoration;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.retrieval.params.CommentSort;
import com.george.redditreader.enums.SubmitType;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    //private static final String GROUPS_KEY = "groups_key";

    public PostAdapter postAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostActivity activity;
    public Submission post;
    public CommentSort commentSort;
    public ProgressBar progressBar;
    private boolean loadFromList;
    public boolean noResponseObject;
    public String commentLinkId;
    public int parentsShown = -1;
    private boolean titleUpdated = true;
    public boolean commentsLoaded;
    public boolean showFullCommentsButton;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        post = (Submission) activity.getIntent().getSerializableExtra("post");
        String[] postInfo = activity.getIntent().getStringArrayExtra("postInfo");
        loadFromList = (post != null);
        //loadFromLink = activity.getIntent().getBooleanExtra("loadFromLink", false);
        if(!loadFromList) {
            if(postInfo!=null) {
                post = new Submission(postInfo[1]);
                post.setSubreddit(postInfo[0]);
                commentLinkId = postInfo[2];
                if(commentLinkId != null) showFullCommentsButton = true;
                if (postInfo[3] != null) parentsShown = Integer.valueOf(postInfo[3]);
            }
            else {
                titleUpdated = false;
                post = new Submission(activity.getIntent().getStringExtra("postId"));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MainActivity.swipeRefresh) swipeRefreshLayout.setEnabled(true);
        else swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (PostActivity) activity;
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
        setActionBarSubtitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshComments();
                return true;
            case R.id.action_sort:
                showSortPopup(activity.findViewById(R.id.action_sort));
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

    private void showSortPopup(View v) {
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

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (MainActivity.swipeRefresh && mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0)
                    swipeRefreshLayout.setEnabled(true);
                else swipeRefreshLayout.setEnabled(false);
            }
        });

        setCommentSort(CommentSort.TOP);
        postAdapter = new PostAdapter(activity, this, this);

        if(loadFromList) {
            postAdapter.add(post);
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setAdapter(postAdapter);

        LoadCommentsTask task = new LoadCommentsTask(activity, this);
        task.execute();

        if(!titleUpdated) setActionBarTitle(); //TODO: test for nullpointerexception
        //if(postAdapter == null) {
        //    setCommentSort(CommentSort.TOP);
        //    postAdapter = new PostAdapter(activity, this, this);
//
        //    if(loadFromList) {
        //        postAdapter.add(post);
        //    }
        //    else {
        //        progressBar.setVisibility(View.VISIBLE);
        //    }
//
        //    mRecyclerView.setAdapter(postAdapter);
//
        //    LoadCommentsTask task = new LoadCommentsTask(activity, this);
        //    task.execute();
//
        //    if(!titleUpdated) setActionBarTitle(); //TODO: test for nullpointerexception
        //}
        //else {
        //    mRecyclerView.setAdapter(postAdapter);
        //}

        //if (savedInstanceState != null) {
        //    List<Integer> groups = savedInstanceState.getIntegerArrayList(GROUPS_KEY);
        //    postAdapter.restoreGroups(groups);
        //}

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
        if(!noResponseObject) {
            postAdapter.clear();
            postAdapter.add(post);
            postAdapter.notifyDataSetChanged();
            commentsLoaded = false;
        }
        //else {
        //    progressBar.setVisibility(View.VISIBLE);
        //}

        LoadCommentsTask task = new LoadCommentsTask(activity, this);
        task.execute();
        //progressBar.setVisibility(View.GONE);
    }

    public void setCommentSort(CommentSort sort) {
        this.commentSort = sort;
    }

    public void setActionBarTitle() {
        activity.getSupportActionBar().setTitle(post.getSubreddit());
    }

    public void setActionBarSubtitle() {
        activity.getSupportActionBar().setSubtitle(commentSort.value());
    }

}
