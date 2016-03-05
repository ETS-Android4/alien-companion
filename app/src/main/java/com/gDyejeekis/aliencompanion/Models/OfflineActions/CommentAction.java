package com.gDyejeekis.aliencompanion.Models.OfflineActions;

import android.content.Context;

import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import java.io.Serializable;

/**
 * Created by sound on 3/4/2016.
 */
public class CommentAction extends OfflineUserAction implements Serializable{

    private String parentFullname;
    private String commentText;

    public CommentAction(String accountName, String fullname, String commentText) {
        super(accountName);
        this.parentFullname = fullname;
        this.commentText = commentText;
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

}
