package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.gDyejeekis.aliencompanion.Activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 4/10/2016.
 */
public class PendingActionsService extends IntentService {

    public static final String TAG = "PendingActionsService";

    public static final int SERVICE_ID = 5312;

    public static final int SERVICE_NOTIF_ID = 6312;

    public PendingActionsService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent i) {
        if(!PendingUserActionsActivity.isActive && GeneralUtils.isNetworkAvailable(this)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean actionsPending = prefs.getBoolean("pendingActions", false);
            if (actionsPending) {
                try {
                    File file = new File(getFilesDir(), MyApplication.OFFLINE_USER_ACTIONS_FILENAME);
                    List<OfflineUserAction> pendingActions = (List<OfflineUserAction>) GeneralUtils.readObjectFromFile(file);
                    List<OfflineUserAction> remainingActions = new ArrayList<>();

                    for (OfflineUserAction action : pendingActions) {
                        action.executeAction(this);
                        if(!action.isActionCompleted()) {
                            remainingActions.add(action);
                        }
                    }

                    if(remainingActions.size()==0) {
                        file.delete();

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("pendingActions", false);
                        editor.commit();
                    }
                    else {
                        GeneralUtils.writeObjectToFile(remainingActions, file);
                        showActionsFailedNotification(remainingActions.size());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showActionsFailedNotification(int count) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle("Alien Companion - Pending actions")
                .setContentText(count + " action" + ((count>1) ? "s" : "") + " failed to complete");

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(SERVICE_NOTIF_ID, notification);
    }

}
