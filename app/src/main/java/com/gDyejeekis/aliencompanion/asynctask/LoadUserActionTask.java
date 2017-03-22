package com.gDyejeekis.aliencompanion.asynctask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.SubredditSidebarDialogFragment;
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
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
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

/**
 * Created by sound on 8/26/2015.
 */
public class LoadUserActionTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private HttpClient httpClient = new PoliteRedditHttpClient();
    private UserActionType userActionType;
    private String postName;
    private Exception exception;
    private Submission submission;
    private String text;
    private User user;
    private String currentPass, newPass;
    private String title, linkOrText, subreddit, captcha_iden, captcha_sol;
    private String recipient, subject, message;
    private SubredditSidebarDialogFragment dialogSidebar;
    private OfflineUserAction offlineUserAction;

    public LoadUserActionTask(Context context, String recipient, String subject, String message) {
        this.context = context;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        userActionType = UserActionType.sendMessage;
    }

    public LoadUserActionTask(Context context, String postName, UserActionType userActionType) {
        this.context = context;
        this.userActionType = userActionType;
        this.postName = postName;
    }

    public LoadUserActionTask(Context context, String postName, UserActionType userActionType, Submission submission) {
        this.context = context;
        this.userActionType = userActionType;
        this.postName = postName;
        this.submission = submission;
    }

    public LoadUserActionTask(Context context, String postName, UserActionType userActionType, String text) {
        this.context = context;
        this.userActionType = userActionType;
        this.postName = postName;
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
            postName = ((UpvoteAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof DownvoteAction) {
            userActionType = UserActionType.downvote;
            postName = ((DownvoteAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof NoVoteAction) {
            userActionType = UserActionType.novote;
            postName = ((NoVoteAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof SaveAction) {
            userActionType = UserActionType.save;
            postName = ((SaveAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof UnsaveAction) {
            userActionType = UserActionType.unsave;
            postName = ((UnsaveAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof HideAction) {
            userActionType = UserActionType.hide;
            postName = ((HideAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof UnhideAction) {
            userActionType = UserActionType.unhide;
            postName = ((UnhideAction) offlineAction).getItemFullname();
        }
        else if(offlineAction instanceof CommentAction) {
            userActionType = UserActionType.submitComment;
            postName = ((CommentAction) offlineAction).getParentFullname();
            text = ((CommentAction) offlineAction).getCommentText();
        }
        else if(offlineAction instanceof ReportAction) {
            userActionType = UserActionType.report;
            postName = ((ReportAction) offlineAction).getItemFullname();
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
                    markActions.vote(postName, 0);
                    break;
                case upvote:
                    markActions.vote(postName, 1);
                    break;
                case downvote:
                    markActions.vote(postName, -1);
                    break;
                case save:
                    markActions.save(postName);
                    break;
                case unsave:
                    markActions.unsave(postName);
                    break;
                case hide:
                    markActions.hide(postName);
                    break;
                case unhide:
                    markActions.unhide(postName);
                    break;
                case report:
                    markActions.report(postName, text);
                    break;
                case edit:
                    submitActions.editUserText(postName, text);
                    break;
                case delete:
                    submitActions.delete(postName);
                    break;
                case markNSFW:
                    markActions.markNSFW(postName);
                    break;
                case unmarkNSFW:
                    markActions.unmarkNSFW(postName);
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
                    submitActions.comment(postName, text);
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
                ToastUtils.showToast(context, "Error completing user action");
            }
        }
        else {
            if(offlineUserAction != null) {
                offlineUserAction.setActionCompleted(true);
            }
            else {
                switch (userActionType) {
                    case submitText:
                    case submitLink:
                        ((Activity) context).finish();
                        ToastUtils.showToast(context, "Submission successful");
                        break;
                    case submitComment:
                        ((Activity) context).finish();
                        ToastUtils.showToast(context, "Reply sent");
                        break;
                    case save:
                        ToastUtils.showToast(context, "Saved");
                        break;
                    case unsave:
                        ToastUtils.showToast(context, "Unsaved");
                        break;
                    case delete:
                        ToastUtils.showToast(context, "Deleted");
                        break;
                    case edit:
                        ((Activity) context).finish();
                        ToastUtils.showToast(context, "Edit successful");
                        break;
                    case changePassword:
                        ToastUtils.showToast(context, "Password change successful");
                        break;
                    case report:
                        ToastUtils.showToast(context, "Report sent");
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

}
