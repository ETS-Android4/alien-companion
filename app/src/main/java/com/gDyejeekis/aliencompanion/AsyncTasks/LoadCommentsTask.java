package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.Utils.ImageLoader;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Comments;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.FileInputStream;
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
            if(MyApplication.offlineModeEnabled) {
                String postId = postFragment.post.getIdentifier();
                String filename = postFragment.post.getSubreddit().toLowerCase() + postId;
                comments = readCommentsFromFile(filename);
                if(exception != null) comments = readCommentsFromFile("frontpage" + postId);
            }
            else {
                Comments cmnts = new Comments(httpClient, MyApplication.currentUser);
                int depth = (postFragment.commentLinkId!=null) ? 999 : MyApplication.initialCommentDepth;
                comments = cmnts.ofSubmission(postFragment.post, postFragment.commentLinkId, postFragment.parentsShown, depth,
                        MyApplication.initialCommentCount, postFragment.commentSort);

                //if (postFragment.post.getThumbnailObject() == null) {
                //    ImageLoader.preloadThumbnail(postFragment.post, context);
                //}
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
            if (postFragment.post.getThumbnailObject() == null) {
                ImageLoader.preloadThumbnail(postFragment.post, context);
            }

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
