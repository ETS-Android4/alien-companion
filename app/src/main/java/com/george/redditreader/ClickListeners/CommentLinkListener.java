package com.george.redditreader.ClickListeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.api.entity.Comment;

/**
 * Created by George on 6/30/2015.
 */
public class CommentLinkListener implements View.OnClickListener {

    private Activity activity;

    private Comment comment;

    public CommentLinkListener(Activity activity, Comment comment) {
        this.activity = activity;
        this.comment = comment;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(activity, PostActivity.class);
        String postInfo[] = {comment.getSubreddit(), comment.getLinkId().substring(3), comment.getIdentifier(), null};
        intent.putExtra("postInfo", postInfo);
        activity.startActivity(intent);
    }

}
