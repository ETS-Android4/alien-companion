package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.SearchActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;

/**
 * Created by George on 6/20/2015.
 */
public class PostItemListener implements View.OnClickListener {

    private Context context;
    private RecyclerView.ViewHolder viewHolder;
    private RecyclerView.Adapter adapter;

    public PostItemListener(Context context, RecyclerView.ViewHolder viewHolder, RecyclerView.Adapter adapter) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.adapter = adapter;
    }

    private Submission getCurrentPost() {
        if (adapter instanceof RedditItemListAdapter)
            return (Submission) ((RedditItemListAdapter) adapter).getItemAt(viewHolder.getAdapterPosition());
        else if (adapter instanceof PostAdapter)
            return (Submission) ((PostAdapter) adapter).getItemAt(0);
        return null;
    }

    @Override
    public void onClick(View v) {
        Submission post = getCurrentPost();
        if(!post.isClicked()) {
            //if(adapter instanceof RedditItemListAdapter)
            post.setClicked(true);
            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }

        if(v.getId() == R.id.layout_postCommentsButton || post.isSelf()) {
            openComments(context, post);
        }
        else {
            LinkHandler linkHandler = new LinkHandler(context, post);
            linkHandler.handleIt();
        }
    }

    public static void openComments(Context context, Submission post) {
        if(MyApplication.dualPaneActive) {
            PostFragment fragment = PostFragment.newInstance(post);
            addPostFragment(context, fragment);
        }
        else {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("post", post);
            context.startActivity(intent);
        }
    }

    private static void addPostFragment(Context context, PostFragment fragment) {
        if(context instanceof MainActivity) ((MainActivity) context).setupPostFragment(fragment);
        else if(context instanceof SubredditActivity) ((SubredditActivity) context).setupPostFragment(fragment);
        else if(context instanceof UserActivity) ((UserActivity) context).setupPostFragment(fragment);
        else if(context instanceof SearchActivity) ((SearchActivity) context).setupPostFragment(fragment);
    }
}
