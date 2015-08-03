package com.george.redditreader.Fragments;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
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
import com.george.redditreader.Adapters.PostAdapter;
import com.george.redditreader.LoadTasks.LoadCommentsTask;
import com.george.redditreader.Models.Thumbnail;
import com.george.redditreader.Utils.DisplayToast;
import com.george.redditreader.LinkHandler;
import com.george.redditreader.R;
import com.george.redditreader.Utils.ImageLoader;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.Comments;
import com.george.redditreader.api.retrieval.params.CommentSort;
import com.george.redditreader.api.utils.RedditConstants;
import com.george.redditreader.api.utils.restClient.HttpRestClient;
import com.george.redditreader.api.utils.restClient.RestClient;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements View.OnClickListener {
    private static final String GROUPS_KEY = "groups_key";

    public PostAdapter postAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PostActivity activity;
    public Submission post;
    public CommentSort commentSort;
    public ProgressBar progressBar;
    private boolean loadFromList;
    public boolean noResponseObject;
    public static String commentLinkId;
    public int parentsShown = -1;
    private boolean titleUpdated = true;
    private List<Integer> groups;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        post = (Submission) activity.getIntent().getSerializableExtra("post");
        loadFromList = (post != null);
        if(!loadFromList) {
            if(activity.getIntent().getStringArrayExtra("postInfo")!=null) {
                String[] postInfo = activity.getIntent().getStringArrayExtra("postInfo");
                post = new Submission(postInfo[1]);
                post.setSubreddit(postInfo[0]);
                commentLinkId = postInfo[2];
                if(commentLinkId != null) MainActivity.showFullComments = true;
                if (postInfo[3] != null) parentsShown = Integer.valueOf(postInfo[3]);
            }
            else {
                titleUpdated = false;
                post = new Submission(activity.getIntent().getStringExtra("postId"));
            }
        }
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
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar_fullLoad);
        progressBar.setVisibility(View.GONE);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.content_recyclerview);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);

        if(postAdapter == null) {
            setCommentSort(CommentSort.TOP);
            postAdapter = new PostAdapter(activity, this);

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
        }
        else {
            mRecyclerView.setAdapter(postAdapter);
        }

        //if (savedInstanceState != null) {
        //    List<Integer> groups = savedInstanceState.getIntegerArrayList(GROUPS_KEY);
        //    postAdapter.restoreGroups(groups);
        //}

        return rootView;
    }

    //@Override
    //public void onSaveInstanceState(Bundle outState) {
    //    //Log.e("geo debug", "onsaveinstancestate called");
    //    outState.putIntegerArrayList(GROUPS_KEY, postAdapter.saveGroups());
    //    super.onSaveInstanceState(outState);
    //}

    //@Override
    //public void onPause() {
    //    super.onPause();
    //    groups = postAdapter.saveGroups();
    //}
//
    //@Override
    //public void onResume() {
    //    super.onResume();
    //    postAdapter.restoreGroups(groups);
    //}

    @Override
    public void onClick(View v) {
        int position = mRecyclerView.getChildPosition(v);
        if (postAdapter.getItemViewType(position) == PostAdapter.VIEW_TYPE_ITEM) {
            postAdapter.toggleGroup(position);
        }
        else if (postAdapter.getItemViewType(position) == PostAdapter.VIEW_TYPE_CONTENT) {
            if(!post.isSelf()) {
                LinkHandler linkHandler = new LinkHandler(activity, post);
                linkHandler.handleIt();
            }
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
            MainActivity.commentsLoaded = false;
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

    //class LoadCommentsTask extends AsyncTask<Void, Void, List<Comment>> {
//
    //    private Exception exception;
//
    //    @Override
    //    protected List<Comment> doInBackground(Void... unused) {
    //        try {
    //            Comments cmnts = new Comments(restClient);
    //            List<Comment> comments;
    //            comments = cmnts.ofSubmission(post, commentLinkId, parentsShown, RedditConstants.MAX_COMMENT_DEPTH, RedditConstants.MAX_LIMIT_COMMENTS, commentSort);
//
    //            if(post.getThumbnailObject() == null) {
    //                ImageLoader.preloadThumbnail(post, activity);
    //            }
    //            Comments.indentCommentTree(comments);
//
    //            return comments;
    //        } catch (RetrievalFailedException | RedditError e) {
    //            e.printStackTrace();
    //            exception = e;
    //        }
    //        return null;
    //    }
//
    //    @Override
    //    protected void onPostExecute(List<Comment> comments) {
    //        progressBar.setVisibility(View.GONE);
    //        if(exception != null) {
    //            noResponseObject = true;
    //            DisplayToast.commentsLoadError(activity);
    //        }
    //        else {
    //            noResponseObject = false;
    //            MainActivity.commentsLoaded = true;
    //            postAdapter.clear();
    //            postAdapter.add(post);
    //            postAdapter.addAll(comments);
    //            postAdapter.notifyDataSetChanged();
    //        }
    //    }
    //}

}
