package com.gDyejeekis.aliencompanion.models.sync_profile;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.gDyejeekis.aliencompanion.models.Profile;
import com.gDyejeekis.aliencompanion.views.adapters.ProfileListAdapter;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sound on 1/21/2016.
 */
public class SyncProfile extends Profile implements Serializable {

    public static final String TAG = "SyncProfile";

    private static final long serialVersionUID = 1234542L;

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
        super("");
        this.subreddits = new ArrayList<>();
        this.multireddits = new ArrayList<>();
        this.schedules = new ArrayList<>();
        this.isActive = false;
        useGlobalSyncOptions = true;
    }

    public SyncProfile(SyncProfile profile) {
        super(profile.getName());
        this.subreddits = profile.getSubreddits();
        this.multireddits = profile.getMultireddits();
        this.schedules = profile.getSchedules();
        this.isActive = profile.isActive();
        useGlobalSyncOptions = profile.isUseGlobalSyncOptions();
        syncOptions = profile.getSyncOptions();
    }

    public SyncProfile(String name) {
        super(name);
        this.subreddits = new ArrayList<>();
        this.multireddits = new ArrayList<>();
        this.schedules = new ArrayList<>();
        this.isActive = false;
        useGlobalSyncOptions = true;
    }

    public SyncProfile(String name, List<String> subreddits, List<String> multireddits, List<SyncSchedule> schedules) {
        super(name);
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
            intent.putExtra("profileId", this.profileId);
            context.startService(intent);
        }
        else {
            if(context instanceof Activity) {
                ToastUtils.showToast(context, "Network connection unavailable");
            }
        }
    }

    public boolean addSchedule(SyncSchedule schedule) {
        if(schedules == null) {
            schedules = new ArrayList<>();
        }
        return schedules.add(schedule);
    }

    public boolean removeSchedule(SyncSchedule schedule) {
        return schedules != null && schedules.remove(schedule);
    }

    public void setActive(boolean flag) {
        isActive = flag;
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
        // cancel pending intents for all days of the week (seven possible time windows)
        for(int i=0;i<7;i++) {
            PendingIntent pendingIntent = PendingIntent.getService(context, getPendingIntentRequestCode(schedule, i), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mgr.cancel(pendingIntent);

            //cancel pending intents with FLAG_ONE_SHOT (changed flag to FLAG_UPDATE_CURRENT after 0.2.2)
            //pendingIntent = PendingIntent.getService(context, profileId + schedule.getScheduleId() + i, intent, PendingIntent.FLAG_ONE_SHOT);
            //mgr.cancel(pendingIntent);
        }
    }

    public void scheduleAllPendingIntents(Context context) {
        for(SyncSchedule schedule : schedules) {
            schedulePendingIntents(context, schedule);
        }
    }

    public void schedulePendingIntents(Context context, SyncSchedule schedule) {
        Log.d(TAG, name + " (id: " + profileId + ") - Scheduling sync services...");
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = getSyncIntent(context);
        int i = 0;
        for (SyncSchedule.TimeWindow timeWindow : schedule.getSyncTimeWindows()) {
            Log.d(TAG, name + " - start time: " + new Date(timeWindow.windowStart).toString() + " - time window: " + timeWindow.windowLength);

            PendingIntent pendingIntent = PendingIntent.getService(context, getPendingIntentRequestCode(schedule, i), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mgr.setWindow(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, timeWindow.windowLength, pendingIntent);
            } else {
                mgr.set(AlarmManager.RTC_WAKEUP, timeWindow.windowStart, pendingIntent);
            }
            i++;
        }
    }

    // this should be unique for every single time window of a sync schedule
    private int getPendingIntentRequestCode(SyncSchedule schedule, int timeWindow) {
        return schedule.getScheduleId().hashCode() + timeWindow;
    }

    private Intent getSyncIntent(Context context) {
        Intent intent = new Intent(context, DownloaderService.class);
        //if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        //    intent.putExtra("profileId", this.profileId);
        //}
        //else {
        //    intent.putExtra("profile", this);
        //}
        intent.putExtra("profileId", this.profileId);
        intent.putExtra("reschedule", true);

        return intent;
    }

    @Override
    public boolean save(Context context, boolean newProfile) {
        boolean saved = super.save(context, newProfile);
        if(saved) {
            if(isActive) {
                scheduleAllPendingIntents(context);
            }
            else {
                unscheduleAllPendingIntents(context);
            }
        }
        return saved;
    }

    @Override
    public boolean delete(Context context) {
        boolean deleted = super.delete(context);
        if(deleted) {
            unscheduleAllPendingIntents(context);
        }
        return deleted;
    }

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

    public boolean addSubreddit(String subreddit) {
        if(subreddits==null) {
            subreddits = new ArrayList<>();
        }
        return subreddits.add(subreddit);
    }

    public boolean removeSubreddit(int index) {
        if(subreddits!=null && subreddits.size()>index) {
            subreddits.remove(index);
            return true;
        }
        return false;
    }

    public boolean addMultireddit(String multireddit) {
        if(multireddits==null) {
            multireddits = new ArrayList<>();
        }
        return multireddits.add(multireddit);
    }

    public boolean removeMultireddit(int index) {
        if(multireddits!=null && multireddits.size()>index) {
            multireddits.remove(index);
            return true;
        }
        return false;
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
        return ProfileListAdapter.VIEW_TYPE_PROFILE_ITEM;
    }

    public boolean hasSchedule() {
        return schedules!=null && !schedules.isEmpty();
    }

    public boolean isActive() {
        //if(!hasTime) return false;
        return isActive;
    }

    public List<SyncSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<SyncSchedule> schedules) {
        this.schedules = schedules;
    }

}
