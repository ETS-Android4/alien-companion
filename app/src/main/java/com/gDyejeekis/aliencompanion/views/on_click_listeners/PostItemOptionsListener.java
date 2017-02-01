package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.asynctask.SaveOfflineActionTask;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ReportDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.TwoOptionDialogFragment;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.offline_actions.DownvoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.HideAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.NoVoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.SaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UnhideAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UnsaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UpvoteAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

/**
 * Created by George on 8/9/2015.
 */
public class PostItemOptionsListener implements View.OnClickListener {

    public static final int ACTION_REMOVE_SYNCED = 1;

    private Context context;
    private Submission post;
    private RecyclerView.Adapter recyclerAdapter;

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
            case R.id.btn_upvote:case R.id.imageView_upvote_classic:
                UserActionType actionType;
                LoadUserActionTask task;
                SaveOfflineActionTask task1;
                if(MyApplication.currentUser!=null) {
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

                    recyclerAdapter.notifyDataSetChanged();

                    if(GeneralUtils.isNetworkAvailable(context)) {
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if(actionType == UserActionType.novote) {
                            action = new NoVoteAction(accountName, post.getFullName(), post.getTitle());
                        }
                        else {
                            action = new UpvoteAction(accountName, post.getFullName(), post.getTitle());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                }
                else ToastUtils.displayShortToast(context, "Must be logged in to vote");
                break;
            case R.id.btn_downvote: case R.id.imageView_downvote_classic:
                if(MyApplication.currentUser!=null) {
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

                    recyclerAdapter.notifyDataSetChanged();

                    if(GeneralUtils.isNetworkAvailable(context)) {
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if(actionType == UserActionType.novote) {
                            action = new NoVoteAction(accountName, post.getFullName(), post.getTitle());
                        }
                        else {
                            action = new DownvoteAction(accountName, post.getFullName(), post.getTitle());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                }
                else ToastUtils.displayShortToast(context, "Must be logged in to vote");
                break;
            case R.id.btn_save:
                if(MyApplication.currentUser!=null) {
                    if (post.isSaved()) {
                        post.setSaved(false);
                        actionType = UserActionType.unsave;
                    } else {
                        post.setSaved(true);
                        actionType = UserActionType.save;
                    }

                    recyclerAdapter.notifyDataSetChanged();

                    if(GeneralUtils.isNetworkAvailable(context)) {
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.execute();
                    }
                    else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if(actionType == UserActionType.save) {
                            action = new SaveAction(accountName, post.getFullName(), post.getTitle());
                        }
                        else {
                            action = new UnsaveAction(accountName, post.getFullName(), post.getTitle());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                }
                else ToastUtils.displayShortToast(context, "Must be logged in to save");
                break;
            case R.id.btn_hide:
                if(MyApplication.currentUser!=null) {
                    if (post.isHidden()) {
                        post.setHidden(false);
                        actionType = UserActionType.unhide;
                        recyclerAdapter.notifyDataSetChanged();
                    }
                    else {
                        post.setHidden(true);
                        actionType = UserActionType.hide;
                        if(!(context instanceof PostActivity)) {
                            ((RedditItemListAdapter) recyclerAdapter).remove(post);
                            notifyDataSetChangedDelayed();
                        }
                    }

                    if(GeneralUtils.isNetworkAvailable(context)) {
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if(actionType == UserActionType.hide) {
                            action = new HideAction(accountName, post.getFullName(), post.getTitle());
                        }
                        else {
                            action = new UnhideAction(accountName, post.getFullName(), post.getTitle());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                }
                else {
                    ToastUtils.displayShortToast(context, "Must be logged in to hide");
                }
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
        final int resource;
        int labelNSFWindex = -1;
        String currentUser = (MyApplication.currentUser!=null) ? MyApplication.currentUser.getUsername() : "";
        if(post.getAuthor().equals(currentUser)) {
            if(post.isSelf()) {
                labelNSFWindex = 2;
                if(MyApplication.currentPostListView == R.layout.post_list_item_card
                        || recyclerAdapter instanceof PostAdapter) {
                    resource = R.menu.menu_self_post_card_more_options_account;
                }
                else {
                    resource = R.menu.menu_self_post_more_options_account;
                }
            }
            else {
                labelNSFWindex = 1;
                if(MyApplication.currentPostListView == R.layout.post_list_item_card) {
                    resource = R.menu.menu_post_card_more_options_account;
                }
                else {
                    resource = R.menu.menu_post_more_options_account;
                }
            }
        }
        else {
            if(post.isSelf()) {
                resource = (MyApplication.currentPostListView == R.layout.post_list_item_card
                        || recyclerAdapter instanceof PostAdapter) ? R.menu.menu_self_post_card_more_options : R.menu.menu_self_post_more_options;
            }
            else {
                resource = (MyApplication.currentPostListView == R.layout.post_list_item_card || recyclerAdapter instanceof PostAdapter)
                        ? R.menu.menu_post_card_more_options : R.menu.menu_post_more_options;
            }
        }
        popupMenu.inflate(resource);
        if(labelNSFWindex != -1 && post.isNSFW()) popupMenu.getMenu().getItem(labelNSFWindex).setTitle("Unmark NSFW");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sync:
                        String toastMessage;
                        if(GeneralUtils.isNetworkAvailable(context)) {
                            if(MyApplication.syncOverWifiOnly && !GeneralUtils.isConnectedOverWifi(context)) {
                                toastMessage = "Syncing over mobile data connection is disabled";
                            }
                            else {
                                toastMessage = "Post added to sync queue";
                                Intent intent = new Intent(context, DownloaderService.class);
                                intent.putExtra("post", post);
                                context.startService(intent);
                            }
                        }
                        else {
                            toastMessage = "Network connection unavailable";
                        }
                        ToastUtils.displayShortToast(context, toastMessage);
                        return true;
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
                        choiceDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
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
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                            choiceDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
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
                        if (MyApplication.currentUser != null) {
                            ReportDialogFragment dialog = new ReportDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("postId", post.getFullName());
                            dialog.setArguments(bundle);
                            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        } else ToastUtils.displayShortToast(context, "Must be logged in to report");
                        return true;
                    case ACTION_REMOVE_SYNCED:
                        ((RedditItemListAdapter) recyclerAdapter).remove(post);
                        notifyDataSetChangedDelayed();
                        new AsyncTask<String, Void, Boolean>() {

                            @Override
                            protected Boolean doInBackground(String... params) {
                                return GeneralUtils.deleteSyncedPostFromCategory(context, DownloaderService.INDIVIDUALLY_SYNCED_FILENAME, params[0]);
                            }

                            @Override
                            protected void onPostExecute(Boolean success) {
                                String message = (success) ? "Post deleted" : "Failed to delete post";
                                ToastUtils.displayShortToast(context, message);
                            }
                        }.execute(post.getIdentifier());
                        return true;
                    default:
                        return false;
                }
            }
        });
        if(MyApplication.offlineModeEnabled && context instanceof MainActivity) {
            PostListFragment fragment = ((MainActivity) context).getListFragment();
            if(fragment.isOther && fragment.subreddit.equals("synced")) {
                popupMenu.getMenu().add(Menu.NONE, ACTION_REMOVE_SYNCED, 6, "Remove");
            }
        }
        popupMenu.show();
    }

    private void notifyDataSetChangedDelayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerAdapter.notifyDataSetChanged();
            }
        }, 500);
    }

}
