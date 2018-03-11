package com.gDyejeekis.aliencompanion.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.UserFragment;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;

/**
 * Created by George on 11/30/2017.
 */

public class CommentSubmittedReceiver extends BroadcastReceiver {

    public static final String TAG = "CommentSubmtReceiver";

    public static final String COMMENT_SUBMISSION = "com.gDyejeekis.aliencompanion.COMMENT_SUBMISSION";

    public static final String COMMENT_EDIT = "com.gDyejeekis.aliencompanion.COMMENT_EDIT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == null) return;
        Comment comment = (Comment) intent.getSerializableExtra("comment");
        int position = intent.getIntExtra("position", -1);

        if (context instanceof PostActivity) {
            PostFragment fragment = ((PostActivity) context).getPostFragment();
            if (fragment != null) {
                if (comment != null && position != -1) {
                    fragment.postAdapter.selectedPosition = -1;
                    fragment.postAdapter.notifyDataSetChanged();
                    if (intent.getAction().equals(COMMENT_SUBMISSION)) {
                        updateCommentIndentation(comment, fragment.postAdapter, position);
                        fragment.postAdapter.add(position, comment);
                        fragment.mLayoutManager.scrollToPosition(position);
                    } else if (intent.getAction().equals(COMMENT_EDIT)) {
                        fragment.postAdapter.updateItem(position, comment);
                    }
                }
            }
        } else if (context instanceof UserActivity && intent.getAction().equals(COMMENT_EDIT)) {
            UserFragment fragment = ((UserActivity) context).getListFragment();
            if (fragment != null) {
                if (comment != null && position != -1) {
                    fragment.adapter.updateItem(position, comment);
                }
            }
        }
    }

    private void updateCommentIndentation(Comment comment, PostAdapter adapter, int position) {
        try {
            RedditItem aboveItem = (RedditItem) adapter.getItemAt(position - 1);
            if (aboveItem instanceof Comment) {
                Comment aboveComment = (Comment) aboveItem;
                if (comment.getParentId().equals(aboveComment.getFullName())) {
                    comment.setIndentation(aboveComment.getIndentation() + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
