package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.utils.ImageLoader;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.retrieval.Comments;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 8/1/2015.
 */
public class LoadCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

    public static final String TAG = "LoadCommentsTask";

    private Exception exception;
    private Context context;
    private PostFragment postFragment;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private boolean initialLoad;

    public LoadCommentsTask(Context context, PostFragment postFragment, boolean initialLoad) {
        this.context = context;
        this.postFragment = postFragment;
        this.initialLoad =  initialLoad;
    }

    private Submission readPostFromFile(final String postId) {
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.equals(postId);
            }
        };

        try {
            File postFile = null;
            List<File> files = new ArrayList<>();
            StorageUtils.listFilesRecursive(GeneralUtils.getSyncedRedditDataDir(context),
                    filenameFilter, files);
            for(File file : files) {
                // always keep the last updated matching post file
                if(postFile == null || file.lastModified() > postFile.lastModified())
                    postFile = file;
            }

            Log.d(TAG, "reading comments from " + postFile.getAbsolutePath());
            FileInputStream fis = new FileInputStream(postFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Submission post = (Submission) ois.readObject();
            return post;
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected List<Comment> doInBackground(Void... unused) {
        try {
            List<Comment> comments;
            //Log.d("GEOTEST", "RETRIEVING COMMENTS");
            if(MyApplication.offlineModeEnabled) {
                Submission post = readPostFromFile(postFragment.post.getIdentifier());
                if(post==null) {
                    throw new IOException("File not found ("+postFragment.post.getIdentifier()+")");
                }
                else {
                    postFragment.showFullCommentsButton = false;
                    String thumbUri = postFragment.post.getThumbnail();
                    postFragment.post = post;
                    postFragment.post.setThumbnail(thumbUri);
                    comments = post.getSyncedComments();
                }
            }
            else {
                Comments cmnts = new Comments(httpClient, MyApplication.currentUser);
                int depth = (postFragment.commentLinkId!=null) ? 999 : MyApplication.initialCommentDepth;
                comments = cmnts.ofSubmission(postFragment.post, postFragment.commentLinkId, postFragment.parentsShown, depth,
                        MyApplication.initialCommentCount, postFragment.commentSort);
            }
            //Log.d("GEOTEST", "RETRIEVAL COMPLETE");
            //Log.d("GEOTEST", "INDENTING COMMENTS");
            Comments.indentCommentTree(comments);
            //Log.d("GEOTEST", "WE GUCCI");

            return comments;
        } catch (RetrievalFailedException | RedditError | NullPointerException | IOException e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        if(MyApplication.accountChanges) {
            MyApplication.accountChanges = false;
            GeneralUtils.saveAccountChanges(context);
        }
        //PostFragment.currentlyLoading = false;
        try {
            PostFragment fragment = (PostFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("postFragment");
            postFragment = fragment;
            postFragment.progressBar.setVisibility(View.GONE);
            postFragment.swipeRefreshLayout.setRefreshing(false);
            postFragment.commentsLoaded = true;
            if (!MyApplication.noThumbnails && postFragment.post.getThumbnailObject() == null) {
                ImageLoader.preloadThumbnail(postFragment.post, context);
            } //TODO: monitor this for any exceptions

            if (exception != null) {
                postFragment.postAdapter.notifyItemChanged(0);
                if (exception instanceof IOException) {
                    ToastUtils.showSnackbar(postFragment.getSnackbarParentView(), "No synced comments found");
                }
                else {
                    postFragment.noResponseObject = true;
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            postFragment.refreshPostAndComments();
                        }
                    };
                    postFragment.setSnackbar(ToastUtils.showSnackbar(postFragment.getSnackbarParentView(), "Error loading comments", "Retry", listener, Snackbar.LENGTH_INDEFINITE));
                }
            }
            else {
                if(!postFragment.titleUpdated) postFragment.setActionBarTitle();
                postFragment.noResponseObject = false;
                postFragment.postAdapter.commentsRefreshed(postFragment.post, comments);
                if(!initialLoad) {
                    postFragment.mLayoutManager.scrollToPosition(0);
                    postFragment.setCommentSort(postFragment.tempSort);
                    postFragment.setActionBarSubtitle();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
