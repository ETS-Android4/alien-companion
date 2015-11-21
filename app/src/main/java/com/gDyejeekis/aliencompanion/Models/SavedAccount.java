package com.gDyejeekis.aliencompanion.Models;

import com.gDyejeekis.aliencompanion.api.entity.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 8/25/2015.
 */
public class SavedAccount implements Serializable {

    private String username;
    private String modhash;
    private String cookie;
    private List<String> subreddits;
    public boolean loggedIn;

    public SavedAccount(String username, String modhash, String cookie, List<String> subreddits) {
        this.username = username;
        this.modhash = modhash;
        this.cookie = cookie;
        this.subreddits = subreddits;
        loggedIn = true;
    }

    public SavedAccount(User user, List<String> subreddits) {
        this.username = user.getUsername();
        this.modhash = user.getModhash();
        this.cookie = user.getCookie();
        this.subreddits = subreddits;
        loggedIn = true;
    }

    public SavedAccount(List<String> subreddits) {
        this.username = "Logged out";
        this.subreddits = subreddits;
        loggedIn = false;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public ArrayList<String> getSubredditsArraylist() {
        return (ArrayList<String>) subreddits;
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
