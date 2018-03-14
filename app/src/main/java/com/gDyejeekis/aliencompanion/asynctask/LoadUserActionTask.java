package com.gDyejeekis.aliencompanion.asynctask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.MessageActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.SearchActivity;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SubredditSidebarDialogFragment;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.models.SubmitActionResponse;
import com.gDyejeekis.aliencompanion.models.offline_actions.CommentAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.DownvoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.HideAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.NoVoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.ReportAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.SaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.SubmitLinkAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.SubmitTextAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UnhideAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UnsaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UpvoteAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.action.MarkActions;
import com.gDyejeekis.aliencompanion.api.action.ProfileActions;
import com.gDyejeekis.aliencompanion.api.action.SubmitActions;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.entity.Subreddit;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.enums.UserActionType;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.views.multilevelexpindlistview.MultiLevelExpIndListAdapter;

/**
 * Created by sound on 8/26/2015.
 */
public class LoadUserActionTask extends AsyncTask<Void, Void, Void> {

    public static final String DEFAULT_ACTION_FAILED_MESSAGE = "Error completing user action";

    private Context context;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private UserActionType userActionType;
    private boolean notifyOnPostExecute = true;
    private String itemName;
    private int itemIndex = -1;
    private Exception exception;
    private Submission submission;
    private Comment comment;
    private String text;
    private User user;
    private String currentPass, newPass;
    private String title, linkOrText, subreddit, captcha_iden, captcha_sol;
    private String recipient, subject, message;
    private SubredditSidebarDialogFragment dialogSidebar;
    private OfflineUserAction offlineUserAction;
    private SubmitActionResponse submitActionResponse;

    public void setNotifyOnPostExecute(boolean flag) {
        notifyOnPostExecute = flag;
    }

    public LoadUserActionTask(Context context, String recipient, String subject, String message) {
        this.context = context;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        userActionType = UserActionType.sendMessage;
    }

    public LoadUserActionTask(Context context, String itemName, UserActionType userActionType) {
        this.context = context;
        this.userActionType = userActionType;
        this.itemName = itemName;
    }

    public LoadUserActionTask(Context context, Submission submission, UserActionType userActionType) {
        this.context = context;
        this.userActionType = userActionType;
        this.submission = submission;
        this.itemName = submission.getFullName();
    }

    public LoadUserActionTask(Context context, Submission submission, int itemIndex, UserActionType userActionType) {
        this.context = context;
        this.userActionType = userActionType;
        this.submission = submission;
        this.itemIndex = itemIndex;
        this.itemName = submission.getFullName();
    }

    public LoadUserActionTask(Context context, Comment comment, UserActionType userActionType) {
        this.context = context;
        this.userActionType = userActionType;
        this.comment = comment;
        this.itemName = comment.getFullName();
    }

    public LoadUserActionTask(Context context, String itemName, UserActionType userActionType, String text) {
        this.context = context;
        this.userActionType = userActionType;
        this.itemName = itemName;
        this.text = text;
    }

    public LoadUserActionTask(Context context, UserActionType userActionType, User user, String currentPass, String newPass) {
        this.context = context;
        this.userActionType = userActionType;
        this.user = user;
        this.currentPass = currentPass;
        this.newPass = newPass;
    }

    public LoadUserActionTask(Context context, UserActionType userActionType, String title, String linkOrText, String subreddit) {
        this.context = context;
        this.userActionType = userActionType;
        this.title = title;
        this.linkOrText = linkOrText;
        this.subreddit = subreddit;
    }

    public LoadUserActionTask(Context context, UserActionType type, String subreddit) {
        this.context = context;
        this.userActionType = type;
        this.subreddit = subreddit;
    }

    public LoadUserActionTask(Context context, UserActionType type, String subreddit, SubredditSidebarDialogFragment dialog) {
        this.context = context;
        this.userActionType = type;
        this.subreddit = subreddit;
        this.dialogSidebar = dialog;
    }

    public LoadUserActionTask(Context context, OfflineUserAction offlineAction) {
        this.context = context;
        this.offlineUserAction = offlineAction;
        if(offlineAction instanceof UpvoteAction) {
            userActionType = UserActionType.upvote;
            itemName = ((UpvoteAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof DownvoteAction) {
            userActionType = UserActionType.downvote;
            itemName = ((DownvoteAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof NoVoteAction) {
            userActionType = UserActionType.novote;
            itemName = ((NoVoteAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof SaveAction) {
            userActionType = UserActionType.save;
            itemName = ((SaveAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof UnsaveAction) {
            userActionType = UserActionType.unsave;
            itemName = ((UnsaveAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof HideAction) {
            userActionType = UserActionType.hide;
            itemName = ((HideAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof UnhideAction) {
            userActionType = UserActionType.unhide;
            itemName = ((UnhideAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof CommentAction) {
            userActionType = UserActionType.submitComment;
            itemName = ((CommentAction) offlineAction).getParentFullname();
            text = ((CommentAction) offlineAction).getCommentText();
        }
        else if(offlineAction instanceof ReportAction) {
            userActionType = UserActionType.report;
            itemName = ((ReportAction) offlineAction).getItemFullname();
            text = ((ReportAction) offlineAction).getReportReason();
        }
        else if(offlineAction instanceof SubmitTextAction) {
            userActionType = UserActionType.submitText;
            title = ((SubmitTextAction) offlineAction).getTitle();
            linkOrText = ((SubmitTextAction) offlineAction).getSelfText();
            subreddit = ((SubmitTextAction) offlineAction).getSubreddit();
        }
        else if(offlineAction instanceof SubmitLinkAction) {
            userActionType = UserActionType.submitLink;
            title = ((SubmitLinkAction) offlineAction).getTitle();
            linkOrText = ((SubmitLinkAction) offlineAction).getLink();
            subreddit = ((SubmitLinkAction) offlineAction).getSubreddit();
        }
    }

    @Override
    protected Void doInBackground(Void... unused) {
        try {
            MarkActions markActions = new MarkActions(httpClient, MyApplication.currentUser);
            SubmitActions submitActions = new SubmitActions(httpClient, MyApplication.currentUser);
            ProfileActions profileActions = new ProfileActions(httpClient, MyApplication.currentUser);
            switch (userActionType) {
                case novote:
                    markActions.vote(itemName, 0);
                    break;
                case upvote:
                    markActions.vote(itemName, 1);
                    break;
                case downvote:
                    markActions.vote(itemName, -1);
                    break;
                case save:
                    markActions.save(itemName);
                    break;
                case unsave:
                    markActions.unsave(itemName);
                    break;
                case hide:
                    markActions.hide(itemName);
                    break;
                case unhide:
                    markActions.unhide(itemName);
                    break;
                case report:
                    markActions.report(itemName, text);
                    break;
                case edit:
                    submitActionResponse = submitActions.editUserText(itemName, text);
                    break;
                case delete:
                    submitActions.delete(itemName);
                    break;
                case markNSFW:
                    markActions.markNSFW(itemName);
                    break;
                case unmarkNSFW:
                    markActions.unmarkNSFW(itemName);
                    break;
                case markSpoiler:
                    markActions.markSpoiler(itemName);
                    break;
                case unmarkSpoiler:
                    markActions.unmarkSpoiler(itemName);
                    break;
                case submitLink:
                    captcha_iden = "";
                    captcha_sol = "";
                    submitActions.submitLink(title, linkOrText, subreddit, captcha_iden, captcha_sol);
                    break;
                case submitText:
                    captcha_iden = "";
                    captcha_sol = "";
                    submitActions.submitSelfPost(title, linkOrText, subreddit, captcha_iden, captcha_sol);
                    break;
                case submitComment:
                    submitActionResponse = submitActions.comment(itemName, text);
                    break;
                case changePassword:
                    //profileActions.changePassword(currentPass, newPass);
                    break;
                case subscribe:
                    String fullname = Subreddit.getSubreddit(httpClient, subreddit).getFullName();
                    profileActions.subscribe(fullname);
                    break;
                case unsubscribe:
                    fullname = Subreddit.getSubreddit(httpClient, subreddit).getFullName();
                    profileActions.unsubscribe(fullname);
                    break;
                case sendMessage:
                    captcha_iden = "";
                    captcha_sol = "";
                    submitActions.compose(recipient, subject, message, captcha_iden, captcha_sol);
                    break;
            }
        } catch (ActionFailedException | NullPointerException | RetrievalFailedException e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        if(MyApplication.accountChanges) {
            MyApplication.accountChanges = false;
            GeneralUtils.saveAccountChanges(context);
        }

        if(exception != null) {
            if(dialogSidebar !=null) {
                dialogSidebar.updateSubUnsubButton(false);
            }

            if(context instanceof Activity) {
                ToastUtils.showToast(context, DEFAULT_ACTION_FAILED_MESSAGE);
            }
        }
        else {
            if(offlineUserAction != null) {
                offlineUserAction.setActionCompleted(true);
            }
            else if(notifyOnPostExecute) {
                switch (userActionType) {
                    case submitText:
                    case submitLink:
                        ((Activity) context).finish();
                        ToastUtils.showToast(context, "Submission successful");
                        break;
                    case edit:
                    case submitComment:
                        String message;
                        if (submitActionResponse != null) {
                            if (submitActionResponse.isSuccess()) {
                                message = (userActionType == UserActionType.edit) ? "Edit successful" : "Reply sent";
                                if (context instanceof SubmitActivity) {
                                    if (submitActionResponse.getRedditItem() instanceof Comment)
                                        ((SubmitActivity) context).sendBroadcast((Comment) submitActionResponse.getRedditItem());
                                    ((SubmitActivity) context).finish();
                                }
                            }
                            else {
                                message = submitActionResponse.getFailReason();
                            }
                        } else {
                            message = DEFAULT_ACTION_FAILED_MESSAGE;
                        }
                        ToastUtils.showToast(context, message);
                        break;
                    case save:
                        View.OnClickListener listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setItemSaved(false);
                                LoadUserActionTask task = new LoadUserActionTask(context, itemName, UserActionType.unsave);
                                task.setNotifyOnPostExecute(false);
                                task.execute();
                            }
                        };
                        showSnackbar("Saved", "Undo", listener);
                        break;
                    case unsave:
                        listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setItemSaved(true);
                                LoadUserActionTask task = new LoadUserActionTask(context, itemName, UserActionType.save);
                                task.setNotifyOnPostExecute(false);
                                task.execute();
                            }
                        };
                        showSnackbar("Unsaved", "Undo", listener);
                        break;
                    case hide:
                        listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                unhidePost();
                                LoadUserActionTask task = new LoadUserActionTask(context, itemName, UserActionType.unhide);
                                task.setNotifyOnPostExecute(false);
                                task.execute();
                            }
                        };
                        showSnackbar("Hidden", "Undo", listener);
                        break;
                    case delete:
                        onRedditItemDeleted();
                        showSnackbar("Deleted");
                        break;
                    case changePassword:
                        ToastUtils.showToast(context, "Password change successful");
                        break;
                    case report:
                        showSnackbar("Report sent");
                        break;
                    case subscribe:
                        if (dialogSidebar != null) {
                            dialogSidebar.updateSubUnsubButton(true);
                        }
                        ToastUtils.showToast(context, "Subscribed to " + subreddit);
                        break;
                    case unsubscribe:
                        if (dialogSidebar != null) {
                            dialogSidebar.updateSubUnsubButton(true);
                        }
                        ToastUtils.showToast(context, "Unsubscribed from " + subreddit);
                        break;
                    case sendMessage:
                        ((Activity) context).finish();
                        ToastUtils.showToast(context, "Message sent");
                        break;
                }
            }
        }
    }

    // TODO: 3/7/2018 re-write to work while dual pane mode is active
    private void onRedditItemDeleted() {
        final String id = itemName.substring(3);
        RedditItemListAdapter listAdapter = getListFragmentAdapter();
        if (listAdapter != null) {
            for (RedditItem item : listAdapter.redditItems) {
                if (item.getIdentifier().equals(id)) {
                    listAdapter.remove(item);
                    break;
                }
            }
        } else {
            PostAdapter postAdapter = getPostFragmentAdapter();
            if (postAdapter != null) {
                if (itemName.startsWith("t1_")) { // comment case
                    for (MultiLevelExpIndListAdapter.ExpIndData item : postAdapter.getData()) {
                        if (item instanceof Comment && ((Comment) item).getIdentifier().equals(id)) {
                            Comment comment = (Comment) item;
                            int commentIndex = postAdapter.indexOf(comment);
                            Comment belowComment = null;
                            try {
                                belowComment = (Comment) postAdapter.getItemAt(commentIndex + 1);
                            } catch (Exception e) {}
                            if (belowComment!=null && belowComment.getParentId().equals(comment.getFullName())) {
                                comment.setAuthor("[deleted]");
                                comment.setBodyHTML("[deleted]");
                                comment.setBody("[deleted]");
                                postAdapter.notifyItemChanged(commentIndex);
                            } else {
                                postAdapter.remove(item, true);
                            }
                            break;
                        }
                    }
                } else if (itemName.startsWith("t3_")) { // post case
                    Submission post = (Submission) postAdapter.getItemAt(0);
                    post.setAuthor("[deleted]");
                    if (post.isSelf()) {
                        post.setSelftext("[deleted]");
                        post.setSelftextHTML("[deleted]");
                    }
                    postAdapter.notifyItemChanged(0);
                }
            }
        }
    }

    private void showSnackbar(String message) {
        showSnackbar(message, Snackbar.LENGTH_SHORT);
    }

    private void showSnackbar(String message, int duration) {
        showSnackbar(message, null, null, duration);
    }

    private void showSnackbar(String message, String actionText, View.OnClickListener listener) {
        showSnackbar(message, actionText, listener, Snackbar.LENGTH_LONG);
    }

    // TODO: 3/7/2018 re-write to work while dual pane mode is active
    private void showSnackbar(String message, String actionText, View.OnClickListener listener, int duration) {
        PostFragment postFragment = getPostFragment();
        if (postFragment!=null) {
            postFragment.setSnackbar(ToastUtils.showSnackbar(postFragment.getSnackbarParentView(),
                    message, actionText, listener, duration));
        } else {
            RedditContentFragment fragment = getListFragment();
            if (fragment!=null) {
                fragment.setSnackbar(ToastUtils.showSnackbar(fragment.getSnackbarParentView(),
                        message, actionText, listener, duration));
            }
        }
    }

    private void unhidePost() {
        if(submission!=null && itemIndex!=-1) {
            submission.setHidden(false);
            RecyclerView.Adapter listFragmentAdapter = getListFragmentAdapter();
            if(listFragmentAdapter!=null) {
                ((RedditItemListAdapter) listFragmentAdapter).add(submission, itemIndex);
                notifyDataSetChangedDelayed(listFragmentAdapter);
            }
            notifyDataSetChanged(getPostFragmentAdapter());
        }
    }

    private void notifyDataSetChangedDelayed(final RecyclerView.Adapter adapter) {
        if(adapter!=null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            }, 500);
        }
    }

    private void notifyDataSetChanged(RecyclerView.Adapter adapter) {
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setItemSaved(boolean saved) {
        if(submission!=null) {
            submission.setSaved(saved);
        }
        else if(comment!=null) {
            comment.setSaved(saved);
        }
        notifyDataSetChanged(getListFragmentAdapter());
        notifyDataSetChanged(getPostFragmentAdapter());
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

}
