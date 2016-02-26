package com.gDyejeekis.aliencompanion.Models;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.gDyejeekis.aliencompanion.Adapters.SyncProfileListAdapter;
import com.gDyejeekis.aliencompanion.Services.DownloaderService;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.gDyejeekis.aliencompanion.enums.DaysEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by sound on 1/21/2016.
 */
public class SyncProfile implements Serializable {

    static class TimeWindow {

        long windowStart;
        long windowLength;

        TimeWindow(long startTime, long windowLength) {
            this.windowStart = startTime;
            this.windowLength = windowLength;
        }
    }

    private int profileId;
    private String name;
    private List<String> subreddits;
    private int fromTime;
    private int toTime;
    private boolean hasTime;
    private boolean isActive;
    private String days;

    public SyncProfile() {
        profileId = UUID.randomUUID().hashCode();
        this.name = "";
        this.subreddits = new ArrayList<>();
        this.fromTime = -1;
        this.toTime = -1;
        this.hasTime = false;
        this.isActive = false;
        days = "";
    }

    public SyncProfile(SyncProfile profile) {
        profileId = UUID.randomUUID().hashCode();
        this.name = profile.getName();
        this.subreddits = profile.getSubreddits();
        this.fromTime = profile.getFromTime();
        this.toTime = profile.getToTime();
        this.hasTime = profile.hasTime();
        this.isActive = profile.isActive();
        this.days = profile.getDaysString();
    }

    public SyncProfile(String name) {
        profileId = UUID.randomUUID().hashCode();
        this.name = name;
        this.subreddits = new ArrayList<>();
        this.fromTime = -1;
        this.toTime = -1;
        this.hasTime = false;
        this.isActive = false;
        days = "";
    }

    public SyncProfile(String name, List<String> subreddits, int fromTime, int toTime, String days) {
        profileId = UUID.randomUUID().hashCode();
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
                context.startService(getSubredditSyncIntent(context, subreddit));
            }
        }
        else {
            if(context instanceof Activity) {
                ToastUtils.displayShortToast(context, "Network connection unavailable");
            }
        }
    }

    public void scheduleSync(Context context) {
        Log.d("geotest", "Scheduling sync services...");

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //for(String subreddit : subreddits) {
        //    PendingIntent pendingIntent = PendingIntent.getService(context, profileId, getSubredditSyncIntent(context, subreddit), 0);
        //    mgr.cancel(pendingIntent); //cancel previous pending intents for this profile
//
        //    for(TimeWindow timeWindow : getSyncTimeWindows()) {
        //        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
        //            mgr.setWindow(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, timeWindow.windowLength, pendingIntent);
        //        }
        //        else {
        //            mgr.set(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, pendingIntent);
        //        }
        //    }
        //}
        Log.d("geotest", "finished scheduling");
    }

    public void unscheduleSync(Context context) {
        //for(String subreddit : subreddits) {
        //    PendingIntent pendingIntent = PendingIntent.getService(context, profileId, getSubredditSyncIntent(context, subreddit), PendingIntent.FLAG_NO_CREATE);
        //    if(pendingIntent != null) {
        //        pendingIntent.cancel();
        //    }
        //}

    }

    private List<TimeWindow> getSyncTimeWindows() {
        List<TimeWindow> timeWindows = new ArrayList<>();

        int windowStart = (fromTime > toTime) ? toTime : fromTime;
        long windowLength = Math.abs(fromTime - toTime);
        //TimeUnit.MILLISECONDS.convert(windowLength, TimeUnit.HOURS);
        TimeUnit.HOURS.toMillis(windowLength);
        Log.d("geotest", "windowLength: " + windowLength);

        if(days.contains("mon")) {
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.set(Calendar.HOUR_OF_DAY, windowStart);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeWindows.add(new TimeWindow(cal.getTimeInMillis(), windowLength));
        }
        if(days.contains("tue")) {
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            cal.set(Calendar.HOUR_OF_DAY, windowStart);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeWindows.add(new TimeWindow(cal.getTimeInMillis(), windowLength));
        }
        if(days.contains("wed")) {
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            cal.set(Calendar.HOUR_OF_DAY, windowStart);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeWindows.add(new TimeWindow(cal.getTimeInMillis(), windowLength));
        }
        if(days.contains("thu")) {
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
            cal.set(Calendar.HOUR_OF_DAY, windowStart);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeWindows.add(new TimeWindow(cal.getTimeInMillis(), windowLength));
        }
        if(days.contains("fri")) {
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            cal.set(Calendar.HOUR_OF_DAY, windowStart);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeWindows.add(new TimeWindow(cal.getTimeInMillis(), windowLength));
        }
        if(days.contains("sat")) {
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            cal.set(Calendar.HOUR_OF_DAY, windowStart);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeWindows.add(new TimeWindow(cal.getTimeInMillis(), windowLength));
        }
        if(days.contains("sun")) {
            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            cal.set(Calendar.HOUR_OF_DAY, windowStart);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeWindows.add(new TimeWindow(cal.getTimeInMillis(), windowLength));
        }

        return timeWindows;
    }

    private Intent getSubredditSyncIntent(Context context, String subreddit) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.putExtra("sort", SubmissionSort.HOT);
        boolean isMulti = false;
        if(subreddit.contains(" ")) {
            isMulti = true;
            subreddit = subreddit.split("\\s")[0];
        }
        intent.putExtra("subreddit", (subreddit.equalsIgnoreCase("frontpage")) ? null : subreddit);
        intent.putExtra("isMulti", isMulti);

        return intent;
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

    public void setHasTime(boolean flag) {
        this.hasTime = flag;
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

    public int getProfileId() {
        return profileId;
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
