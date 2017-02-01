package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.SearchActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
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
            //if(adapter instanceof RedditItemListAdapter)
            post.setClicked(true);
            adapter.notifyItemChanged(position);
        }

        if(v.getId() == R.id.layout_postCommentsButton || post.isSelf()) {
            if(MainActivity.dualPaneActive) {
                PostFragment fragment = PostFragment.newInstance(post);
                addPostFragment(context, fragment);
            }
            else {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("post", post);
                context.startActivity(intent);
            }
        }
        else {
            //if(MainActivity.dualPaneActive && (post.getDomain().equals("reddit.com") || post.getDomain().substring(3).equals("reddit.com"))) {
            //    String url = post.getURL().toLowerCase();
            //    if(url.contains("/wiki/") || url.contains("/about/")) {
            //        LinkHandler linkHandler = new LinkHandler(context, post);
            //        linkHandler.handleIt();
            //    }
            //    else {
            //        String[] postInfo = LinkHandler.getRedditPostInfo(post.getURL());
            //        PostFragment fragment = PostFragment.newInstance(postInfo);
            //        addPostFragment(context, fragment);
            //    }
            //}
            //else {
                LinkHandler linkHandler = new LinkHandler(context, post);
                linkHandler.handleIt();
            //}
        }
    }

    private void addPostFragment(Context context, PostFragment fragment) {
        if(context instanceof MainActivity) ((MainActivity) context).setupPostFragment(fragment);
        else if(context instanceof SubredditActivity) ((SubredditActivity) context).setupPostFragment(fragment);
        else if(context instanceof UserActivity) ((UserActivity) context).setupPostFragment(fragment);
        else if(context instanceof SearchActivity) ((SearchActivity) context).setupPostFragment(fragment);
    }
}
