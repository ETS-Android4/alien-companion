package com.dyejeekis.aliencompanion.enums;

/**
 * Created by George on 6/26/2015.
 */
public enum MenuType {
    profile("Profile"), messages("Messages"), user("User"), subreddit("Subreddit"), settings("Settings"), cached("Cached Posts");

    private final String value;

    MenuType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
