package com.gDyejeekis.aliencompanion.api.retrieval.params;

import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;

/**
 * Enum to represent comment sorts on Reddit. You see these on a page that lists comments.
 *
 * @author Evin Ugur
 * @author Raul Rene Lepsa
 * @author Simon Kassing
 */
public enum CommentSort {

    NEW("new"), 
    BEST("best"),
    TOP("top"), 
    CONTROVERSIAL("controversial"), 
    OLD("old"), 
    RANDOM("random"), 
    CONFIDENCE("confidence"),
    QA("qa");

    public static CommentSort getCommentSort(String value) {
        value = value.toLowerCase();
        switch (value) {
            case "new":
                return NEW;
            case "best":
                return BEST;
            case "top":
                return TOP;
            case "controversial":
                return CONTROVERSIAL;
            case "old":
                return OLD;
            case "random":
                return RANDOM;
            case "confidence":
                return CONFIDENCE;
            case "qa":
                return QA;
            default:
                return RedditConstants.DEFAULT_COMMENT_SORT;
        }
    }

    private final String value;

    CommentSort(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
    
}
