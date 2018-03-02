package com.gDyejeekis.aliencompanion.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.LinkHandler;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

public class HandleUrlActivity extends AppCompatActivity {

    public static final String TAG = "HandleUrlActivity";

    public static boolean notifySwitchedMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url;
        Intent intent = getIntent();
        if (intent.getAction()!=null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            url = intent.getDataString();
        }
        else {
            url = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        if (url!=null && !url.isEmpty()) {
            MyApplication.checkAccountInit(this, new PoliteRedditHttpClient());
            if(MyApplication.offlineModeEnabled) {
                notifySwitchedMode = true;
                MainActivity.notifySwitchedMode = true;
                MyApplication.offlineModeEnabled = false;
                MainActivity.notifyDrawerChanged = true;
                SharedPreferences.Editor editor = MyApplication.prefs.edit();
                editor.putBoolean("offlineMode", MyApplication.offlineModeEnabled);
                editor.apply();
            }
            LinkHandler linkHandler = new LinkHandler(this, url);
            linkHandler.handleIt();
        }
        this.finish();
    }
}
