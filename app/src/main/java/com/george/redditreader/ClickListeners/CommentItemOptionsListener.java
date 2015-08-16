package com.george.redditreader.ClickListeners;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.george.redditreader.Activities.SubredditActivity;
import com.george.redditreader.Activities.UserActivity;
import com.george.redditreader.R;
import com.george.redditreader.api.entity.Comment;

/**
 * Created by George on 8/15/2015.
 */
public class CommentItemOptionsListener implements View.OnClickListener {

    private Activity activity;
    private Comment comment;

    public CommentItemOptionsListener(Activity activity, Comment comment) {
        this.activity = activity;
        this.comment = comment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upvote:
                break;
            case R.id.btn_downvote:
                break;
            case R.id.btn_reply:
                break;
            case R.id.btn_view_user:
                Intent intent = new Intent(activity, UserActivity.class);
                intent.putExtra("username", comment.getAuthor());
                activity.startActivity(intent);
                break;
            case R.id.btn_more:
                showMoreOptionsPopup(v);
                break;
        }
    }

    private void showMoreOptionsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_comment_more_options);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_copy_link:
                        String commentLink = "http://www.reddit.com/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3)
                                + "?comment=" + comment.getIdentifier();
                        return true;
                    case R.id.action_copy_text:
                        return true;
                    case R.id.action_open_browser:
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3)
                        + "?comment=" + comment.getIdentifier()));
                        activity.startActivity(intent);
                        return true;
                    case R.id.action_save:
                        return true;
                    case R.id.action_report:
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }
}
