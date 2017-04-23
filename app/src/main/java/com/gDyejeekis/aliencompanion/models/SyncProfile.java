package com.gDyejeekis.aliencompanion.models;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;
import com.gDyejeekis.aliencompanion.views.adapters.SyncProfileListAdapter;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.enums.DaysEnum;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by sound on 1/21/2016.
 */
public class SyncProfile implements Serializable {

    public static final String TAG = "SyncProfile";

    private static final long serialVersionUID = 1234542L;

    private final int profileId;
    private String name;
    private List<String> subreddits;
    private List<String> multireddits;
    private List<SyncSchedule> schedules;
    private boolean isActive;

    public boolean isUseGlobalSyncOptions() {
        return useGlobalSyncOptions;
    }

    public void setUseGlobalSyncOptions(boolean useGlobalSyncOptions) {
        this.useGlobalSyncOptions = useGlobalSyncOptions;
    }

    private boolean useGlobalSyncOptions;

    public SyncProfileOptions getSyncOptions() {
        return syncOptions;
    }

    public void setSyncOptions(SyncProfileOptions syncOptions) {
        this.syncOptions = syncOptions;
    }

    private SyncProfileOptions syncOptions;

    public SyncProfile() {
        profileId = UUID.randomUUID().hashCode();
        this.name = "";
        this.subreddits = new ArrayList<>();
        this.multireddits = new ArrayList<>();
        this.schedules = new ArrayList<>();
        this.isActive = false;
        useGlobalSyncOptions = true;
    }

    public SyncProfile(SyncProfile profile) {
        profileId = UUID.randomUUID().hashCode();
        this.name = profile.getName();
        this.subreddits = profile.getSubreddits();
        this.multireddits = profile.getMultireddits();
        this.schedules = profile.getSchedules();
        this.isActive = profile.isActive();
        useGlobalSyncOptions = profile.isUseGlobalSyncOptions();
        syncOptions = profile.getSyncOptions();
    }

    public SyncProfile(String name) {
        profileId = UUID.randomUUID().hashCode();
        this.name = name;
        this.subreddits = new ArrayList<>();
        this.multireddits = new ArrayList<>();
        this.schedules = new ArrayList<>();
        this.isActive = false;
        useGlobalSyncOptions = true;
    }

    public SyncProfile(String name, List<String> subreddits, List<String> multireddits, List<SyncSchedule> schedules) {
        profileId = UUID.randomUUID().hashCode();
        this.name = name;
        this.subreddits = subreddits;
        this.multireddits = multireddits;
        this.schedules = schedules;
        this.isActive = true;
        useGlobalSyncOptions = true;
    }

    public void startSync(Context context) {
        if(GeneralUtils.isNetworkAvailable(context)) {
            Intent intent = new Intent(context, DownloaderService.class);
            //intent.putStringArrayListExtra("subreddits", (ArrayList) subreddits);
            intent.putExtra("profile", this);
            context.startService(intent);
        }
        else {
            if(context instanceof Activity) {
                ToastUtils.showToast(context, "Network connection unavailable");
            }
        }
    }

    public void removeSchedule(SyncSchedule schedule) {
        //unschedulePendingIntents(context, schedule);
        schedules.remove(schedule);
    }

    public void setActive(Context context, boolean flag) {
        isActive = flag;
        if(flag) {
            scheduleAllPendingIntents(context);
        }
        else {
            unscheduleAllPendingIntents(context);
        }
    }

    public void unscheduleAllPendingIntents(Context context) {
        for(SyncSchedule schedule : schedules) {
            unschedulePendingIntents(context, schedule);
        }
    }

    public void unschedulePendingIntents(Context context, List<SyncSchedule> schedules) {
        for(SyncSchedule schedule : schedules) {
            unschedulePendingIntents(context, schedule);
        }
    }

    public void unschedulePendingIntents(Context context, SyncSchedule schedule) {
        Log.d(TAG, name + " (id: " + profileId + ") - Unscheduling sync services...");
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = getSyncIntent(context);
        // cancel pending intents for all days of the week
        for(int i=0;i<7;i++) {
            PendingIntent pendingIntent = PendingIntent.getService(context, profileId + schedule.getScheduleId() + i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mgr.cancel(pendingIntent);

            //cancel pending intents with FLAG_ONE_SHOT (changed flag to FLAG_UPDATE_CURRENT after 0.2.2)
            //pendingIntent = PendingIntent.getService(context, profileId + schedule.getScheduleId() + i, intent, PendingIntent.FLAG_ONE_SHOT);
            //mgr.cancel(pendingIntent);
        }
    }

    private void scheduleAllPendingIntents(Context context) {
        for(SyncSchedule schedule : schedules) {
            schedulePendingIntents(context, schedule);
        }
    }

    private void schedulePendingIntents(Context context, SyncSchedule schedule) {
        Log.d(TAG, name + " (id: " + profileId + ") - Scheduling sync services...");
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = getSyncIntent(context);
        int i = 0;
        for (SyncSchedule.TimeWindow timeWindow : schedule.getSyncTimeWindows()) {
            Log.d(TAG, name + " - start time: " + new Date(timeWindow.windowStart).toString() + " - time window: " + timeWindow.windowLength);

            PendingIntent pendingIntent = PendingIntent.getService(context, profileId + schedule.getScheduleId() + i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mgr.setWindow(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, timeWindow.windowLength, pendingIntent);
            } else {
                mgr.set(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, pendingIntent);
            }
            i++;
        }
    }

    private Intent getSyncIntent(Context context) {
        Intent intent = new Intent(context, DownloaderService.class);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            intent.putExtra("profileId", this.profileId);
        }
        else {
            intent.putExtra("profile", this);
        }
        intent.putExtra("reschedule", true);

        return intent;
    }

    public void saveChanges(Context context, boolean newProfile) {
        try {
            File file = new File(context.getFilesDir(), MyApplication.SYNC_PROFILES_FILENAME);
            List<SyncProfile> profiles = (List<SyncProfile>) GeneralUtils.readObjectFromFile(file);
            if(newProfile) {
                profiles.add(this);
            }
            else {
                int index = -1;
                for (SyncProfile profile : profiles) {
                    if (profile.getProfileId() == this.getProfileId()) {
                        index = profiles.indexOf(profile);
                        break;
                    }
                }
                profiles.set(index, this);
            }
            GeneralUtils.writeObjectToFile(profiles, file);
        } catch (Exception  e) {
            ToastUtils.showToast(context, "Error saving profile");
            Log.e(TAG, "Error saving profile " + name + " (id: " + profileId + ")");
            e.printStackTrace();
        }
    }

    public boolean delete(Context context) {
        try {
            File file = new File(context.getFilesDir(), MyApplication.SYNC_PROFILES_FILENAME);
            List<SyncProfile> profiles = (List<SyncProfile>) GeneralUtils.readObjectFromFile(file);
            boolean removed = profiles.remove(this);
            if(!removed) {
                throw new RuntimeException("Profile not found");
            }
            GeneralUtils.writeObjectToFile(profiles, file);
            return true;
        } catch (Exception  e) {
            ToastUtils.showToast(context, "Error deleting profile");
            Log.e(TAG, "Error deleting profile " + name + " (id: " + profileId + ")");
            e.printStackTrace();
        }
        return false;
    }

    //public void schedulePendingIntents(Context context) {
    //    Log.d("SCHEDULE_DEBUG", "Scheduling sync services...");
    //    AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    //    Intent intent = new Intent(context, DownloaderService.class);
    //    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
    //        intent.putExtra("profileId", this.profileId);
    //    }
    //    else {
    //        intent.putExtra("profile", this);
    //    }
    //    intent.putExtra("reschedule", true);
    //    //intent.putStringArrayListExtra("subreddits", (ArrayList) subreddits);
//
    //    //first cancel any previous pending intents for this profile
    //    for(int i=0;i<7;i++) {
    //        PendingIntent pendingIntent = PendingIntent.getService(context, profileId + i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    //        mgr.cancel(pendingIntent);
//
    //        //cancel pending intents with FLAG_ONE_SHOT (changed flag to FLAG_UPDATE_CURRENT after 0.2.2)
    //        pendingIntent = PendingIntent.getService(context, profileId + i, intent, PendingIntent.FLAG_ONE_SHOT);
    //        mgr.cancel(pendingIntent);
    //    }
//
    //    //Then schedule new pending intents for this profile (if the profile is active)
//
    //    if(isActive()) {
    //        for(SyncSchedule schedule : schedules) {
    //            int i = 0;
    //            for (SyncSchedule.TimeWindow timeWindow : schedule.getSyncTimeWindows()) {
    //                Log.d("SCHEDULE_DEBUG", name + " - start time: " + new Date(timeWindow.windowStart).toString() + " - time window: " + timeWindow.windowLength);
//
    //                PendingIntent pendingIntent = PendingIntent.getService(context, profileId + i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    //                    mgr.setWindow(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, timeWindow.windowLength, pendingIntent);
    //                } else {
    //                    mgr.set(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, pendingIntent);
    //                }
    //                i++;
    //            }
    //        }
    //    }
//
    //    Log.d("SCHEDULE_DEBUG", "finished scheduling");
    //}

    public void setName(String name) {
        this.name = name;
    }

    public void setSubreddits(List<String> subreddits) {
        this.subreddits = subreddits;
    }

    public String getName() {
        return name;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public void addSubreddit(String subreddit) {
        if(subreddits==null) {
            subreddits = new ArrayList<>();
        }
        subreddits.add(subreddit);
    }

    public void addMultireddit(String multireddit) {
        if(multireddits==null) {
            multireddits = new ArrayList<>();
        }
        multireddits.add(multireddit);
    }

    public void setMultireddits(List<String> multireddits) {
        this.multireddits = multireddits;
    }

    public List<String> getMultireddits() {
        return multireddits;
    }

    public List<String> getRedditsList() {
        List<String> list = subreddits;
        list.addAll(multireddits);
        return list;
    }

    public int getViewType() {
        return SyncProfileListAdapter.VIEW_TYPE_PROFILE_ITEM;
    }

    public boolean hasSchedule() {
        return schedules!=null && !schedules.isEmpty();
    }

    //public void setActive(boolean active) {
    //    this.isActive = active;
    //}

    public boolean isActive() {
        //if(!hasTime) return false;
        return isActive;
    }

    //public void setActiveDay(DaysEnum day, boolean flag) {
    //    if(flag) {
    //        if(!days.contains(day.value())) {
    //            days = days.concat(day.value());
    //        }
    //    }
    //    else {
    //        days = days.replace(day.value(), "");
    //    }
    //}

    public int getProfileId() {
        return profileId;
    }

    //public boolean isActiveDay(DaysEnum day) {
    //    return days.contains(day.value());
    //}

    //public boolean toggleActiveDay(DaysEnum day) {
    //    boolean activeState = isActiveDay(day);
    //    setActiveDay(day, !activeState);
    //    return !activeState;
    //}

    public List<SyncSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<SyncSchedule> schedules) {
        this.schedules = schedules;
    }

}
