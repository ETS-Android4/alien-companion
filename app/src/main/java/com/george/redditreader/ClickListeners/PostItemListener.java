package com.george.redditreader.ClickListeners;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.LinkHandler;
import com.george.redditreader.R;
import com.george.redditreader.api.entity.Submission;

/**
 * Created by George on 6/20/2015.
 */
public class PostItemListener implements View.OnClickListener {

    private Context context;
    private Submission post;

    public PostItemListener(Context context, Submission post) {
        this.context = context;
        this.post = post;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.commentsButton || v.getId() == R.id.layout_postCommentsButton || post.isSelf()) {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("post", post);
            context.startActivity(intent);
        }
        else {
            //Log.d("Clicks", "Post number " + v.getTag() + " ,loading post content...");
            LinkHandler linkHandler = new LinkHandler(context, post);
            linkHandler.handleIt();
        }
    }
}
