package com.george.redditreader.ClickListeners;

import android.app.Activity;
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

    private Activity activity;
    //private List<Submission> posts;
    //private List<Thumbnail> thumbnails;
    private Submission post;
    //private Thumbnail thumbnail;

    //public PostItemListener(Activity activity, List<Submission> posts, List<Thumbnail> thumbnails) {
    //    this.activity = activity;
    //    this.posts = posts;
    //    this.thumbnails = thumbnails;
    //}

    public PostItemListener(Activity activity, Submission post) {
        this.activity = activity;
        this.post = post;
        //this.thumbnail = thumbnail;
    }

    @Override
    public void onClick(View v) {
        //Submission post = posts.get((Integer) v.getTag());

        if(v.getId() == R.id.commentsButton || post.isSelf()) {
            Intent intent = new Intent(activity, PostActivity.class);
            intent.putExtra("post", post);
            //if(!post.isSelf() && thumbnail.hasThumbnail()) {
            //    String thumbUrl = thumbnail.getUrl();
            //    intent.putExtra("thumbUrl", thumbUrl);
            //}
            //Log.d("Clicks", "Post number " + v.getTag() + " ,taking you to post page...");
            activity.startActivity(intent);
        }
        else {
            //Log.d("Clicks", "Post number " + v.getTag() + " ,loading post content...");
            LinkHandler linkHandler = new LinkHandler(activity, post);
            linkHandler.handleIt();
        }
    }
}
