package com.gDyejeekis.aliencompanion.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;
import com.gDyejeekis.aliencompanion.fragments.UserFragment;

/**
 * Created by George on 11/30/2017.
 */

public class RedditItemSubmittedReceiver extends BroadcastReceiver {

    public static final String TAG = "RedditItemReceiver";

    public static final String POST_SUBMISSION = "com.gDyejeekis.aliencompanion.POST_SUBMISSION";

    public static final String POST_SELF_TEXT_EDIT = "com.gDyejeekis.aliencompanion.POST_SELF_TEXT_EDIT";

    public static final String COMMENT_SUBMISSION = "com.gDyejeekis.aliencompanion.COMMENT_SUBMISSION";

    public static final String COMMENT_EDIT = "com.gDyejeekis.aliencompanion.COMMENT_EDIT";

    private Submission post;
    private Comment comment;
    private int commentIndex;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()==null) return;
        initFields(intent);
        switch (intent.getAction()) {
            case POST_SUBMISSION:
                if (validPostExtras()) {
                    Intent postIntent = new Intent(context, PostActivity.class);
                    postIntent.putExtra("post", post);
                    context.startActivity(postIntent);
                }
                break;
            case POST_SELF_TEXT_EDIT:
                if (validPostExtras() && context instanceof PostActivity) {
                    PostFragment postFragment = ((PostActivity) context).getPostFragment();
                    if (postFragment!=null) postFragment.itemEdited(0, post);
                }
                break;
            case COMMENT_SUBMISSION:
                if (validCommentExtras() && context instanceof PostActivity) {
                    PostFragment postFragment = ((PostActivity) context).getPostFragment();
                    if (postFragment!=null) postFragment.newCommentSubmitted(commentIndex, comment);
                }
                break;
            case COMMENT_EDIT:
                if (validCommentExtras()) {
                    if (context instanceof PostActivity) {
                        PostFragment postFragment = ((PostActivity) context).getPostFragment();
                        if (postFragment!=null) postFragment.itemEdited(commentIndex, comment);
                    } else if (context instanceof UserActivity){
                        UserFragment fragment = ((UserActivity) context).getListFragment();
                        if (fragment!=null) fragment.itemEdited(commentIndex, comment);
                    }
                }
                break;
        }
    }

    private void initFields(Intent intent) {
        post = (Submission) intent.getSerializableExtra("post");
        comment = (Comment) intent.getSerializableExtra("comment");
        commentIndex = intent.getIntExtra("position", -1);
    }

    private boolean validPostExtras() {
        return post != null;
    }

    private boolean validCommentExtras() {
        return comment != null && commentIndex != -1;
    }

}
