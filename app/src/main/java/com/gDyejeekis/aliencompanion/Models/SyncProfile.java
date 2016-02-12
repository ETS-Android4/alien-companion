package com.gDyejeekis.aliencompanion.Models;

import android.content.Context;

import com.gDyejeekis.aliencompanion.Adapters.SyncProfileListAdapter;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 1/21/2016.
 */
public class SyncProfile implements Serializable {

    private String name;
    private List<String> subreddits;
    private double executeTime;
    private boolean hasTime;
    private boolean isActive;

    public SyncProfile() {
        this.name = "";
        this.subreddits = new ArrayList<>();
        executeTime = -1;
        this.hasTime = false;
        this.isActive = false;
    }

    public SyncProfile(String name) {
        this.name = name;
        this.subreddits = new ArrayList<>();
        executeTime = -1;
        this.hasTime = false;
        this.isActive = false;
    }

    public SyncProfile(String name, List<String> subreddits, double executeTime) {
        this.name = name;
        this.subreddits = subreddits;
        this.executeTime = executeTime;
        this.hasTime = true;
        this.isActive = true;
    }

    public void startSync(Context context) {
        if(GeneralUtils.isNetworkAvailable(context)) {
            //start syncing
        }
        else {
            //raise a notification, network unavailable
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubreddits(List<String> subreddits) {
        this.subreddits = subreddits;
    }

    public void setExecuteTime(double time) {
        this.executeTime = time;
        this.hasTime = true;
    }

    public String getName() {
        return name;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public double getExecuteTime() {
        return executeTime;
    }

    public boolean isComplete() {
        if(name==null || subreddits == null || executeTime==-1) {
            return false;
        }
        return true;
    }

    public int getViewType() {
        return SyncProfileListAdapter.VIEW_TYPE_PROFILE_ITEM;
    }

    public boolean hasTime() {
        return hasTime;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        //if(!hasTime) return false;
        return isActive;
    }
}
