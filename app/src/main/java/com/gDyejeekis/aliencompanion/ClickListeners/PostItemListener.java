package com.gDyejeekis.aliencompanion.ClickListeners;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.PostActivity;
import com.gDyejeekis.aliencompanion.Activities.SearchActivity;
import com.gDyejeekis.aliencompanion.Activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.Activities.UserActivity;
import com.gDyejeekis.aliencompanion.Fragments.PostFragment;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;

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
            adapter.notifyItemChanged(position); //TODO: write clicked post name to a file (maybe)
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