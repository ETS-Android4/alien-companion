package com.gDyejeekis.aliencompanion.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.gDyejeekis.aliencompanion.Activities.PostActivity;
import com.gDyejeekis.aliencompanion.Adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.Fragments.PostFragment;
import com.gDyejeekis.aliencompanion.Models.MoreComment;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 7/28/2016.
 */
public class LoadMoreCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

    private Exception exception;
    private PostActivity postActivity;
    private MoreComment moreChildren;

    public LoadMoreCommentsTask(PostActivity postActivity, MoreComment moreChildren) {
        this.postActivity = postActivity;
        this.moreChildren = moreChildren;
    }

    @Override
    protected List<Comment> doInBackground(Void... params) {
        try {
            SystemClock.sleep(2000);
            return new ArrayList<>();
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        if(exception!=null) {
            ToastUtils.displayShortToast(postActivity, "Error loading comments");
        }
        //else if(comments.size()==0) {
        //    ToastUtils.displayShortToast(postActivity, "Replies not found");
        //}
        else {
            PostAdapter postAdapter = postActivity.getPostFragment().postAdapter;
            int index = postAdapter.getData().indexOf(moreChildren);
            moreChildren.setLoadingMore(false);
            //postAdapter.notifyItemChanged(index);
            postAdapter.notifyDataSetChanged();
        }
    }
}
