package com.gDyejeekis.aliencompanion.Models;

import android.os.AsyncTask;

import com.gDyejeekis.aliencompanion.api.entity.OAuthToken;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.RedditOAuth;

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
    private OAuthToken token;
    private List<String> subreddits;
    public boolean loggedIn;
    public boolean oauth2;

    public SavedAccount(String username, String modhash, String cookie, List<String> subreddits) {
        this.username = username;
        this.modhash = modhash;
        this.cookie = cookie;
        this.subreddits = subreddits;
        loggedIn = true;
        oauth2 = false;
    }

    public SavedAccount(User user, List<String> subreddits) {
        this.username = user.getUsername();
        this.modhash = user.getModhash();
        this.cookie = user.getCookie();
        this.subreddits = subreddits;
        loggedIn = true;
        oauth2 = false;
    }

    public SavedAccount(String username, OAuthToken token, List<String> subreddits) {
        this.username = username;
        this.token = token;
        this.subreddits = subreddits;
        loggedIn = true;
        oauth2 = true;
    }

    public SavedAccount(List<String> subreddits) {
        this.username = "Logged out";
        this.subreddits = subreddits;
        loggedIn = false;
        oauth2 = false;
    }

    public void setToken(OAuthToken token) {
        this.token = token;
    }

    public OAuthToken getToken() {
        return token;
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
