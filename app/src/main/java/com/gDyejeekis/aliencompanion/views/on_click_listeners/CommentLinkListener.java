package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.api.entity.Comment;

/**
 * Created by George on 6/30/2015.
 */
public class CommentLinkListener implements View.OnClickListener {

    public static final int DEFAULT_COMMENT_PARENTS_SHOWN = 5;

    private Context context;
    private Comment comment;

    public CommentLinkListener(Context context, Comment comment) {
        this.context = context;
        this.comment = comment;
    }

    @Override
    public void onClick(View v) {
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
