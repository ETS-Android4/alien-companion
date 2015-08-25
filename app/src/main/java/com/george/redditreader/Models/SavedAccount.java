package com.george.redditreader.Models;

import java.io.Serializable;

/**
 * Created by sound on 8/25/2015.
 */
public class SavedAccount implements Serializable {

    private String username;
    private String password;


    public SavedAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
