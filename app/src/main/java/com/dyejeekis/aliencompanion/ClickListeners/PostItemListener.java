package com.dyejeekis.aliencompanion.ClickListeners;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Activities.PostActivity;
import com.dyejeekis.aliencompanion.Activities.SearchActivity;
import com.dyejeekis.aliencompanion.Activities.SubredditActivity;
import com.dyejeekis.aliencompanion.Activities.UserActivity;
import com.dyejeekis.aliencompanion.Fragments.PostFragment;
import com.dyejeekis.aliencompanion.LinkHandler;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.api.entity.Submission;

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
        PostFragment.currentlyLoading = true;
        if(!post.isClicked() && position != -1) {
            post.setClicked(true);
            adapter.notifyItemChanged(position); //TODO: write clicked post name to a file
        }
        if(v.getId() == R.id.layout_postCommentsButton || post.isSelf()) {
            if(MainActivity.dualPaneActive) {
                PostFragment fragment = PostFragment.newInstance(post);
                if(context instanceof MainActivity) ((MainActivity) context).setupPostFragment(fragment);
                else if(context instanceof SubredditActivity) ((SubredditActivity) context).setupPostFragment(fragment);
                else if(context instanceof UserActivity) ((UserActivity) context).setupPostFragment(fragment);
                else if(context instanceof SearchActivity) ((SearchActivity) context).setupPostFragment(fragment);
            }
            else {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("post", post);
                context.startActivity(intent);
            }
        }
        else {
            LinkHandler linkHandler = new LinkHandler(context, post);
            linkHandler.handleIt();
        }
    }
}
