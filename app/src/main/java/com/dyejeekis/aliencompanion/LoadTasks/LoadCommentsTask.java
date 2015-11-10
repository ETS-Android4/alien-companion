package com.dyejeekis.aliencompanion.LoadTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Fragments.PostFragment;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.Utils.ImageLoader;
import com.dyejeekis.aliencompanion.api.entity.Comment;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.api.exception.RedditError;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.retrieval.Comments;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

    private Exception exception;
    private Context context;
    private PostFragment postFragment;
    private HttpClient httpClient = new PoliteRedditHttpClient();

    public LoadCommentsTask(Context context, PostFragment postFragment) {
        this.context = context;
        this.postFragment = postFragment;
        //this.httpClient = new PoliteRedditHttpClient();
    }

    private List<Comment> readCommentsFromFile(String filename) {
        //Log.d("Geo test", "reading comments from " + filename);
        try {
            exception = null;
            FileInputStream fis = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Submission post = (Submission) ois.readObject();
            return post.getSyncedComments();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected List<Comment> doInBackground(Void... unused) {
        try {
            List<Comment> comments;
            if(MainActivity.offlineModeEnabled) {
                String postId = postFragment.post.getIdentifier();
                String filename = postFragment.post.getSubreddit().toLowerCase() + postId;
                comments = readCommentsFromFile(filename);
                if(exception != null) comments = readCommentsFromFile("frontpage" + postId);
            }
            else {
                Comments cmnts = new Comments(httpClient, MainActivity.currentUser);
                int depth = (postFragment.commentLinkId!=null) ? 999 : MainActivity.initialCommentDepth;
                comments = cmnts.ofSubmission(postFragment.post, postFragment.commentLinkId, postFragment.parentsShown, depth,
                        MainActivity.initialCommentCount, postFragment.commentSort);

                if (postFragment.post.getThumbnailObject() == null) {
                    ImageLoader.preloadThumbnail(postFragment.post, context);
                }
            }
            Comments.indentCommentTree(context, comments);

            return comments;
        } catch (RetrievalFailedException | RedditError | NullPointerException e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        //PostFragment.currentlyLoading = false;
        try {
            PostFragment fragment = (PostFragment) ((Activity) context).getFragmentManager().findFragmentByTag("postFragment");
            postFragment = fragment;
            postFragment.progressBar.setVisibility(View.GONE);
            postFragment.commentsLoaded = true;

            if (exception != null) {
                postFragment.postAdapter.notifyItemChanged(0);
                if (exception instanceof IOException)
                    ToastUtils.displayShortToast(context, "No comments found");
                else {
                    postFragment.noResponseObject = true;
                    ToastUtils.commentsLoadError(context);
                }
            } else {
                if(!postFragment.titleUpdated) postFragment.setActionBarTitle();
                postFragment.noResponseObject = false;
                postFragment.postAdapter.commentsRefreshed(postFragment.post, comments);
            }
        } catch (NullPointerException e) {}
    }
}
