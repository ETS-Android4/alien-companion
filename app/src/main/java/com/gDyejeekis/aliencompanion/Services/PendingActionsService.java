package com.gDyejeekis.aliencompanion.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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

    }

}
