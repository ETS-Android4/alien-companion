package com.dyejeekis.aliencompanion.Activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.GeneralUtils;

/**
 * Created by sound on 10/23/2015.
 */
public class OAuthActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_oauth);
    }

    @Override
    public void onDestroy() {
        GeneralUtils.clearCookies(this);
        super.onDestroy();
    }

}
