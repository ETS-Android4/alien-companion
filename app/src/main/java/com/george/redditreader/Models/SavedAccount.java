package com.george.redditreader.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sound on 8/25/2015.
 */
public class SavedAccount implements Serializable {

    private String username;
    private String modhash;
    private String cookie;
    private List<String> subreddits;

    public SavedAccount(String username, String modhash, String cookie, List<String> subreddits) {
        this.username = username;
        this.modhash = modhash;
        this.cookie = cookie;
        this.subreddits = subreddits;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public void setSubreddits(List<String> subreddits) {
        this.subreddits = subreddits;
    }

    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getUsername() {
        return username;
    }

}
