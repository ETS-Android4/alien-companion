package com.dyejeekis.aliencompanion.ClickListeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Activities.PostActivity;
import com.dyejeekis.aliencompanion.Activities.UserActivity;
import com.dyejeekis.aliencompanion.Fragments.PostFragment;
import com.dyejeekis.aliencompanion.api.entity.Comment;

/**
 * Created by George on 6/30/2015.
 */
public class CommentLinkListener implements View.OnClickListener {

    //private Activity activity;
    private Context context;
    private Comment comment;

    public CommentLinkListener(Context context, Comment comment) {
        //this.activity = activity;
        this.context = context;
        this.comment = comment;
    }

    @Override
    public void onClick(View v) {
        String postInfo[] = {comment.getSubreddit(), comment.getLinkId().substring(3), comment.getIdentifier(), "5"};
        if(MainActivity.dualPaneActive) {
            PostFragment fragment = PostFragment.newInstance(postInfo);
            ((UserActivity) context).setupPostFragment(fragment);
        }
        else {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("postInfo", postInfo);
            context.startActivity(intent);
        }
    }

}
