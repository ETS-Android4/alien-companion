package com.george.redditreader.ClickListeners;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;

import com.george.redditreader.Activities.SubredditActivity;
import com.george.redditreader.Activities.UserActivity;
import com.george.redditreader.Adapters.PostListAdapter;
import com.george.redditreader.LoadTasks.LoadUserActionTask;
import com.george.redditreader.R;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.enums.UserActionType;
import com.george.redditreader.multilevelexpindlistview.MultiLevelExpIndListAdapter;

/**
 * Created by George on 8/9/2015.
 */
public class PostItemOptionsListener implements View.OnClickListener {

    private Activity activity;
    private Submission post;
    private BaseAdapter adapter;
    private RecyclerView.Adapter recyclerAdapter;

    public PostItemOptionsListener(Activity activity, Submission post, BaseAdapter adapter) {
        this.activity = activity;
        this.post = post;
        this.adapter = adapter;
    }

    public PostItemOptionsListener(Activity activity, Submission post, RecyclerView.Adapter adapter) {
        this.activity = activity;
        this.post = post;
        this.recyclerAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upvote:
                UserActionType actionType;
                if(post.getLikes().equals("true")) {
                    post.setLikes("null");
                    post.setScore(post.getScore() - 1);
                    actionType = UserActionType.novote;
                }
                else {
                    if(post.getLikes().equals("false")) post.setScore(post.getScore() + 2);
                    else post.setScore(post.getScore() + 1);
                    post.setLikes("true");
                    actionType = UserActionType.upvote;
                }

                if(adapter != null) adapter.notifyDataSetChanged();
                else recyclerAdapter.notifyDataSetChanged();

                LoadUserActionTask task = new LoadUserActionTask(activity, post.getFullName(), actionType);
                task.execute();
                break;
            case R.id.btn_downvote:
                if(post.getLikes().equals("false")) {
                    post.setLikes("null");
                    post.setScore(post.getScore() + 1);
                    actionType = UserActionType.novote;
                }
                else {
                    if(post.getLikes().equals("true")) post.setScore(post.getScore() - 2);
                    else post.setScore(post.getScore() - 1);
                    post.setLikes("false");
                    actionType = UserActionType.downvote;
                }

                if(adapter != null) adapter.notifyDataSetChanged();
                else recyclerAdapter.notifyDataSetChanged();

                task = new LoadUserActionTask(activity, post.getFullName(), actionType);
                task.execute();
                break;
            case R.id.btn_save:
                if(post.isSaved()) {
                    post.setSaved(false);
                    actionType = UserActionType.unsave;
                }
                else {
                    post.setSaved(true);
                    actionType = UserActionType.save;
                }

                if(adapter != null) adapter.notifyDataSetChanged();
                else recyclerAdapter.notifyDataSetChanged();

                task = new LoadUserActionTask(activity, post.getFullName(), actionType);
                task.execute();
                break;
            case R.id.btn_hide:
                if(post.isHidden()) {
                    post.setHidden(false);
                    actionType = UserActionType.unhide;
                }
                else {
                    post.setHidden(true);
                    actionType = UserActionType.hide;
                    PostListAdapter postListAdapter = (PostListAdapter) adapter;
                    postListAdapter.remove(post);
                    postListAdapter.selectedPosition = -1;
                }

                if(adapter != null) adapter.notifyDataSetChanged();
                else recyclerAdapter.notifyDataSetChanged();

                task = new LoadUserActionTask(activity, post.getFullName(), actionType);
                task.execute();
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
                        intent.putExtra("subreddit", post.getSubreddit().toLowerCase());
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
