package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.action.MarkActions;
import com.gDyejeekis.aliencompanion.api.action.SubmitActions;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by sound on 3/4/2016.
 */
public class CommentAction extends OfflineUserAction implements Serializable {

    public static final String ACTION_NAME = "Comment";

    public static final int ACTION_TYPE = 0;

    private String parentFullname;
    private String commentText;
    //private String parentText;

    public CommentAction(String accountName, String fullname, String commentText) {
        super(accountName);
        this.actionName = ACTION_NAME;
        this.actionType = ACTION_TYPE;
        this.parentFullname = fullname;
        this.commentText = commentText;
        this.actionId = ACTION_NAME + "-" + parentFullname + "-" + UUID.randomUUID();
    }

    public String getParentFullname() {
        return parentFullname;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void executeAction(Context context) {
        User user = getUserByAccountName(context);

        if(user != null) {
            try {
                SubmitActions submitActions = new SubmitActions(new PoliteRedditHttpClient(user), user);
                submitActions.comment(parentFullname, commentText);
                actionCompleted = true;
                saveAnyAccountChanges(context);
            } catch (Exception e) {
                actionCompleted = false;
                e.printStackTrace();
            }
        }
    }

}
