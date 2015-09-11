package com.dyejeekis.aliencompanion.LoadTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Utils.ToastUtils;
import com.dyejeekis.aliencompanion.api.action.MarkActions;
import com.dyejeekis.aliencompanion.api.action.SubmitActions;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.api.exception.ActionFailedException;
import com.dyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.dyejeekis.aliencompanion.api.utils.httpClient.RedditHttpClient;
import com.dyejeekis.aliencompanion.enums.UserActionType;

/**
 * Created by sound on 8/26/2015.
 */
public class LoadUserActionTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private HttpClient httpClient;
    private UserActionType userActionType;
    private String postName;
    private Exception exception;
    private Submission submission;
    private String commentText;

    public LoadUserActionTask(Context context, String postName, UserActionType userActionType) {
        this.context = context;
        this.userActionType = userActionType;
        this.postName = postName;
        httpClient = new RedditHttpClient();
    }

    public LoadUserActionTask(Context context, String postName, UserActionType userActionType, Submission submission) {
        this.context = context;
        this.userActionType = userActionType;
        this.postName = postName;
        this.submission = submission;
        httpClient = new RedditHttpClient();
    }

    public LoadUserActionTask(Context context, String postName, UserActionType userActionType, String commentText) {
        this.context = context;
        this.userActionType = userActionType;
        this.postName = postName;
        this.commentText = commentText;
        httpClient = new RedditHttpClient();
    }

    @Override
    protected Void doInBackground(Void... unused) {
        try {
            MarkActions markActions = new MarkActions(httpClient, MainActivity.currentUser);
            SubmitActions submitActions = new SubmitActions(httpClient, MainActivity.currentUser);
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
                    break;
                case edit:
                    submitActions.editUserText(postName, commentText);
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
                    break;
                case submitText:
                    break;
                case submitComment:
                    submitActions.comment(postName, commentText);
                    break;
            }
        } catch (ActionFailedException | NullPointerException e) {
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
                    ToastUtils.displayShortToast(context, "Submission sucessful");
                    break;
                case submitComment:
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
            }
        }
    }
}
