package com.gDyejeekis.aliencompanion.Models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.gDyejeekis.aliencompanion.Adapters.SyncProfileListAdapter;
import com.gDyejeekis.aliencompanion.Services.DownloaderService;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;

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
            for(String subreddit : subreddits) {
                Intent intent = new Intent(context, DownloaderService.class);
                intent.putExtra("sort", SubmissionSort.HOT);
                boolean isMulti = false;
                if(subreddit.contains(" ")) {
                    isMulti = true;
                    subreddit = subreddit.split("\\s")[0];
                }
                intent.putExtra("subreddit", (subreddit.equalsIgnoreCase("frontpage")) ? null : subreddit);
                intent.putExtra("isMulti", isMulti);
                context.startService(intent);
            }
        }
        else {
            if(context instanceof Activity) {
                ToastUtils.displayShortToast(context, "Network connection unavailable");
            }
            //else {
            //    //raise notification
            //}
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
