package com.gDyejeekis.aliencompanion.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;

public class HandleUrlActivity extends AppCompatActivity {

    public static final String TAG = "HandleUrlActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyApplication.checkAccountInit(this, new PoliteRedditHttpClient());

        String url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if(url!=null) {
            if(MyApplication.offlineModeEnabled) {
                MyApplication.offlineModeEnabled = false;
                MainActivity.restartApp = true;
            }
            LinkHandler linkHandler = new LinkHandler(this, url);
            linkHandler.handleIt();
        }
        this.finish();
    }
}
