package com.gDyejeekis.aliencompanion.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by sound on 4/10/2016.
 */
public class MessageCheckService extends Service {

    public static final int SERVICE_ID = 5311;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }
}
