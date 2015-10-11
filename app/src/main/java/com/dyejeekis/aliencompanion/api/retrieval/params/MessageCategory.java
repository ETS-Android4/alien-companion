package com.dyejeekis.aliencompanion.api.retrieval.params;

/**
 * Created by sound on 10/9/2015.
 */
public enum MessageCategory {

    INBOX("inbox"),
    SENT("sent");

    private final String value;

    MessageCategory(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
