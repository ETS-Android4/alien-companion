package com.george.redditreader.Models;

/**
 * Created by George on 6/5/2015.
 */
public class OptionItem {

    private String title;
    private String subtitle;

    public OptionItem(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
