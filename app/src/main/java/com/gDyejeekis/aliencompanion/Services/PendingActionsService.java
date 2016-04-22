package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gDyejeekis.aliencompanion.Activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

import java.io.File;
import java.util.List;

/**
 * Created by sound on 4/10/2016.
 */
public class PendingActionsService extends IntentService {

    public static final String TAG = "PendingActionsService";

    public static final int SERVICE_ID = 5312;

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
                    List<OfflineUserAction> pendingActions = (List<OfflineUserAction>) GeneralUtils.readObjectFromFile(new File(getFilesDir(), MyApplication.OFFLINE_USER_ACTIONS_FILENAME));

                    for (OfflineUserAction action : pendingActions) {
                        // TODO: 4/22/2016
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
