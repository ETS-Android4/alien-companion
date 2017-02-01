package com.gDyejeekis.aliencompanion.broadcast_receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gDyejeekis.aliencompanion.services.DownloaderService;

/**
 * Created by George on 7/8/2016.
 */
public class SyncStateReceiver extends BroadcastReceiver {

    public static final String TAG = "SyncStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(intent.getAction().endsWith("SYNC_CANCEL")) {
            Log.d(TAG, "Cancelling sync..");
            DownloaderService.manualSyncCancel(manager);
        }
        else if(intent.getAction().endsWith("SYNC_PAUSE")) {
            Log.d(TAG, "Pausing sync..");
            DownloaderService.manualSyncPause(context, manager);
        }
        else if(intent.getAction().endsWith("SYNC_RESUME")) {
            Log.d(TAG, "Resuming sync..");
            DownloaderService.manualSyncResume(context, manager);
        }
    }
}
