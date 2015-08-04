package com.george.redditreader.LoadTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Fragments.PostFragment;
import com.george.redditreader.Utils.DisplayToast;
import com.george.redditreader.Utils.ImageLoader;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.Comments;
import com.george.redditreader.api.utils.RedditConstants;
import com.george.redditreader.api.utils.restClient.HttpRestClient;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

    private Exception exception;
    private Context context;
    private PostFragment postFragment;
    private HttpRestClient restClient;

    public LoadCommentsTask(Context context, PostFragment postFragment) {
        this.context = context;
        this.postFragment = postFragment;
        this.restClient = new HttpRestClient();
    }

    @Override
    protected List<Comment> doInBackground(Void... unused) {
        try {
            Comments cmnts = new Comments(restClient);
            List<Comment> comments;
            comments = cmnts.ofSubmission(postFragment.post, PostFragment.commentLinkId, postFragment.parentsShown, RedditConstants.MAX_COMMENT_DEPTH, RedditConstants.MAX_LIMIT_COMMENTS, postFragment.commentSort);

            if(postFragment.post.getThumbnailObject() == null) {
                ImageLoader.preloadThumbnail(postFragment.post, context);
            }
            Comments.indentCommentTree(comments);

            return comments;
        } catch (RetrievalFailedException | RedditError | NullPointerException e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        postFragment.progressBar.setVisibility(View.GONE);
        if(exception != null) {
            postFragment.noResponseObject = true;
            DisplayToast.commentsLoadError(context);
        }
        else {
            postFragment.noResponseObject = false;
            MainActivity.commentsLoaded = true;
            postFragment.postAdapter.clear();
            postFragment.postAdapter.add(postFragment.post);
            postFragment.postAdapter.addAll(comments);
            postFragment.postAdapter.notifyDataSetChanged();
        }
    }
}
