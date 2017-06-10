package com.gDyejeekis.aliencompanion.models;

import java.io.Serializable;

/**
 * Created by George on 6/10/2017.
 */

public class Article implements Serializable {

    private static final long serialVersionUID = 74392428L;

    private String imageSource;
    private String title;
    private String body;

    public Article() {

    }

    public Article(String title, String body, String imageSource) {
        this.title = title;
        this.body = body;
        this.imageSource = imageSource;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
