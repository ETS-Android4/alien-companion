package com.george.redditreader.ClickListeners;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.Adapters.RedditItemListAdapter;
import com.george.redditreader.LinkHandler;
import com.george.redditreader.R;
import com.george.redditreader.api.entity.Submission;

import org.w3c.dom.Text;

/**
 * Created by George on 6/20/2015.
 */
public class PostItemListener implements View.OnClickListener {

    private Context context;
    private Submission post;
    private RecyclerView.Adapter adapter;
    private int position;

    public PostItemListener(Context context, Submission post) {
        this.context = context;
        this.post = post;
        position = -1;
    }

    public PostItemListener(Context context, Submission post, RecyclerView.Adapter adapter, int position) {
        this.context = context;
        this.post = post;
        this.adapter = adapter;
        this.position = position;
    }

    @Override
    public void onClick(View v) {
        if(!post.isClicked() && position != -1) {
            post.setClicked(true);
            adapter.notifyItemChanged(position); //TODO: write clicked post name to a file
        }
        if(v.getId() == R.id.layout_postCommentsButton || post.isSelf()) {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("post", post);
            context.startActivity(intent);
        }
        else {
            LinkHandler linkHandler = new LinkHandler(context, post);
            linkHandler.handleIt();
        }
    }
}
