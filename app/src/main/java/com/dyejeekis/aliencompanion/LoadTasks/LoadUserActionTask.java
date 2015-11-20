package com.dyejeekis.aliencompanion.LoadTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.api.action.MarkActions;
import com.dyejeekis.aliencompanion.api.action.ProfileActions;
import com.dyejeekis.aliencompanion.api.action.SubmitActions;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.api.entity.Subreddit;
import com.dyejeekis.aliencompanion.api.entity.User;
import com.dyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.dyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;
import com.dyejeekis.aliencompanion.enums.UserActionType;

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

    @Override
    protected Void doInBackground(Void... unused) {
        try {
            MarkActions markActions = new MarkActions(httpClient, MainActivity.currentUser);
            SubmitActions submitActions = new SubmitActions(httpClient, MainActivity.currentUser);
            ProfileActions profileActions = new ProfileActions(httpClient, MainActivity.currentUser);
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
                    captcha_iden = null;
                    captcha_sol = null;
                    submitActions.submitLink(title, linkOrText, subreddit, captcha_iden, captcha_sol);
                    break;
                case submitText:
                    captcha_iden = null;
                    captcha_sol = null;
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
                    captcha_iden = null;
                    captcha_sol = null;
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
        if(exception != null) {
            ToastUtils.displayShortToast(context, "Error completing user action");
        }
        else {
            switch (userActionType) {
                case submitText: case submitLink:
                    ((Activity) context).finish();
                    ToastUtils.displayShortToast(context, "Submission successful");
                    break;
                case submitComment:
                    ((Activity) context).finish();
                    ToastUtils.displayShortToast(context, "Reply sent");
                    break;
                case save:
                    ToastUtils.displayShortToast(context, "Saved");
                    break;
                case unsave:
                    ToastUtils.displayShortToast(context, "Unsaved");
                    break;
                case delete:
                    ToastUtils.displayShortToast(context, "Deleted");
                    break;
                case edit:
                    ToastUtils.displayShortToast(context, "Edit successful");
                    break;
                case changePassword:
                    ToastUtils.displayShortToast(context, "Password change successful");
                    break;
                case report:
                    ToastUtils.displayShortToast(context, "Report sent");
                    break;
                case subscribe:
                    ToastUtils.displayShortToast(context, "Subscribed to " + subreddit);
                    break;
                case unsubscribe:
                    ToastUtils.displayShortToast(context, "Unsubscribed from " + subreddit);
                    break;
                case sendMessage:
                    ((Activity) context).finish();
                    ToastUtils.displayShortToast(context, "Message sent");
                    break;
            }
        }
    }
}
