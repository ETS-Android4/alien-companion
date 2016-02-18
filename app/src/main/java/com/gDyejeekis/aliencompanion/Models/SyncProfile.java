package com.gDyejeekis.aliencompanion.Models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.gDyejeekis.aliencompanion.Adapters.SyncProfileListAdapter;
import com.gDyejeekis.aliencompanion.Services.DownloaderService;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.enums.DaysEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 1/21/2016.
 */
public class SyncProfile implements Serializable {

    private String name;
    private List<String> subreddits;
    private int fromTime;
    private int toTime;
    private boolean hasTime;
    private boolean isActive;
    private String days;

    public SyncProfile() {
        this.name = "";
        this.subreddits = new ArrayList<>();
        this.fromTime = -1;
        this.toTime = -1;
        this.hasTime = false;
        this.isActive = false;
        days = "";
    }

    public SyncProfile(SyncProfile profile) {
        this.name = profile.getName();
        this.subreddits = profile.getSubreddits();
        this.fromTime = profile.getFromTime();
        this.toTime = profile.getToTime();
        this.hasTime = profile.hasTime();
        this.isActive = profile.isActive();
        this.days = profile.getDaysString();
    }

    public SyncProfile(String name) {
        this.name = name;
        this.subreddits = new ArrayList<>();
        this.fromTime = -1;
        this.toTime = -1;
        this.hasTime = false;
        this.isActive = false;
        days = "";
    }

    public SyncProfile(String name, List<String> subreddits, int fromTime, int toTime, String days) {
        this.name = name;
        this.subreddits = subreddits;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.hasTime = true;
        this.isActive = true;
        this.days = days;
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
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubreddits(List<String> subreddits) {
        this.subreddits = subreddits;
    }

    public void setFromTime(int fromTime) {
        this.fromTime = fromTime;
    }

    public void setToTime(int toTime) {
        this.toTime = toTime;
    }

    public int getFromTime() {
        return fromTime;
    }

    public int getToTime() {
        return toTime;
    }

    public String getName() {
        return name;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public boolean isComplete() {
        if(name==null || subreddits == null || fromTime==-1 || toTime==-1) {
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

    public String getDaysString() {
        return days;
    }

    public void setDaysString(String days) {
        this.days = days;
    }

    public void setActiveDay(DaysEnum day, boolean flag) {
        if(flag) {
            if(!days.contains(day.value())) {
                days = days.concat(day.value());
            }
        }
        else {
            days = days.replace(day.value(), "");
        }
    }

    public boolean isActiveDay(DaysEnum day) {
        return days.contains(day.value());
    }

    public boolean toggleActiveDay(DaysEnum day) {
        boolean activeState = isActiveDay(day);
        setActiveDay(day, !activeState);
        return !activeState;
    }
}
