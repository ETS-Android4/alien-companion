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
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.api.entity.Submission;

/**
 * Created by George on 8/9/2015.
 */
public class PostItemOptionsListener implements View.OnClickListener {

    private Activity activity;
    private Submission post;

    public PostItemOptionsListener(Activity activity, Submission post) {
        this.activity = activity;
        this.post = post;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upvote:
                break;
            case R.id.btn_downvote:
                break;
            case R.id.btn_save:
                break;
            case R.id.btn_hide:
                break;
            case R.id.btn_view_user:
                Intent intent = new Intent(activity, UserActivity.class);
                intent.putExtra("username", post.getAuthor());
                activity.startActivity(intent);
                break;
            case R.id.btn_open_browser:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getURL()));
                activity.startActivity(intent);
                break;
            case R.id.btn_more:
                showMoreOptionsPopup(v);
                break;
        }
    }

    private void showMoreOptionsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(activity, v);
        popupMenu.inflate(R.menu.menu_post_more_options);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_copy_link:
                        return true;
                    case R.id.action_share:
                        return true;
                    case R.id.action_view_subreddit:
                        Intent intent = new Intent(activity, SubredditActivity.class);
                        intent.putExtra("subreddit", post.getSubreddit());
                        activity.startActivity(intent);
                        return true;
                    case R.id.action_download_comments:
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
