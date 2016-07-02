package com.gDyejeekis.aliencompanion.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Services.PendingActionsService;

/**
 * Created by George on 6/26/2016.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {

    public static final String TAG = "ConnectivityChange";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Connectivity receiver executing..");

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(MyApplication.offlineActionsInterval!=-1 && MyApplication.pendingOfflineActions && networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "Starting PendingActionsService..");
            Intent serviceIntent = new Intent(context, PendingActionsService.class);
            context.startService(serviceIntent);
        }
    }
}
