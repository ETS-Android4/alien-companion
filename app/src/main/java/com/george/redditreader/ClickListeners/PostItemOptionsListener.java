package com.george.redditreader.ClickListeners;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.Activities.SubredditActivity;
import com.george.redditreader.Activities.UserActivity;
import com.george.redditreader.Adapters.RedditItemListAdapter;
import com.george.redditreader.LoadTasks.LoadUserActionTask;
import com.george.redditreader.R;
import com.george.redditreader.Utils.ToastUtils;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.utils.ApiEndpointUtils;
import com.george.redditreader.enums.UserActionType;

/**
 * Created by George on 8/9/2015.
 */
public class PostItemOptionsListener implements View.OnClickListener {

    private Context context;
    private Submission post;
    private BaseAdapter adapter; //TODO: to be deleted
    private RecyclerView.Adapter recyclerAdapter;

    public PostItemOptionsListener(Context context, Submission post, BaseAdapter adapter) { //TODO: to be deleted
        this.context = context;
        this.post = post;
        this.adapter = adapter;
    }

    public PostItemOptionsListener(Context context, Submission post, RecyclerView.Adapter adapter) {
        this.context = context;
        this.post = post;
        this.recyclerAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_upvote:
                UserActionType actionType;
                LoadUserActionTask task;
                if(MainActivity.currentUser!=null) {
                    if (post.getLikes().equals("true")) {
                        post.setLikes("null");
                        post.setScore(post.getScore() - 1);
                        actionType = UserActionType.novote;
                    } else {
                        if (post.getLikes().equals("false")) post.setScore(post.getScore() + 2);
                        else post.setScore(post.getScore() + 1);
                        post.setLikes("true");
                        actionType = UserActionType.upvote;
                    }

                    if (adapter != null) adapter.notifyDataSetChanged();
                    else recyclerAdapter.notifyDataSetChanged();

                    task = new LoadUserActionTask(context, post.getFullName(), actionType);
                    task.execute();
                }
                else ToastUtils.displayShortToast(context, "Must be logged in to vote");
                break;
            case R.id.btn_downvote:
                if(MainActivity.currentUser!=null) {
                    if (post.getLikes().equals("false")) {
                        post.setLikes("null");
                        post.setScore(post.getScore() + 1);
                        actionType = UserActionType.novote;
                    } else {
                        if (post.getLikes().equals("true")) post.setScore(post.getScore() - 2);
                        else post.setScore(post.getScore() - 1);
                        post.setLikes("false");
                        actionType = UserActionType.downvote;
                    }

                    if (adapter != null) adapter.notifyDataSetChanged();
                    else recyclerAdapter.notifyDataSetChanged();

                    task = new LoadUserActionTask(context, post.getFullName(), actionType);
                    task.execute();
                }
                else ToastUtils.displayShortToast(context, "Must be logged in to vote");
                break;
            case R.id.btn_save:
                if(MainActivity.currentUser!=null) {
                    if (post.isSaved()) {
                        post.setSaved(false);
                        actionType = UserActionType.unsave;
                    } else {
                        post.setSaved(true);
                        actionType = UserActionType.save;
                    }

                    if (adapter != null) adapter.notifyDataSetChanged();
                    else recyclerAdapter.notifyDataSetChanged();

                    task = new LoadUserActionTask(context, post.getFullName(), actionType);
                    task.execute();
                }
                else ToastUtils.displayShortToast(context, "Must be logged in to save");
                break;
            case R.id.btn_hide:
                if(MainActivity.currentUser!=null) {
                    if (post.isHidden()) {
                        post.setHidden(false);
                        actionType = UserActionType.unhide;
                    } else {
                        post.setHidden(true);
                        actionType = UserActionType.hide;
                        RedditItemListAdapter redditItemListAdapter = (RedditItemListAdapter) recyclerAdapter;
                        redditItemListAdapter.remove(post);
                    }

                    if (adapter != null) adapter.notifyDataSetChanged();
                    else recyclerAdapter.notifyDataSetChanged();

                    task = new LoadUserActionTask(context, post.getFullName(), actionType);
                    task.execute();
                }
                else ToastUtils.displayShortToast(context, "Must be logged in to hide");
                break;
            case R.id.btn_view_user:
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("username", post.getAuthor());
                context.startActivity(intent);
                break;
            case R.id.btn_open_browser:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getURL()));
                context.startActivity(intent);
                break;
            case R.id.btn_more:
                showMoreOptionsPopup(v);
                break;
        }
    }

    private void showMoreOptionsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        //inflate the right menu layout
        String currentUser = (MainActivity.currentUser!=null) ? MainActivity.currentUser.getUsername() : "";
        if(post.getAuthor().equals(currentUser)) {
            if(post.isSelf()) popupMenu.inflate((R.menu.menu_self_post_more_options_account));
            else popupMenu.inflate(R.menu.menu_post_more_options_account);
        }
        else {
            if(post.isSelf()) popupMenu.inflate(R.menu.menu_self_post_more_options);
            else popupMenu.inflate(R.menu.menu_post_more_options);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        return true;
                    case R.id.action_delete:
                        RedditItemListAdapter redditItemListAdapter = (RedditItemListAdapter) recyclerAdapter;
                        redditItemListAdapter.remove(post);
                        LoadUserActionTask task = new LoadUserActionTask(context, post.getFullName(), UserActionType.delete);
                        task.execute();
                        return true;
                    case R.id.action_mark_nsfw: //TODO: show proper user action
                        UserActionType actionType;
                        if(post.isNSFW()) actionType = UserActionType.unmarkNSFW;
                        else actionType = UserActionType.markNSFW;
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.execute();
                        return true;
                    case R.id.action_copy_link:
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Post link", post.getURL());
                        clipboard.setPrimaryClip(clip);
                        return true;
                    case R.id.action_copy_permalink:
                        String postLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getFullName().substring(3);
                        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clip = ClipData.newPlainText("Post permalink", postLink);
                        clipboard.setPrimaryClip(clip);
                        return true;
                    case R.id.action_share:
                        postLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getFullName().substring(3);
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, postLink);
                        sendIntent.setType("text/plain");
                        context.startActivity(Intent.createChooser(sendIntent, "Share post to.."));
                        return true;
                    case R.id.action_view_subreddit:
                        Intent intent = new Intent(context, SubredditActivity.class);
                        intent.putExtra("subreddit", post.getSubreddit().toLowerCase());
                        context.startActivity(intent);
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
