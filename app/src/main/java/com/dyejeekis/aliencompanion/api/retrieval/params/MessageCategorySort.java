package com.dyejeekis.aliencompanion.api.retrieval.params;

/**
 * Created by sound on 10/9/2015.
 */
public enum MessageCategorySort {

    ALL("all"),
    UNREAD("unread"),
    MESSAGES("messages"),
    COMMENT_REPLIES("comment replies"),
    POST_REPLIES("post replies"),
    USERNAME_MENTIONS("username mentions");

    private final String value;

    MessageCategorySort(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

}
