package com.dyejeekis.aliencompanion.LoadTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Fragments.PostFragment;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.Utils.ImageLoader;
import com.dyejeekis.aliencompanion.api.entity.Comment;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.retrieval.Comments;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;

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
            comments = cmnts.ofSubmission(postFragment.post, postFragment.commentLinkId, postFragment.parentsShown, MainActivity.initialCommentDepth,
                    MainActivity.initialCommentCount, postFragment.commentSort);

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
