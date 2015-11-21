package com.gDyejeekis.aliencompanion.ClickListeners;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.Activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.Activities.UserActivity;
import com.gDyejeekis.aliencompanion.Adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.Adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ReportDialogFragment;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.TwoOptionDialogFragment;
import com.gDyejeekis.aliencompanion.LoadTasks.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

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

    private void viewUser() {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra("username", post.getAuthor());
        context.startActivity(intent);
    }

    private void openInBrowser() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getURL()));
        context.startActivity(intent);
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
                viewUser();
                break;
            case R.id.btn_open_browser:
                openInBrowser();
                break;
            case R.id.btn_more:
                showMoreOptionsPopup(v);
                break;
        }
    }

    private void showMoreOptionsPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        //inflate the right menu layout
        int resource;
        int labelNSFWindex = -1;
        String currentUser = (MainActivity.currentUser!=null) ? MainActivity.currentUser.getUsername() : "";
        if(post.getAuthor().equals(currentUser)) {
            if(post.isSelf()) {
                labelNSFWindex = 2;
                if(MainActivity.currentPostListView == R.layout.post_list_item_card || MainActivity.currentPostListView == R.layout.post_list_item_small_card
                        || recyclerAdapter instanceof PostAdapter) resource = R.menu.menu_self_post_card_more_options_account;
                else resource = R.menu.menu_self_post_more_options_account;
            //resource = (MainActivity.currentPostListView == R.layout.post_list_item_card || recyclerAdapter instanceof PostAdapter) ? R.menu.menu_self_post_card_more_options_account : R.menu.menu_self_post_more_options_account;
            }
            else {
                labelNSFWindex = 1;
                if(MainActivity.currentPostListView == R.layout.post_list_item_card || MainActivity.currentPostListView == R.layout.post_list_item_small_card)
                    resource = R.menu.menu_post_card_more_options_account;
                else resource = R.menu.menu_post_more_options_account;
            //resource = (MainActivity.currentPostListView == R.layout.post_list_item_card) ? R.menu.menu_post_card_more_options_account : R.menu.menu_post_more_options_account;
            }
        }
        else {
            if(post.isSelf()) resource = (MainActivity.currentPostListView == R.layout.post_list_item_card || MainActivity.currentPostListView == R.layout.post_list_item_small_card
                    || recyclerAdapter instanceof PostAdapter) ? R.menu.menu_self_post_card_more_options : R.menu.menu_self_post_more_options;
            else resource = (MainActivity.currentPostListView == R.layout.post_list_item_card || MainActivity.currentPostListView == R.layout.post_list_item_small_card || recyclerAdapter instanceof PostAdapter)
                    ? R.menu.menu_post_card_more_options : R.menu.menu_post_more_options;
        }
        popupMenu.inflate(resource);
        if(labelNSFWindex != -1 && post.isNSFW()) popupMenu.getMenu().getItem(labelNSFWindex).setTitle("Unmark NSFW");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_copy_to_clipboard:
                        TwoOptionDialogFragment choiceDialog = TwoOptionDialogFragment.newInstance("POST LINK", "COMMENTS URL",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String stringToCopy;
                                        String label;
                                        if (v.getId() == R.id.button_option_one) {
                                            stringToCopy = post.getURL();
                                            label = "Post url";
                                        } else {
                                            stringToCopy = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getIdentifier();
                                            label = "Comments url";
                                        }
                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText(label, stringToCopy);
                                        clipboard.setPrimaryClip(clip);
                                    }
                                });
                        choiceDialog.show(((Activity) context).getFragmentManager(), "dialog");
                        return true;
                    case R.id.action_edit:
                        Intent intent = new Intent(context, SubmitActivity.class);
                        intent.putExtra("submitType", SubmitType.comment);
                        intent.putExtra("selfText", post.getSelftext());
                        intent.putExtra("edit", true);
                        intent.putExtra("postName", post.getFullName());
                        context.startActivity(intent);
                        return true;
                    case R.id.action_delete:
                        if (recyclerAdapter instanceof RedditItemListAdapter)
                            ((RedditItemListAdapter) recyclerAdapter).remove(post);
                        LoadUserActionTask task = new LoadUserActionTask(context, post.getFullName(), UserActionType.delete);
                        task.execute();
                        return true;
                    case R.id.action_mark_nsfw:
                        UserActionType actionType;
                        if (post.isNSFW()) {
                            post.setNSFW(false);
                            actionType = UserActionType.unmarkNSFW;
                        }
                        else {
                            post.setNSFW(true);
                            actionType = UserActionType.markNSFW;
                        }
                        recyclerAdapter.notifyDataSetChanged();
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.execute();
                        return true;
                    case R.id.action_copy_link:
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Post link", post.getURL());
                        clipboard.setPrimaryClip(clip);
                        return true;
                    case R.id.action_copy_permalink:
                        String postLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getIdentifier();
                        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clip = ClipData.newPlainText("Post permalink", postLink);
                        clipboard.setPrimaryClip(clip);
                        return true;
                    case R.id.action_open_browser:
                        openInBrowser();
                        return true;
                    case R.id.action_share:
                        final String commentsUrl = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getIdentifier();
                        if(post.isSelf()) GeneralUtils.shareUrl(context, "Share self-post url to..", commentsUrl);
                        else {
                            choiceDialog = TwoOptionDialogFragment.newInstance("SHARE LINK", "SHARE COMMENTS", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String label;
                                    String url;
                                    if (v.getId() == R.id.button_option_one) {
                                        label = "Share post url to..";
                                        url = post.getURL();
                                    } else {
                                        label = "Share comments url to..";
                                        url = commentsUrl;
                                    }
                                    GeneralUtils.shareUrl(context, label, url);
                                }
                            });
                            choiceDialog.show(((Activity) context).getFragmentManager(), "dialog");
                        }
                        //postLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getFullName().substring(3);
                        //Intent sendIntent = new Intent();
                        //sendIntent.setAction(Intent.ACTION_SEND);
                        //sendIntent.putExtra(Intent.EXTRA_TEXT, postLink);
                        //sendIntent.setType("text/plain");
                        //context.startActivity(Intent.createChooser(sendIntent, "Share post to.."));
                        return true;
                    case R.id.action_view_user:
                        viewUser();
                        return true;
                    case R.id.action_view_subreddit:
                        intent = new Intent(context, SubredditActivity.class);
                        intent.putExtra("subreddit", post.getSubreddit().toLowerCase());
                        context.startActivity(intent);
                        return true;
                    //case R.id.action_download_comments:
                    //    return true;
                    case R.id.action_report:
                        if (MainActivity.currentUser != null) {
                            ReportDialogFragment dialog = new ReportDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("postId", post.getFullName());
                            dialog.setArguments(bundle);
                            dialog.show(((Activity) context).getFragmentManager(), "dialog");
                        } else ToastUtils.displayShortToast(context, "Must be logged in to report");
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }
}
