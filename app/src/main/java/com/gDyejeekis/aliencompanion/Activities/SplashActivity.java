package com.gDyejeekis.aliencompanion.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 4/11/2016.
 */
public class SplashActivity extends AppCompatActivity {

    public static final String TAG = "SplashActivity";

    public static final int SPLASH_DISPLAY_LENGTH = 0;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        //if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
        //    // Activity was brought to front and not created,
        //    // Thus finishing this will get us to the last viewed activity
        //    Log.d(TAG, "Killing additional SplashActivity that was brought to front");
        //    finish();
        //    return;
        //}
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
                SplashActivity.this.overridePendingTransition(0, android.R.anim.fade_in);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
