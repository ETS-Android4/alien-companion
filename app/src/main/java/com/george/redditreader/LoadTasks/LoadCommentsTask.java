package com.george.redditreader.LoadTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Fragments.PostFragment;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.Utils.ImageLoader;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.api.exception.RedditError;
import com.george.redditreader.api.exception.RetrievalFailedException;
import com.george.redditreader.api.retrieval.Comments;
import com.george.redditreader.api.utils.RedditConstants;
import com.george.redditreader.api.utils.httpClient.HttpClient;
import com.george.redditreader.api.utils.httpClient.RedditHttpClient;

import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

    private Exception exception;
    private Context context;
    private PostFragment postFragment;
    private HttpClient httpClient;

    public LoadCommentsTask(Context context, PostFragment postFragment) {
        this.context = context;
        this.postFragment = postFragment;
        this.httpClient = new RedditHttpClient();
    }

    @Override
    protected List<Comment> doInBackground(Void... unused) {
        try {
            Comments cmnts = new Comments(httpClient, MainActivity.currentUser);
            List<Comment> comments;
            comments = cmnts.ofSubmission(postFragment.post, PostFragment.commentLinkId, postFragment.parentsShown, RedditConstants.MAX_COMMENT_DEPTH,
                    Integer.parseInt(MainActivity.prefs.getString("initialComments", "100")), postFragment.commentSort);

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
            ToastUtils.commentsLoadError(context);
        }
        else {
            postFragment.noResponseObject = false;
            postFragment.commentsLoaded = true;
            postFragment.postAdapter.clear();
            postFragment.postAdapter.add(postFragment.post);
            postFragment.postAdapter.addAll(comments);
            postFragment.postAdapter.notifyDataSetChanged();
        }
    }
}
