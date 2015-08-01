package com.george.redditreader.enums;

/**
 * Created by George on 8/1/2015.
 */
public enum UserContent {
    overview("overview"), comments("comments"), submitted("submitted");

    private final String value;

    UserContent(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
