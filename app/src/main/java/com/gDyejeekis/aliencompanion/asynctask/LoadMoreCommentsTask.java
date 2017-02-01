package com.gDyejeekis.aliencompanion.asynctask;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.models.MoreComment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.retrieval.Comments;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.util.List;

/**
 * Created by George on 7/28/2016.
 */
public class LoadMoreCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

    private Exception exception;
    private AppCompatActivity activity;
    private PostFragment postFragment;
    private MoreComment moreChildren;

    public LoadMoreCommentsTask(AppCompatActivity activity, MoreComment moreChildren) {
        this.activity = activity;
        this.moreChildren = moreChildren;
        if(activity instanceof MainActivity) {
            postFragment = ((MainActivity)activity).getPostFragment();
        }
        else {
            postFragment = ((PostActivity) activity).getPostFragment();
        }
    }

    @Override
    protected List<Comment> doInBackground(Void... params) {
        try {
            Comments comments = new Comments(new PoliteRedditHttpClient(), MyApplication.currentUser);
            return comments.moreChildren(postFragment.post.getFullName(), moreChildren.getMoreCommentIds(),
                    postFragment.commentSort);
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        PostAdapter postAdapter = postFragment.postAdapter;
        int index = postAdapter.getData().indexOf(moreChildren);
        if(exception!=null) {
            ToastUtils.displayShortToast(activity, "Error loading comments");
            moreChildren.setLoadingMore(false);
            postAdapter.notifyItemChanged(index);
        }
        else if(comments.size()==0) {
            ToastUtils.displayShortToast(activity, "Replies not found");
            moreChildren.setLoadingMore(false);
            postAdapter.notifyItemChanged(index);
        }
        else {
            moreChildren.setLoadingMore(false);
            postAdapter.commentsAdded(moreChildren, comments);
        }
    }
}
