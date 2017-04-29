package com.gDyejeekis.aliencompanion.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gDyejeekis.aliencompanion.models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Created by sound on 3/28/2016.
 */
public class OnBootReceiver extends BroadcastReceiver {

    public static final String TAG = "OnBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Scheduling sync profiles..");

        try {
            MyApplication.scheduleMessageCheckService(context);
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule message check service");
            e.printStackTrace();
        }

        try {
            MyApplication.scheduleOfflineActionsService(context);
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule offline actions service");
            e.printStackTrace();
        }

        scheduleSyncProfiles(context);
    }

    private void scheduleSyncProfiles(Context context) {
        try {
            FileInputStream fis = context.openFileInput(MyApplication.SYNC_PROFILES_FILENAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            List<SyncProfile> syncProfiles = (List<SyncProfile>) is.readObject();
            is.close();
            fis.close();

            for(SyncProfile profile : syncProfiles) {
                if(profile.isActive()) {
                    profile.scheduleAllPendingIntents(context);
                }
            }
            Log.d(TAG, "Sync profiles schedules successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule sync profiles");
            e.printStackTrace();
        }
    }

}
