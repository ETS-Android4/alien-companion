package com.gDyejeekis.aliencompanion.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;

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
