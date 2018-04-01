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

    public static final String SYNC_CANCEL = "com.gDyejeekis.aliencompanion.SYNC_CANCEL";

    public static final String SYNC_PAUSE = "com.gDyejeekis.aliencompanion.SYNC_PAUSE";

    public static final String SYNC_RESUME = "com.gDyejeekis.aliencompanion.SYNC_RESUME";

    public static final String TAG = "SyncStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()!=null) {
            switch (intent.getAction()) {
                case SYNC_CANCEL:
                    Log.d(TAG, "Cancelling sync..");
                    DownloaderService.manualSyncCancel(context);
                    break;
                case SYNC_PAUSE:
                    Log.d(TAG, "Pausing sync..");
                    DownloaderService.manualSyncPause(context);
                    break;
                case SYNC_RESUME:
                    Log.d(TAG, "Resuming sync..");
                    DownloaderService.manualSyncResume(context);
                    break;
            }
        }
    }

}
