package com.gDyejeekis.aliencompanion.api.retrieval.params;

public enum UserSubmissionsCategory {

    OVERVIEW("overview"),
    SUBMITTED("submitted"),
    COMMENTS("comments"),
    GILDED("gilded"),
    LIKED("upvoted"),
    DISLIKED("downvoted"),
    HIDDEN("hidden"),
    SAVED("saved");

    private final String value;

    UserSubmissionsCategory(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
    
}
