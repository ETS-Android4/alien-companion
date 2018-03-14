package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;

/**
 * Created by George on 6/30/2015.
 */
public class CommentLinkListener implements View.OnClickListener {

    public static final int DEFAULT_COMMENT_PARENTS_SHOWN = 5;

    private Context context;
    private RecyclerView.ViewHolder viewHolder;
    private RedditItemListAdapter adapter;

    public CommentLinkListener(Context context, RecyclerView.ViewHolder viewHolder, RedditItemListAdapter adapter) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.adapter = adapter;
    }

    @Override
    public void onClick(View v) {
        Comment comment = (Comment) adapter.getItemAt(viewHolder.getAdapterPosition());
        String url = "https://wwww.reddit.com/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3) + "/title/" + comment.getIdentifier()
                + "/?context=" + DEFAULT_COMMENT_PARENTS_SHOWN;
        if(MyApplication.dualPaneActive) {
            PostFragment fragment = PostFragment.newInstance(url);
            ((UserActivity) context).setupPostFragment(fragment);
        }
        else {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("url", url);
            context.startActivity(intent);
        }
    }

}
