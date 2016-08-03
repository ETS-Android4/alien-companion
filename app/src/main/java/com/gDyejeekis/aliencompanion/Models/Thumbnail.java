package com.gDyejeekis.aliencompanion.Models;

import java.io.Serializable;

/**
 * Created by George on 6/7/2015.
 */
public class Thumbnail implements Serializable {

    private static final long serialVersionUID = 1234544L;

    private String url;
    private boolean hasThumbnail;
    private boolean isSelf;
    private boolean isNSFW;

    public Thumbnail() {
    }

    public Thumbnail(String url) {
        this.url = url;
        if(url.equals("self") || url.equals("default")) {
            isSelf = true;
            isNSFW = false;
        }
        else if(url.equals("nsfw")) {
            isSelf = false;
            isNSFW = true;
        }
    }

    public Thumbnail(String url, boolean hasThumbnail) {
        this.url = url;
        this.hasThumbnail = hasThumbnail;
        if(url.equals("self") || url.equals("default")) {
            isSelf = true;
            isNSFW = false;
        }
        else if(url.equals("nsfw")) {
            isSelf = false;
            isNSFW = true;
        }
    }

    public String getUrl() {
        return url;
    }

    public boolean hasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasUrl) {
        this.hasThumbnail = (!isSelf) && hasUrl;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public boolean isNSFW() {
        return isNSFW;
    }

    //public void setNSFW(boolean setting) {
    //    isNSFW = setting;
    //}
}
