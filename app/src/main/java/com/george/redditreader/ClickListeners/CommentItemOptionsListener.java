package com.george.redditreader.ClickListeners;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;

import com.george.redditreader.Activities.SubredditActivity;
import com.george.redditreader.Activities.UserActivity;
import com.george.redditreader.LoadTasks.LoadUserActionTask;
import com.george.redditreader.R;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.enums.UserActionType;

/**
 * Created by George on 8/15/2015.
 */
public class CommentItemOptionsListener implements View.OnClickListener {

    //private Activity activity;
    private Context context;
    private Comment comment;
    private RecyclerView.Adapter recyclerAdapter;
    private BaseAdapter adapter;

    public CommentItemOptionsListener(Context context, Comment comment, BaseAdapter adapter) {
        this.context = context;
        this.comment = comment;
        this.adapter = adapter;
    }

    public CommentItemOptionsListener(Context context, Comment comment, RecyclerView.Adapter adapter) {
        this.context = context;
        this.comment = comment;
        this.recyclerAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upvote:
                UserActionType actionType;
                if(comment.getLikes().equals("true")) {
                    comment.setLikes("null");
                    comment.setScore(comment.getScore() - 1);
                    actionType = UserActionType.novote;
                }
                else {
                    if(comment.getLikes().equals("false")) comment.setScore(comment.getScore() + 2);
                    else comment.setScore(comment.getScore() + 1);
                    comment.setLikes("true");
                    actionType = UserActionType.upvote;
                }

                if(adapter != null) adapter.notifyDataSetChanged();
                else recyclerAdapter.notifyDataSetChanged();

                LoadUserActionTask task = new LoadUserActionTask(context, comment.getFullName(), actionType);
                task.execute();
                break;
            case R.id.btn_downvote:
                if(comment.getLikes().equals("false")) {
                    comment.setLikes("null");
                    comment.setScore(comment.getScore() + 1);
                    actionType = UserActionType.novote;
                }
                else {
                    if(comment.getLikes().equals("true")) comment.setScore(comment.getScore() - 2);
                    else comment.setScore(comment.getScore() - 1);
                    comment.setLikes("false");
                    actionType = UserActionType.downvote;
                }

                if(adapter != null) adapter.notifyDataSetChanged();
                else recyclerAdapter.notifyDataSetChanged();

                task = new LoadUserActionTask(context, comment.getFullName(), actionType);
                task.execute();
                break;
            case R.id.btn_reply:
                break;
            case R.id.btn_view_user:
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("username", comment.getAuthor());
                context.startActivity(intent);
                break;
            case R.id.btn_more:
                showMoreOptionsPopup(v);
                break;
        }
    }

    private void showMoreOptionsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
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
                        context.startActivity(intent);
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
