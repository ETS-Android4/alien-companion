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
        Log.d("SCHEDULE_DEBUG", "Scheduling sync services...");

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, DownloaderService.class);
        intent.putStringArrayListExtra("subreddits", (ArrayList) subreddits);
        PendingIntent pendingIntent = PendingIntent.getService(context, profileId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.cancel(pendingIntent); //cancel previous pending intent for this profile

        for(TimeWindow timeWindow : getSyncTimeWindows()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
                mgr.setWindow(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, timeWindow.windowLength, pendingIntent);
            }
            else {
                mgr.set(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, pendingIntent);
            }
        }

        Log.d("SCHEDULE_DEBUG", "finished scheduling");
    }

    public void unscheduleSync(Context context) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.putStringArrayListExtra("subreddits", (ArrayList) subreddits);
        PendingIntent pendingIntent = PendingIntent.getService(context, profileId, intent, PendingIntent.FLAG_NO_CREATE);
        if(pendingIntent != null) {
            pendingIntent.cancel();
        }
    }

    private List<TimeWindow> getSyncTimeWindows() {
        List<TimeWindow> timeWindows = new ArrayList<>();

        int windowStart = (fromTime > toTime) ? toTime : fromTime;
        long windowLength = Math.abs(fromTime - toTime);
        //TimeUnit.MILLISECONDS.convert(windowLength, TimeUnit.HOURS);
        windowLength = TimeUnit.HOURS.toMillis(windowLength);

        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.WEEK_OF_MONTH, cur_cal.get(Calendar.WEEK_OF_MONTH));

        if(days.contains("mon")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);

            timeWindows.add(new TimeWindow(calendar.getTimeInMillis(), windowLength));
        }
        if(days.contains("tue")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);

            timeWindows.add(new TimeWindow(calendar.getTimeInMillis(), windowLength));
        }
        if(days.contains("wed")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);

            timeWindows.add(new TimeWindow(calendar.getTimeInMillis(), windowLength));
        }
        if(days.contains("thu")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);

            timeWindows.add(new TimeWindow(calendar.getTimeInMillis(), windowLength));
        }
        if(days.contains("fri")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);

            timeWindows.add(new TimeWindow(calendar.getTimeInMillis(), windowLength));
        }
        if(days.contains("sat")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);

            timeWindows.add(new TimeWindow(calendar.getTimeInMillis(), windowLength));
        }
        if(days.contains("sun")) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            calendar.set(Calendar.HOUR_OF_DAY, windowStart);

            timeWindows.add(new TimeWindow(calendar.getTimeInMillis(), windowLength));
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
