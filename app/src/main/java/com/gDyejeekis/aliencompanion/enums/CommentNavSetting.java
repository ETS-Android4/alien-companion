package com.gDyejeekis.aliencompanion.enums;

/**
 * Created by George on 3/5/2017.
 */

public enum CommentNavSetting {

    threads("Threads"),
    ama("AMA mode"),
    op("Original poster"),
    searchText("Search text"),
    time("Time"),
    gilded("Gilded");


    private final String value;

    CommentNavSetting(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
