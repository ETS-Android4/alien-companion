package com.gDyejeekis.aliencompanion.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 4/10/2016.
 */
public class PendingActionsService extends IntentService {

    public static final String TAG = "PendingActionsService";

    public static final String RESULT = TAG + "_RESULT";

    public static final int SERVICE_ID = 5312;

    public static final int SERVICE_NOTIF_ID = 6312;

    private LocalBroadcastManager broadcaster;

    public static boolean isRunning = false;

    public PendingActionsService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onHandleIntent(Intent i) {
        if(GeneralUtils.isNetworkAvailable(this)) {
            //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            //boolean actionsPending = prefs.getBoolean("pendingActions", false);
            if (MyApplication.pendingOfflineActions) {
                isRunning = true;
                Log.d(TAG, "Executing remaining offline actions..");
                try {
                    File file = new File(getFilesDir(), AppConstants.OFFLINE_USER_ACTIONS_FILENAME);
                    List<OfflineUserAction> pendingActions = (List<OfflineUserAction>) GeneralUtils.readObjectFromFile(file);
                    List<OfflineUserAction> remainingActions = new ArrayList<>(pendingActions);

                    for (OfflineUserAction action : pendingActions) {
                        action.executeAction(this);
                        if(action.isActionCompleted()) {
                            remainingActions.remove(action);
                        }
                        else {
                            int index = remainingActions.indexOf(action);
                            remainingActions.get(index).setActionFailed(true);
                        }
                        GeneralUtils.writeObjectToFile(remainingActions, file);

                        if(PendingUserActionsActivity.isActive) {
                            sendResult(action.getActionId(), action.isActionCompleted());
                        }
                    }

                    if(remainingActions.size()==0) {
                        Log.d(TAG, "All remaining offline actions completed");
                        file.delete();

                        MyApplication.pendingOfflineActions = false;
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                        editor.putBoolean("pendingActions", false);
                        editor.commit();
                    }
                    else {
                        Log.d(TAG, remainingActions.size() + " actions failed to complete");
                        if(!PendingUserActionsActivity.isActive) {
                            showActionsFailedNotification(remainingActions.size());
                        }
                    }

                    MyApplication.scheduleOfflineActionsService(this);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        isRunning = false;
    }

    private void sendResult(String actionid, boolean success) {
        Intent intent = new Intent(RESULT);
        intent.putExtra("actionId", actionid);
        intent.putExtra("success", success);
        broadcaster.sendBroadcast(intent);
    }

    private void showActionsFailedNotification(int count) {
        Intent intent = new Intent(this, PendingUserActionsActivity.class);
        intent.setClass(getApplicationContext(), PendingUserActionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle("Alien Companion - Pending actions")
                .setContentText(count + " action" + ((count>1) ? "s" : "") + " failed to complete")
                .setContentIntent(pIntent);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(SERVICE_NOTIF_ID, notification);
    }

}
