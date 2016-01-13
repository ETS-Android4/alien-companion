package com.gDyejeekis.aliencompanion.enums;

/**
 * Created by sound on 1/12/2016.
 */
public enum YoutubeThumbnailSize {

    DEFAULT_SIZE(""),
    HIGH_QUALITY("hq"),
    MEDIUM_QUALITY("mq"),
    STANDARD_QUALITY("sd"),
    MAX_QUALITY("maxres");

    private final String value;

    YoutubeThumbnailSize(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

}
