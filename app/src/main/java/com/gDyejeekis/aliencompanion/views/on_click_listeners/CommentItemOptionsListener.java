package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.MessageActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.SearchActivity;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.asynctask.SaveOfflineActionTask;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ReportDialogFragment;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.models.offline_actions.DownvoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.NoVoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.SaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UnsaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UpvoteAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

/**
 * Created by George on 8/15/2015.
 */
public class CommentItemOptionsListener implements View.OnClickListener {

    private Context context;
    private RecyclerView.ViewHolder viewHolder;
    private RecyclerView.Adapter recyclerAdapter;

    public CommentItemOptionsListener(Context context, RecyclerView.ViewHolder viewHolder, RecyclerView.Adapter adapter) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.recyclerAdapter = adapter;
    }

    private Comment getCurrentComment() {
        int position = viewHolder.getAdapterPosition();
        if (recyclerAdapter instanceof RedditItemListAdapter)
            return (Comment) ((RedditItemListAdapter) recyclerAdapter).getItemAt(position);
        else if (recyclerAdapter instanceof PostAdapter)
            return (Comment) ((PostAdapter) recyclerAdapter).getItemAt(position);
        return null;
    }

    @Override
    public void onClick(View v) {
        Comment comment = getCurrentComment();
        switch (v.getId()) {
            case R.id.btn_upvote:
                UserActionType actionType;
                LoadUserActionTask task;
                SaveOfflineActionTask task1;
                if (MyApplication.currentUser!=null) {

                    if (comment.getLikes().equals("true")) {
                        comment.setLikes("null");
                        comment.setScore(comment.getScore() - 1);
                        actionType = UserActionType.novote;
                    } else {
                        if (comment.getLikes().equals("false"))
                            comment.setScore(comment.getScore() + 2);
                        else comment.setScore(comment.getScore() + 1);
                        comment.setLikes("true");
                        actionType = UserActionType.upvote;
                    }

                    recyclerAdapter.notifyDataSetChanged();
                    notifySecondPaneChanges();

                    if (GeneralUtils.isNetworkAvailable(context)) {

                        task = new LoadUserActionTask(context, comment.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if (actionType == UserActionType.novote) {
                            action = new NoVoteAction(accountName, comment.getFullName(), comment.getBody());
                        } else {
                            action = new UpvoteAction(accountName, comment.getFullName(), comment.getBody());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                } else {
                    showSnackbar("Must be logged in to vote");
                }
                break;
            case R.id.btn_downvote:
                if (MyApplication.currentUser!=null) {

                    if (comment.getLikes().equals("false")) {
                        comment.setLikes("null");
                        comment.setScore(comment.getScore() + 1);
                        actionType = UserActionType.novote;
                    } else {
                        if (comment.getLikes().equals("true"))
                            comment.setScore(comment.getScore() - 2);
                        else comment.setScore(comment.getScore() - 1);
                        comment.setLikes("false");
                        actionType = UserActionType.downvote;
                    }

                    recyclerAdapter.notifyDataSetChanged();
                    notifySecondPaneChanges();

                    if (GeneralUtils.isNetworkAvailable(context)) {

                        task = new LoadUserActionTask(context, comment.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if (actionType == UserActionType.novote) {
                            action = new NoVoteAction(accountName, comment.getFullName(), comment.getBody());
                        } else {
                            action = new DownvoteAction(accountName, comment.getFullName(), comment.getBody());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                } else {
                    showSnackbar("Must be logged in to vote");
                }
                break;
            case R.id.btn_reply:
                boolean postIsLocked = false;
                if (context instanceof PostActivity) {
                    Submission post = (Submission) ((PostActivity) context).getIntent().getSerializableExtra("post");
                    if (post!=null) {
                        postIsLocked = post.isLocked();
                    }
                }
                if (postIsLocked) {
                    showSnackbar("This post is locked. You won't be able to comment.");
                } else {
                    if (MyApplication.currentUser != null) {
                        Intent intent = new Intent(context, SubmitActivity.class);
                        intent.putExtra("submitType", SubmitType.comment);
                        intent.putExtra("originalComment", comment);
                        intent.putExtra("position", getCommentPosition(true));
                        context.startActivity(intent);
                    } else {
                        showSnackbar("Must be logged in to reply");
                    }
                }
                break;
            case R.id.btn_view_user:
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("username", comment.getAuthor());
                context.startActivity(intent);
                break;
            case R.id.btn_save:
                saveComment(comment);
                break;
            case R.id.btn_share:
                shareComment(comment);
                break;
            case R.id.btn_more:
                showMoreOptionsPopup(v, comment);
                break;
        }
    }

    private void showMoreOptionsPopup(View v, final Comment comment) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(R.menu.menu_comment_more_options);

        Menu menu = popupMenu.getMenu();
        // check the context/adapter and tweak accordingly
        if(context instanceof UserActivity && recyclerAdapter instanceof RedditItemListAdapter) {
            menu.removeItem(R.id.action_save);
            menu.removeItem(R.id.action_share);
        }
        // check if comment belongs to current user
        if(MyApplication.currentUser!=null && comment.getAuthor().equals(MyApplication.currentUser.getUsername())) {
            menu.removeItem(R.id.action_report);
        }
        else {
            menu.removeItem(R.id.action_edit);
            menu.removeItem(R.id.action_delete);
            menu.removeItem(R.id.action_disable_inbox_replies);
        }

        // check if comment is saved
        if(comment.isSaved()) {
            menu.findItem(R.id.action_save).setTitle("Unsave");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoadUserActionTask task = new LoadUserActionTask(context, comment.getFullName(), UserActionType.delete);
                                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        };
                        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.MyAlertDialogStyle)).setMessage("Are you sure you want to delete this comment?").setPositiveButton("Yes", listener)
                                .setNegativeButton("No", null).show();
                        return true;
                    case R.id.action_edit:
                        Intent intent = new Intent(context, SubmitActivity.class);
                        intent.putExtra("submitType", SubmitType.comment);
                        intent.putExtra("originalComment", comment);
                        intent.putExtra("position", getCommentPosition(false));
                        intent.putExtra("edit", true);
                        context.startActivity(intent);
                        return true;
                    case R.id.action_copy_link:
                        String commentLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3)
                                + "?comment=" + comment.getIdentifier();
                        GeneralUtils.copyTextToClipboard(context, "Comment permalink", commentLink);
                        return true;
                    case R.id.action_select_text:
                        // TODO: 4/9/2017
                        return true;
                    case R.id.action_copy_text:
                        GeneralUtils.copyTextToClipboard(context, "Comment body", comment.getBody());
                        return true;
                    case R.id.action_share:
                        shareComment(comment);
                        return true;
                    case R.id.action_open_browser:
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3)
                        + "?comment=" + comment.getIdentifier()));
                        context.startActivity(intent);
                        return true;
                    case R.id.action_save:
                        saveComment(comment);
                        return true;
                    case R.id.action_report:
                        if (MyApplication.currentUser!=null) {
                            ReportDialogFragment dialog = new ReportDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("postId", comment.getFullName());
                            dialog.setArguments(bundle);
                            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        } else {
                            showSnackbar("Must be logged in to report");
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void saveComment(Comment comment) {
        if (MyApplication.currentUser!=null) {
            UserActionType actionType;
            if (comment.isSaved()) {
                comment.setSaved(false);
                actionType = UserActionType.unsave;
            } else {
                comment.setSaved(true);
                actionType = UserActionType.save;
            }

            recyclerAdapter.notifyDataSetChanged();
            notifySecondPaneChanges();

            if (GeneralUtils.isNetworkAvailable(context)) {
                LoadUserActionTask task = new LoadUserActionTask(context, comment, actionType);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                OfflineUserAction action;
                String accountname = MyApplication.currentAccount.getUsername();
                if (actionType == UserActionType.save) {
                    action = new SaveAction(accountname, comment.getFullName(), comment.getBody());
                } else {
                    action = new UnsaveAction(accountname, comment.getFullName(), comment.getBody());
                }
                SaveOfflineActionTask task1 = new SaveOfflineActionTask(context, action);
                task1.execute();
            }
        } else {
            showSnackbar("Must be logged in to save");
        }
    }

    private void shareComment(Comment comment) {
        String commentLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + comment.getSubreddit() + "/comments/" + comment.getLinkId().substring(3)
                + "?comment=" + comment.getIdentifier();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, commentLink);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, "Share comment url via.."));
    }

    private void notifySecondPaneChanges() {
        RecyclerView.Adapter secondPaneAdapter = getSecondPaneAdapter();
        if (secondPaneAdapter != null) {
            try {
                Comment comment = getCurrentComment();
                int index = -1;
                Comment secondPaneComment = null;
                if (secondPaneAdapter instanceof PostAdapter) {
                    index = ((PostAdapter) secondPaneAdapter).indexOf(comment);
                    secondPaneComment = (Comment) ((PostAdapter) secondPaneAdapter).getItemAt(index);
                } else if (secondPaneAdapter instanceof RedditItemListAdapter) {
                    index = ((RedditItemListAdapter) secondPaneAdapter).indexOf(comment);
                    secondPaneComment = (Comment) ((RedditItemListAdapter) secondPaneAdapter).getItemAt(index);
                }

                secondPaneComment.setLikes(comment.getLikes());
                secondPaneComment.setSaved(comment.isSaved());
                secondPaneAdapter.notifyItemChanged(index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private RecyclerView.Adapter getSecondPaneAdapter() {
        RecyclerView.Adapter adapter = null;
        if(recyclerAdapter instanceof PostAdapter) {
            adapter = getListFragmentAdapter();
        }
        else if(recyclerAdapter instanceof RedditItemListAdapter)  {
            adapter = getPostFragmentAdapter();
        }
        return adapter;
    }

    private RedditItemListAdapter getListFragmentAdapter() {
        RedditContentFragment fragment = getListFragment();
        if (fragment!=null)
            return fragment.adapter;
        return null;
    }

    private PostAdapter getPostFragmentAdapter() {
        PostFragment fragment = getPostFragment();
        if (fragment!=null)
            return fragment.postAdapter;
        return null;
    }

    private int getCommentPosition(boolean newComment) {
        int position = viewHolder.getAdapterPosition();
        if (newComment) position++;
        return position;
    }

    private RedditContentFragment getListFragment() {
        // TODO: 3/23/2017 add abstraction
        try {
            if (context instanceof MainActivity) {
                return ((MainActivity) context).getListFragment();
            } else if (context instanceof SubredditActivity) {
                return ((SubredditActivity) context).getListFragment();
            } else if (context instanceof UserActivity) {
                return ((UserActivity) context).getListFragment();
            } else if (context instanceof SearchActivity) {
                return ((SearchActivity) context).getSearchFragment();
            } else if (context instanceof MessageActivity) {
                return ((MessageActivity) context).getMessageFragment();
            }
        } catch (Exception e) {}
        return null;
    }

    private PostFragment getPostFragment() {
        // TODO: 3/23/2017 add abstraction
        try {
            if (context instanceof MainActivity) {
                return ((MainActivity) context).getPostFragment();
            } else if (context instanceof SubredditActivity) {
                return ((SubredditActivity) context).getPostFragment();
            } else if (context instanceof UserActivity) {
                return ((UserActivity) context).getPostFragment();
            } else if (context instanceof PostActivity) {
                return ((PostActivity) context).getPostFragment();
            } else if (context instanceof SearchActivity) {
                return ((SearchActivity) context).getPostFragment();
            } else if (context instanceof MessageActivity) {
                return ((MessageActivity) context).getPostFragment();
            }
        } catch (Exception e) {}
        return null;
    }

    private void showSnackbar(String message) {
        showSnackbar(message, Snackbar.LENGTH_SHORT);
    }

    private void showSnackbar(String message, int duration) {
        if (context instanceof PostActivity) {
            PostFragment fragment = ((PostActivity) context).getPostFragment();
            if (fragment!=null)
                fragment.setSnackbar(ToastUtils.showSnackbar(fragment.getSnackbarParentView(), message, duration));
        }
    }
}
